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

    public static void addVariable(String name, Class<? extends BaseVariable> variableClass) {
        try {
            BaseVariable variable = variableClass.newInstance();
            variables.put(name, variable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String stringReplace(Player player, String inString) {
        for (BaseVariable variable : variables.values()) {
            inString = variable.stringReplace(player, inString);
        }
        return inString;
    }

}
