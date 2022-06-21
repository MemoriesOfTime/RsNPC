package com.smallaswater.npc.utils.update;

import cn.nukkit.plugin.Plugin;
import com.smallaswater.npc.RsNPC;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * @author LT_Name
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigUpdateUtils {

    public static void updateConfig(RsNPC rsNPC) {
        //RsNPC 1.X.X  暂时不需要更新
        updateRsNpcX1_X_X_To_RsNPC2_0_0(rsNPC);
    }

    /**
     * 从RsNPCX-1.X.X 更新到RsNPC-2.0.0
     */
    private static void updateRsNpcX1_X_X_To_RsNPC2_0_0(RsNPC rsNPC) {
        //卸载RsNPCX插件
        Plugin rsNPCX = rsNPC.getServer().getPluginManager().getPlugin("RsNPCX");
        if (rsNPCX != null) {
            try {
                Class.forName("com.smallaswater.npc.RsNpcX");
                rsNPC.getServer().getPluginManager().disablePlugin(rsNPCX);
            }catch (Exception ignored) {

            }
        }
        //仅文件夹名称修改
        File file = new File(rsNPC.getServer().getPluginPath() + "/RsNPCX");
        if (file.exists()) {
            if (file.renameTo(rsNPC.getDataFolder())) {
                rsNPC.getLogger().info("[ConfigUpdateUtils](updateRsNpcX1_X_X_To_RsNPC2_0_0) 配置文件更新成功！");
            }else {
                rsNPC.getLogger().error("[ConfigUpdateUtils](updateRsNpcX1_X_X_To_RsNPC2_0_0) RsNPCX文件夹重命名失败，请手动将文件夹重命名为RsNPC");
            }
        }
    }

}
