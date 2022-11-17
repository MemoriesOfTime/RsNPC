package com.smallaswater.npc.utils.update;

import cn.lanink.gamecore.utils.VersionUtils;
import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.utils.Utils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;

/**
 * @author LT_Name
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUpdateUtils {

    public static void updateConfig(RsNPC rsNPC) {
        //RsNPC 1.X.X -- RsNpcX 1.X.X 暂时不需要更新
        updateRsNpcX1_X_X_To_RsNPC2_0_0(rsNPC);
        updateRsNPC2_0_0_To_RsNPC2_2_3();
    }

    /**
     * 从RsNPCX 1.X.X 更新到 RsNPC 2.0.0
     */
    private static void updateRsNpcX1_X_X_To_RsNPC2_0_0(RsNPC rsNPC) {
        //卸载并删除RsNPCX插件
        Plugin rsNPCX = rsNPC.getServer().getPluginManager().getPlugin("RsNPCX");
        if (rsNPCX != null) {
            try {
                Class.forName("com.smallaswater.npc.RsNpcX"); //防止误操作其他重名插件
                File file = Utils.getPluginFile(rsNPCX);
                Server.getInstance().getPluginManager().disablePlugin(rsNPCX);
                ClassLoader classLoader = rsNPCX.getClass().getClassLoader();
                if (classLoader instanceof URLClassLoader) {
                    ((URLClassLoader) classLoader).close();
                }
                if (file != null) {
                    file.delete();
                }
            }catch (Exception ignored) {

            }
        }
        //文件夹名称修改
        File file = new File(rsNPC.getServer().getPluginPath() + "/RsNPCX");
        if (file.exists()) {
            if (file.renameTo(rsNPC.getDataFolder())) {
                rsNPC.getLogger().info("[ConfigUpdateUtils](updateRsNpcX1_X_X_To_RsNPC2_0_0) 配置文件更新成功！");
            }else {
                rsNPC.getLogger().error("[ConfigUpdateUtils](updateRsNpcX1_X_X_To_RsNPC2_0_0) RsNPCX文件夹重命名失败，请手动将文件夹重命名为RsNPC");
            }
        }
    }

    /**
     * 从RsNPC 2.0.0--2.2.2 更新到 RsNPC-2.2.3
     */
    private static void updateRsNPC2_0_0_To_RsNPC2_2_3() {
        File[] files = (new File(RsNPC.getInstance().getDataFolder() + "/Npcs")).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!file.isFile() && file.getName().endsWith(".yml")) {
                    continue;
                }
                Config config = new Config(file, Config.YAML);

                if (VersionUtils.compareVersion(config.getString(RsNpcConfig.NPC_CONFIG_VERSION_KEY, "2.0.0"), "2.2.3") >= 0) {
                    continue;
                }

                // 表情动作.间隔(秒) -> 表情动作.间隔
                HashMap<Object, Object> map = config.get("表情动作", new HashMap<>());
                map.put("间隔", map.getOrDefault("间隔(秒)", 10));
                map.remove("间隔(秒)");
                config.set("表情动作", map);

                config.set(RsNpcConfig.NPC_CONFIG_VERSION_KEY, "2.2.3");

                config.save();
            }
        }
    }

}
