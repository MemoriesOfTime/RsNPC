package com.smallaswater.npc.command.sub;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.command.base.BaseSubCommand;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.utils.Utils;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * @author LT_Name
 */
public class CreateSubCommand extends BaseSubCommand {

    public CreateSubCommand(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("RsNPC.admin.create");
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            // 允许含 "/" 的相对路径（如 分类A/NPC1），归一化分隔符
            String name = Utils.normalizePathSeparator(args[1].trim());
            if ("".equals(name)) {
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.nameRequired"));
                return true;
            }
            if (!Utils.isSafeRelativePath(name)) {
                sender.sendMessage("§cNPC 名称包含非法路径字符（禁止 .. 或绝对路径）！");
                return true;
            }
            if (this.rsNPC.getNpcs().containsKey(name)) {
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.npcAlreadyExist", name));
                return true;
            }
            File targetFile = new File(new File(this.rsNPC.getDataFolder(), "Npcs"), name + ".yml");
            // 大小写不敏感文件系统（Windows/macOS）上，仅大小写不同的旧文件躲过 containsKey 检查，按磁盘存在性兜底
            if (targetFile.exists()) {
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.npcAlreadyExist", name));
                return true;
            }
            if (!Utils.ensureParentDir(targetFile)) {
                this.rsNPC.getLogger().error("NPC 分类目录创建失败: " + targetFile.getParentFile());
                sender.sendMessage("创建NPC失败！分类目录创建失败，请查看控制台错误信息！");
                return true;
            }
            // saveResource 的 output 相对 dataFolder，需含 Npcs/ 前缀
            if (!this.rsNPC.saveResource("Npc.yml", "Npcs/" + name + ".yml", false)) {
                this.rsNPC.getLogger().error("NPC 配置模板写入失败: " + targetFile);
                sender.sendMessage("创建NPC失败！配置文件写入失败，请查看控制台错误信息！");
                return true;
            }
            Config config = new Config(targetFile, Config.YAML);
            // 显示名取相对路径的末尾段，避免 nameTag 出现 "分类A/NPC1"
            config.set("name", new File(name).getName());
            Player player = (Player) sender;
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("x", player.getX());
            map.put("y", player.getY());
            map.put("z", player.getZ());
            map.put("yaw", Utils.getYaw(player));
            map.put("level", player.getLevel().getName());
            config.set("坐标", map);
            if (!config.save()) {
                this.rsNPC.getLogger().error("NPC 配置文件保存失败: " + targetFile);
                sender.sendMessage("创建NPC失败！配置文件保存失败，请查看控制台错误信息！");
                //noinspection ResultOfMethodCallIgnored
                targetFile.delete();
                cleanupCategoryDirs(targetFile);
                return true;
            }
            RsNpcConfig rsNpcConfig;
            try {
                rsNpcConfig = new RsNpcConfig(name, config, targetFile);
            } catch (Exception e) {
                sender.sendMessage("创建NPC失败！请查看控制台错误信息！");
                this.rsNPC.getLogger().error("创建NPC失败！", e);
                //noinspection ResultOfMethodCallIgnored
                targetFile.delete();
                cleanupCategoryDirs(targetFile);
                return true;
            }
            this.rsNPC.getNpcs().put(name, rsNpcConfig);
            rsNpcConfig.checkEntity();
            //修复首次生成不显示的问题 通过重复生成实体解决nk未能及时发送PlayerListPacket的问题
            Server.getInstance().getScheduler().scheduleDelayedTask(this.rsNPC, () -> rsNpcConfig.getEntityRsNpc().close(), 20);
            Server.getInstance().getScheduler().scheduleDelayedTask(this.rsNPC, rsNpcConfig::checkEntity, 40);
            sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.npcCreateSuccess", name));
        } else {
            sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.nameRequired"));
        }
        return true;
    }

    private void cleanupCategoryDirs(File targetFile) {
        Utils.cleanupEmptyParentDirs(targetFile.getParentFile(),
                new File(this.rsNPC.getDataFolder(), "Npcs"));
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("NPC_Name", CommandParamType.TEXT) };
    }
}
