package com.smallaswater.npc.variable;

import cn.nukkit.Player;
import com.smallaswater.npc.data.RsNpcConfig;

/**
 * @author lt_name
 */
public class DefaultVariable extends BaseVariableV2 {

    @Override
    public void onUpdate(Player player, RsNpcConfig rsNpcConfig) {
        this.addVariable("\\n", "\n"); //将字符 \n 替换为换行
        this.addVariable("\\\n", "\\n"); //将字符 \\n 替换为字符 \n
        if (rsNpcConfig != null) {
            this.addVariable("%npcName%", rsNpcConfig.getName());
        }
        if (player != null && player.isOnline()) {
            this.addVariable("@p", player.getName());
        }
    }

}
