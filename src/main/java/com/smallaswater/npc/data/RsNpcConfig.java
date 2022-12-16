package com.smallaswater.npc.data;

import cn.lanink.gamecore.utils.ConfigUtils;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.entitys.EntityRsNPC;
import com.smallaswater.npc.entitys.EntityRsNPCCustomEntity;
import com.smallaswater.npc.utils.CustomEntityUtils;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.utils.exception.RsNpcConfigLoadException;
import com.smallaswater.npc.utils.exception.RsNpcLoadException;
import com.smallaswater.npc.variable.VariableManage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lt_name
 */
public class RsNpcConfig {

    public static final String NPC_CONFIG_VERSION_KEY = "ConfigVersion";

    private final Config config;
    private final String name;
    @Setter
    private String showName;

    private final String levelName;
    private final Location location;

    @Setter
    private Item hand;
    @Setter
    private Item[] armor = new Item[4];

    @Setter
    @Getter
    private String skinName;
    @Setter
    private Skin skin;

    @Getter
    private int networkId;

    @Setter
    @Getter
    private float scale;

    @Setter
    private boolean lookAtThePlayer;

    @Setter
    private boolean enableEmote;
    private final ArrayList<String> emoteIDs = new ArrayList<>();
    @Setter
    private int showEmoteInterval;

    @Setter
    @Getter
    private boolean canProjectilesTrigger;

    private final ArrayList<String> cmds = new ArrayList<>();
    private final ArrayList<String> messages = new ArrayList<>();

    @Getter
    private final double baseMoveSpeed;

    @Getter
    private final ArrayList<Vector3> route = new ArrayList<>();
    @Setter
    @Getter
    private boolean enablePathfinding;

    @Getter
    private final double whirling;

    @Setter
    @Getter
    private boolean enabledDialogPages;
    @Setter
    @Getter
    private String dialogPagesName;

    private EntityRsNPC entityRsNpc;

    private boolean enableCustomEntity;
    private String customEntityIdentifier;
    private int customEntitySkinId;

    public RsNpcConfig(@NonNull String name, @NonNull Config config) throws RsNpcConfigLoadException, RsNpcLoadException {
        this.config = config;
        this.name = name;

        try {
            this.showName = config.getString("name");
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 显示名称配置错误！请检查配置文件！", e);
        }

        try {
            HashMap<String, Object> map = config.get("坐标", new HashMap<>());
            this.levelName = (String) map.get("level");
            if (!Server.getInstance().loadLevel(this.levelName)) {
                throw new RsNpcLoadException("世界：" + this.levelName + " 不存在！无法加载当前世界的NPC");
            }
            Level level = Server.getInstance().getLevelByName(this.levelName);
            this.location = new Location((double) map.get("x"), (double) map.get("y"), (double) map.get("z"),
                    (double) map.getOrDefault("yaw", 0D), 0, level);
        } catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 位置/世界配置错误！请检查配置文件！", e);
        }

        try {
            this.hand = Item.fromString("".equals(config.getString("手持", "")) ? "0:0" : config.getString("手持", ""));
            this.armor[0] = Item.fromString("".equals(config.getString("头部")) ? "0:0" : config.getString("头部"));
            this.armor[1] = Item.fromString("".equals(config.getString("胸部")) ? "0:0" : config.getString("胸部"));
            this.armor[2] = Item.fromString("".equals(config.getString("腿部")) ? "0:0" : config.getString("腿部"));
            this.armor[3] = Item.fromString("".equals(config.getString("脚部")) ? "0:0" : config.getString("脚部"));
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 手持物品/护甲加载失败！请检查配置文件！", e);
        }

        try {
            this.skinName = config.getString("皮肤", "默认");
            if (!RsNPC.getInstance().getSkins().containsKey(this.skinName)) {
                RsNPC.getInstance().getLogger().warning("NPC: " + this.name + " 皮肤: " + this.skinName + " 不存在！已切换为默认皮肤！");
            }
            this.skin = RsNPC.getInstance().getSkinByName(this.skinName);
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 皮肤加载失败！请检查配置文件！", e);
        }

        try {
            this.setNetworkId(config.getInt("实体NetworkId", -1));
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 实体NetworkId加载失败！请检查配置文件！", e);
        }

        try {
            this.scale = (float) Utils.toDouble(config.get("实体大小", 1));
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 实体大小加载失败！请检查配置文件！", e);
        }

