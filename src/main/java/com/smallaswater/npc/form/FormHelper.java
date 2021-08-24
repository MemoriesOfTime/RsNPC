package com.smallaswater.npc.form;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.item.Item;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import com.smallaswater.npc.RsNpcX;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.form.element.ResponseElementButton;
import com.smallaswater.npc.form.windows.AdvancedFormWindowCustom;
import com.smallaswater.npc.form.windows.AdvancedFormWindowModal;
import com.smallaswater.npc.form.windows.AdvancedFormWindowSimple;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author LT_Name
 */
public class FormHelper {

    private FormHelper() {
        throw new RuntimeException("你想干什么？");
    }

    public static void sendMain(@NotNull Player player) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(">>RsNpcX - 主菜单<<");
        simple.setContent(getRandomMessage() + "\n\n");

        simple.addButton(new ResponseElementButton("创建NPC").onClicked(FormHelper::sendCreateNpc));
        simple.addButton(new ResponseElementButton("管理NPC").onClicked(FormHelper::sendAdminNpcAll));
        simple.addButton(new ResponseElementButton("重载配置")
                .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "rsnpcx reload"))
        );

        player.showFormWindow(simple);
    }

    public static void sendCreateNpc(@NotNull Player player) {
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(">>RsNpcX - 创建NPC<<");

        custom.addElement(new ElementInput("Npc名称"));

        custom.onResponded((formResponseCustom, cp) -> {
            String name = formResponseCustom.getInputResponse(0);
            Server.getInstance().dispatchCommand(cp, "rsnpcx create " + name);
        });
        custom.onClosed(FormHelper::sendMain);

        player.showFormWindow(custom);
    }

    public static void sendAdminNpcAll(@NotNull Player player) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(">>RsNpcX - 管理NPC<<");
        simple.setContent("请选择要设置的Npc");

        for (Map.Entry<String, RsNpcConfig> entry : RsNpcX.getInstance().getNpcs().entrySet()) {
            simple.addButton(new ResponseElementButton(entry.getKey())
                    .onClicked(cp -> sendAdminNpc(cp, entry.getValue())));
        }
        simple.onClosed(FormHelper::sendMain);

        player.showFormWindow(simple);
    }

    public static void sendAdminNpc(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(">>RsNpcX - 管理NPC<<");

        Item hand = rsNpcConfig.getHand();
        Item[] armor = rsNpcConfig.getArmor();

        StringBuilder emotes = new StringBuilder();
        if (rsNpcConfig.getEmoteIDs().isEmpty()) {
            emotes.append("无");
        }else {
            for (String s : rsNpcConfig.getEmoteIDs()) {
                emotes.append("\n    ").append(s);
            }
        }

        StringBuilder cmds = new StringBuilder();
        if (rsNpcConfig.getCmds().isEmpty()) {
            cmds.append("无");
        }else {
            for (String s : rsNpcConfig.getCmds()) {
                cmds.append("\n  ").append(s);
            }
        }

        StringBuilder messages = new StringBuilder();
        if (rsNpcConfig.getMessages().isEmpty()) {
            messages.append("无");
        }else {
            for (String s : rsNpcConfig.getMessages()) {
                messages.append("\n  ").append(s);
            }
        }

        StringBuilder route = new StringBuilder();
        if (rsNpcConfig.getRoute().isEmpty()) {
            route.append("无");
        }else {
            for (Vector3 vector3 : rsNpcConfig.getRoute()) {
                route.append("\n  ")
                        .append("x: ")
                        .append(NukkitMath.round(vector3.x, 2))
                        .append("y: ")
                        .append(NukkitMath.round(vector3.y, 2))
                        .append("z: ")
                        .append(NukkitMath.round(vector3.z, 2));
            }
        }

        simple.setContent(
                "名称: " + rsNpcConfig.getName() +
                "\n显示名称: " + rsNpcConfig.getShowName() +
                "\n坐标:\n  x: " + NukkitMath.round(rsNpcConfig.getLocation().getX(), 2) +
                "\n  y: " + NukkitMath.round(rsNpcConfig.getLocation().getY(), 2) +
                "\n  z: " + NukkitMath.round(rsNpcConfig.getLocation().getZ(), 2) + "" +
                "\n  yaw: " + NukkitMath.round(rsNpcConfig.getLocation().getYaw(), 3) +
                "\n  世界: " + rsNpcConfig.getLocation().getLevel().getName() +
                "\n物品:\n  手持: " + hand.getId() + ":" + hand.getDamage() +
                "\n  头部: " + armor[0].getId() + ":" + armor[0].getDamage() +
                "\n  胸部: " + armor[1].getId() + ":" + armor[1].getDamage() +
                "\n  腿部: " + armor[2].getId() + ":" + armor[2].getDamage() +
                "\n  脚部: " + armor[3].getId() + ":" + armor[3].getDamage() +
                "\n皮肤: " + rsNpcConfig.getSkinName() +
                "\n实体大小: " + rsNpcConfig.getScale() +
                "\n看向玩家: " + (rsNpcConfig.isLookAtThePlayer() ? "是" : "否") +
                "\n表情动作:\n  启用: " + (rsNpcConfig.isEnableEmote() ? "是" : "否") +
                "\n  表情ID: " + emotes +
                "\n  间隔(秒): " + rsNpcConfig.getShowEmoteInterval() +
                "\n允许抛射物触发: " + (rsNpcConfig.isCanProjectilesTrigger() ? "是" : "否") +
                "\n点击执行指令: " + cmds +
                "\n发送消息: " + messages +
                "\n基础移动速度: " + rsNpcConfig.getBaseMoveSpeed() +
                "\n路径：" + route +
                "\n\n");

        simple.addButton(new ResponseElementButton("修改基础配置")
                .onClicked(cp -> sendAdminNpcConfig(cp, rsNpcConfig)));
        simple.addButton(new ResponseElementButton("修改表情动作")
                .onClicked(cp -> sendAdminNpcConfigEmote(cp, rsNpcConfig)));
        simple.addButton(new ResponseElementButton("修改点击命令")
                .onClicked(cp -> sendAdminNpcConfigCommand(cp, rsNpcConfig)));
        simple.addButton(new ResponseElementButton("修改点击消息")
                .onClicked(cp -> sendAdminNpcConfigMessage(cp, rsNpcConfig)));
        simple.addButton(new ResponseElementButton("删除NPC")
                .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "rsnpcx delete " + rsNpcConfig.getName())));
        simple.onClosed(FormHelper::sendAdminNpcAll);

        player.showFormWindow(simple);
    }

    /**
     * 设置npc基础配置界面
     *
     * @param player 玩家
     * @param rsNpcConfig npc配置
     */
    public static void sendAdminNpcConfig(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(">>RsNpcX - 设置NPC<<");

        Item hand = rsNpcConfig.getHand();
        Item[] armor = rsNpcConfig.getArmor();
        custom.addElement(new ElementInput("显示名称", "", rsNpcConfig.getShowName())); //0
        //物品
        custom.addElement(new ElementInput("手持", "0:0", hand.getId() + ":" + hand.getDamage())); //1
        custom.addElement(new ElementInput("头部", "0:0", armor[0].getId() + ":" + armor[0].getDamage())); //2
        custom.addElement(new ElementInput("胸部", "0:0", armor[1].getId() + ":" + armor[1].getDamage())); //3
        custom.addElement(new ElementInput("腿部", "0:0", armor[2].getId() + ":" + armor[2].getDamage())); //4
        custom.addElement(new ElementInput("脚部", "0:0", armor[3].getId() + ":" + armor[3].getDamage())); //5
        //皮肤
        ArrayList<String> skinOptions = new ArrayList<>(RsNpcX.getInstance().getSkins().keySet());
        int defaultOption = 0;
        for (String name : skinOptions) {
            if (name.equals(rsNpcConfig.getSkinName())) {
                break;
            }
            defaultOption++;
        }
        custom.addElement(new ElementDropdown("皮肤", skinOptions, defaultOption)); //6
        custom.addElement(new ElementInput("实体大小", "1.0", rsNpcConfig.getScale() + "")); //7
        custom.addElement(new ElementToggle("看向玩家", rsNpcConfig.isLookAtThePlayer())); //8
        custom.addElement(new ElementToggle("允许抛射物触发", rsNpcConfig.isCanProjectilesTrigger())); //9

        custom.onResponded((formResponseCustom, cp) -> {
            String showName = formResponseCustom.getInputResponse(0);
            if ("".equals(showName.trim())) {
                cp.sendMessage("显示名称不能为空！");
                return;
            }
            rsNpcConfig.setShowName(showName);
            //物品
            rsNpcConfig.setHand(Item.fromString(formResponseCustom.getInputResponse(1)));
            Item[] items = new Item[4];
            items[0] = Item.fromString(formResponseCustom.getInputResponse(2));
            items[1] = Item.fromString(formResponseCustom.getInputResponse(3));
            items[2] = Item.fromString(formResponseCustom.getInputResponse(4));
            items[3] = Item.fromString(formResponseCustom.getInputResponse(5));
            rsNpcConfig.setArmor(items);
            //皮肤
            String skinName = skinOptions.get(formResponseCustom.getDropdownResponse(6).getElementID());
            rsNpcConfig.setShowName(skinName);
            rsNpcConfig.setSkin(RsNpcX.getInstance().getSkinByName(skinName));
            //实体大小
            String scaleString = formResponseCustom.getInputResponse(7);
            float scale = rsNpcConfig.getScale();
            try {
                scale = (float) Double.parseDouble(scaleString);
            } catch (Exception ignored) {
                try {
                    scale = Integer.parseInt(scaleString);
                } catch (Exception e) {
                    player.sendMessage("实体大小应为数字！");
                }
            }
            rsNpcConfig.setScale(scale);
            rsNpcConfig.setLookAtThePlayer(formResponseCustom.getToggleResponse(8));
            rsNpcConfig.setCanProjectilesTrigger(formResponseCustom.getToggleResponse(9));
            //保存并重载
            rsNpcConfig.save();
            if (rsNpcConfig.getEntityRsNpc() != null) {
                rsNpcConfig.getEntityRsNpc().close();
            }
            rsNpcConfig.checkEntity();
            AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                    ">>RsNpcX - 设置NPC<<",
                    "Npc: " + rsNpcConfig.getName() + " 配置保存成功！",
                    "返回",
                    "关闭");
            modal.onClickedTrue(cp2 -> sendAdminNpc(cp2, rsNpcConfig));
            cp.showFormWindow(modal);
        });
        custom.onClosed(cp -> sendAdminNpc(cp, rsNpcConfig));

        player.showFormWindow(custom);
    }

    /**
     * 设置表情动作界面
     *
     * @param player 玩家
     * @param rsNpcConfig npc配置
     */
    public static void sendAdminNpcConfigEmote(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(">>RsNpcX - 设置NPC表情动作<<");

        custom.addElement(new ElementLabel("注意：Npc添加路径后此功能将无法正常工作！")); //0
        custom.addElement(new ElementToggle("启用表情动作", rsNpcConfig.isEnableEmote())); //1

        StringBuilder ids = new StringBuilder();
        for (String id : rsNpcConfig.getEmoteIDs()) {
            ids.append(id).append(";");
        }
        ids.deleteCharAt(ids.length() - 1);
        custom.addElement(new ElementInput("表情动作ID(多个请使用 ; 分割)", "",ids.toString())); //2
        custom.addElement(new ElementInput("间隔(秒)", "", rsNpcConfig.getShowEmoteInterval() + "")); //3

        custom.onResponded((formResponseCustom, cp) -> {
            rsNpcConfig.setEnableEmote(formResponseCustom.getToggleResponse(1));
            rsNpcConfig.getEmoteIDs().clear();
            String[] emoteIDs = formResponseCustom.getInputResponse(2).split(";");
            for (String id : emoteIDs) {
                if (!"".equals(id.trim())) {
                    rsNpcConfig.getEmoteIDs().add(id);
                }
            }
            int showEmoteInterval = rsNpcConfig.getShowEmoteInterval();
            try {
                showEmoteInterval = Integer.parseInt(formResponseCustom.getInputResponse(3));
                if (showEmoteInterval <= 0) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                cp.sendMessage("间隔必须是正整数");
                return;
            }
            rsNpcConfig.setShowEmoteInterval(showEmoteInterval);
            rsNpcConfig.save();
            AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                    ">>RsNpcX - 设置NPC表情动作<<",
                    "Npc: " + rsNpcConfig.getName() + " 表情动作设置保存成功！",
                    "返回",
                    "关闭");
            modal.onClickedTrue(cp2 -> sendAdminNpc(cp2, rsNpcConfig));
            cp.showFormWindow(modal);
        });
        custom.onClosed(cp -> sendAdminNpc(cp, rsNpcConfig));

        player.showFormWindow(custom);
    }

    /**
     * 设置Npc命令界面
     *
     * @param player 玩家
     * @param rsNpcConfig npc配置
     */
    public static void sendAdminNpcConfigCommand(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(">>RsNpcX - 设置NPC命令<<");
        simple.setContent("当前设置NPC: " + rsNpcConfig.getName());

        simple.addButton(new ResponseElementButton("添加新的命令")
                .onClicked(cp -> sendAdminNpcConfigCommandAdd(cp, rsNpcConfig)));
        if (!rsNpcConfig.getCmds().isEmpty()) {
            simple.addButton(new ResponseElementButton("删除现有命令")
                    .onClicked(cp -> sendAdminNpcConfigCommandDelete(cp, rsNpcConfig)));
        }

        player.showFormWindow(simple);
    }

    public static void sendAdminNpcConfigCommandAdd(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(">>RsNpcX - 添加新的命令<<");

        custom.addElement(new ElementLabel("当前设置NPC: " + rsNpcConfig.getName())); //0
        custom.addElement(new ElementInput("命令(可以用 @p 代表玩家)", "", "me 萌萌哒~")); //1
        custom.addElement(new ElementDropdown("执行权限", Arrays.asList("玩家", "OP", "控制台"))); //2

        custom.onResponded((formResponseCustom, cp) -> {
            String cmd = formResponseCustom.getInputResponse(1).replace("&", "");
            if ("".equals(cmd.trim())) {
                cp.sendMessage("命令不能为空！");
                return;
            }
            int elementID = formResponseCustom.getDropdownResponse(2).getElementID();
            if (elementID == 1) {
                cmd += "&op";
            }else if (elementID == 2) {
                cmd += "&con";
            }
            rsNpcConfig.getCmds().add(cmd);
            rsNpcConfig.save();
            AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                    ">>RsNpcX - 添加新的命令<<",
                    "命令: " + cmd + " 添加成功！",
                    "返回",
                    "关闭");
            modal.onClickedTrue(cp2 -> sendAdminNpcConfigCommand(cp2, rsNpcConfig));
            cp.showFormWindow(modal);
        });
        custom.onClosed(cp -> sendAdminNpcConfigCommand(cp, rsNpcConfig));

        player.showFormWindow(custom);
    }

    /**
     * 删除现有命令界面
     *
     * @param player 玩家
     * @param rsNpcConfig npc配置
     */
    public static void sendAdminNpcConfigCommandDelete(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(">>RsNpcX - 删除现有命令<<");
        simple.setContent("当前设置Npc: " + rsNpcConfig.getName() + "\n请选择要删除的命令");

        for (String cmd : rsNpcConfig.getCmds()) {
            simple.addButton(new ResponseElementButton(cmd)
                    .onClicked(cp -> {
                        rsNpcConfig.getCmds().remove(cmd);
                        rsNpcConfig.save();

                        AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                                ">>RsNpcX - 删除现有命令<<",
                                "命令: " + cmd + " 删除成功！",
                                "返回",
                                "关闭");
                        modal.onClickedTrue(cp2 -> sendAdminNpcConfigCommandDelete(cp2, rsNpcConfig));
                        cp.showFormWindow(modal);
                    })
            );
        }
        simple.onClosed(cp -> sendAdminNpcConfigCommand(cp, rsNpcConfig));

        player.showFormWindow(simple);
    }

    /**
     * 管理点击消息界面
     *
     * @param player 玩家
     * @param rsNpcConfig npc配置
     */
    public static void sendAdminNpcConfigMessage(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(">>RsNpcX - 设置NPC消息<<");
        simple.setContent("当前设置NPC: " + rsNpcConfig.getName());

        simple.addButton(new ResponseElementButton("添加新的消息")
                .onClicked(cp -> sendAdminNpcConfigMessageAdd(cp, rsNpcConfig)));
        if (!rsNpcConfig.getMessages().isEmpty()) {
            simple.addButton(new ResponseElementButton("删除现有消息")
                    .onClicked(cp -> sendAdminNpcConfigMessageDelete(cp, rsNpcConfig)));
        }

        player.showFormWindow(simple);
    }

    public static void sendAdminNpcConfigMessageAdd(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(">>RsNpcX - 添加新的消息<<");

        custom.addElement(new ElementLabel("当前设置NPC: " + rsNpcConfig.getName())); //0
        custom.addElement(new ElementInput("消息(可以用 @p 代表玩家)", "", "@p 你好！我是%npcName%")); //1

        custom.onResponded((formResponseCustom, cp) -> {
            String message = formResponseCustom.getInputResponse(1);
            if ("".equals(message.trim())) {
                cp.sendMessage("消息不能为空！");
                return;
            }
            rsNpcConfig.getMessages().add(message);
            rsNpcConfig.save();
            AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                    ">>RsNpcX - 添加新的消息<<",
                    "消息: " + message + " 添加成功！",
                    "返回",
                    "关闭");
            modal.onClickedTrue(cp2 -> sendAdminNpcConfigMessage(cp2, rsNpcConfig));
            cp.showFormWindow(modal);
        });
        custom.onClosed(cp -> sendAdminNpcConfigMessage(cp, rsNpcConfig));

        player.showFormWindow(custom);
    }

    /**
     * 删除现有命令界面
     *
     * @param player 玩家
     * @param rsNpcConfig npc配置
     */
    public static void sendAdminNpcConfigMessageDelete(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(">>RsNpcX - 删除现有消息<<");
        simple.setContent("当前设置Npc: " + rsNpcConfig.getName() + "\n请选择要删除的消息");

        for (String message : rsNpcConfig.getMessages()) {
            simple.addButton(new ResponseElementButton(message)
                    .onClicked(cp -> {
                        rsNpcConfig.getMessages().remove(message);
                        rsNpcConfig.save();

                        AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                                ">>RsNpcX - 删除现有消息<<",
                                "消息: " + message + " 删除成功！",
                                "返回",
                                "关闭");
                        modal.onClickedTrue(cp2 -> sendAdminNpcConfigMessageDelete(cp2, rsNpcConfig));
                        cp.showFormWindow(modal);
                    })
            );
        }
        simple.onClosed(cp -> sendAdminNpcConfigMessage(cp, rsNpcConfig));

        player.showFormWindow(simple);
    }

    private final static List<String> RANDOM_MESSAGE = Arrays.asList(
            "要快乐地面对一切挑战，哪怕恐惧渗入骨髓，因为就算我们是凡人，趁着还活在人世，就该绽放光彩。",
            "听说LT_Name还写了很多小游戏插件！快去试试吧！",
            "我们所过的每个平凡的日常，也许就是连续发生的奇迹。",
            "我也不知道我怎么想的，就是想加入很多随机的内容",
            "Time waits for no one.",
            "我的名字是LT_Name不是IT_Name！！！经常被误解的IT(划)LT_Name如是说",
            "愿你有一天，能与你最重要的人重逢",
            "相信奇迹的人本身就和奇迹一样了不起啊",
            "是的，RsNpcX终于添加GUI了！",
            "如果因为害怕失败，而不去努力，那就不会有能够成功的人。",
            "不辜负年华，做自己想做的事，唱自己想唱的歌",
            "RsNpcX最初只是修了RsNpc的一个bug",
            "114514...这个菜单还是丢掉吧！",
            "若水！永远滴神！",
            "人类的赞歌就是勇气的赞歌。",
            "RsNpcX是开源免费的插件！",
            "即使从梦中醒来，还会有回忆留下。",
            "放火烧山可莉完蛋",
            "RsNpcX的寻路只是辅助性质的哦！"
    );

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd");

    private static String getRandomMessage() {
        switch (DATE_FORMAT.format(new Date())) {
            case "01-01":
                return "一年开开心心，一家和和睦睦，一生快快乐乐，一世平平安安，天天精神百倍，月月喜气扬扬，年年财源广进。元旦快乐！快乐元旦！";
            case "03-08":
                return "三月的风，让你的心情灿烂。三月的雨，让你的快乐绵长。三月的阳光，让你的心头温暖。三月的祝福，让你的生活美满。祝你三八妇女节快乐哦!";
            case "03-12":
                return "挖个坑，埋点土，数个12345；植树节，种棵树，清新空气好舒服；动双手，有幸福，劳动创造新财富。";
            case "04-01":
                return "愚人节，让我们选择快乐，而不是悲伤！";
            case "05-01":
                return "劳动创造幸福未来，双手铺就通达之路，辛勤耕耘百花绽放，汗水浇灌美丽生活，劳动者最最美丽，劳动节来到了，祝你胸前戴红花，当一个劳动榜样，前途无量。";
            case "06-01":
                return "儿童节快乐，永远童真的你！愿你永远保持一颗童心。";
            case "08-01":
                return "守卫边疆，无怨无悔;抢险抗灾，身先士卒;科技建军，国防稳固;为国为民，军功无量。八一建军节，向人民子弟兵问好，愿他们兵强马壮，再立新功!";
            case "08-15":
                return "许一个美好的心愿，祝你快乐连连，送一份美妙的感觉，祝你万事圆圆，传一份短短的祝福，祝你微笑甜甜。中秋节快乐！";
            case "10-01":
                return "灿烂的烟花绽放，欢乐的歌声飞扬，我的祝福乘着洁白的月光，悄悄来到你身旁。每逢佳节倍思友，愿你的幸福乐无忧。国庆佳节，为朋友祝福。";
            case "12-13":
                return "南京大屠杀，国民难忘记。六朝古都城，断壁残垣地。三十万同胞，顷刻魂归西。血淋淋历史，后辈永牢记。国弱被人欺，自强是真理。南京大屠杀纪念日，不忘国耻，自强不息！";
            default:
                return RANDOM_MESSAGE.get(RsNpcX.RANDOM.nextInt(RANDOM_MESSAGE.size()));
        }
    }

}
