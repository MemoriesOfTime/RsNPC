package com.smallaswater.npc.variable;

import cn.nukkit.Player;
import com.smallaswater.npc.data.RsNpcConfig;

/**
 * @author lt_name
 */
@Deprecated
public abstract class BaseVariable {
    
    public abstract String stringReplace(Player player, String inString);
    
    public String stringReplace(Player player, String inString, RsNpcConfig rsNpcConfig) {
        return this.stringReplace(player, inString);
    }

}
