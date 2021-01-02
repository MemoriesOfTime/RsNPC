package com.smallaswater.npc.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Location;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.io.File;
import java.io.IOException;

public class Util {

    private Util() {

    }

    public static String readFile(File file) {
        String content = "";
        try {
            content = Utils.readFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static CompoundTag getTag(Location location, String name) {
        CompoundTag tag = Entity.getDefaultNBT(location);
        tag.putString("rsnpcName", name);
        return tag;
    }

    public static double getYaw(Location location) {
        if (location.getYaw() > 315 || location.getYaw() <= 45) {
            return 0D;
        }else if (location.getYaw() > 45 && location.getYaw() <= 135) {
            return 90D;
        }else if (location.getYaw() > 135 && location.getYaw() <= 225) {
            return 180D;
        }else {
            return 270D;
        }
    }

}
