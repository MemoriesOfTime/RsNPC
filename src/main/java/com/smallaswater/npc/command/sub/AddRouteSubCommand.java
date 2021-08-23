package com.smallaswater.npc.command.sub;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
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
        return sender.isPlayer() && sender.isOp();
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
            if (!this.rsNpcX.getNpcs().containsKey(name)) {
                sender.sendMessage("§c§lNPC " + name + " 不存在！");
                return true;
            }
            RsNpcConfig rsNpcConfig = this.rsNpcX.getNpcs().get(name);
            rsNpcConfig.getRoute().add(player.clone());
            List<String> list = rsNpcConfig.getConfig().getStringList("route");
            list.add(player.getX() + ":" + player.getY() + ":" + player.getZ());
            rsNpcConfig.getConfig().set("route", list);
            rsNpcConfig.getConfig().save();
            sender.sendMessage("§a§l已添加到路径");
        }else {
            sender.sendMessage("§c§l请输入要设置的NPC的名字！");
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("NPC名称", CommandParamType.TEXT) };
    }
}
