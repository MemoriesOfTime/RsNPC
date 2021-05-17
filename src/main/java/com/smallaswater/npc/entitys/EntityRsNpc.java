package com.smallaswater.npc.entitys;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EmotePacket;
import com.smallaswater.npc.RsNpcX;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.route.Node;
import com.smallaswater.npc.route.RouteFinder;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

public class EntityRsNpc extends EntityHuman {

    private final RsNpcConfig config;
    private int emoteSecond = 0;
    private int nextRouteIndex = 0;
    @Getter
    private LinkedList<Node> nodes = new LinkedList<>();
    private Node nowNode;
    @Setter
    private boolean lockRoute = false;
    private int lastUpdateNodeTick;

    @Deprecated
    public EntityRsNpc(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityRsNpc(FullChunk chunk, CompoundTag nbt, RsNpcConfig config) {
        super(chunk, nbt);
        this.config = config;
        if (this.config == null) {
            this.close();
            return;
        }
        this.setNameTagAlwaysVisible();
        this.setNameTagVisible();
        this.setNameTag(config.getShowName());
        this.setMaxHealth(20);
        this.setHealth(20.0F);
        this.getInventory().setItemInHand(config.getHand());
        this.getInventory().setArmorContents(config.getArmor());
    }

    private RouteFinder routeFinder;
    
    @Override
    public boolean onUpdate(int currentTick) {
        if (this.config == null) {
            this.close();
            return false;
        }
        if (this.config.isLookAtThePlayer() &&
                this.config.getRoute().isEmpty() &&
                !this.getLevel().getPlayers().isEmpty() && currentTick%2 == 0) {
            RsNpcX.THREAD_POOL_EXECUTOR.execute(() -> {
                LinkedList<Player> npd = new LinkedList<>(this.getViewers().values());
                npd.sort((mapping1, mapping2) ->
                        Double.compare(this.distance(mapping1) - this.distance(mapping2), 0.0D));
                Player player = npd.poll();
                if (player != null) {
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
        
        if (currentTick%4 == 0) {
            if (!this.config.getRoute().isEmpty()) {
                if (this.nodes.isEmpty() && !this.lockRoute) {
                    this.lockRoute = true;
                    Vector3 next = this.config.getRoute().get(this.nextRouteIndex);
                    this.nextRouteIndex++;
                    if (this.nextRouteIndex >= this.config.getRoute().size()) {
                        this.nextRouteIndex = 0;
                    }
                    routeFinder = new RouteFinder(this.getLevel(), this, next, this);
                }
                
                //TODO
                if (routeFinder != null) {
                    routeFinder.show();
                }
                
                if (!this.nodes.isEmpty()) {
                    if (this.nowNode == null || this.distance(nowNode.getVector3()) < 0.3) {
                        this.nowNode = this.nodes.poll();
                        this.lastUpdateNodeTick = currentTick;
                    }
                    if (this.nowNode != null) {
                        Vector3 vector3 = this.nowNode.getVector3();
    
                        if (currentTick - this.lastUpdateNodeTick > 100) {
                            this.setPosition(vector3);
                            this.lastUpdateNodeTick = currentTick;
                        }else {
                            double x = vector3.x - this.x;
                            double z = vector3.z - this.z;
                            double diff = Math.abs(x) + Math.abs(z);
                            this.move(x / diff * 0.5, vector3.y - this.y, z / diff * 0.5);
                        }
    
                        //视角计算
                        if (!this.nodes.isEmpty()) {
                            Vector3 last = this.nodes.getLast().getVector3();
                            double npcx = this.x - last.x;
                            double npcz = this.z - last.z;
                            double yaw = Math.asin(npcx / Math.sqrt(npcx * npcx + npcz * npcz)) / 3.14D * 180.0D;
                            if (npcz > 0.0D) {
                                yaw = -yaw + 180.0D;
                            }
                            this.yaw = yaw;
                            this.pitch = 0;
                        }
                    }
                }
            }
        }
        
        return super.onUpdate(currentTick);
    }

    public RsNpcConfig getConfig() {
        return this.config;
    }

}