        try {
            this.lookAtThePlayer = config.getBoolean("看向玩家", true);
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 看向玩家选项加载失败！请检查配置文件！", e);
        }

        try {
            this.enableEmote = config.getBoolean("表情动作.启用");
            this.emoteIDs.addAll(config.getStringList("表情动作.表情ID"));
            this.showEmoteInterval = config.getInt("表情动作.间隔", 10);
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 表情动作加载失败！请检查配置文件！", e);
        }

        try {
            this.canProjectilesTrigger = config.getBoolean("允许抛射物触发", true);
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 允许抛射物触发选项加载失败！请检查配置文件！", e);
        }

        try {
            if (!(config.get("点击执行指令") instanceof List)) {
                throw new RuntimeException("点击执行指令 配置读取到的内容不是List类型！请检查您的配置格式是否正确！");
            }
            this.cmds.addAll(config.getStringList("点击执行指令"));
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 点击执行指令加载失败！请检查配置文件！", e);
        }

        try {
            if (!(config.get("发送消息") instanceof List)) {
                throw new RuntimeException("发送消息 配置读取到的内容不是List类型！请检查您的配置格式是否正确！");
            }
            this.messages.addAll(config.getStringList("发送消息"));
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 发送消息加载失败！请检查配置文件！", e);
        }

        try {
            this.baseMoveSpeed = Utils.toDouble(config.get("基础移动速度", 1.0D));
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 基础移动速度加载失败！请检查配置文件！", e);
        }

        try {
            for (String string : config.getStringList("route")) {
                String[] s = string.split(":");
                this.route.add(new Vector3(Double.parseDouble(s[0]),
                        Double.parseDouble(s[1]),
                        Double.parseDouble(s[2])));
            }
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 路径加载失败！请检查配置文件！", e);
        }

        try {
            this.enablePathfinding = config.getBoolean("启用辅助寻路", true);
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 启用辅助寻路选项加载失败！请检查配置文件！", e);
        }

        try {
            this.whirling = Utils.toDouble(config.get("旋转", 0.0));
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 旋转加载失败！请检查配置文件！", e);
        }

        try {
            this.enabledDialogPages = RsNPC.getInstance().getDialogManager() != null && config.getBoolean("对话框.启用");
            this.dialogPagesName = config.getString("对话框.页面", "demo");
            if (RsNPC.getInstance().getDialogManager().getDialogConfig(this.dialogPagesName) == null) {
                RsNPC.getInstance().getLogger().warning("NPC配置 对话框-页面 选项加载失败！不存在名为 " + this.dialogPagesName + " 的对话框页面！");
            }
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 对话框加载失败！请检查配置文件！", e);
        }

        try {
            this.enableCustomEntity = this.config.getBoolean("CustomEntity.enable", false);
            this.customEntityIdentifier = this.config.getString("CustomEntity.identifier", "RsNPC:Demo");
            this.customEntitySkinId = this.config.getInt("CustomEntity.skinId", 0);
            if (this.enableCustomEntity && CustomEntityUtils.getRuntimeId(this.customEntityIdentifier) == -1) {
                CustomEntityUtils.registerCustomEntity(this.customEntityIdentifier);
            }
        }catch (Exception e) {
            throw new RsNpcConfigLoadException("NPC配置 自定义实体配置加载失败！请检查配置文件！", e);
        }

