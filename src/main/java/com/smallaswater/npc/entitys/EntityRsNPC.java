package com.smallaswater.npc.entitys;

import cn.lanink.gamecore.utils.EntityUtils;
import cn.lanink.gamecore.utils.packet.ProtocolVersion;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockLiquid;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.*;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.route.Node;
import com.smallaswater.npc.route.RouteFinder;
import com.smallaswater.npc.variable.VariableManage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashSet;
import java.util.LinkedList;

public class EntityRsNPC extends EntityHuman {

    @Getter
    private final RsNpcConfig config;
    private int emoteSecond = 0;
    private int nextRouteIndex = 0;
    @Getter
    private final LinkedList<Node> nodes = new LinkedList<>();
    private Node nowNode;
    @Setter
    private boolean lockRoute = false;

    private Vector3 lastPos;
    private int lastUpdateNodeTick;

    private RouteFinder nowRouteFinder;
    @Setter
    private int pauseMoveTick = 0;

    /**
     * RsNPC实体在创建时必须传入RsNPCConfig参数，保留此方法仅为兼容核心创建实体方法
     */
    @Deprecated
    public EntityRsNPC(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityRsNPC(@NonNull FullChunk chunk, @NonNull CompoundTag nbt, RsNpcConfig config) {
        super(chunk, nbt);
        this.config = config;
        if (this.config == null) {
            this.close();
            return;
        }
        this.setNameTagAlwaysVisible(config.isNameTagAlwaysVisible());
        this.setNameTagVisible();
        this.setNameTag(config.getShowName());
        this.setMaxHealth(20);
        this.setHealth(20.0F);
        this.getInventory().setItemInHand(config.getHand());
        this.getInventory().setArmorContents(config.getArmor());

        //以下内容在initEntity()中执行，需要在获取到config后再执行一次
        if (config.isEnableCustomCollisionSize()) {
            this.dataProperties.putFloat(EntityUtils.getEntityField("DATA_BOUNDING_BOX_HEIGHT", DATA_BOUNDING_BOX_HEIGHT), this.getHeight());
            this.dataProperties.putFloat(EntityUtils.getEntityField("DATA_BOUNDING_BOX_WIDTH", DATA_BOUNDING_BOX_WIDTH), this.getWidth());
            this.dataProperties.putInt(EntityUtils.getEntityField("DATA_HEALTH", DATA_HEALTH), (int) this.getHealth());
        }
    }

    @Override
    public float getWidth() {
        if (this.config != null && this.config.isEnableCustomCollisionSize()) {
            return this.config.getCustomCollisionSizeWidth();
        }
        return super.getWidth();
    }

    @Override
    public float getLength() {
        if (this.config != null && this.config.isEnableCustomCollisionSize()) {
            return this.config.getCustomCollisionSizeLength();
        }
        return super.getLength();
    }

    @Override
    public float getHeight() {
        if (this.config != null && this.config.isEnableCustomCollisionSize()) {
            return this.config.getCustomCollisionSizeHeight();
        }
        return super.getHeight();
    }

    @Override
    public int getNetworkId() {
        if (this.config == null) {
            return super.getNetworkId();
        }
        return this.config.getNetworkId();
    }

    @Override
    protected float getBaseOffset() {
        if (this.getNetworkId() == -1) {
            return super.getBaseOffset();
        }
        return 0.0F;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.config == null) {
            this.close();
            return false;
        }

        //旋转
        if (this.config.getWhirling() != 0) {
            this.yaw += this.config.getWhirling();
        }else {
            //寻路
            if (!this.config.getRoute().isEmpty() && this.pauseMoveTick <= 0) {
                this.processMove(currentTick);
            } else {
                //看向玩家
                if (currentTick%2 == 0 && this.config.isLookAtThePlayer() && !this.getViewers().isEmpty()) {
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
                        packet.emoteID = this.config.getEmoteIDs().get(RsNPC.RANDOM.nextInt(this.config.getEmoteIDs().size()));
                        packet.flags = 0;
                        if (ProtocolInfo.CURRENT_PROTOCOL >= ProtocolVersion.v1_20_0_23) {
                            packet.xuid = "";
                            packet.platformId = "";
                        }
                        Server.broadcastPacket(this.getViewers().values(), packet);
                    }
                }
            }
        }
        
