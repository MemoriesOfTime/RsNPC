
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
            if (!owner.spawns.containsKey(name)) {
                owner.spawnNPC(name);
                continue;
            }
            RsNpc rsNpc = owner.spawns.get(name);
            if (rsNpc.isClosed()) {
                owner.spawnNPC(name);
            }
        }
    }

}