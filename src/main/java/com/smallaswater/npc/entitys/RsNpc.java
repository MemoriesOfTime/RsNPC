package com.smallaswater.npc.entitys;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EmotePacket;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.NpcMainClass;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

public class RsNpc extends EntityHuman {

    private boolean lookAtThePlayer;

    private boolean enableEmote;
    private ArrayList<String> emoteIDs = new ArrayList<>();
    private int setEmoteSecond;
    private int emoteSecond = 0;

    public RsNpc(FullChunk chunk, CompoundTag nbt, String name, Config config) {
        super(chunk, nbt);
        this.setNameTagAlwaysVisible();
        this.setNameTagVisible();
        this.setNameTag(name);
        //setDataFlag(37, -1);
        this.setMaxHealth(20);
        this.setHealth(20.0F);
        //判断是否看向玩家
        this.lookAtThePlayer = config.getBoolean("看向玩家", true);
        //表情动作
        this.enableEmote = config.getBoolean("表情动作.启用");
        this.emoteIDs.addAll(config.getStringList("表情动作.表情ID"));
        this.setEmoteSecond = config.getInt("表情动作.间隔(秒)", 10);
    }


    public void setItemHand(Item itemHand) {
        this.getInventory().setItemInHand(itemHand);
    }

    public void setArmor(Item[] armor) {
        this.getInventory().setArmorContents(armor);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (currentTick%20 == 0) {
            this.emoteSecond++;
        }
        if (this.emoteSecond == this.setEmoteSecond && !this.emoteIDs.isEmpty()) {
            this.emoteSecond = 0;
            EmotePacket packet = new EmotePacket();
            packet.runtimeId = this.getId();
            packet.emoteID = this.emoteIDs.get(NpcMainClass.RANDOM.nextInt(this.emoteIDs.size()));
            packet.flags = 0;
            this.getLevel().getPlayers().values().forEach(player -> player.dataPacket(packet));
        }
        if (this.lookAtThePlayer && !getLevel().getPlayers().isEmpty() && currentTick%2 == 0) {
            CompletableFuture.runAsync(() -> {
                LinkedList<String> npd = new LinkedList<>();
                for (Player player : this.getLevel().getPlayers().values()) {
                    double distance = this.distance(player);
                    npd.add(player.getName() + "@" + distance);
                }
                npd.sort((mapping1, mapping2) -> {
                    String[] nameMapNum1 = mapping1.split("@");
                    String[] nameMapNum2 = mapping2.split("@");
                    double compare = Double.parseDouble(nameMapNum1[1]) - Double.parseDouble(nameMapNum2[1]);
                    return Double.compare(compare, 0.0D);
                });
                String name = npd.get(0).split("@")[0];
                Player player = getServer().getPlayer(name);
                if (player != null && player.isOnline() && player.getLevel() == this.getLevel()) {
                    double npcx = this.x - player.x;
                    double npcy = this.y - player.y;
                    double npcz = this.z - player.z;
                    double yaw = Math.asin(npcx / Math.sqrt(npcx * npcx + npcz * npcz)) / 3.14D * 180.0D;
                    double pitch = Math.round(Math.asin(npcy / Math.sqrt(npcx * npcx + npcz * npcz + npcy * npcy)) / 3.14D * 180.0D);
                    if (npcz > 0.0D) {
                        yaw = -yaw + 180.0D;
                    }
                    this.yaw = yaw;
                    this.pitch = pitch;
                }
            });
        }
        return super.onUpdate(currentTick);
    }

}