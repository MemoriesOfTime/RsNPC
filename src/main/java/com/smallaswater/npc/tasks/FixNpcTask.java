
package com.smallaswater.npc.tasks;

import cn.nukkit.scheduler.PluginTask;
import com.smallaswater.npc.NpcMainClass;
import com.smallaswater.npc.entitys.RsNpc;

public class FixNpcTask extends PluginTask<NpcMainClass> {

    public FixNpcTask(NpcMainClass owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for (String name : owner.npcs.keySet()) {
            RsNpc rsNpc = owner.spawns.get(name);
            if (rsNpc == null || rsNpc.isClosed()) {
                owner.spawnNPC(name);
            }
        }
    }

}