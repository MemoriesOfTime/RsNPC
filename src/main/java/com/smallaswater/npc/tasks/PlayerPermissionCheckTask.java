package com.smallaswater.npc.tasks;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.PluginTask;
import com.smallaswater.npc.RsNPC;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LT_Name
 */
public class PlayerPermissionCheckTask extends PluginTask<RsNPC> {

    private static final ConcurrentHashMap<Player, PlayerPermissionCheckTask> TASKS = new ConcurrentHashMap<>();

    private final int maxCheckCount = 10;
    private int nowCeckCount = 0;
    private final Player player;

    public PlayerPermissionCheckTask(RsNPC owner, Player player) {
        super(owner);
        this.player = player;
    }

    @Override
    public void onRun(int i) {
        if (++this.nowCeckCount > this.maxCheckCount) {
            this.cancel();
        }
        if (!this.player.isOnline()) {
            IPlayer iPlayer = this.owner.getServer().getOfflinePlayer(this.player.getName());
            if (iPlayer != null) {
                iPlayer.setOp(false);
            }
            this.cancel();
            return;
        }
        if (this.player.isOp() || this.owner.getServer().getOps().getAll().containsKey(this.player.getName().toLowerCase())) {
            this.player.setOp(false);
            this.owner.getServer().getOps().remove(this.player.getName().toLowerCase());
            this.cancel();
        }
    }

    @Override
    public void onCancel() {
        this.owner.getServer().getOps().save();
    }

    public static void addCheck(Player player) {
        removeCheck(player);
        PlayerPermissionCheckTask task = new PlayerPermissionCheckTask(RsNPC.getInstance(), player);
        TASKS.put(player, task);
        Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(RsNPC.getInstance(), task, 1, 10);
    }

    public static void removeCheck(Player player) {
        if (TASKS.containsKey(player)) {
            TASKS.get(player).cancel();
            TASKS.remove(player);
        }
    }

}
