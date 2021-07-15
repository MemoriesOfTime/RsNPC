package com.smallaswater.npc;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.entitys.EntityRsNpc;
import com.smallaswater.npc.tasks.CheckNpcEntityTask;
import com.smallaswater.npc.utils.RsNpcLoadException;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.variable.DefaultVariable;
import com.smallaswater.npc.variable.VariableManage;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
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
        VariableManage.addVariable("default", DefaultVariable.class);
    
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
        
        this.getServer().getPluginManager().registerEvents(new OnListener(this), this);
        
        this.getServer().getScheduler().scheduleRepeatingTask(this, new CheckNpcEntityTask(this), 60);
        
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
    }

    private void loadNpcs() {
        File[] files = (new File(getDataFolder() + "/Npcs")).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                String npcName = file.getName().split("\\.")[0];
                RsNpcConfig rsNpcConfig;
                try {
                    rsNpcConfig = new RsNpcConfig(npcName, new Config(file, Config.YAML));
                } catch (RsNpcLoadException e) {
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
        File[] files = (new File(this.getDataFolder() + "/Skins")).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String skinName = file.getName();
                File skinDataFile = new File(getDataFolder() + "/Skins/" + skinName + "/skin.png");
                if (skinDataFile.exists()) {
                    Skin skin = new Skin();
                    skin.setTrusted(true);
                    BufferedImage skindata;
                    try {
                        skindata = ImageIO.read(skinDataFile);
                    } catch (IOException var19) {
                        this.getLogger().error("皮肤 " + skinName + " 读取错误");
                        break;
                    }
                    if (skindata != null) {
                        skin.setSkinData(skindata);
                        skin.setSkinId(skinName);
                    }
                    File skinJsonFile = new File(getDataFolder() + "/Skins/" + skinName + "/skin.json");
                    if (skinJsonFile.exists()) {
                        Map<String, Object> skinJson = (new Config(skinJsonFile, Config.JSON)).getAll();
                        String geometryName = null;
                        for (Map.Entry<String, Object> entry1 : skinJson.entrySet()) {
                            if (geometryName != null) {
                                break;
                            }
                            geometryName = entry1.getKey();
                        }
                        skin.setGeometryName(geometryName);
                        skin.setGeometryData(Utils.readFile(skinJsonFile));
                    }
                    if (skin.isValid()) {
                        this.skins.put(skinName, skin);
                        this.getLogger().info("皮肤 " + skinName + " 读取完成");
                    }else {
                        this.getLogger().error("皮肤 " + skinName + "验证失败");
                    }
                } else {
                    this.getLogger().error("错误的皮肤名称格式 请将皮肤文件命名为 skin.png");
                }
            }
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("rsnpcx")) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "create":
                        if (!(sender instanceof Player)) {
                            sender.sendMessage("§c请在游戏内使用此命令！");
                            return true;
                        }
                        if (args.length > 1) {
                            String name = args[1];
                            if (this.npcs.containsKey(name)) {
                                sender.sendMessage("§c§lNPC " + name + "已经存在...");
                                return true;
                            }
                            this.saveResource("npc.yml", "/Npcs/" + name + ".yml", false);
                            Config config = new Config(this.getDataFolder() + "/Npcs/" + name + ".yml", Config.YAML);
                            config.set("name", name);
                            Player player = (Player) sender;
                            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                            map.put("x", player.getX());
                            map.put("y", player.getY());
                            map.put("z", player.getZ());
                            map.put("yaw", Utils.getYaw(player));
                            map.put("level", player.getLevel().getName());
                            config.set("坐标", map);
                            config.save();
                            RsNpcConfig rsNpcConfig;
                            try {
                                rsNpcConfig = new RsNpcConfig(name, config);
                            } catch (RsNpcLoadException e) {
                                sender.sendMessage("创建NPC失败！");
                                this.getLogger().error("创建NPC失败！", e);
                                return true;
                            }
                            this.npcs.put(name, rsNpcConfig);
                            rsNpcConfig.checkEntity();
                            sender.sendMessage("§a§lNPC " + name + "创建成功!!");
                        } else {
                            sender.sendMessage("§c§l请输入要创建的NPC的名字！");
                        }
                        return true;
                    case "delete":
                        if (args.length > 1) {
                            String name = args[1];
                            if (!this.npcs.containsKey(name)) {
                                sender.sendMessage("§c§lNPC " + name + "不存在...");
                                return true;
                            }
                            this.npcs.get(name).getEntityRsNpc().close();
                            this.npcs.remove(name);
                            if (!(new File(getDataFolder() + "/Npcs/" + name + ".yml")).delete()) {
                                sender.sendMessage("§c§lNPC " + name + "文件删除失败");
                            }
                            sender.sendMessage("§a§lNPC " + name + "移除成功 ");
                        } else {
                            sender.sendMessage("§c§l请输入要删除的NPC的名字！");
                        }
                        return true;
                    case "addroute":
                        Player player = (Player) sender;
                        if (args.length > 1) {
                            String name = args[1];
                            if (!this.npcs.containsKey(name)) {
                                sender.sendMessage("§c§lNPC " + name + " 不存在！");
                                return true;
                            }
                            RsNpcConfig rsNpcConfig = this.npcs.get(name);
                            rsNpcConfig.getRoute().add(player.clone());
                            List<String> list = rsNpcConfig.getConfig().getStringList("route");
                            list.add(player.getX() + ":" + player.getY() + ":" + player.getZ());
                            rsNpcConfig.getConfig().set("route", list);
                            rsNpcConfig.getConfig().save();
                            sender.sendMessage("§a§l已添加到路径");
                        }else {
                            sender.sendMessage("§c§l请输入要设置的NPC的名字！");
                        }
                        return true;
                    case "reload":
                        for (Level level : Server.getInstance().getLevels().values()) {
                            for (Entity entity : level.getEntities()) {
                                if (entity instanceof EntityRsNpc) {
                                    entity.close();
                                }
                            }
                        }
                        this.npcs.clear();
                        this.saveDefaultSkin();
                        this.loadSkins();
                        this.loadNpcs();
                        sender.sendMessage("§a§l成功重载配置.. ");
                        return true;
                    default:
                        this.sendCommandHelp(sender);
                        return true;
                }
            } else {
                this.sendCommandHelp(sender);
            }
            return true;
        }
        return false;
    }

    public void sendCommandHelp(CommandSender sender) {
        sender.sendMessage("§a§l >> §eHelp for RsNPCX §a<<");
        sender.sendMessage("§a§l/rsnpcx create <NPC名称> §7在当前位置创建NPC");
        sender.sendMessage("§a§l/rsnpcx delete <NPC名称> §7移除NPC");
        sender.sendMessage("§a§l/rsnpcx addroute <NPC名称> §7将当前位置添加到NPC路径");
        sender.sendMessage("§a§l/rsnpcx reload §7重载NPC");
        sender.sendMessage("§a§l >> §eHelp for RsNPC §a<<");
    }

    public Skin getSkinByName(String name) {
        return this.getSkins().getOrDefault(name, this.getSkins().get("尸鬼"));
    }

}