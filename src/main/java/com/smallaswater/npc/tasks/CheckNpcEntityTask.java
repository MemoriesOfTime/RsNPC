package com.smallaswater.npc.tasks;

import cn.nukkit.scheduler.PluginTask;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.data.RsNpcConfig;

public class CheckNpcEntityTask extends PluginTask<RsNPC> {

    public CheckNpcEntityTask(RsNPC owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for (RsNpcConfig rsNpcConfig : this.owner.getNpcs().values()) {
            rsNpcConfig.checkEntity();
        }
    }

}