package com.smallaswater.npc.command.sub;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import com.smallaswater.npc.command.base.BaseSubCommand;

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
        return sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            String name = args[1];
            if (!this.rsNPC.getNpcs().containsKey(name)) {
                sender.sendMessage("§c§lNPC " + name + "不存在...");
                return true;
            }
            this.rsNPC.getNpcs().get(name).getEntityRsNpc().close();
            this.rsNPC.getNpcs().remove(name);
            if (!(new File(this.rsNPC.getDataFolder() + "/Npcs/" + name + ".yml")).delete()) {
                sender.sendMessage("§c§lNPC " + name + " 文件删除失败！请尝试手动删除！");
            }else {
                sender.sendMessage("§a§lNPC " + name + " 移除成功 ");
            }
        } else {
            sender.sendMessage("§c§l请输入要删除的NPC的名字！");
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("NPC名称", CommandParamType.TEXT) };
    }
}
