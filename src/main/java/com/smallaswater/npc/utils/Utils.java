package com.smallaswater.npc.utils;

import cn.lanink.gamecore.utils.VersionUtils;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Location;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.plugin.Plugin;
import com.google.common.util.concurrent.AtomicDouble;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.tasks.PlayerPermissionCheckTask;
import com.smallaswater.npc.variable.VariableManage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.List;

public class Utils {
    private Utils() {
        throw new RuntimeException("error");
    }

    public static double toDouble(Object object) {
        return new BigDecimal(object.toString()).doubleValue();
    }

    public static int toInt(Object object) {
        return new BigDecimal(object.toString()).intValue();
    }

    public static void executeCommand(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        executeCommand(player, rsNpcConfig, null);
    }

    public static void executeCommand(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig, List<String> cmds) {
        List<String> list;
        if (cmds == null) {
            list = rsNpcConfig.getCmds();
        }else {
            list = cmds;
        }
        for (String cmd : list) {
            String[] c = cmd.split("&");
            String command = c[0];
            if (command.startsWith("/")) {
                command = command.replaceFirst("/", "");
            }
            if (c.length > 1) {
                if ("con".equals(c[1])) {
                    try {
                        Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(),
                                VariableManage.stringReplace(player, command, rsNpcConfig));
                    } catch (Exception e) {
                        RsNPC.getInstance().getLogger().error(
                                "控制台权限执行命令时出现错误！NPC:" + rsNpcConfig.getName() +
                                        " 玩家:" + player.getName() +
                                        " 错误:", e);
                    }
                    continue;
                }else if ("op".equals(c[1])) {
                    boolean needCancelOP = false;
                    if (!player.isOp()) {
                        needCancelOP = true;
                        PlayerPermissionCheckTask.addCheck(player);
                        player.setOp(true);
                    }
                    try {
                        Server.getInstance().dispatchCommand(player, VariableManage.stringReplace(player, command, rsNpcConfig));
                    } catch (Exception e) {
                        RsNPC.getInstance().getLogger().error(
                                "OP权限执行命令时出现错误！NPC:" + rsNpcConfig.getName() +
                                        " 玩家:" + player.getName() +
                                        " 错误:", e);
                    } finally {
                        if (needCancelOP) {
                            player.setOp(false);
                        }
                    }
                    continue;
                }
            }
            try {
                Server.getInstance().dispatchCommand(player, VariableManage.stringReplace(player, command, rsNpcConfig));
            } catch (Exception e) {
                RsNPC.getInstance().getLogger().error(
                        "玩家权限执行命令时出现错误！NPC:" + rsNpcConfig.getName() +
                                " 玩家:" + player.getName() +
                                " 错误:", e);
            }
        }
    }

    public static String readFile(@NotNull File file) {
        String content = "";
        try {
            content = cn.nukkit.utils.Utils.readFile(file);
        } catch (IOException e) {
            RsNPC.getInstance().getLogger().error("Read File Error!", e);
        }
        return content;
    }

    public static double getYaw(@NotNull Location location) {
        if (location.getYaw() > 315 || location.getYaw() <= 45) {
            return 0D;
        }else if (location.getYaw() > 45 && location.getYaw() <= 135) {
            return 90D;
        }else if (location.getYaw() > 135 && location.getYaw() <= 225) {
            return 180D;
        }else {
            return 270D;
        }
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

    public static int checkAndDownloadDepend() {
        return checkAndDownloadDepend(0);
    }

    public static int checkAndDownloadDepend(int retry) {
        if (retry > 0) {
            RsNPC.getInstance().getLogger().info("尝试更换下载链接为 " + RsNPC.getInstance().getGameCoreUrl(retry));
        }
        String version = RsNPC.getInstance().getMinimumGameCoreVersion();
        Plugin plugin = Server.getInstance().getPluginManager().getPlugin("MemoriesOfTime-GameCore");

        if (plugin != null) {
            if (!VersionUtils.checkMinimumVersion(plugin, version)) {
                RsNPC.getInstance().getLogger().warning("MemoriesOfTime-GameCore依赖版本太低！正在尝试更新版本...");
                File file = getPluginFile(plugin);
                Server.getInstance().getPluginManager().disablePlugin(plugin);
                ClassLoader classLoader = plugin.getClass().getClassLoader();
                try {
                    if (classLoader instanceof URLClassLoader) {
                        ((URLClassLoader) classLoader).close();
                    }
                } catch (IOException ignored) {

                }
                if (file != null) {
                    file.delete();
                }
            }
        }

        if (plugin == null || plugin.isDisabled()) {
            RsNPC.getInstance().getLogger().info("下载MemoriesOfTime-GameCore依赖中...");

            String gamecore = Server.getInstance().getFilePath() + "/plugins/MemoriesOfTime-GameCore-" + version + ".jar";

            try {
                AtomicDouble last = new AtomicDouble(-10);
                Download.download(RsNPC.getInstance().getGameCoreUrl(retry), new File(gamecore), (l, len) -> {
                    double d = NukkitMath.round(l * 1.0 / len * 100, 2);
                    if (d - last.get() > 10) {
                        RsNPC.getInstance().getLogger().info("已下载：" + d + "%");
                        last.set(d);
                    }
                });
                /*FileOutputStream fos = new FileOutputStream(gamecore);
                URL url = new URL(RsNPC.getInstance().getGameCoreUrl());
                fos.getChannel().transferFrom(Channels.newChannel(url.openStream()), 0, Long.MAX_VALUE);
                fos.close();*/
            } catch (Exception e) {
                RsNPC.getInstance().getLogger().error(RsNPC.getInstance().getGameCoreUrl(retry) + " 下载失败！");
                if (retry >= 1) {
                    RsNPC.getInstance().getLogger().error("无法下载MemoriesOfTime-GameCore依赖！", e);
                    return 1;
                }
                return checkAndDownloadDepend(++retry);
            }

            RsNPC.getInstance().getLogger().info("MemoriesOfTime-GameCore依赖下载成功！");
            Server.getInstance().getPluginManager().loadPlugin(gamecore);
            return 2;
        }
        return 0;
    }

}
