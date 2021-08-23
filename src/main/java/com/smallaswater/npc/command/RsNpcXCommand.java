package com.smallaswater.npc.command;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.smallaswater.npc.command.base.BaseCommand;
import com.smallaswater.npc.command.sub.AddRouteSubCommand;
import com.smallaswater.npc.command.sub.CreateSubCommand;
import com.smallaswater.npc.command.sub.DeleteSubCommand;
import com.smallaswater.npc.command.sub.ReloadSubCommand;

/**
 * @author LT_Name
 */
public class RsNpcXCommand extends BaseCommand {

    public RsNpcXCommand(String name) {
        super(name, "RsNpcX命令");
        this.setPermission("RsNPCX.npc");

        this.addSubCommand(new CreateSubCommand("Create"));
        this.addSubCommand(new DeleteSubCommand("Delete"));
        this.addSubCommand(new AddRouteSubCommand("AddRoute"));
        this.addSubCommand(new ReloadSubCommand("Reload"));
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage("§a§l >> §eHelp for RsNPCX §a<<");
        sender.sendMessage("§a§l/rsnpcx create <NPC名称> §7在当前位置创建NPC");
        sender.sendMessage("§a§l/rsnpcx delete <NPC名称> §7移除NPC");
        sender.sendMessage("§a§l/rsnpcx addroute <NPC名称> §7将当前位置添加到NPC路径");
        sender.sendMessage("§a§l/rsnpcx reload §7重载NPC");
        sender.sendMessage("§a§l >> §eHelp for RsNPCX §a<<");
    }

    @Override
    public void sendUI(Player sender) {
        //TODO GUI
        this.sendHelp(sender);
    }

}
