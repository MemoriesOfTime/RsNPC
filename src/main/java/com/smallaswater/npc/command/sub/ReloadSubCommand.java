package com.smallaswater.npc.command.sub;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import com.smallaswater.npc.command.base.BaseSubCommand;

/**
 * @author LT_Name
 */
public class ReloadSubCommand extends BaseSubCommand {

    public ReloadSubCommand(String name) {
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
        this.rsNPC.reload();
        sender.sendMessage("§a§l已重载配置！");
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[0];
    }
}
