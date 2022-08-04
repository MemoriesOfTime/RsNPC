package com.smallaswater.npc.entitys;

import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import com.smallaswater.npc.data.RsNpcConfig;
import lombok.NonNull;

/**
 * 基于pnx自定义实体实现的rsnpc实体
 *
 * @author LT_Name
 */
public class EntityRsNPCCustomEntity extends EntityRsNPC implements CustomEntity {

    private CustomEntityDefinition definition;

    @Deprecated
    public EntityRsNPCCustomEntity(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityRsNPCCustomEntity(@NonNull FullChunk chunk, @NonNull CompoundTag nbt, RsNpcConfig config) {
        super(chunk, nbt, config);
        this.definition = CustomEntityDefinition.builder().identifier("RsNPC").spawnEgg(false).summonable(false).build();
    }

    public void setDefinition(CustomEntityDefinition definition) {
        this.definition = definition;
    }

    @Override
    public CustomEntityDefinition getDefinition() {
        return this.definition;
    }

}
