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
        return sender.hasPermission("RsNPC.delete");
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
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.command.npcRemoveFileFailed", name));
            }else {
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.command.npcRemoveSuccess", name));
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
