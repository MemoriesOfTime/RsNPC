package com.smallaswater.npc.variable;

import cn.nukkit.IPlayer;
import cn.nukkit.OfflinePlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.data.RsNpcConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author lt_name
 */
public class VariableManage {

    private static final ConcurrentHashMap<String, BiFunction<IPlayer, RsNpcConfig, Object>> VARIABLES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Supplier<Object>> VARIABLES_SUPPLIER = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, BaseVariable> VARIABLE_CLASS = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, BaseVariableV2> VARIABLE_V2_CLASS = new ConcurrentHashMap<>();

    private VariableManage() {

    }

    public static void addVariable(@NotNull String key, @NotNull Supplier<Object> value) {
        VARIABLES_SUPPLIER.put(key, value);
    }

    public static void addVariable(@NotNull String key, @NotNull BiFunction<IPlayer, RsNpcConfig, Object> value) {
        VARIABLES.put(key, value);
    }

    @Deprecated
    public static void addVariable(@NotNull String name, @NotNull Class<? extends BaseVariable> variableClass) {
        RsNPC.getInstance().getLogger().warning("有插件注册了一个弃用的变量类！名字:" + name + " 类:" + variableClass + " 这可能会导致一些安全问题！");
        try {
            BaseVariable variable = variableClass.newInstance();
            VariableManage.VARIABLE_CLASS.put(name, variable);
        } catch (Exception e) {
            RsNPC.getInstance().getLogger().error("添加变量时出错", e);
        }
    }

    public static void addVariableV2(@NotNull String name, @NotNull Class<? extends BaseVariableV2> variableClass) {
        try {
            BaseVariableV2 variable = variableClass.getDeclaredConstructor().newInstance();
            VariableManage.VARIABLE_V2_CLASS.put(name, variable);
        } catch (Exception e) {
            RsNPC.getInstance().getLogger().error("添加变量时出错", e);
        }
    }

    public static String stringReplace(IPlayer player, @NotNull String inString, @NotNull RsNpcConfig rsNpcConfig) {
        if (player == null) {
            player = new OfflinePlayer(Server.getInstance(), "RsNPCFakePlayer");
        }

        for (Map.Entry<String, Supplier<Object>> entry : VARIABLES_SUPPLIER.entrySet()) {
            inString = inString.replace(entry.getKey(), String.valueOf(entry.getValue().get()));
        }
        for (Map.Entry<String, BiFunction<IPlayer, RsNpcConfig, Object>> entry : VARIABLES.entrySet()) {
            inString = inString.replace(entry.getKey(), String.valueOf(entry.getValue().apply(player, rsNpcConfig)));
        }

        Player p = null;
        if (player instanceof Player) {
            p = (Player) player;
        }
        for (BaseVariable variable : VARIABLE_CLASS.values()) {
            inString = variable.stringReplace(p, inString, rsNpcConfig);
        }
        for (BaseVariableV2 variable : VARIABLE_V2_CLASS.values()) {
            variable.onUpdate(p, rsNpcConfig);
            inString = variable.stringReplace(inString);
        }

        return inString;
    }

}
