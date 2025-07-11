package com.smallaswater.npc;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFishingHook;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityVehicleEnterEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.PlayerListPacket;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.dialog.DialogPages;
import com.smallaswater.npc.entitys.EntityRsNPC;
import com.smallaswater.npc.utils.Utils;
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
            Utils.executeCommand(player, config, null, entityRsNPC);
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
        if (entity instanceof EntityRsNPC entityRsNPC) {
            event.setCancelled(true);
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damage = ((EntityDamageByEntityEvent) event).getDamager();
                if (damage instanceof Player player) {
                    RsNpcConfig rsNpcConfig = entityRsNPC.getConfig();
                    if (!rsNpcConfig.isCanProjectilesTrigger() &&
                            event instanceof EntityDamageByChildEntityEvent e) {
                        if (e.getChild() instanceof EntityFishingHook fishingHook) {
                            if (fishingHook.shootingEntity instanceof Player shooter) {
                                shooter.stopFishing(false);
                            } else {
                                fishingHook.close();
                            }
                        }
                        return;
                    }
                    entityRsNPC.setPauseMoveTick(60);
                    Utils.executeCommand(player, rsNpcConfig, null, entityRsNPC);
                    for (String message : rsNpcConfig.getMessages()) {
                        player.sendMessage(VariableManage.stringReplace(player, message, rsNpcConfig));
                    }

                    if (rsNpcConfig.isEnabledDialogPages()) {
                        DialogPages dialogConfig = this.rsNPC.getDialogManager().getDialogConfig(rsNpcConfig.getDialogPagesName());
                        if (dialogConfig != null) {
                            dialogConfig.getDefaultDialogPage().send(entityRsNPC, player);
                        }else {
                            String message = "§cNPC " + rsNpcConfig.getName() + " 配置错误！不存在名为 " + rsNpcConfig.getDialogPagesName() + " 的对话框页面！";
                            this.rsNPC.getLogger().warning(message);
                            if (player.isOp()) {
                                player.sendMessage(message);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDataPacketSend(DataPacketSendEvent event) {
        if (Api.isHideCustomSkin(event.getPlayer())) {
            if (event.getPacket() instanceof PlayerListPacket) {
                PlayerListPacket packet = (PlayerListPacket) event.getPacket();
                for (PlayerListPacket.Entry entry : packet.entries) {
                    for (RsNpcConfig config : this.rsNPC.getNpcs().values()) {
                        EntityRsNPC entityRsNpc = config.getEntityRsNpc();
                        if (entityRsNpc != null && entityRsNpc.getUniqueId() == entry.uuid) {
                            entry.skin = this.rsNPC.getSkinByName("默认皮肤");
                            break;
                        }
                    }
                }
                packet.encode();
            }
        }
    }

}
