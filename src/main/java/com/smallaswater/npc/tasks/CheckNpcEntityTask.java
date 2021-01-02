
package com.smallaswater.npc.tasks;

import cn.nukkit.scheduler.PluginTask;
import com.smallaswater.npc.RsNpcX;
import com.smallaswater.npc.data.RsNpcConfig;

public class CheckNpcEntityTask extends PluginTask<RsNpcX> {

    public CheckNpcEntityTask(RsNpcX owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for (RsNpcConfig rsNpcConfig : this.owner.getNpcs().values()) {
            rsNpcConfig.checkEntity();
        }
    }

}