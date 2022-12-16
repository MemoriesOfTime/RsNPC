package com.smallaswater.npc.entitys;

import cn.lanink.gamecore.utils.EntityUtils;
import cn.nukkit.Player;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import com.smallaswater.npc.data.RsNpcConfig;
import lombok.NonNull;

/**
 * 基于自定义实体实现的RsNPC实体
 *
 * @author LT_Name
 */
public class EntityRsNPCCustomEntity extends EntityRsNPC implements CustomEntity {

    private static final EntityDefinition DEFAULT_DEFINITION = EntityDefinition.builder()
            .identifier("RsNPC")
            .spawnEgg(false)
            .implementation(EntityRsNPCCustomEntity.class)
            .build();

    private EntityDefinition definition;

    @Deprecated
    public EntityRsNPCCustomEntity(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityRsNPCCustomEntity(@NonNull FullChunk chunk, @NonNull CompoundTag nbt, RsNpcConfig config) {
        super(chunk, nbt.putInt("skinId", 0), config);
    }

    public void setDefinition(EntityDefinition definition) {
        this.definition = definition;
    }

    @Override
    public int getNetworkId() {
        return this.getEntityDefinition().getRuntimeId();
    }

    public void setIdentifier(String identifier) {
        this.definition = EntityDefinition.builder()
                .identifier(identifier)
                .spawnEgg(false)
                .implementation(EntityRsNPCCustomEntity.class)
                .build();
    }

    /**
     * 获取实体定义
     * （PNX和PM1E分支独有方法）
     *
     * @return 实体定义
     */
    public EntityDefinition getDefinition() {
        return this.getEntityDefinition();
    }

    /**
     * 获取实体定义
     * （PM1E分支独有方法）
     *
     * @return 实体定义
     */
    @Override
    public EntityDefinition getEntityDefinition() {
        if (this.definition == null) {
            return DEFAULT_DEFINITION;
        }
        return this.definition;
    }

    public void setSkinId(int skinId) {
        this.namedTag.putInt("skinId", skinId);
    }

    public int getSkinId() {
        return this.namedTag.getInt("skinId");
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.setDataProperty(
                new IntEntityData(EntityUtils.getEntityField("DATA_SKIN_ID", DATA_SKIN_ID),
                        this.namedTag.getInt("skinId")
                )
        );
    }

    @Override
    public void spawnTo(Player player) {
        if (!this.hasSpawned.containsKey(player.getLoaderId()) && this.chunk != null && player.usedChunks.containsKey(Level.chunkHash(this.chunk.getX(), this.chunk.getZ()))) {
            this.hasSpawned.put(player.getLoaderId(), player);
            player.dataPacket(createAddEntityPacket());
        }

        if (this.riding != null) {
            this.riding.spawnTo(player);

            SetEntityLinkPacket pk = new SetEntityLinkPacket();
            pk.vehicleUniqueId = this.riding.getId();
            pk.riderUniqueId = this.getId();
            pk.type = 1;
            pk.immediate = 1;

            player.dataPacket(pk);
        }
    }

}
