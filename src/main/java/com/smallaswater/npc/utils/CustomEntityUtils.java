package com.smallaswater.npc.utils;

import cn.lanink.gamecore.api.Info;

/**
 * @author LT_Name
 */
@Deprecated
@Info("此类仅适用于NKX")
public class CustomEntityUtils {

    private CustomEntityUtils() {
        throw new UnsupportedOperationException();
    }

    public static int getRuntimeId(String identifier) {
        return -1;
    }

    public static void registerCustomEntity(String identifier) {
        throw new UnsupportedOperationException("PM1E 分支不支持此方法！");
    }

}
