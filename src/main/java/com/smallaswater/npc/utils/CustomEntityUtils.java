package com.smallaswater.npc.utils;

import cn.nukkit.Nukkit;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AvailableEntityIdentifiersPacket;
import com.smallaswater.npc.RsNPC;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LT_Name
 */
public class CustomEntityUtils {

    private static final ConcurrentHashMap<String, Integer> IDENTIFIER_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger RUNTIME_ID = new AtomicInteger(10000);

    private CustomEntityUtils() {
        throw new UnsupportedOperationException();
    }

    public static int getRuntimeId(String identifier) {
        if (identifier == null) {
            return -1;
        }
        return IDENTIFIER_MAP.getOrDefault(identifier, -1);
    }

    public static void registerCustomEntity(String identifier) {
        IDENTIFIER_MAP.put(identifier, RUNTIME_ID.getAndIncrement());
        //反射修改参数
        try {
            InputStream inputStream = Nukkit.class.getClassLoader().getResourceAsStream("entity_identifiers.dat");
            if (inputStream == null) {
                throw new AssertionError("Could not find entity_identifiers.dat");
            }
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            CompoundTag nbt = NBTIO.read(bis, ByteOrder.BIG_ENDIAN, true);
            ListTag<CompoundTag> list = nbt.getList("idlist", CompoundTag.class);
            for (Map.Entry<String, Integer> entry : IDENTIFIER_MAP.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean("hasspawnegg", false);
                tag.putString("id", entry.getKey());
                tag.putBoolean("summonable", false);
                tag.putString("bid", "");
                tag.putInt("rid", entry.getValue());
                tag.putBoolean("experimental", false);

                list.add(tag);
            }
            nbt.putList(list);

            Class<AvailableEntityIdentifiersPacket> aClass = AvailableEntityIdentifiersPacket.class;
            Field tagField = aClass.getDeclaredField("TAG");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(tagField, tagField.getModifiers() & ~Modifier.FINAL); // 移除final修饰符

            tagField.setAccessible(true);
            tagField.set(null, NBTIO.write(nbt, ByteOrder.BIG_ENDIAN, true));
        }catch (Exception e) {
            IDENTIFIER_MAP.remove(identifier);
            RsNPC.getInstance().getLogger().error("注册自定义实体失败！", e);
        }
    }

}
