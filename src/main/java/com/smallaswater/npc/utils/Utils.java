package com.smallaswater.npc.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Location;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Utils {

    private Utils() {

    }

    public static String readFile(@NotNull File file) {
        String content = "";
        try {
            content = cn.nukkit.utils.Utils.readFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static CompoundTag getTag(@NotNull Location location, @NotNull String name) {
        CompoundTag tag = Entity.getDefaultNBT(location);
        tag.putString("rsnpcName", name);
        return tag;
    }

    public static double getYaw(@NotNull Location location) {
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
