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
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.entitys.EntityRsNpc;
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
        if (event.getEntity() instanceof EntityRsNpc ||
                event.getVehicle() instanceof EntityRsNpc) {
            event.setCancelled(true);
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
                    EntityRsNpc rsNpc = (EntityRsNpc) entity;
                    RsNpcConfig config = rsNpc.getConfig();
                    if (!config.isCanProjectilesTrigger() &&
                            event instanceof EntityDamageByChildEntityEvent) {
                        return;
                    }
                    rsNpc.setPauseMoveTick(60);
                    this.executeCommand(player, config);
                    for (String message : config.getMessages()) {
                        player.sendMessage(VariableManage.stringReplace(player, message, config));
                    }
                }
            }
        }
    }

    private void executeCommand(Player player, RsNpcConfig rsNpcConfig) {
        for (String cmd : rsNpcConfig.getCmds()) {
            String[] c = cmd.split("&");
            String cm = c[0];
            if (c.length > 1) {
                if ("con".equals(c[1])) {
                    try {
                        Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(),
                                VariableManage.stringReplace(player, cm, rsNpcConfig));
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
                        Server.getInstance().dispatchCommand(player, VariableManage.stringReplace(player, cm, rsNpcConfig));
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
                Server.getInstance().dispatchCommand(player, VariableManage.stringReplace(player, cm, rsNpcConfig));
            } catch (Exception e) {
                this.rsNpcX.getLogger().error(
                        "玩家权限执行命令时出现错误！NPC:" + rsNpcConfig.getName() +
                                " 玩家:" + player.getName() +
                                " 错误:", e);
            }
        }
    }

}
