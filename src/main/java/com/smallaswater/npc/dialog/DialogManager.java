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

        File[] files = new File(this.rsNPC.getDataFolder() + "/Dialog").listFiles();
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
                        this.rsNPC.getLogger().error(this.rsNPC.getLanguage().translateString("plugin.load.dialog.dataError", file.getName()), e);
                    }
                });
        this.rsNPC.getLogger().info(this.rsNPC.getLanguage().translateString("plugin.load.dialog.loadComplete", this.dialogConfigs.size()));
    }

    public void loadDialog(@NotNull String name) {
        Config config = new Config(this.rsNPC.getDataFolder() + "/Dialog/" + name + ".yml", Config.YAML);
        ConfigUtils.addDescription(config, this.description); //添加描述
        this.dialogConfigs.put(name, new DialogPages(name, config));
    }

    public DialogPages getDialogConfig(@NotNull String name) {
        return this.dialogConfigs.get(name);
    }

    public HashMap<String, DialogPages> getDialogConfigs() {
        return new HashMap<>(dialogConfigs); //不允许直接修改源Map
    }

}
