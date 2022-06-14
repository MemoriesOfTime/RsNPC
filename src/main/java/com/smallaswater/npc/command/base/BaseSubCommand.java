package com.smallaswater.npc.command.base;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import com.smallaswater.npc.RsNPC;


/**
 * @author SmallasWater
 */
public abstract class BaseSubCommand {

    protected RsNPC rsNPC = RsNPC.getInstance();

    private final String name;

    protected BaseSubCommand(String name) {
        this.name = name.toLowerCase();
    }

    /**
     * @param sender CommandSender
     * @return boolean
     */
    public abstract boolean canUser(CommandSender sender);

    /**
     * 获取名称
     * @return string
     */
    public String getName(){
        return name;
    }

    /**
     * 获取别名
     * @return string[]
     */
    public abstract String[] getAliases();

    /**
     * 命令响应
     * @param sender the sender      - CommandSender
     * @param args   The arrugements      - String[]
     * @param label  label..
     * @return true if true
     */
    public abstract boolean execute(CommandSender sender, String label, String[] args);

    /**
     * 指令参数.
     * @return  提示参数
     * */
    public abstract CommandParameter[] getParameters();

}
