package com.smallaswater.npc.command.sub;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.command.base.BaseSubCommand;
import com.smallaswater.npc.data.RsNpcConfig;
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
        return sender.isPlayer() && sender.hasPermission("RsNPC.admin.create");
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
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.nameRequired"));
                return true;
            }
            if (this.rsNPC.getNpcs().containsKey(name)) {
                sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.npcAlreadyExist", name));
                return true;
            }
            this.rsNPC.saveResource("Npc.yml", "/Npcs/" + name + ".yml", false);
            Config config = new Config(this.rsNPC.getDataFolder() + "/Npcs/" + name + ".yml", Config.YAML);
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
            } catch (Exception e) {
                sender.sendMessage("创建NPC失败！请查看控制台错误信息！");
                this.rsNPC.getLogger().error("创建NPC失败！", e);
                return true;
            }
            this.rsNPC.getNpcs().put(name, rsNpcConfig);
            rsNpcConfig.checkEntity();
            //玄学解决首次生成不显示的问题
            Server.getInstance().getScheduler().scheduleDelayedTask(this.rsNPC, () -> {
                rsNpcConfig.getEntityRsNpc().close();
                rsNpcConfig.checkEntity();
            }, 20);
            sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.npcCreateSuccess", name));
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
