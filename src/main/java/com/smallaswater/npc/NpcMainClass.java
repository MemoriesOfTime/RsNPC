package com.smallaswater.npc;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.smallaswater.village.api.VillageGuiAPI;
import cn.smallaswater.village.api.items.VillageBuyItem;
import cn.smallaswater.village.api.menu.VillageUi;
import com.smallaswater.npc.entitys.RsNpc;
import com.smallaswater.npc.tasks.FixNpcTask;
import com.smallaswater.npc.utils.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class NpcMainClass extends PluginBase implements Listener {

    private static NpcMainClass npcMainClass;
    public HashMap<String, Skin> loadSkins = new HashMap<>();
    public final HashMap<String, Config> npcs = new HashMap<>();
    public final HashMap<String, RsNpc> spawns = new HashMap<>();
    private final String[] skins = new String[]{"小丸子", "小埋", "小黑苦力怕", "尸鬼", "拉姆", "熊孩子", "狂三", "米奇", "考拉", "黑岩射手"};

    public static NpcMainClass getInstance() {
        return npcMainClass;
    }

    @Override
    public void onLoad() {
        npcMainClass = this;
    }

    @Override
    public void onEnable() {
        getLogger().info("NPC开始启动");
        Entity.registerEntity("RsNpc", RsNpc.class);
        getLogger().info("开始加载皮肤文件");
        if (!(new File(getDataFolder() + "/Npcs")).exists()) {
            getLogger().info("未检测到Npcs文件夹，正在创建");
            if (!(new File(getDataFolder() + "/Npcs")).mkdirs()) {
                getLogger().info("Npcs文件夹创建失败");
            }
        }
        loadDefaultSkin();
        getLogger().info("开始读取皮肤");
        loadSkin();
        loadNpc();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleRepeatingTask(this, new FixNpcTask(this), 60);
    }

    @Override
    public void onDisable() {
        Iterator<Map.Entry<String, RsNpc>> iterator = this.spawns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, RsNpc> entry = iterator.next();
            entry.getValue().close();
            this.npcs.remove(entry.getKey());
            iterator.remove();
        }
        this.npcs.clear();
        this.spawns.clear();
    }

    private void initSkin() {
        for (String s : this.skins) {
            if (!(new File(getDataFolder() + "/Skins/" + s)).exists() &&
                    !(new File(getDataFolder() + "/Skins/" + s)).mkdirs()) {
                getLogger().info("载入 " + s + "失败");
            } else {
                saveResource("skin/" + s + "/skin.json", "/Skins/" + s + "/skin.json", false);
                saveResource("skin/" + s + "/skin.png", "/Skins/" + s + "/skin.png", false);
                getLogger().info("成功载入 " + s + "皮肤");
            }
        }
    }


    private void loadNpc() {
        File[] files = (new File(getDataFolder() + "/Npcs")).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String npcName = file.getName().split("\\.")[0];
                Config config = new Config(file, 2);
                this.npcs.put(npcName, config);
            }
        }
    }


    private void loadSkin() {
        if (!(new File(getDataFolder() + "/Skins")).exists()) {
            initSkin();
        }
        File[] files = (new File(getDataFolder() + "/Skins")).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String skinName = file.getName();
                if ((new File(getDataFolder() + "/Skins/" + skinName + "/skin.png")).exists()) {
                    Skin skin = new Skin();
                    skin.setTrusted(true);
                    BufferedImage skindata = null;
                    try {
                        skindata = ImageIO.read(new File(getDataFolder() + "/Skins/" + skinName + "/skin.png"));
                    } catch (IOException var19) {
                        System.out.println("不存在皮肤");
                    }
                    if (skindata != null) {
                        skin.setSkinData(skindata);
                        skin.setSkinId(skinName);
                    }
                    if ((new File(getDataFolder() + "/Skins/" + skinName + "/skin.json")).exists()) {
                        Map<String, Object> skinJson = (new Config(getDataFolder() + "/Skins/" + skinName + "/skin.json", 1)).getAll();
                        String geometryName = null;
                        for (Map.Entry<String, Object> entry1 : skinJson.entrySet()) {
                            if (geometryName == null) {
                                geometryName = entry1.getKey();
                            }else {
                                break;
                            }
                        }
                        skin.setGeometryName(geometryName);
                        skin.setGeometryData(Util.readFile(new File(getDataFolder() + "/Skins/" + skinName + "/skin.json")));
                    }
                    getLogger().info(skinName + "皮肤读取完成");
                    this.loadSkins.put(skinName, skin);
                } else {
                    getLogger().info("错误的皮肤名称格式 请将皮肤文件命名为 skin.png");
                }
            }
        }
    }


    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("rnpc") && sender instanceof Player && args.length > 0) {
            switch (args[0]) {
                case "help":
                    sender.sendMessage("§a§l >> §eHelp for RsNPC §a<<");
                    sender.sendMessage("§a§l/rnpc create <名称> §7创建NPC");
                    sender.sendMessage("§a§l/rnpc delete <名称> §7移除NPC");
                    sender.sendMessage("§a§l/rnpc reload §7重载NPC");
                    sender.sendMessage("§a§l >> §eHelp for RsNPC §a<<");
                    return true;
                case "create":
                    if (args.length > 1) {
                        String name = args[1];
                        if (this.npcs.containsKey(name)) {
                            sender.sendMessage("§c§lNPC " + name + "已经存在...");
                            return true;
                        }
                        saveResource("npc.yml", "/Npcs/" + name + ".yml", false);
                        Config config = new Config(getDataFolder() + "/Npcs/" + name + ".yml", Config.YAML);
                        config.set("name", name);
                        Player player = (Player) sender;
                        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                        map.put("x", player.getX());
                        map.put("y", player.getY());
                        map.put("z", player.getZ());
                        map.put("level", player.getLevel().getName());
                        config.set("坐标", map);
                        config.save();
                        this.npcs.put(name, config);
                        sender.sendMessage("§a§lNPC " + name + "创建成功!!");
                    }
                    return true;
                case "delete":
                    if (args.length > 1) {
                        String name = args[1];
                        if (!this.npcs.containsKey(name)) {
                            sender.sendMessage("§c§lNPC " + name + "不存在...");
                            return true;
                        }
                        this.npcs.remove(name);
                        if (!(new File(getDataFolder() + "/Npcs/" + name + ".yml")).delete()) {
                            sender.sendMessage("§c§lNPC " + name + "文件删除失败");
                        }
                        if (this.spawns.containsKey(name)) {
                            this.spawns.get(name).close();
                            this.spawns.remove(name);
                        }
                        sender.sendMessage("§a§lNPC " + name + "移除成功 ");
                    }
                    return true;
                case "reload":
                    getLogger().info("未检测到Npcs文件夹，正在创建");
                    if (!(new File(getDataFolder() + "/Npcs")).exists() && !(new File(getDataFolder() + "/Npcs")).mkdirs()) {
                        getLogger().info("Npcs文件夹创建失败");
                    }
                    loadDefaultSkin();
                    loadSkin();
                    loadNpc();
                    for (Level level : Server.getInstance().getLevels().values()) {
                        for (Entity entity : level.getEntities()) {
                            if (entity instanceof RsNpc) {
                                entity.close();
                            }
                        }
                    }
                    sender.sendMessage("§a§l成功重载配置.. ");
                    return true;
            }
        }
        return false;
    }

    private void loadDefaultSkin() {
        if (!(new File(getDataFolder() + "/Skins")).exists()) {
            getLogger().info("未检测到Skins文件夹，正在创建");
            if (!(new File(getDataFolder() + "/Skins")).mkdirs()) {
                getLogger().info("Skins文件夹创建失败");
            } else {
                getLogger().info("Skins 文件夹创建完成，正在载入预设皮肤");
                initSkin();
            }
        }
    }

    public void spawnNPC(String name) {
        Config config = this.npcs.get(name);
        String names = config.getString("name");
        Item hand = Item.fromString("".equals(config.getString("手持", "")) ? "0:0" : config.getString("手持", ""));
        String skinName = config.getString("皮肤", "尸鬼");
        Location location = getLocationByMap((Map) config.get("坐标"));
        if (location != null) {
            Skin skin;
            for (Entity entity : location.level.getEntities()) {
                if (entity instanceof RsNpc &&
                        entity.namedTag.getString("rsnpcName").equals(name)) {
                    entity.close();
                    return;
                }
            }
            if (this.loadSkins.containsKey(skinName)) {
                skin = this.loadSkins.get(skinName);
            } else {
                skin = this.loadSkins.get("尸鬼");
            }
            RsNpc npc = new RsNpc(location.getChunk(), getTag(location, name).putString("rsnpcName", name).putCompound("Skin", (new CompoundTag())
                    .putByteArray("Data", (skin.getSkinData()).data)
                    .putString("ModelId", skin.getSkinId())), names);
            npc.setItemHand(hand);
            npc.setScale(1.0F);
            npc.setArmor(getArmor(config));
            skin.setTrusted(true);
            npc.setSkin(skin);
            npc.spawnToAll();
            this.spawns.put(name, npc);
        }
    }


    private Item[] getArmor(Config config) {
        Item to = Item.fromString("".equals(config.getString("头部", "")) ? "0:0" : config.getString("头部", ""));
        Item x = Item.fromString("".equals(config.getString("胸部", "")) ? "0:0" : config.getString("头部", ""));
        Item t = Item.fromString("".equals(config.getString("腿部", "")) ? "0:0" : config.getString("头部", ""));
        Item j = Item.fromString("".equals(config.getString("脚部", "")) ? "0:0" : config.getString("头部", ""));
        Item[] items = new Item[4];
        items[0] = to;
        items[1] = x;
        items[2] = t;
        items[3] = j;
        return items;
    }

    private CompoundTag getTag(Location player, String name) {
        CompoundTag tag = Entity.getDefaultNBT(new Vector3(player.x, player.y, player.z));
        tag.putString("rsnpcName", name);
        return tag;
    }


    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof RsNpc) {
            event.setCancelled();
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damage = ((EntityDamageByEntityEvent) event).getDamager();
                String name = entity.namedTag.getString("rsnpcName");
                Config config = this.npcs.get(name);
                if (damage instanceof Player) {
                    if (Server.getInstance().getPluginManager().getPlugin("VillageUiApi") != null) {
                        List<Map> items = config.getMapList("交易");
                        if (items.size() > 0) {
                            ArrayList<VillageUi> uis = new ArrayList<>();
                            for (Map map : items) {
                                Item b = null;
                                String[] s = map.get("need1").toString().split(":");
                                String[] s2 = map.get("give").toString().split(":");
                                if (!"".equals(map.get("need2").toString())) {
                                    String[] s1 = map.get("need2").toString().split(":");
                                    b = Item.get(Integer.parseInt(s1[0]), Integer.parseInt(s1[1]), Integer.parseInt(s1[2]));
                                }
                                Item a = Item.get(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
                                Item sell = Item.get(Integer.parseInt(s2[0]), Integer.parseInt(s2[1]), Integer.parseInt(s2[2]));
                                VillageUi ui = new VillageUi(new VillageBuyItem(a, b), sell);
                                ui.buyCountA = 1;
                                ui.buyCountB = 1;
                                uis.add(ui);
                            }
                            VillageGuiAPI.createVillageUi(entity.getNameTag(), false, uis.toArray(new VillageUi[0])).sendPlayer((Player) damage);
                        }
                    }
                    List<String> cmds = config.getStringList("点击执行指令");
                    for (String cmd : cmds) {
                        String[] c = cmd.split("&");
                        String cm = c[0];
                        if (c.length > 1) {
                            if ("con".equals(c[1])) {
                                Server.getInstance().dispatchCommand(new ConsoleCommandSender(), cm.replace("@p", damage.getName()));
                                continue;
                            }
                            Server.getInstance().dispatchCommand((CommandSender) damage, cm.replace("@p", damage.getName()));
                            continue;
                        }
                        Server.getInstance().dispatchCommand((CommandSender) damage, cm.replace("@p", damage.getName()));
                    }
                }
            }
        }
    }


    private Location getLocationByMap(Map map) {
        double x = (Double) map.get("x");
        double y = (Double) map.get("y");
        double z = (Double) map.get("z");
        String level = (String) map.get("level");
        Level level1 = Server.getInstance().getLevelByName(level);
        if (level1 != null) {
            return new Location(x, y, z, level1);
        }
        return null;
    }

}