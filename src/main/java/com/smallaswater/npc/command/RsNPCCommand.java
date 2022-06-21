package com.smallaswater.npc.command;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.smallaswater.npc.command.base.BaseCommand;
import com.smallaswater.npc.command.sub.AddRouteSubCommand;
import com.smallaswater.npc.command.sub.CreateSubCommand;
import com.smallaswater.npc.command.sub.DeleteSubCommand;
import com.smallaswater.npc.command.sub.ReloadSubCommand;
import com.smallaswater.npc.form.FormHelper;

/**
 * @author LT_Name
 */
public class RsNPCCommand extends BaseCommand {

    public RsNPCCommand(String name) {
        super(name.toLowerCase(), "RsNPC命令");
        this.setPermission("RsNPC.admin");

        this.addSubCommand(new CreateSubCommand("Create"));
        this.addSubCommand(new DeleteSubCommand("Delete"));
        this.addSubCommand(new AddRouteSubCommand("AddRoute"));
        this.addSubCommand(new ReloadSubCommand("Reload"));
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sender.sendMessage("§a§l >> §eHelp for RsNPC §a<<");
        sender.sendMessage("§a§l/rsnpc create <NPC名称> §7在当前位置创建NPC");
        sender.sendMessage("§a§l/rsnpc delete <NPC名称> §7移除NPC");
        sender.sendMessage("§a§l/rsnpc addroute <NPC名称> §7将当前位置添加到NPC路径");
        sender.sendMessage("§a§l/rsnpc reload §7重载NPC");
        sender.sendMessage("§a§l >> §eHelp for RsNPC §a<<");
    }

    @Override
    public void sendUI(Player player) {
        FormHelper.sendMain(player);
    }

}
