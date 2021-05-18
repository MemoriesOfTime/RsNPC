package com.smallaswater.npc.variable;

import cn.nukkit.Player;
import com.smallaswater.npc.data.RsNpcConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author lt_name
 */
public class VariableManage {

    private static final HashMap<String, BaseVariable> variables = new HashMap<>();

    private VariableManage() {

    }

    public static void addVariable(@NotNull String name, @NotNull Class<? extends BaseVariable> variableClass) {
        try {
            BaseVariable variable = variableClass.newInstance();
            variables.put(name, variable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String stringReplace(Player player, @NotNull String inString, RsNpcConfig rsNpcConfig) {
        for (BaseVariable variable : variables.values()) {
            inString = variable.stringReplace(player, inString, rsNpcConfig);
        }
        return inString;
    }

}
