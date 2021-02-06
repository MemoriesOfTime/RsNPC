package com.smallaswater.npc.data;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.RsNpcX;
import com.smallaswater.npc.entitys.EntityRsNpc;
import com.smallaswater.npc.utils.RsNpcLoadException;
import com.smallaswater.npc.variable.VariableManage;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author lt_name
 */
public class RsNpcConfig {

    private final Config config;
    private final String name;
    private final String showName;

    private final String levelName;
    private final Location location;

    private final Item hand;
    private final Item[] armor = new Item[4];

    private final Skin skin;

    private final boolean lookAtThePlayer;

    private final boolean enableEmote;
    private final ArrayList<String> emoteIDs = new ArrayList<>();
    private final int showEmoteInterval;

    @Getter
    private final boolean canProjectilesTrigger;

    private final ArrayList<String> cmds = new ArrayList<>();
    private final ArrayList<String> messages = new ArrayList<>();

    private EntityRsNpc entityRsNpc;

    public RsNpcConfig(@NonNull String name, @NonNull Config config) throws RsNpcLoadException {
        this.config = config;
        this.name = name;
        this.showName = config.getString("name");

        HashMap<String, Object> map = config.get("坐标", new HashMap<>());
        this.levelName = (String) map.get("level");
        if (!Server.getInstance().loadLevel(this.levelName)) {
            throw new RsNpcLoadException("世界：" + this.levelName + " 不存在！无法加载当前世界的NPC");
        }
        Level level = Server.getInstance().getLevelByName(this.levelName);
        this.location = new Location((double) map.get("x"), (double) map.get("y"), (double) map.get("z"),
                (double) map.getOrDefault("yaw", 0D), 0, level);

        this.hand = Item.fromString("".equals(config.getString("手持", "")) ? "0:0" : config.getString("手持", ""));
        config.set("手持", this.hand.getId() + ":" + this.hand.getDamage());
        this.armor[0] = Item.fromString("".equals(config.getString("头部")) ? "0:0" : config.getString("头部"));
        config.set("头部", this.armor[0].getId() + ":" + this.armor[0].getDamage());
        this.armor[1] = Item.fromString("".equals(config.getString("胸部")) ? "0:0" : config.getString("胸部"));
        config.set("胸部", this.armor[1].getId() + ":" + this.armor[1].getDamage());
        this.armor[2] = Item.fromString("".equals(config.getString("腿部")) ? "0:0" : config.getString("腿部"));
        config.set("腿部", this.armor[2].getId() + ":" + this.armor[2].getDamage());
        this.armor[3] = Item.fromString("".equals(config.getString("脚部")) ? "0:0" : config.getString("脚部"));
        config.set("脚部", this.armor[3].getId() + ":" + this.armor[3].getDamage());

        String skinName = config.getString("皮肤", "尸鬼");
        config.set("皮肤", skinName);
        this.skin = RsNpcX.getInstance().getSkinByName(skinName);

        this.lookAtThePlayer = config.getBoolean("看向玩家", true);
        config.set("看向玩家", this.lookAtThePlayer);

        this.enableEmote = config.getBoolean("表情动作.启用");
        config.set("表情动作.启用", this.enableEmote);
        this.emoteIDs.addAll(config.getStringList("表情动作.表情ID"));
        config.set("表情动作.表情ID", this.emoteIDs);
        this.showEmoteInterval = config.getInt("表情动作.间隔(秒)", 10);
        config.set("表情动作.间隔(秒)", this.showEmoteInterval);

        this.canProjectilesTrigger = config.getBoolean("允许抛射物触发", true);
        config.set("允许抛射物触发", this.canProjectilesTrigger);

        this.cmds.addAll(config.getStringList("点击执行指令"));
        config.set("点击执行指令", this.cmds);
        this.messages.addAll(config.getStringList("发送消息"));
        config.set("发送消息", this.messages);

        config.save();
    }

    public void checkEntity() {
        if (this.location.getLevel() == null && !Server.getInstance().loadLevel(this.levelName)) {
            RsNpcX.getInstance().getLogger().error("世界: " + this.levelName + " 不存在！NPC: " + this.name + "无法生成！");
        }
        if (this.location.getChunk() != null && this.location.getChunk().isLoaded()) {
            if (this.entityRsNpc == null || this.entityRsNpc.isClosed()) {
                this.entityRsNpc = new EntityRsNpc(location.getChunk(), Entity.getDefaultNBT(location)
                        .putString("rsnpcName", this.name)
                        .putCompound("Skin", (new CompoundTag())
                                .putByteArray("Data", (skin.getSkinData()).data)
                                .putString("ModelId", skin.getSkinId())), this);
                this.entityRsNpc.setSkin(this.skin);
                this.entityRsNpc.setScale(1F);
                this.entityRsNpc.spawnToAll();
            }
            this.entityRsNpc.setPosition(this.location);
            if (!this.lookAtThePlayer) {
                this.entityRsNpc.setRotation(this.location.yaw, this.location.pitch);
            }
            this.entityRsNpc.setNameTag(VariableManage.stringReplace(null, this.showName));
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

    public EntityRsNpc getEntityRsNpc() {
        return this.entityRsNpc;
    }

}
