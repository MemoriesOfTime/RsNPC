package com.smallaswater.npc;

import cn.lanink.gamecore.api.Info;
import cn.nukkit.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author LT_Name
 */
public final class Api {

    private Api() {
        throw new RuntimeException("This class cannot be instantiated");
    }

    private static final Set<String> HIDE_CUSTOM_SKIN_PLAYERS = new HashSet<>();

    public static void removeAll(Player player) {
        HIDE_CUSTOM_SKIN_PLAYERS.remove(player);
    }

    @Info("针对指定玩家隐藏自定义皮肤")
    public static void hideCustomSkin(@NotNull Player player) {
        hideCustomSkin(player.getName());
    }

    public static void hideCustomSkin(@NotNull String playerName) {
        HIDE_CUSTOM_SKIN_PLAYERS.add(playerName);
    }

    public static void showCustomSkin(@NotNull Player player) {
        showCustomSkin(player.getName());
    }

    public static void showCustomSkin(@NotNull String playerName) {
        HIDE_CUSTOM_SKIN_PLAYERS.remove(playerName);
    }

    public static boolean isHideCustomSkin(@NotNull Player player) {
        return isHideCustomSkin(player.getName());
    }

    public static boolean isHideCustomSkin(@NotNull String playerName) {
        return HIDE_CUSTOM_SKIN_PLAYERS.contains(playerName);
    }

}
