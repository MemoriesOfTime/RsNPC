package com.smallaswater.npc.entitys;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockLiquid;
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
    private final LinkedList<Node> nodes = new LinkedList<>();
    private Node nowNode;
    @Setter
    private boolean lockRoute = false;
    private int lastUpdateNodeTick;
    private RouteFinder nowRouteFinder;
    @Setter
    private int pauseMoveTick = 0;

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
    
    @Override
    public boolean onUpdate(int currentTick) {
        if (this.config == null) {
            this.close();
            return false;
        }

        //旋转
        if (Math.abs(this.config.getWhirling()) > 0) {
            this.yaw += this.config.getWhirling();
        }else {
            //寻路
            if (!this.config.getRoute().isEmpty() && this.pauseMoveTick <= 0) {
                if (this.nodes.isEmpty()) {
                    if (!this.lockRoute) {
                        this.setLockRoute(true);
                        Vector3 next = this.config.getRoute().get(this.nextRouteIndex);
                        this.nextRouteIndex++;
                        if (this.nextRouteIndex >= this.config.getRoute().size()) {
                            this.nextRouteIndex = 0;
                        }
                        this.nowRouteFinder = new RouteFinder(this.getLevel(), this, next);
                    } else if (this.nowRouteFinder != null && this.nowRouteFinder.isProcessingComplete()) {
                        this.nodes.clear();
                        this.nodes.addAll(this.nowRouteFinder.getNodes());
                        this.setLockRoute(false);
                    }
                }

                if (!this.nodes.isEmpty()) {
                    if (this.nowNode == null || this.distance(this.nowNode.getVector3()) <= 0.35/*((this.getWidth()) / 2 + 0.05)*/) {
                        this.nowNode = this.nodes.poll();
                        this.lastUpdateNodeTick = currentTick;
                    }
                    if (this.nowNode != null) {
                        Vector3 vector3 = this.nowNode.getVector3();

                        if (currentTick - this.lastUpdateNodeTick > 100) {
                            this.setPosition(vector3);
                            this.lastUpdateNodeTick = currentTick;
                        } else {
                            double x = vector3.x - this.x;
                            double z = vector3.z - this.z;
                            double diff = Math.abs(x) + Math.abs(z);

                            this.motionY = vector3.y - this.y;
                            if (this.getLevelBlock() instanceof BlockLiquid) {
                                this.motionX = this.config.getBaseMoveSpeed() * 0.05 * (x / diff);
                                this.motionZ = this.config.getBaseMoveSpeed() * 0.05 * (z / diff);
                            } else {
                                this.motionX = this.config.getBaseMoveSpeed() * 0.15 * (x / diff);
                                this.motionZ = this.config.getBaseMoveSpeed() * 0.15 * (z / diff);
                            }

                            this.move(this.motionX, this.motionY, this.motionZ);
                        }

                        //视角计算
                        if (currentTick % 4 == 0) {
                            if (this.nodes.size() >= 2) {
                                vector3 = this.nodes.get(1).getVector3();
                            }
                            double dx = this.x - vector3.x;
                            double dz = this.z - vector3.z;
                            double yaw = Math.asin(dx / Math.sqrt(dx * dx + dz * dz)) / Math.PI * 180.0D;
                            if (dz > 0.0D) {
                                yaw = -yaw + 180.0D;
                            }
                            this.yaw = yaw;
                            this.pitch = 0;
                        }
                    }
                }
            } else {
                //看向玩家
                if (currentTick%2 == 0 && this.config.isLookAtThePlayer() && !this.getLevel().getPlayers().isEmpty()) {
                    this.seePlayer();
                }

                if (this.pauseMoveTick > 0) {
                    this.pauseMoveTick--;
                }

                //表情
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
            }
        }
        
        return super.onUpdate(currentTick);
    }

    private void seePlayer() {
        RsNpcX.THREAD_POOL_EXECUTOR.execute(() -> {
            LinkedList<Player> npd = new LinkedList<>(this.getViewers().values());
            npd.sort((p1, p2) -> Double.compare(this.distance(p1) - this.distance(p2), 0.0D));
            Player player = npd.poll();
            if (player != null) {
                double dx = this.x - player.x;
                double dy = this.y - player.y;
                double dz = this.z - player.z;
                double yaw = Math.asin(dx / Math.sqrt(dx * dx + dz * dz)) / Math.PI * 180.0D;
                double pitch = Math.round(Math.asin(dy / Math.sqrt(dx * dx + dz * dz + dy * dy)) / Math.PI * 180.0D);
                if (dz > 0.0D) {
                    yaw = -yaw + 180.0D;
                }
                this.yaw = yaw;
                this.pitch = pitch;
            }
        });
    }

    public RsNpcConfig getConfig() {
        return this.config;
    }

}