package com.smallaswater.npc;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.google.gson.Gson;
import com.smallaswater.npc.command.RsNpcXCommand;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.entitys.EntityRsNpc;
import com.smallaswater.npc.form.FormListener;
import com.smallaswater.npc.tasks.CheckNpcEntityTask;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.variable.VariableManage;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RsNpcX extends PluginBase {

    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            5,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(Runtime.getRuntime().availableProcessors() * 4),
            new ThreadPoolExecutor.DiscardPolicy());
    public static final Random RANDOM = new Random();
    public static final Gson GSON = new Gson();

    private static RsNpcX rsNpcX;

    @Getter
    private final HashMap<String, Skin> skins = new HashMap<>();
    @Getter
    private final HashMap<String, RsNpcConfig> npcs = new HashMap<>();
    private final String[] defaultSkins = new String[]{"小丸子", "小埋", "小黑苦力怕", "尸鬼", "拉姆", "熊孩子", "狂三", "米奇", "考拉", "黑岩射手"};

    public static RsNpcX getInstance() {
        return rsNpcX;
    }

    @Override
    public void onLoad() {
        rsNpcX = this;
        VariableManage.addVariable("%npcName%", (player, rsNpcConfig) -> rsNpcConfig.getName());
        VariableManage.addVariable("@p", (player, rsNpcConfig) -> player.getName());
    
        File file = new File(getDataFolder() + "/Npcs");
        if (!file.exists() && !file.mkdirs()) {
            this.getLogger().error("Npcs文件夹创建失败");
        }
    }

    @Override
    public void onEnable() {
        this.getLogger().info("RsNpcX开始加载");
        Entity.registerEntity("EntityRsNpc", EntityRsNpc.class);
        
        this.getLogger().info("开始加载皮肤");
        this.saveDefaultSkin();
        this.loadSkins();
        
        this.getLogger().info("开始加载NPC");
        this.loadNpcs();

        this.getServer().getPluginManager().registerEvents(new FormListener(), this);
        this.getServer().getPluginManager().registerEvents(new OnListener(this), this);
        
        this.getServer().getScheduler().scheduleRepeatingTask(this, new CheckNpcEntityTask(this), 60);

        this.getServer().getCommandMap().register("rsnpcx", new RsNpcXCommand("rsnpcx"));
        
        this.getLogger().info("RsNpcX加载完成");
    }

    @Override
    public void onDisable() {
        for (RsNpcConfig config : this.npcs.values()) {
            if (config.getEntityRsNpc() != null) {
                config.getEntityRsNpc().close();
            }
        }
        this.npcs.clear();
        this.getLogger().info("RsNpcX卸载完成");
    }

    private void loadNpcs() {
        File[] files = (new File(getDataFolder() + "/Npcs")).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!file.isFile() && file.getName().endsWith(".yml")) {
                    continue;
                }
                String npcName = file.getName().split("\\.")[0];
                RsNpcConfig rsNpcConfig;
                try {
                    rsNpcConfig = new RsNpcConfig(npcName, new Config(file, Config.YAML));
                } catch (Exception e) {
                    this.getLogger().error("加载NPC出现错误！", e);
                    continue;
                }
                this.npcs.put(npcName, rsNpcConfig);
                rsNpcConfig.checkEntity();
                this.getLogger().info("NPC: " + rsNpcConfig.getName() + " 加载完成！");
            }
        }
    }

    private void saveDefaultSkin() {
        File file = new File(this.getDataFolder() + "/Skins");
        if (!file.exists()) {
            this.getLogger().info("未检测到Skins文件夹，正在创建");
            if (!file.mkdirs()) {
                this.getLogger().info("Skins文件夹创建失败");
            } else {
                this.getLogger().info("Skins 文件夹创建完成，正在保存预设皮肤");
                for (String s : this.defaultSkins) {
                    File f = new File(this.getDataFolder() + "/Skins/" + s);
                    if (!f.exists() && !f.mkdirs()) {
                        this.getLogger().info("载入 " + s + "失败");
                    } else {
                        this.saveResource("Skins/" + s + "/skin.json");
                        this.saveResource("Skins/" + s + "/skin.png");
                        this.getLogger().info("成功保存 " + s + " 皮肤");
                    }
                }
            }
        }
    }

    private void loadSkins() {
        File[] files = new File(this.getDataFolder() + "/Skins").listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String skinName = file.getName();
                File skinDataFile = new File(this.getDataFolder() + "/Skins/" + skinName + "/skin.png");
                if (skinDataFile.exists()) {
                    Skin skin = new Skin();
                    BufferedImage skindata;
                    try {
                        skindata = ImageIO.read(skinDataFile);
                    } catch (IOException e) {
                        this.getLogger().error("皮肤 " + skinName + " 读取错误", e);
                        continue;
                    }

                    if (skindata != null) {
                        skin.setSkinData(skindata);
                        skin.setSkinId(skinName);
                    }

                    //如果是4D皮肤
                    File skinJsonFile = new File(this.getDataFolder() + "/Skins/" + skinName + "/skin.json");
                    if (skinJsonFile.exists()) {
                        Map<String, Object> skinJson = (new Config(this.getDataFolder() + "/Skins/" + skinName + "/skin.json", Config.JSON)).getAll();
                        String geometryName = null;

                        if (skinJson.containsKey("format_version")) {
                            skin.generateSkinId("RsNpcX" + skinName);
                            for (Map.Entry<String, Object> entry : skinJson.entrySet()) {
                                if (geometryName == null) {
                                    if (entry.getKey().startsWith("geometry")) {
                                        geometryName = entry.getKey();
                                    }
                                }else {
                                    break;
                                }
                            }
                            skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
                            skin.setGeometryData(Utils.readFile(skinJsonFile));
                        } else {
                            for (Map.Entry<String, Object> entry : skinJson.entrySet()) {
                                if (geometryName == null) {
                                    geometryName = entry.getKey();
                                }else {
                                    break;
                                }
                            }
                            skin.setGeometryName(geometryName);
                            skin.setGeometryData(Utils.readFile(skinJsonFile));
                        }
                    }

                    skin.setTrusted(true);

                    if (skin.isValid()) {
                        this.skins.put(skinName, skin);
                        this.getLogger().info("皮肤 " + skinName + " 读取完成");
                    }else {
                        this.getLogger().error("皮肤 " + skinName + " 验证失败，请检查皮肤文件完整性！");
                    }
                } else {
                    this.getLogger().error("皮肤 " + skinName + " 错误的名称格式，请将皮肤文件命名为 skin.png");
                }
            }
        }
    }

    public void reload() {
        this.npcs.clear();
        for (Level level : Server.getInstance().getLevels().values()) {
            for (Entity entity : level.getEntities()) {
                if (entity instanceof EntityRsNpc) {
                    entity.close();
                }
            }
        }
        this.saveDefaultSkin();
        this.loadSkins();
        this.loadNpcs();
    }

    public Skin getSkinByName(String name) {
        return this.getSkins().get(name);
    }

}