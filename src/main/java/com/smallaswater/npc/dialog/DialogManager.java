package com.smallaswater.npc.dialog;

import cn.nukkit.utils.Config;
import com.smallaswater.npc.RsNpcX;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author LT_Name
 */
public class DialogManager {

    private RsNpcX rsNpcX;
    private final HashMap<String, DialogPages> dialogConfigs = new HashMap<>();

    public DialogManager(RsNpcX rsNpcX) {
        this.rsNpcX = rsNpcX;
        this.loadAllDialog();
    }

    public void loadAllDialog() {
        this.dialogConfigs.clear();

        File[] files = new File(this.rsNpcX.getDataFolder() + "/Dialog").listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        Arrays.stream(files)
                .filter(File::isFile)
                .filter(file -> file.getName().endsWith(".yml"))
                .forEach(file -> {
                    try {
                        this.loadDialog(file.getName().split("\\.")[0]);
                    } catch (Exception e) {
                        this.rsNpcX.getLogger().error("加载对话文件失败：" + file.getName(), e);
                    }
                });
        RsNpcX.getInstance().getLogger().info("成功加载: " + this.dialogConfigs.size() + "个对话页面配置");
    }

    public void loadDialog(String name) {
        Config config = new Config(this.rsNpcX.getDataFolder() + "/Dialog/" + name + ".yml", Config.YAML);
        this.dialogConfigs.put(name, new DialogPages(name, config));
    }

    public DialogPages getDialogConfig(String name) {
        return this.dialogConfigs.get(name);
    }

}
