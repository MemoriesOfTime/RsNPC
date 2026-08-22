package com.smallaswater.npc.utils;

import cn.lanink.gamecore.utils.NukkitTypeUtils;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.plugin.Plugin;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.command.RsNPCCommandSender;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.entitys.EntityRsNPC;
import com.smallaswater.npc.tasks.PlayerPermissionCheckTask;
import com.smallaswater.npc.variable.VariableManage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class Utils {

    private Utils() {
        throw new RuntimeException("error");
    }

    /**
     * 将物品对象转换为保存用字符串ID
     *
     * @param item 物品对象
     * @return 保存用字符串ID
     */
    public static String item2String(Item item) {
        if (NukkitTypeUtils.getNukkitType() == NukkitTypeUtils.NukkitType.MOT) {
            //StringItem 仅存在于 Nukkit-MOT，反射调用以兼容 NukkitX
            try {
                if (Class.forName("cn.nukkit.item.StringItem").isInstance(item)) {
                    return (String) item.getClass().getMethod("getNamespaceId").invoke(item);
                }
            } catch (Exception ignored) {
            }
        }
        return item.getId() + ":" + item.getDamage();
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
        executeCommand(player, rsNpcConfig, cmds, null);
    }

    public static void executeCommand(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig, List<String> cmds, EntityRsNPC entityRsNPC) {
        List<String> list;
        if (cmds == null) {
            list = rsNpcConfig.getCmds();
        }else {
            list = cmds;
        }
        for (String cmd : list) {
            if (cmd == null || cmd.trim().isEmpty()) {
                continue;
            }
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
                } else if ("op".equals(c[1])) {
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
                } else if ("self".equals(c[1])) {
                    try {
                        Server.getInstance().dispatchCommand(new RsNPCCommandSender(entityRsNPC, player),
                                VariableManage.stringReplace(player, command, rsNpcConfig));
                    } catch (Exception e) {
                        RsNPC.getInstance().getLogger().error(
                                "self 权限执行命令时出现错误！NPC:" + rsNpcConfig.getName() +
                                        " 玩家:" + player.getName() +
                                        " 错误:", e);
                    }
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
        return GameCoreDownload.getPluginFile(plugin);
    }

    public static void playSound(Player player, String sound) {
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound;
        packet.volume = 1;
        packet.pitch = 1;
        packet.x = player.getFloorX();
        packet.y = player.getFloorY();
        packet.z = player.getFloorZ();
        player.dataPacket(packet);
    }

    /**
     * 计算 file 相对 rootDir 的路径，分隔符统一为 "/".
     * <p>用于把 NPC / 皮肤配置的磁盘位置转换为相对路径标识（如 {@code 分类A/NPC1}），
     * 作为运行时 map 的 key，使任意深度的子目录都能被唯一标识。
     *
     * @param rootDir 根目录（如 {@code .../Npcs}、{@code .../Skins}）
     * @param file    目标文件或目录
     * @return 相对路径字符串，分隔符为 "/"；若无法计算返回 file 的名称
     */
    public static String relativePath(File rootDir, File file) {
        try {
            return rootDir.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
        } catch (Exception e) {
            return file.getName();
        }
    }

    /**
     * 确保文件所在目录存在，不存在则创建（支持多级子目录）。
     *
     * @param file 目标文件
     * @return 父目录存在或创建成功返回 true
     */
    public static boolean ensureParentDir(File file) {
        File parent = file.getParentFile();
        return parent != null && (parent.exists() || parent.mkdirs());
    }

    /**
     * 将路径中的反斜杠归一为正斜杠，便于跨平台以统一标识引用 NPC / 皮肤。
     *
     * @param path 原始路径
     * @return 归一化后的路径
     */
    public static String normalizePathSeparator(String path) {
        return path == null ? null : path.replace('\\', '/');
    }

    /**
     * 取相对路径的最后一段（如 {@code 分类A/NPC1} → {@code NPC1}）。
     * <p>用于把含分类路径的内部 key 转换为与旧版扁平名一致的展示值，
     * 兼容可能残留的 {@code \} 分隔符。
     *
     * @param relativePath 相对路径
     * @return 末段；入参为 null 返回 null
     */
    public static String lastSegment(String relativePath) {
        if (relativePath == null) {
            return null;
        }
        String normalized = relativePath.replace('\\', '/');
        int idx = normalized.lastIndexOf('/');
        return idx < 0 ? normalized : normalized.substring(idx + 1);
    }

    /**
     * Windows 保留设备名（CON、NUL 等）。即使带扩展名（如 CON.yml），
     * 部分 Windows API 仍会将其解析为设备而非文件，需整段禁止。
     */
    private static final Set<String> WINDOWS_RESERVED_NAMES = new HashSet<>(Arrays.asList(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"));

    /**
     * 校验相对路径是否安全（禁止 {@code ..} 段、绝对路径、Windows 非法字符与保留设备名），
     * 防止路径穿越逃出 Npcs/Skins 目录，并避免跨平台（尤其 Windows）写入失败或产生
     * NTFS 备用数据流等异常文件。
     *
     * @param relativePath 归一化后的相对路径（如 {@code 分类A/NPC1}）
     * @return 安全返回 true；含 {@code ..} 段、以 {@code /} 开头、含 Windows 非法字符
     *         （{@code : * ? " < > |} 及控制字符）、段名以点/空格结尾或为保留设备名返回 false
     */
    public static boolean isSafeRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }
        if (relativePath.startsWith("/")) {
            return false;
        }
        // 禁止 Windows 文件名非法字符与控制字符（也覆盖了盘符，如 C:/...）
        for (int i = 0; i < relativePath.length(); i++) {
            char c = relativePath.charAt(i);
            if (c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|' || c < 0x20) {
                return false;
            }
        }
        for (String segment : relativePath.split("/", -1)) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                return false;
            }
            // Windows 会静默剥离段尾的点/空格，导致运行时 key 与磁盘实际路径不一致
            if (segment.endsWith(".") || segment.endsWith(" ")) {
                return false;
            }
            // 保留设备名带扩展名（如 CON.1）在 Windows 上同样可能被解析为设备，取首个点之前的部分判断
            String base = segment.split("\\.", 2)[0].toUpperCase(Locale.ROOT);
            if (WINDOWS_RESERVED_NAMES.contains(base)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从已删除文件所在目录向上递归删除空目录，直到 {@code root}（不含）或遇到非空目录停止。
     * 用于 NPC 删除/创建失败后清理残留的空分类目录。
     *
     * @param dir  已删除文件的原所在目录
     * @param root 分类根目录（如 {@code .../Npcs}），自身不会被删除
     */
    public static void cleanupEmptyParentDirs(File dir, File root) {
        try {
            File current = dir;
            File rootFile = root.getCanonicalFile();
            while (current != null) {
                File canon = current.getCanonicalFile();
                // 带分隔符的前缀判断，防止符号链接逃逸后误过 /NpcsX 之类的前缀
                if (canon.equals(rootFile) || !canon.getPath().startsWith(rootFile.getPath() + File.separator)) {
                    break;
                }
                File[] children = current.listFiles();
                if (children != null && children.length == 0) {
                    //noinspection ResultOfMethodCallIgnored
                    current.delete();
                    current = current.getParentFile();
                } else {
                    break;
                }
            }
        } catch (Exception ignore) {
        }
    }

}
