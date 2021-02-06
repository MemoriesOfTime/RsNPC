package com.smallaswater.npc;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.entitys.EntityRsNpc;
import com.smallaswater.npc.variable.VariableManage;

/**
 * @author lt_name
 */
public class OnListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityRsNpc) {
            event.setCancelled();
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damage = ((EntityDamageByEntityEvent) event).getDamager();
                if (damage instanceof Player) {
                    Player player = (Player) damage;
                    RsNpcConfig config = ((EntityRsNpc) entity).getConfig();
                    if (!config.isCanProjectilesTrigger() &&
                            event instanceof EntityDamageByChildEntityEvent) {
                        return;
                    }
                    for (String cmd : config.getCmds()) {
                        String[] c = cmd.split("&");
                        String cm = c[0];
                        if (c.length > 1 && "con".equals(c[1])) {
                            Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(), VariableManage.stringReplace(player, cm));
                        }else {
                            Server.getInstance().dispatchCommand(player, VariableManage.stringReplace(player, cm));
                        }
                    }
                    for (String message : config.getMessages()) {
                        player.sendMessage("[" + config.getName() + "] " + VariableManage.stringReplace(player, message));
                    }
                }
            }
        }
    }

}
