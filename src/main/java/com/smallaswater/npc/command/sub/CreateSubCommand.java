package com.smallaswater.npc.command.sub;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.command.base.BaseSubCommand;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.utils.RsNpcLoadException;
import com.smallaswater.npc.utils.Utils;

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
        return sender.isPlayer() && sender.isOp();
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            String name = args[1].trim();
            if ("".equals(name)) {
                sender.sendMessage("§c§lNPC的名字不能是空格！");
                return true;
            }
            if (this.rsNpcX.getNpcs().containsKey(name)) {
                sender.sendMessage("§c§lNPC " + name + "已经存在...");
                return true;
            }
            this.rsNpcX.saveResource("npc.yml", "/Npcs/" + name + ".yml", false);
            Config config = new Config(this.rsNpcX.getDataFolder() + "/Npcs/" + name + ".yml", Config.YAML);
            config.set("name", name);
            Player player = (Player) sender;
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("x", player.getX());
            map.put("y", player.getY());
            map.put("z", player.getZ());
            map.put("yaw", Utils.getYaw(player));
            map.put("level", player.getLevel().getName());
            config.set("坐标", map);
            config.save();
            RsNpcConfig rsNpcConfig;
            try {
                rsNpcConfig = new RsNpcConfig(name, config);
            } catch (RsNpcLoadException e) {
                sender.sendMessage("创建NPC失败！");
                this.rsNpcX.getLogger().error("创建NPC失败！", e);
                return true;
            }
            this.rsNpcX.getNpcs().put(name, rsNpcConfig);
            rsNpcConfig.checkEntity();
            sender.sendMessage("§a§lNPC " + name + "创建成功!!");
        } else {
            sender.sendMessage("§c§l请输入要创建的NPC的名字！");
        }
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
        return new CommandParameter[] { CommandParameter.newType("NPC名称", CommandParamType.TEXT) };
    }
}
