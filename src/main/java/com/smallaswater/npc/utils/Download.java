package com.smallaswater.npc.utils;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author iGxnon
 * 多线程下载
 */
public class Download {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

    // 每个任务下载 128 kb数据
    private static final int THRESHOLD = 128 * 1024;


    /**
     * 下载
     *
     * @param strUrl   目标url
     * @param saveFile 保存到文件
     * @param callback 下载完的回调
     */
    public static void download(String strUrl, File saveFile, BiConsumer<Long, Long> callback) throws Exception {
        URL url = new URL(strUrl);
        HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setReadTimeout(5000);


        long len = connection.getContentLength();
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
                    callback.accept(count, len);
                }
            }
            in.close();
            out.close();
            return;
        }
        ForkJoinPool pool = new ForkJoinPool();
        AtomicLong atomicLong = new AtomicLong();
        pool.submit(new DownloadTask(strUrl,0, len, saveFile, (l) -> {
            atomicLong.addAndGet(l);
            callback.accept(atomicLong.get(), len);
        }));
        pool.shutdown();
        // 同步
        while (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
        }
        if (len < 1 || saveFile.length() < 1) {
            throw new Exception("下载失败");
        }
    }

    static class DownloadTask extends RecursiveAction {

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

        @SneakyThrows
        @Override
        protected void compute() {
            long l = end - start;
            if (l < THRESHOLD) {
                HttpURLConnection connection = getConnection();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + end);

                RandomAccessFile out = new RandomAccessFile(file, "rw");
                out.seek(start);
                InputStream in = connection.getInputStream();
                byte[] b = new byte[1024];
                int len;
                while ((len = in.read(b)) >= 0) {
                    out.write(b, 0, len);
                    callback.accept(len);
                }
                in.close();
                out.close();
            }else {
                long mid = (start + end) / 2;
                new SubDownloadTask(strUrl, start, mid, file, callback).fork();
                new SubDownloadTask(strUrl, mid, end, file, callback).fork();
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

    static class SubDownloadTask extends DownloadTask {

        public SubDownloadTask(@NonNull String strUrl, long start, long end, File file, Consumer<Integer> callback) {
            super(strUrl, start, end, file, callback);
        }

    }

}

