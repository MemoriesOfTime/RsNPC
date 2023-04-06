package com.smallaswater.npc.utils;

import cn.lanink.gamecore.utils.VersionUtils;
import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.plugin.Plugin;
import com.google.common.util.concurrent.AtomicDouble;
import com.smallaswater.npc.RsNPC;
import lombok.NonNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 自动下载GameCore依赖工具类
 */
public class GameCoreDownload {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

    // 每个任务下载 128 kb数据
    private static final int THRESHOLD = 128 * 1024;

    public static final String MINIMUM_GAME_CORE_VERSION = "1.6.9";
    private static String ACTUAL_MINIMUM_GAME_CORE_VERSION;

    private static final String MAVEN_URL_CENTRAL = "https://repo1.maven.org/maven2/";
    private static final String MAVEN_URL_HUAWEI = "https://repo.huaweicloud.com/repository/maven/";
    private static final String MAVEN_URL_LANINK = "https://repo.lanink.cn/";

    private static final List<String> GAME_CORE_URL_LIST;

    static {
        //为了防止编译依赖和实际环境区别，这里重新检查GameCore完整版本号
        ACTUAL_MINIMUM_GAME_CORE_VERSION = MINIMUM_GAME_CORE_VERSION.split("-")[0];
        String codename = Server.getInstance().getCodename();
        if ("PowerNukkitX".equalsIgnoreCase(codename)/* || "PowerNukkit".equalsIgnoreCase(codename)*/) {
            ACTUAL_MINIMUM_GAME_CORE_VERSION += "-PNX";
        } else if ("PM1E".equalsIgnoreCase(codename)) {
            ACTUAL_MINIMUM_GAME_CORE_VERSION += "-PM1E";
        }

        GAME_CORE_URL_LIST = Collections.unmodifiableList(Arrays.asList(
                getGameCoreUrl(MAVEN_URL_CENTRAL),
                getGameCoreUrl(MAVEN_URL_HUAWEI),
                getGameCoreUrl(MAVEN_URL_LANINK)
        ));
    }

    private static String getGameCoreUrl(String mavenUrl) {
        //插件完整下载地址
        return mavenUrl + "cn/lanink/MemoriesOfTime-GameCore/" + ACTUAL_MINIMUM_GAME_CORE_VERSION + "/MemoriesOfTime-GameCore-" + ACTUAL_MINIMUM_GAME_CORE_VERSION + ".jar";
    }

    private GameCoreDownload() {
        throw new RuntimeException("error");
    }

    /**
     * 检查并下载GameCore依赖
     *
     * @return 0 - GameCore已加载且是最新版本    1 - 无法下载GameCore     2 下载成功
     */
    public static int checkAndDownload() {
        return checkAndDownload(0);
    }

    /**
     * 检查并下载GameCore依赖
     *
     * @param retry 重试次数(下载链接序号)
     * @return 0 - GameCore已加载且是最新版本    1 - 无法下载GameCore     2 下载成功
     */
    private static int checkAndDownload(int retry) {
        if (retry >= GAME_CORE_URL_LIST.size()) {
            return 1;
        }
        String url = GAME_CORE_URL_LIST.get(retry);

        Plugin plugin = Server.getInstance().getPluginManager().getPlugin("MemoriesOfTime-GameCore");

        if (plugin != null) {
            if (!VersionUtils.checkMinimumVersion(plugin, ACTUAL_MINIMUM_GAME_CORE_VERSION)) {
                RsNPC.getInstance().getLogger().warning("MemoriesOfTime-GameCore依赖版本太低！正在尝试更新版本...");
                File file = getPluginFile(plugin);
                if (file != null) {
                    Server.getInstance().getPluginManager().disablePlugin(plugin);
                    ClassLoader classLoader = plugin.getClass().getClassLoader();
                    try {
                        if (classLoader instanceof URLClassLoader) {
                            ((URLClassLoader) classLoader).close();
                        }
                    } catch (IOException ignored) {

                    }
                    file.delete();
                }else {
                    RsNPC.getInstance().getLogger().error("删除旧版本失败！请手动删除！");
                }
            }
        }

        if (plugin == null || plugin.isDisabled()) {
            RsNPC.getInstance().getLogger().info("尝试从 " + url + " 下载 MemoriesOfTime-GameCore 中...");

            File file = new File(Server.getInstance().getFilePath() + "/plugins/MemoriesOfTime-GameCore-" + ACTUAL_MINIMUM_GAME_CORE_VERSION + ".jar");

            try {
                AtomicDouble last = new AtomicDouble(-16);
                download(url, file, (len, fullLength) -> {
                    double d = NukkitMath.round(len * 1.0 / fullLength * 100, 2);
                    if (d - last.get() > 15) { // 每15%提示一次
                        RsNPC.getInstance().getLogger().info("已下载：" + d + "%");
                        last.set(d);
                    }
                });
            } catch (Exception e) {
                RsNPC.getInstance().getLogger().error("MemoriesOfTime-GameCore依赖下载失败！");
                return checkAndDownload(++retry);
            }

            RsNPC.getInstance().getLogger().info("MemoriesOfTime-GameCore依赖下载成功！");
            Server.getInstance().getPluginManager().loadPlugin(file);
            return 2;
        }
        return 0;
    }

