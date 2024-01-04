package com.smallaswater.npc.command.sub;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.math.Vector3;
import com.smallaswater.npc.command.base.BaseSubCommand;
import com.smallaswater.npc.data.RsNpcConfig;

import java.util.List;

/**
 * @author LT_Name
 */
public class AddRouteSubCommand extends BaseSubCommand {

    public AddRouteSubCommand(String name) {
        super(name);
    }

    @Override
    public boolean canUser(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("RsNPC.admin.addroute");
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length > 1) {
            String name = args[1];
            if (!this.rsNPC.getNpcs().containsKey(name)) {
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.npcNotExist", name));
                return true;
            }
            RsNpcConfig rsNpcConfig = this.rsNPC.getNpcs().get(name);
            rsNpcConfig.getRoute().add(player.clone());
            List<String> list = rsNpcConfig.getConfig().getStringList("route");
            Vector3 floor = player.floor().add(0.5, 0.01, 0.5);
            list.add(floor.getX() + ":" + player.getY() + ":" + player.getZ());
            rsNpcConfig.getConfig().set("route", list);
            rsNpcConfig.getConfig().save();
            sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.npcRouteAddSuccess"));
        }else {
            sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.nameRequired"));
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("NPC_Name", CommandParamType.TEXT) };
    }
}
