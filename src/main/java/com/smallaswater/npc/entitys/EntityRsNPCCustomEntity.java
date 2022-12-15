package com.smallaswater.npc.entitys;

import cn.nukkit.Player;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import com.smallaswater.npc.data.RsNpcConfig;
import lombok.NonNull;

/**
 * 基于pnx自定义实体实现的rsnpc实体
 *
 * @author LT_Name
 */
public class EntityRsNPCCustomEntity extends EntityRsNPC implements CustomEntity {

    private EntityDefinition definition;

    @Deprecated
    public EntityRsNPCCustomEntity(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityRsNPCCustomEntity(@NonNull FullChunk chunk, @NonNull CompoundTag nbt, RsNpcConfig config) {
        super(chunk, nbt, config);
        this.definition = EntityDefinition.builder().identifier("RsNPC").spawnEgg(false).implementation(EntityRsNPCCustomEntity.class).build();
    }

    public void setDefinition(EntityDefinition definition) {
        this.definition = definition;
    }

    @Override
    public int getNetworkId() {
        return 0;
    }

    public EntityDefinition getDefinition() {
        return this.getEntityDefinition();
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return this.definition;
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
