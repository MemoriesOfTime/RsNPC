package com.smallaswater.npc.entitys;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EmotePacket;
import com.smallaswater.npc.RsNpcX;
import com.smallaswater.npc.data.RsNpcConfig;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

public class EntityRsNpc extends EntityHuman {

    private final RsNpcConfig config;
    private int emoteSecond = 0;

    public EntityRsNpc(FullChunk chunk, CompoundTag nbt, RsNpcConfig config) {
        super(chunk, nbt);
        this.config = config;
        this.setNameTagAlwaysVisible();
        this.setNameTagVisible();
        this.setNameTag(config.getShowName());
        //setDataFlag(37, -1);
        this.setMaxHealth(20);
        this.setHealth(20.0F);
        this.getInventory().setItemInHand(config.getHand());
        this.getInventory().setArmorContents(config.getArmor());
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.config.isLookAtThePlayer() && !getLevel().getPlayers().isEmpty() && currentTick%2 == 0) {
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
        if (this.config.isEnableEmote() && !this.config.getEmoteIDs().isEmpty()) {
            if (currentTick % 20 == 0) {
                this.emoteSecond++;
            }
            if (this.emoteSecond >= this.config.getShowEmoteInterval()) {
                this.emoteSecond = 0;
                EmotePacket packet = new EmotePacket();
                packet.runtimeId = this.getId();
                packet.emoteID = this.config.getEmoteIDs().get(RsNpcX.RANDOM.nextInt(this.config.getEmoteIDs().size()));
                packet.flags = 0;
                Server.broadcastPacket(this.getViewers().values(), packet);
            }
        }
        return super.onUpdate(currentTick);
    }

}