    public static File getPluginFile(Plugin plugin) {
        File file = null;
        ClassLoader PluginClass = plugin.getClass().getClassLoader();
        try {
            if (PluginClass instanceof URLClassLoader) {
                URLClassLoader pluginClass = (URLClassLoader) PluginClass;
                URL url = pluginClass.getURLs()[0];
                file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException ignored) {

        }
        return file;
    }

    /**
     * 下载
     *
     * @param strUrl   目标url
     * @param saveFile 保存到文件
     * @param callback 下载完的回调
     */
    private static void download(String strUrl, File saveFile, BiConsumer<Long, Long> callback) throws Exception {
        URL url = new URL(strUrl);
        HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setReadTimeout(5000);


        long fullLength = connection.getContentLength();
        if ("chunked".equals(connection.getHeaderField("Transfer-Encoding"))) { // chunked transfer 采用单线程下载
            RandomAccessFile out = new RandomAccessFile(saveFile, "rw");
            out.seek(0);
            byte[] b = new byte[1024];
            InputStream in = connection.getInputStream();
            int read;
            long count = 0;
            while ((read = in.read(b)) >= 0) {
                out.write(b, 0, read);
                count += read;
                if (callback != null) {
                    callback.accept(count, fullLength);
                }
            }
            in.close();
            out.close();
            return;
        }
        ForkJoinPool pool = new ForkJoinPool();
        AtomicLong atomicLong = new AtomicLong();
        pool.submit(new DownloadTask(strUrl,0, fullLength, saveFile, (l) -> {
            atomicLong.addAndGet(l);
            callback.accept(atomicLong.get(), fullLength);
        }));
        pool.shutdown();
        // 同步  等待所有线程完成操作
        while (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
        }
        if (fullLength < 1 || saveFile.length() < 1) {
            throw new Exception("下载失败");
        }
    }

    private static class DownloadTask extends RecursiveAction {

        private final String strUrl;
        private final File file;
        private final long start;
        private final long end;

        private final Consumer<Integer> callback;

        public DownloadTask(@NonNull String strUrl, long start, long end, File file, Consumer<Integer> callback) {
            this.strUrl = strUrl;
            this.start = start;
            this.end = end;
            this.file = file;
            this.callback = callback;
        }

        @Override
        protected void compute() {
            RandomAccessFile out = null;
            InputStream in = null;
            try {
                long l = end - start;
                if (l < THRESHOLD) {
                    HttpURLConnection connection = getConnection();
                    connection.setRequestProperty("Range", "bytes=" + start + "-" + end);

                    out = new RandomAccessFile(file, "rw");
                    out.seek(start);
                    in = connection.getInputStream();
                    byte[] b = new byte[1024];
                    int len;
                    while ((len = in.read(b)) >= 0) {
                        out.write(b, 0, len);
                        callback.accept(len);
                    }
                    in.close();
                    out.close();
                } else {
                    long mid = (start + end) / 2;
                    new SubDownloadTask(strUrl, start, mid, file, callback).fork();
                    new SubDownloadTask(strUrl, mid, end, file, callback).fork();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public HttpURLConnection getConnection() throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(strUrl).openConnection();
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", USER_AGENT);

            return connection;
        }
    }

    private static class SubDownloadTask extends DownloadTask {

        public SubDownloadTask(@NonNull String strUrl, long start, long end, File file, Consumer<Integer> callback) {
            super(strUrl, start, end, file, callback);
        }

    }

}

