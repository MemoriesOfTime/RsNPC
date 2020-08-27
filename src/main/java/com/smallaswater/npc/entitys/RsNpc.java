package com.smallaswater.npc.entitys;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.NpcMainClass;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

public class RsNpc extends EntityHuman {

    //private Item itemHand = Item.get(0, 0);
    //private Item[] armor = new Item[0];
    private boolean lookAtThePlayer;

    public RsNpc(FullChunk chunk, CompoundTag nbt, String name) {
        super(chunk, nbt);
        setNameTagAlwaysVisible();
        setNameTagVisible();
        setNameTag(name);
        //setDataFlag(37, -1);
        setHealth(20.0F);
        setMaxHealth(20);
        //判断是否看向玩家
        String rsnpcName = this.namedTag.getString("rsnpcName");
        Config config = NpcMainClass.getInstance().npcs.get(rsnpcName);
        this.lookAtThePlayer = config.getBoolean("看向玩家", true);

    }


    public void setItemHand(Item itemHand) {
        this.getInventory().setItemInHand(itemHand);
        //this.itemHand = itemHand;
    }

    public void setArmor(Item[] armor) {
        this.getInventory().setArmorContents(armor);
       // this.armor = armor;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.lookAtThePlayer && !getLevel().getPlayers().isEmpty() && currentTick%2 == 0) {
            CompletableFuture.runAsync(() -> {
                LinkedList<String> npd = new LinkedList<>();
                for (Player player : getLevel().getPlayers().values()) {
                    double distance = player.x - this.x + player.y - this.y + player.z - this.z;
                    npd.add(player.getName() + "@" + distance);
                }
                npd.sort((mapping1, mapping2) -> {
                    String[] nameMapNum1 = mapping1.split("@");
                    String[] nameMapNum2 = mapping2.split("@");
                    double compare = Double.parseDouble(nameMapNum1[1]) - Double.parseDouble(nameMapNum2[1]);
                    return Double.compare(compare, 0.0D);
                });
                String name = npd.get(0).split("@")[0];
                Player player = getServer().getPlayer(name);
                if (player == null) {
                    return;
                }
                if (player.getLevel().getName().equals(getLevel().getName()) && getServer().getOnlinePlayers().containsValue(player)) {
                    double npcx = this.x - player.x;
                    double npcy = this.y - player.y;
                    double npcz = this.z - player.z;
                    double yaw = Math.asin(npcx / Math.sqrt(npcx * npcx + npcz * npcz)) / 3.14D * 180.0D;
                    double pitch = Math.round(Math.asin(npcy / Math.sqrt(npcx * npcx + npcz * npcz + npcy * npcy)) / 3.14D * 180.0D);
                    if (npcz > 0.0D) {
                        yaw = -yaw + 180.0D;
                    }
                    this.yaw = yaw;
                    this.pitch = pitch;

                    /*MobEquipmentPacket pk = new MobEquipmentPacket();
                    pk.eid = getId();
                    pk.item = this.itemHand;
                    pk.inventorySlot = 0;
                    player.dataPacket(pk);
                    MobArmorEquipmentPacket pks = new MobArmorEquipmentPacket();
                    pks.eid = getId();
                    pks.slots = this.armor;
                    player.dataPacket(pks);*/
                }
            });
        }
        return super.onUpdate(currentTick);
    }

}