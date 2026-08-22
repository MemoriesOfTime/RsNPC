package com.smallaswater.npc.dialog;

import cn.lanink.gamecore.utils.ConfigUtils;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.RsNPC;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author LT_Name
 */
public class DialogManager {

    private final RsNPC rsNPC;
    private final HashMap<String, DialogPages> dialogConfigs = new HashMap<>();

    private final Config description = new Config();

    public DialogManager(@NotNull RsNPC rsNPC) {
        this.rsNPC = rsNPC;
        InputStream resource = rsNPC.getResource("Language/" + rsNPC.getSetLang() + "/DialogConfigDescription.yml");
        if (resource == null) {
            resource = rsNPC.getResource("Language/chs/DialogConfigDescription.yml");
        }
        this.description.load(resource);
        this.loadAllDialog();
    }

    public void loadAllDialog() {
        this.dialogConfigs.clear();
        File dialogFolder = new File(this.rsNPC.getDataFolder() + "/Dialog");
        loadDialogFromDirectory(dialogFolder);
        this.rsNPC.getLogger().info(this.rsNPC.getLanguage().translateString("plugin.load.dialog.loadComplete", this.dialogConfigs.size()));
    }

    private void loadDialogFromDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        Arrays.stream(files)
                .forEach(file -> {
                    if (file.isDirectory()) {
                        loadDialogFromDirectory(file);
                    } else if (file.isFile() && file.getName().endsWith(".yml")) {
                        try {
                            String name = file.getName().split("\\.")[0];
                            this.loadDialog(name, file);
                        } catch (Exception e) {
                            this.rsNPC.getLogger().error(this.rsNPC.getLanguage().translateString("plugin.load.dialog.dataError", file.getName()), e);
                        }
                    }
                });
    }

    public void loadDialog(@NotNull String name, File file) {
        Config config = new Config(file, Config.YAML);
        ConfigUtils.addDescription(config, this.description);
        if (this.dialogConfigs.containsKey(name)) {
            // 对话框 key 为扁平文件名（不含子目录），跨子目录同名会覆盖，提示用户定位
            this.rsNPC.getLogger().warning("对话框页面: " + name + " 配置重复（跨子目录同名），后加载的已覆盖前者！文件: " + file.getAbsolutePath());
        }
        this.dialogConfigs.put(name, new DialogPages(name, config));
    }

    public DialogPages getDialogConfig(@NotNull String name) {
        return this.dialogConfigs.get(name);
    }

    public HashMap<String, DialogPages> getDialogConfigs() {
        return new HashMap<>(dialogConfigs); //不允许直接修改源Map
    }

}