        return super.onUpdate(currentTick);
    }

    private void processMove(int currentTick) {
        if (this.nodes.isEmpty()) {
            Vector3 next = this.config.getRoute().get(this.nextRouteIndex);
            if (this.config.isEnablePathfinding()) {
                if (!this.lockRoute) {
                    this.setLockRoute(true);
                    this.nextRouteIndex++;
                    if (this.nextRouteIndex >= this.config.getRoute().size()) {
                        this.nextRouteIndex = 0;
                    }
                    this.nowRouteFinder = new RouteFinder(this.getLevel(), this, next);
                } else if (this.nowRouteFinder != null && this.nowRouteFinder.isProcessingComplete()) {
                    this.nodes.addAll(this.nowRouteFinder.getNodes());
                    this.setLockRoute(false);
                }
            }else {
                this.nodes.add(new Node(next));
                this.nextRouteIndex++;
                if (this.nextRouteIndex >= this.config.getRoute().size()) {
                    this.nextRouteIndex = 0;
                }
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
                    if (this.distance(lastPos) < 0.1) {
                        this.setPosition(vector3);
                        return;
                    }
                    this.lastUpdateNodeTick = currentTick;
                }

                this.lastPos = this.getLocation();
                double x = vector3.x - this.x;
                double z = vector3.z - this.z;
                double diff = Math.abs(x) + Math.abs(z);
                this.motionY = this.config.getBaseMoveSpeed() * vector3.y - this.y;
                if (this.getLevelBlock() instanceof BlockLiquid) {
                    this.motionX = this.config.getBaseMoveSpeed() * 0.05 * (x / diff);
                    this.motionZ = this.config.getBaseMoveSpeed() * 0.05 * (z / diff);
                } else {
                    this.motionX = this.config.getBaseMoveSpeed() * 0.15 * (x / diff);
                    this.motionZ = this.config.getBaseMoveSpeed() * 0.15 * (z / diff);
                }
                this.move(this.motionX, this.motionY, this.motionZ);

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
                    this.headYaw = yaw;
                    this.pitch = 0;
                }
            }
        }
    }

    private void seePlayer() {
        RsNPC.THREAD_POOL_EXECUTOR.execute(() -> {
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
                this.headYaw = yaw;
                this.pitch = pitch;
            }
        });
    }

    @Override
    public void addMovement(double x, double y, double z, double yaw, double pitch, double headYaw) {
        if (this.getNetworkId() == -1) {
            this.level.addPlayerMovement(this, x, y, z, yaw, pitch, headYaw);
        } else {
            this.level.addEntityMovement(this, x, y, z, yaw, pitch, headYaw);
        }
    }

    @Override
    public void spawnTo(Player player) {
        if (this.getNetworkId() == -1) {
            super.spawnTo(player);
            this.sendData(player);
        }

        if (!this.hasSpawned.containsKey(player.getLoaderId()) && this.chunk != null && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))) {
            this.hasSpawned.put(player.getLoaderId(), player);
            player.dataPacket(this.createAddEntityPacket());
            this.sendData(player);
        }
        if (this.riding != null) {
            this.riding.spawnTo(player);
            SetEntityLinkPacket pkk = new SetEntityLinkPacket();
            pkk.vehicleUniqueId = this.riding.getId();
            pkk.riderUniqueId = this.getId();
            pkk.type = 1;
            pkk.immediate = 1;
            player.dataPacket(pkk);
        }
    }

    @Override
    public void despawnFrom(Player player) {
        if (this.getNetworkId() == -1) {
            super.despawnFrom(player);
        }

        if (this.hasSpawned.containsKey(player.getLoaderId())) {
            RemoveEntityPacket pk = new RemoveEntityPacket();
            pk.eid = this.getId();
            player.dataPacket(pk);
            this.hasSpawned.remove(player.getLoaderId());
        }
    }

    @Override
    public void setSkin(Skin skin) {
        Skin oldSkin = this.getSkin();
        super.setSkin(skin);
        this.sendSkin(oldSkin);
    }

    protected void sendSkin(Skin oldSkin) {
        PlayerSkinPacket packet = new PlayerSkinPacket();
        packet.skin = this.getSkin();
        packet.newSkinName = this.getSkin().getSkinId();
        packet.oldSkinName = oldSkin != null ? oldSkin.getSkinId() : "old";
        packet.uuid = this.getUniqueId();
        HashSet<Player> players = new HashSet<>(this.getViewers().values());
        if (!players.isEmpty()) {
            Server.broadcastPacket(players, packet);
        }
    }

    @Override
    public void sendData(Player player, EntityMetadata data) {
        SetEntityDataPacket pk = new SetEntityDataPacket();
        pk.eid = this.getId();
        pk.metadata = data == null ? this.dataProperties : data;
        pk.metadata.putString(
                EntityUtils.getEntityField("DATA_NAMETAG", DATA_NAMETAG),
                VariableManage.stringReplace(player, this.getNameTag(), this.getConfig())
        );
        player.dataPacket(pk);
    }

    @Override
    public void sendData(Player[] players, EntityMetadata data) {
        SetEntityDataPacket pk = new SetEntityDataPacket();
        pk.eid = this.getId();
        pk.metadata = data == null ? this.dataProperties : data;

        for(Player player : players) {
            SetEntityDataPacket clone = (SetEntityDataPacket) pk.clone();
            clone.metadata.putString(
                    EntityUtils.getEntityField("DATA_NAMETAG", DATA_NAMETAG),
                    VariableManage.stringReplace(player, this.getNameTag(), this.getConfig())
            );
            player.dataPacket(clone);
        }
    }

    //为了兼容PM1E，我们不使用setCanBeSavedWithChunk()方法
    @Override
    public boolean canBeSavedWithChunk() {
        return false;
    }
}