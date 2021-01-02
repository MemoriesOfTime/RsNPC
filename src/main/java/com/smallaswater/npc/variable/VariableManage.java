package com.smallaswater.npc.variable;

import cn.nukkit.Player;

import java.util.HashMap;

/**
 * @author lt_name
 */
public class VariableManage {

    private static final HashMap<String, BaseVariable> variables = new HashMap<>();

    private VariableManage() {

    }

    public static void addVariable(String name, BaseVariable variable) {
        variables.put(name, variable);
    }

    public static String stringReplace(Player player, String inString) {
        for (BaseVariable variable : variables.values()) {
            inString = variable.stringReplace(player, inString);
        }
        return inString;
    }

}
