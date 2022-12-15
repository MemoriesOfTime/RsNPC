package com.smallaswater.npc.utils;

import cn.nukkit.network.protocol.AvailableEntityIdentifiersPacket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LT_Name
 */
public class CustomEntityUtils {

    private static ConcurrentHashMap<String, Integer> IDENTIFIER_MAP = new ConcurrentHashMap<>();
    private static AtomicInteger RUNTIME_ID = new AtomicInteger(10000);

    private CustomEntityUtils() {

    }

    public static void registerCustomEntity(String identifier, int networkId) {
        // TODO
        IDENTIFIER_MAP.put(identifier, RUNTIME_ID.getAndIncrement());

        Class<AvailableEntityIdentifiersPacket> aClass = AvailableEntityIdentifiersPacket.class;

        //TODO 反射注入
    }

}
