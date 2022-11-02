package com.smallaswater.npc.tasks;

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
    private final String playerName;

    public PlayerPermissionCheckTask(RsNPC owner, Player player) {
        super(owner);
        this.player = player;
        this.playerName = this.player.getName().toLowerCase();
    }

    @Override
    public void onRun(int i) {
        if (++this.nowCeckCount > this.maxCheckCount) {
            this.cancel();
        }
        if (this.owner.getServer().getOps().getAll().containsKey(this.playerName)) {
            if (!this.player.isOnline()) {
                this.owner.getServer().getOps().remove(this.playerName);
                this.owner.getServer().getOps().save();
                this.cancel();
            }else {
                this.player.setOp(false);
                this.owner.getServer().getOps().remove(this.playerName);
                this.cancel();
            }
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
