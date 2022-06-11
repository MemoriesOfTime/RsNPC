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
import com.smallaswater.npc.entitys.EntityRsNpc;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.utils.dialog.handler.FormDialogHandler;
import com.smallaswater.npc.utils.dialog.packet.NPCDialoguePacket;
import com.smallaswater.npc.utils.dialog.packet.NPCRequestPacket;
import com.smallaswater.npc.utils.dialog.response.FormResponseDialog;
import com.smallaswater.npc.utils.dialog.window.FormWindowDialog;
import com.smallaswater.npc.variable.VariableManage;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class OnListener implements Listener {

    private final RsNpcX rsNpcX;

    public OnListener(RsNpcX rsNpcX) {
        this.rsNpcX = rsNpcX;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityVehicleEnter(EntityVehicleEnterEvent event) {
        if (event.getEntity() instanceof EntityRsNpc) {
            event.setCancelled(true);
        }
        if (!Server.getInstance().getCodename().equals("PM1E")) {
            if (event.getVehicle() instanceof EntityRsNpc) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityRsNpc) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            EntityRsNpc rsNpc = (EntityRsNpc) entity;
            RsNpcConfig config = rsNpc.getConfig();
            rsNpc.setPauseMoveTick(60);
            this.executeCommand(player, config);
            for (String message : config.getMessages()) {
                player.sendMessage(VariableManage.stringReplace(player, message, config));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityRsNpc) {
            event.setCancelled(true);
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damage = ((EntityDamageByEntityEvent) event).getDamager();
                if (damage instanceof Player) {
                    Player player = (Player) damage;
                    EntityRsNpc entityRsNpc = (EntityRsNpc) entity;
                    RsNpcConfig rsNpcConfig = entityRsNpc.getConfig();
                    if (!rsNpcConfig.isCanProjectilesTrigger() &&
                            event instanceof EntityDamageByChildEntityEvent) {
                        return;
                    }
                    entityRsNpc.setPauseMoveTick(60);
                    this.executeCommand(player, rsNpcConfig);
                    for (String message : rsNpcConfig.getMessages()) {
                        player.sendMessage(VariableManage.stringReplace(player, message, rsNpcConfig));
                    }

                    if (rsNpcConfig.isEnabledDialogPages()) {
                        DialogPages dialogConfig = this.rsNpcX.getDialogManager().getDialogConfig(rsNpcConfig.getDialogPagesName());
                        dialogConfig.getDefaultDialogPage().send(entityRsNpc, player);
                    }
                }
            }
        }
    }

    private void executeCommand(Player player, RsNpcConfig rsNpcConfig) {
        for (String cmd : rsNpcConfig.getCmds()) {
            String[] c = cmd.split("&");
            String command = c[0];
            if (command.startsWith("/")) {
                command = command.replaceFirst("/", "");
            }
            if (c.length > 1) {
                if ("con".equals(c[1])) {
                    try {
                        Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(),
                                VariableManage.stringReplace(player, command, rsNpcConfig));
                    } catch (Exception e) {
                        this.rsNpcX.getLogger().error(
                                "控制台权限执行命令时出现错误！NPC:" + rsNpcConfig.getName() +
                                        " 玩家:" + player.getName() +
                                        " 错误:", e);
                    }
                    continue;
                }else if ("op".equals(c[1])) {
                    boolean needCancelOP = false;
                    final String playerName = player.getName();
                    if (!player.isOp()) {
                        needCancelOP = true;
                        Server.getInstance().getScheduler().scheduleDelayedTask(this.rsNpcX,
                                () -> Server.getInstance().removeOp(playerName), 1);
                        player.setOp(true);
                    }
                    try {
                        Server.getInstance().dispatchCommand(player, VariableManage.stringReplace(player, command, rsNpcConfig));
                    } catch (Exception e) {
                        this.rsNpcX.getLogger().error(
                                "OP权限执行命令时出现错误！NPC:" + rsNpcConfig.getName() +
                                        " 玩家:" + player.getName() +
                                        " 错误:", e);
                    } finally {
                        if (needCancelOP) {
                            try {
                                player.setOp(false);
                            } catch (Exception ignored) {

                            }
                            Server.getInstance().removeOp(playerName);
                        }
                    }
                    continue;
                }
            }
            try {
                Server.getInstance().dispatchCommand(player, VariableManage.stringReplace(player, command, rsNpcConfig));
            } catch (Exception e) {
                this.rsNpcX.getLogger().error(
                        "玩家权限执行命令时出现错误！NPC:" + rsNpcConfig.getName() +
                                " 玩家:" + player.getName() +
                                " 错误:", e);
            }
        }
    }

    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        if (event.getPacket() instanceof NPCRequestPacket) {
            Player player = event.getPlayer();
            NPCRequestPacket npcRequestPacket = (NPCRequestPacket) event.getPacket();
            if (Utils.WINDOW_DIALOG_CACHE.getIfPresent(npcRequestPacket.getSceneName()) != null) {
                FormWindowDialog dialog;
                if (npcRequestPacket.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_CLOSING_COMMANDS) {
                    dialog = Utils.WINDOW_DIALOG_CACHE.getIfPresent(npcRequestPacket.getSceneName());
                    Utils.WINDOW_DIALOG_CACHE.invalidate(npcRequestPacket.getSceneName());
                } else {
                    dialog = Utils.WINDOW_DIALOG_CACHE.getIfPresent(npcRequestPacket.getSceneName());
                }


                FormResponseDialog response = new FormResponseDialog(npcRequestPacket, dialog);
                for (FormDialogHandler handler : dialog.getHandlers()) {
                    handler.handle(player, response);
                }

                response.getClickedButton().callClicked(player);

                if (response.getClickedButton() != null && response.getClickedButton().closeWhenClicked() && npcRequestPacket.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_ACTION) {
                    NPCDialoguePacket closeWindowPacket = new NPCDialoguePacket();
                    closeWindowPacket.setRuntimeEntityId(npcRequestPacket.getRequestedEntityRuntimeId());
                    closeWindowPacket.setSceneName(response.getSceneName());
                    closeWindowPacket.setAction(NPCDialoguePacket.NPCDialogAction.CLOSE);
                    player.dataPacket(closeWindowPacket);
                }
                if (response.getClickedButton() != null && response.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_ACTION && response.getClickedButton().getNextDialog() != null) {
                    response.getClickedButton().getNextDialog().send(player);
                }
            }
        }
    }

}
