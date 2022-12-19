package com.smallaswater.npc.command;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.smallaswater.npc.command.base.BaseCommand;
import com.smallaswater.npc.command.sub.AddRouteSubCommand;
import com.smallaswater.npc.command.sub.CreateSubCommand;
import com.smallaswater.npc.command.sub.DeleteSubCommand;
import com.smallaswater.npc.command.sub.ReloadSubCommand;
import com.smallaswater.npc.utils.FormHelper;

/**
 * @author LT_Name
 */
public class RsNPCCommand extends BaseCommand {

    public RsNPCCommand(String name) {
        super(name.toLowerCase(), "RsNPC Command");
        this.setPermission("RsNPC.admin");

        this.addSubCommand(new CreateSubCommand("Create"));
        this.addSubCommand(new DeleteSubCommand("Delete"));
        this.addSubCommand(new AddRouteSubCommand("AddRoute"));
        this.addSubCommand(new ReloadSubCommand("Reload"));
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.command.help"));
    }

    @Override
    public void sendUI(Player player) {
        FormHelper.sendMain(player);
    }

}