        //更新配置文件
        this.save();
        ConfigUtils.addDescription(this.config, RsNPC.getInstance().getNpcConfigDescription());
    }
    
    public void save() {
        this.config.set("name", this.showName);
    
        HashMap<String, Object> map = this.config.get("坐标", new HashMap<>());
        map.put("level", this.levelName);
        map.put("x", this.location.getX());
        map.put("y", this.location.getY());
        map.put("z", this.location.getZ());
        map.put("yaw", this.location.getYaw());
        this.config.set("坐标", map);
        
        this.config.set("手持", Utils.item2String(this.hand));
        this.config.set("头部", Utils.item2String(this.armor[0]));
        this.config.set("胸部", Utils.item2String(this.armor[1]));
        this.config.set("腿部", Utils.item2String(this.armor[2]));
        this.config.set("脚部", Utils.item2String(this.armor[3]));

        this.config.set("皮肤", this.skinName);

        this.config.set("实体NetworkId", this.networkId);

        this.config.set("实体大小", this.scale);
    
        this.config.set("看向玩家", this.lookAtThePlayer);
    
        this.config.set("表情动作.启用", this.enableEmote);
        this.config.set("表情动作.表情ID", this.emoteIDs);
        this.config.set("表情动作.间隔", this.showEmoteInterval);
    
        this.config.set("允许抛射物触发", this.canProjectilesTrigger);
    
        this.config.set("点击执行指令", this.cmds);
        this.config.set("发送消息", this.messages);

        this.config.set("基础移动速度", this.baseMoveSpeed);
        
        ArrayList<String> list = new ArrayList<>();
        for (Vector3 vector3 : this.route) {
            list.add(vector3.getX() + ":" + vector3.getY() + ":" + vector3.getZ());
        }
        this.config.set("route", list);
        this.config.set("启用辅助寻路", this.enablePathfinding);

        this.config.set("旋转", this.whirling);

        this.config.set("对话框.启用", this.enabledDialogPages);
        this.config.set("对话框.页面", this.dialogPagesName);

        this.config.set("CustomEntity.enable", this.enableCustomEntity);
        this.config.set("CustomEntity.identifier", this.customEntityIdentifier);
        this.config.set("CustomEntity.skinId", this.customEntitySkinId);

        this.config.save();
    }

    public void checkEntity() {
        if (this.location.getLevel() == null && !Server.getInstance().loadLevel(this.levelName)) {
            RsNPC.getInstance().getLogger().error("世界: " + this.levelName + " 不存在！NPC: " + this.name + "无法生成！");
            return;
        }
        if (this.location.getChunk() != null &&
                this.location.getChunk().isLoaded() &&
                !this.location.getLevel().getPlayers().isEmpty()) {
            if (this.entityRsNpc == null || this.entityRsNpc.isClosed()) {
                if (this.enableCustomEntity && this.customEntityIdentifier != null) {
                    this.entityRsNpc = new EntityRsNPCCustomEntity(this.location.getChunk(), Entity.getDefaultNBT(location)
                            .putString("rsnpcName", this.name)
                            .putCompound("Skin", (new CompoundTag())
                                    .putByteArray("Data", this.skin.getSkinData().data)
                                    .putString("ModelId", this.skin.getSkinId())), this);
                    EntityRsNPCCustomEntity entityRsNPC = (EntityRsNPCCustomEntity) this.entityRsNpc;
                    entityRsNPC.setIdentifier(this.customEntityIdentifier);
                    entityRsNPC.setSkinId(this.customEntitySkinId);
                }else {
                    this.entityRsNpc = new EntityRsNPC(this.location.getChunk(), Entity.getDefaultNBT(location)
                            .putString("rsnpcName", this.name)
                            .putCompound("Skin", (new CompoundTag())
                                    .putByteArray("Data", this.skin.getSkinData().data)
                                    .putString("ModelId", this.skin.getSkinId())), this);
                    this.entityRsNpc.setSkin(this.getSkin());
                }
                this.entityRsNpc.setScale(this.scale);
                this.entityRsNpc.spawnToAll();
            }
            if (this.getRoute().isEmpty()) {
                this.entityRsNpc.setPosition(this.location);
            }
            if (!this.lookAtThePlayer) {
                this.entityRsNpc.setRotation(this.location.yaw, this.location.pitch);
            }
            this.entityRsNpc.setNameTag(VariableManage.stringReplace(null, this.showName, this));
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public String getName() {
        return this.name;
    }

    public String getShowName() {
        return this.showName;
    }

    public Location getLocation() {
        return this.location;
    }

    public Item getHand() {
        return this.hand;
    }

    public Item[] getArmor() {
        return this.armor;
    }

    public Skin getSkin() {
        return this.skin;
    }

    public boolean isLookAtThePlayer() {
        return this.lookAtThePlayer;
    }

    public boolean isEnableEmote() {
        return this.enableEmote;
    }

    public ArrayList<String> getEmoteIDs() {
        return this.emoteIDs;
    }

    public int getShowEmoteInterval() {
        return this.showEmoteInterval;
    }

    public ArrayList<String> getCmds() {
        return this.cmds;
    }

    public ArrayList<String> getMessages() {
        return this.messages;
    }

    public EntityRsNPC getEntityRsNpc() {
        return this.entityRsNpc;
    }

    public void setNetworkId(int networkId) {
        if (networkId <= 0) {
            networkId = -1;
        }
        this.networkId = networkId;
    }

}
