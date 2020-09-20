package com.smallaswater.npc.utils;

import cn.nukkit.level.Location;
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
