package com.smallaswater.npc.command.base;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import com.smallaswater.npc.RsNPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SmallasWater
 */
public abstract class BaseCommand extends Command {

    private final ArrayList<BaseSubCommand> subCommand = new ArrayList<>();
    private final ConcurrentHashMap<String, Integer> subCommands = new ConcurrentHashMap<>();
    protected RsNPC rsNPC = RsNPC.getInstance();

    public BaseCommand(String name, String description) {
        super(name,description);
    }

    /**
     * 判断权限
     * @param sender 玩家
     * @return 是否拥有权限
     */
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(this.getPermission());
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if(hasPermission(sender)) {
            if(args.length > 0) {
                String subCommand = args[0].toLowerCase();
                if (subCommands.containsKey(subCommand)) {
                    BaseSubCommand command = this.subCommand.get(this.subCommands.get(subCommand));
                    if (command.canUser(sender)) {
                        return command.execute(sender, s, args);
                    }else if (sender.isPlayer()) {
                        sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.command.noPermissions"));
                    }else {
                        sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.command.cannotBeUsedInConsole"));
                    }
                }else {
                    this.sendHelp(sender);
                }
            }else {
                if (sender.isPlayer()) {
                    this.sendUI((Player) sender);
                }else {
                    this.sendHelp(sender);
                }
            }
            return true;
        }
        sender.sendMessage(this.rsNPC.getLanguage().translateString("tips.command.noPermissions"));
        return true;
    }

    /**
     * 发送帮助
     * @param sender 玩家
     * */
    public abstract void sendHelp(CommandSender sender);

    /**
     * 发送UI
     * @param player 玩家
     */
    public abstract void sendUI(Player player);

    protected void addSubCommand(BaseSubCommand cmd) {
        this.subCommand.add(cmd);
        int commandId = (this.subCommand.size()) - 1;
        this.subCommands.put(cmd.getName().toLowerCase(), commandId);
        for (String alias : cmd.getAliases()) {
            this.subCommands.put(alias.toLowerCase(), commandId);
        }
        this.loadCommandBase();
    }

    private void loadCommandBase(){
        this.commandParameters.clear();
        for(BaseSubCommand subCommand : this.subCommand) {
            LinkedList<CommandParameter> parameters = new LinkedList<>();
            parameters.add(CommandParameter.newEnum(subCommand.getName(), new String[]{subCommand.getName()}));
            parameters.addAll(Arrays.asList(subCommand.getParameters()));
            this.commandParameters.put(subCommand.getName(),parameters.toArray(new CommandParameter[0]));
        }
    }

}
