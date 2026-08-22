package com.smallaswater.npc.command.sub;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import com.smallaswater.npc.command.base.BaseSubCommand;
import com.smallaswater.npc.utils.Utils;

import java.io.File;

/**
 * @author LT_Name
 */
public class DeleteSubCommand extends BaseSubCommand {

    public DeleteSubCommand(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.hasPermission("RsNPC.admin.delete");
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            String name = Utils.normalizePathSeparator(args[1]);
            if (!Utils.isSafeRelativePath(name)) {
                sender.sendMessage("§cNPC 名称包含非法路径字符（禁止 .. 或绝对路径）！");
                return true;
            }
            if (!this.rsNPC.getNpcs().containsKey(name)) {
                sender.sendMessage("§c§lNPC " + name + "不存在...");
                return true;
            }
            com.smallaswater.npc.data.RsNpcConfig rsNpcConfig = this.rsNPC.getNpcs().get(name);
            // 实体可能未生成（如所在世界无玩家），为 null 时跳过关闭
            if (rsNpcConfig.getEntityRsNpc() != null) {
                rsNpcConfig.getEntityRsNpc().close();
            }
            this.rsNPC.getNpcs().remove(name);
            File configFile = rsNpcConfig.getConfigFile();
            if (configFile != null && configFile.delete()) {
                Utils.cleanupEmptyParentDirs(configFile.getParentFile(),
                        new File(this.rsNPC.getDataFolder(), "Npcs"));
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.command.npcRemoveSuccess", name));
            } else {
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.command.npcRemoveFileFailed", name));
            }
        } else {
            sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.nameRequired"));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("NPC_Name", CommandParamType.TEXT) };
    }
}
