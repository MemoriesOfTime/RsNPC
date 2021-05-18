package com.smallaswater.npc.variable;

import cn.nukkit.Player;
import com.smallaswater.npc.data.RsNpcConfig;

/**
 * @author lt_name
 */
public class DefaultVariable extends BaseVariable {
    
    @Override
    public String stringReplace(Player player, String inString) {
        return this.stringReplace(player, inString, null);
    }
    
    @Override
    public String stringReplace(Player player, String inString, RsNpcConfig rsNpcConfig) {
        if (player == null) {
            return inString;
        }
        return inString
                .replace("%npcName%", rsNpcConfig.getName())
                .replace("@p", player.getName());
    }

}
