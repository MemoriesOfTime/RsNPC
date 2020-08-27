package com.smallaswater.npc.utils;

import cn.nukkit.utils.Utils;

import java.io.File;
import java.io.IOException;

public class Util {

  public static String readFile(File file) {
    String content = "";
    try {
      content = Utils.readFile(file);
    } catch (IOException e) {
         e.printStackTrace();
    }
      return content;
  }

}
