package com.smallaswater.npc.variable;

import cn.nukkit.Player;

/**
 * @author lt_name
 */
public class DefaultVariable extends BaseVariable {

    @Override
    public String stringReplace(Player player, String inString) {
        if (player == null) {
            return inString;
        }
        return inString.replace("@p", player.getName());
    }

}
