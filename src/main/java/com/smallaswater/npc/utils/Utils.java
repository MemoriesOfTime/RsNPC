package com.smallaswater.npc.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.Plugin;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.tasks.PlayerPermissionCheckTask;
import com.smallaswater.npc.utils.dialog.packet.NPCDialoguePacket;
import com.smallaswater.npc.utils.dialog.window.AdvancedFormWindowDialog;
import com.smallaswater.npc.variable.VariableManage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static final Cache<String, AdvancedFormWindowDialog> WINDOW_DIALOG_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    private Utils() {
        throw new RuntimeException("error");
    }

    public static int checkAndDownloadDepend() {
        Plugin plugin = Server.getInstance().getPluginManager().getPlugin("MemoriesOfTime-GameCore");
        if (plugin == null || plugin.isDisabled()) {
            RsNPC.getInstance().getLogger().info("下载MemoriesOfTime-GameCore依赖中...");

            String gamecore = Server.getInstance().getFilePath() + "/plugins/MemoriesOfTime-GameCore.jar";

            try {
                FileOutputStream fos = new FileOutputStream(gamecore);
                fos.getChannel().transferFrom(Channels.newChannel(new URL(RsNPC.GAME_CORE_URL).openStream()), 0, Long.MAX_VALUE);
                fos.close();
            } catch (Exception e) {
                RsNPC.getInstance().getLogger().error("无法下载MemoriesOfTime-GameCore依赖！", e);
                Server.getInstance().getPluginManager().disablePlugin(RsNPC.getInstance());
                return 1;
            }

            RsNPC.getInstance().getLogger().info("MemoriesOfTime-GameCore依赖下载成功！");
            Server.getInstance().getPluginManager().loadPlugin(gamecore);
            return 2;
        }
        return 0;
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
            e.printStackTrace();
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

    public static void sendDialogWindows(@NotNull Player player, @NotNull AdvancedFormWindowDialog dialog) {
        if(WINDOW_DIALOG_CACHE.getIfPresent(dialog.getSceneName()) != null) {
            dialog.updateSceneName();
        }
        String actionJson = dialog.getButtonJSONData();

        dialog.getBindEntity().setDataProperty(new ByteEntityData(Entity.DATA_HAS_NPC_COMPONENT, 1));
        dialog.getBindEntity().setDataProperty(new StringEntityData(/*Entity.DATA_NPC_SKIN_DATA*/ 40, dialog.getSkinData()));
        dialog.getBindEntity().setDataProperty(new StringEntityData(/*Entity.DATA_NPC_ACTIONS*/ 41, actionJson));
        dialog.getBindEntity().setDataProperty(new StringEntityData(Entity.DATA_INTERACTIVE_TAG, dialog.getContent()));

        NPCDialoguePacket packet = new NPCDialoguePacket();
        packet.setRuntimeEntityId(dialog.getEntityId());
        packet.setAction(NPCDialoguePacket.NPCDialogAction.OPEN);
        packet.setDialogue(dialog.getContent());
        packet.setNpcName(dialog.getTitle());
        packet.setSceneName(dialog.getSceneName());
        packet.setActionJson(dialog.getButtonJSONData());
        WINDOW_DIALOG_CACHE.put(dialog.getSceneName(),dialog);
        player.dataPacket(packet);
    }

}
