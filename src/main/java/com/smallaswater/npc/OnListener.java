package com.smallaswater.npc;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityVehicleEnterEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.dialog.DialogPages;
import com.smallaswater.npc.entitys.EntityRsNPC;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.utils.dialog.packet.NPCRequestPacket;
import com.smallaswater.npc.utils.dialog.window.AdvancedFormWindowDialog;
import com.smallaswater.npc.variable.VariableManage;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class OnListener implements Listener {

    private final RsNPC rsNPC;

    public OnListener(RsNPC rsNPC) {
        this.rsNPC = rsNPC;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityVehicleEnter(EntityVehicleEnterEvent event) {
        if (event.getEntity() instanceof EntityRsNPC) {
            event.setCancelled(true);
        }
        if (!Server.getInstance().getCodename().equals("PM1E")) {
            if (event.getVehicle() instanceof EntityRsNPC) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityRsNPC) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            EntityRsNPC entityRsNPC = (EntityRsNPC) entity;
            RsNpcConfig config = entityRsNPC.getConfig();
            entityRsNPC.setPauseMoveTick(60);
            Utils.executeCommand(player, config);
            for (String message : config.getMessages()) {
                player.sendMessage(VariableManage.stringReplace(player, message, config));
            }
            if (entityRsNPC.getConfig().isEnabledDialogPages()) {
                DialogPages dialogConfig = this.rsNPC.getDialogManager().getDialogConfig(entityRsNPC.getConfig().getDialogPagesName());
                dialogConfig.getDefaultDialogPage().send(entityRsNPC, player);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityRsNPC) {
            event.setCancelled(true);
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damage = ((EntityDamageByEntityEvent) event).getDamager();
                if (damage instanceof Player) {
                    Player player = (Player) damage;
                    EntityRsNPC entityRsNpc = (EntityRsNPC) entity;
                    RsNpcConfig rsNpcConfig = entityRsNpc.getConfig();
                    if (!rsNpcConfig.isCanProjectilesTrigger() &&
                            event instanceof EntityDamageByChildEntityEvent) {
                        return;
                    }
                    entityRsNpc.setPauseMoveTick(60);
                    Utils.executeCommand(player, rsNpcConfig);
                    for (String message : rsNpcConfig.getMessages()) {
                        player.sendMessage(VariableManage.stringReplace(player, message, rsNpcConfig));
                    }

                    if (rsNpcConfig.isEnabledDialogPages()) {
                        DialogPages dialogConfig = this.rsNPC.getDialogManager().getDialogConfig(rsNpcConfig.getDialogPagesName());
                        dialogConfig.getDefaultDialogPage().send(entityRsNpc, player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        if (event.getPacket() instanceof NPCRequestPacket) {
            AdvancedFormWindowDialog.onEvent((NPCRequestPacket) event.getPacket(), event.getPlayer());
        }
    }

}
