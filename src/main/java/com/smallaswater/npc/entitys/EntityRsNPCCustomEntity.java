package com.smallaswater.npc.entitys;

import cn.lanink.gamecore.utils.EntityUtils;
import cn.nukkit.Player;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import com.smallaswater.npc.data.RsNpcConfig;
import lombok.NonNull;

/**
 * 基于自定义实体功能实现的RsNPC实体
 *
 * @author LT_Name
 */
public class EntityRsNPCCustomEntity extends EntityRsNPC implements CustomEntity {

    private static final CustomEntityDefinition DEFAULT_DEFINITION = CustomEntityDefinition.builder()
            .identifier("RsNPC:Demo")
            .spawnEgg(false)
            .summonable(false)
            .build();

    private CustomEntityDefinition definition;

    @Deprecated
    public EntityRsNPCCustomEntity(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityRsNPCCustomEntity(@NonNull FullChunk chunk, @NonNull CompoundTag nbt, RsNpcConfig config) {
        super(chunk, nbt, config);
    }

    public void setDefinition(CustomEntityDefinition definition) {
        this.definition = definition;
    }

    @Override
    public int getNetworkId() {
        return this.getDefinition().getRuntimeId();
    }

    /**
     * 获取实体定义
     * （PNX和PM1E分支独有方法）
     *
     * @return 实体定义
     */
    @Override
    public CustomEntityDefinition getDefinition() {
        if (this.definition == null) {
            return DEFAULT_DEFINITION;
        }
        return this.definition;
    }

    public void setIdentifier(String identifier) {
        this.definition = CustomEntityDefinition.builder()
                .identifier(identifier)
                .spawnEgg(false)
                .summonable(false)
                .build();
    }

    public String getIdentifier() {
        return this.getDefinition().getStringId();
    }

    public void setSkinId(int skinId) {
        this.namedTag.putInt("skinId", skinId);
        this.setDataProperty(
                new IntEntityData(EntityUtils.getEntityField("DATA_SKIN_ID", DATA_SKIN_ID),
                        this.namedTag.getInt("skinId")
                )
        );
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
