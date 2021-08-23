package com.smallaswater.npc.form;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import com.smallaswater.npc.form.windows.AdvancedFormWindowCustom;
import com.smallaswater.npc.form.windows.AdvancedFormWindowModal;
import com.smallaswater.npc.form.windows.AdvancedFormWindowSimple;

/**
 * @author lt_name
 */
public class FormListener implements Listener {

    @EventHandler
    public void onPlayerFormResponded(PlayerFormRespondedEvent event) {
        if (AdvancedFormWindowSimple.onEvent(event.getWindow(), event.getPlayer())) {
            return;
        }
        if (AdvancedFormWindowModal.onEvent(event.getWindow(), event.getPlayer())) {
            return;
        }
        AdvancedFormWindowCustom.onEvent(event.getWindow(), event.getPlayer());
    }

}
