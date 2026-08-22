package com.smallaswater.npc.variable;

import cn.nukkit.Player;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.utils.Utils;

/**
 * @author lt_name
 */
public class DefaultVariable extends BaseVariableV2 {

    @Override
    public void onUpdate(Player player, RsNpcConfig rsNpcConfig) {
        this.addVariable("\\n", "\n"); //将字符 \n 替换为换行
        this.addVariable("\\\n", "\\n"); //将字符 \\n 替换为字符 \n
        if (rsNpcConfig != null) {
            // getName() 现为相对路径 key（如 分类A/NPC1），取末段保持与旧版扁平名一致
            this.addVariable("%npcName%", Utils.lastSegment(rsNpcConfig.getName()));
        }
        if (player != null && player.isOnline()) {
            this.addVariable("@p", player.getName());
        }
    }

}
