package com.smallaswater.npc.utils;

import cn.lanink.gamecore.form.element.ResponseElementButton;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowCustom;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowModal;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowSimple;
import cn.lanink.gamecore.utils.Language;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.item.Item;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.data.RsNpcConfig;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author LT_Name
 */
public class FormHelper {

    private FormHelper() {
        throw new RuntimeException("Error");
    }

    public static void sendMain(@NotNull Player player) {
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(language.translateString("gui.main.title"));
        if ("chs".equalsIgnoreCase(Server.getInstance().getLanguage().getLang())) {
            simple.setContent(getRandomMessage() + "\n\n");
        }

        simple.addButton(new ResponseElementButton(language.translateString("gui.main.button.createNPCText")).onClicked(FormHelper::sendCreateNpc));
        simple.addButton(new ResponseElementButton(language.translateString("gui.main.button.adminNPCText")).onClicked(FormHelper::sendAdminNpcSelect));
        simple.addButton(new ResponseElementButton(language.translateString("gui.main.button.reloadText"))
                .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "rsnpc reload"))
        );

        player.showFormWindow(simple);
    }

    public static void sendCreateNpc(@NotNull Player player) {
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(language.translateString("gui.createNPC.title"));

        custom.addElement(new ElementInput(language.translateString("gui.createNPC.input.npcNameText")));

        custom.onResponded((formResponseCustom, cp) -> {
            String name = formResponseCustom.getInputResponse(0);
            Server.getInstance().dispatchCommand(cp, "rsnpc create " + name);
        });
        custom.onClosed(FormHelper::sendMain);

        player.showFormWindow(custom);
    }

    public static void sendAdminNpcSelect(@NotNull Player player) {
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(language.translateString("gui.adminNPCSelect.title"));
        simple.setContent(language.translateString("gui.adminNPCSelect.content"));

        for (Map.Entry<String, RsNpcConfig> entry : RsNPC.getInstance().getNpcs().entrySet()) {
            simple.addButton(new ResponseElementButton(entry.getKey())
                    .onClicked(cp -> sendAdminNpc(cp, entry.getValue())));
        }
        simple.onClosed(FormHelper::sendMain);

        player.showFormWindow(simple);
    }

    public static void sendAdminNpc(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(language.translateString("gui.adminNPC.title"));

        Item hand = rsNpcConfig.getHand();
        Item[] armor = rsNpcConfig.getArmor();

        StringBuilder emotes = new StringBuilder();
        if (rsNpcConfig.getEmoteIDs().isEmpty()) {
            emotes.append(language.translateString("gui.adminNPC.text.empty"));
        }else {
            for (String s : rsNpcConfig.getEmoteIDs()) {
                emotes.append("\n    ").append(s);
            }
        }

        StringBuilder cmds = new StringBuilder();
        if (rsNpcConfig.getCmds().isEmpty()) {
            cmds.append(language.translateString("gui.adminNPC.text.empty"));
        }else {
            for (String s : rsNpcConfig.getCmds()) {
                cmds.append("\n  ").append(s);
            }
        }

        StringBuilder messages = new StringBuilder();
        if (rsNpcConfig.getMessages().isEmpty()) {
            messages.append(language.translateString("gui.adminNPC.text.empty"));
        }else {
            for (String s : rsNpcConfig.getMessages()) {
                messages.append("\n  ").append(s);
            }
        }

        StringBuilder route = new StringBuilder();
        if (rsNpcConfig.getRoute().isEmpty()) {
            route.append(language.translateString("gui.adminNPC.text.empty"));
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
                "\n显示名称一直可见: " + toAdminNpcBooleanShowText(rsNpcConfig.isNameTagAlwaysVisible()) +
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
                "\n实体NetworkId: " + rsNpcConfig.getNetworkId() +
                "\n实体大小: " + rsNpcConfig.getScale() +
                "\n看向玩家: " + toAdminNpcBooleanShowText(rsNpcConfig.isLookAtThePlayer()) +
                "\n表情动作:\n  启用: " + toAdminNpcBooleanShowText(rsNpcConfig.isEnableEmote()) +
                "\n  表情ID: " + emotes +
                "\n  间隔(秒): " + rsNpcConfig.getShowEmoteInterval() +
                "\n允许抛射物触发: " + toAdminNpcBooleanShowText(rsNpcConfig.isCanProjectilesTrigger()) +
                "\n点击执行指令: " + cmds +
                "\n点击发送消息: " + messages +
                "\n对话框:" +
                "\n  启用对话框: " + toAdminNpcBooleanShowText(rsNpcConfig.isEnabledDialogPages()) +
                "\n  对话框配置: " + rsNpcConfig.getDialogPagesName() +
                "\n移动:" +
                "\n  基础移动速度: " + rsNpcConfig.getBaseMoveSpeed() +
                "\n  启用辅助寻路: " + toAdminNpcBooleanShowText(rsNpcConfig.isEnablePathfinding()) +
                "\n  路径: " + route +
                "\n\n");

        simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPC.button.modifyBasicConfig"))
                .onClicked(cp -> sendAdminNpcConfig(cp, rsNpcConfig)));
        simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPC.button.modifyEmote"))
                .onClicked(cp -> sendAdminNpcConfigEmote(cp, rsNpcConfig)));
        simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPC.button.modifyCommand"))
                .onClicked(cp -> sendAdminNpcConfigCommand(cp, rsNpcConfig)));
        simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPC.button.modifyMessage"))
                .onClicked(cp -> sendAdminNpcConfigMessage(cp, rsNpcConfig)));
        simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPC.button.deleteNPC"))
                .onClicked(cp -> Server.getInstance().dispatchCommand(cp, "rsnpc delete " + rsNpcConfig.getName())));
        simple.onClosed(FormHelper::sendAdminNpcSelect);

        player.showFormWindow(simple);
    }

    private static String toAdminNpcBooleanShowText(boolean b) {
        return b ? RsNPC.getInstance().getLanguage().translateString("gui.adminNPC.text.true") : RsNPC.getInstance().getLanguage().translateString("gui.adminNPC.text.false");
    }

    /**
     * 设置npc基础配置界面
     *
     * @param player 玩家
     * @param rsNpcConfig npc配置
     */
    public static void sendAdminNpcConfig(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(language.translateString("gui.adminNpcConfig.title"));

        Item hand = rsNpcConfig.getHand();
        Item[] armor = rsNpcConfig.getArmor();
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfig.input.displayName"), "", rsNpcConfig.getShowName())); //0
        custom.addElement(new ElementToggle(language.translateString("gui.adminNPCConfig.input.displayNameAlwaysVisible"), rsNpcConfig.isNameTagAlwaysVisible())); //1
        //物品
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfig.input.itemHand"), "0:0", Utils.item2String(hand))); //2
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfig.input.itemHelmet"), "0:0", Utils.item2String(armor[0]))); //3
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfig.input.itemChestplate"), "0:0", Utils.item2String(armor[1]))); //4
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfig.input.itemLeggings"), "0:0", Utils.item2String(armor[2]))); //5
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfig.input.itemBoots"), "0:0", Utils.item2String(armor[3]))); //6
        //皮肤
        ArrayList<String> skinOptions = new ArrayList<>(RsNPC.getInstance().getSkins().keySet());
        skinOptions.add("Default Skin");
        int defaultOption = -1;
        for (String name : skinOptions) {
            defaultOption++;
            if (name.equals(rsNpcConfig.getSkinName())) {
                break;
            }
        }
        custom.addElement(new ElementDropdown(language.translateString("gui.adminNPCConfig.dropdown.skin"), skinOptions, defaultOption)); //7
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfig.input.entityNetworkId"), "-1", rsNpcConfig.getNetworkId() + "")); //8
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfig.input.entityScale"), "1.0", rsNpcConfig.getScale() + "")); //9
        custom.addElement(new ElementToggle(language.translateString("gui.adminNPCConfig.toggle.lookAtThePlayer"), rsNpcConfig.isLookAtThePlayer())); //10
        custom.addElement(new ElementToggle(language.translateString("gui.adminNPCConfig.toggle.CanProjectilesTrigger"), rsNpcConfig.isCanProjectilesTrigger())); //11
        custom.addElement(new ElementToggle(language.translateString("gui.adminNPCConfig.toggle.EnabledDialogPages"), rsNpcConfig.isEnabledDialogPages())); //12
        ArrayList<String> dialogOptions = new ArrayList<>(RsNPC.getInstance().getDialogManager().getDialogConfigs().keySet());
        if (dialogOptions.isEmpty()) {
            dialogOptions.add("Null");
        }
        defaultOption = -1;
        for (String name : dialogOptions) {
            defaultOption++;
            if (name.equals(rsNpcConfig.getDialogPagesName())) {
                break;
            }
        }
        custom.addElement(new ElementDropdown(language.translateString("gui.adminNPCConfig.dropdown.DialogConfig"), dialogOptions, defaultOption)); //13

        custom.onResponded((formResponseCustom, cp) -> {
            try {
                String showName = formResponseCustom.getInputResponse(0);
                if ("".equals(showName.trim())) {
                    cp.sendMessage(language.translateString("gui.adminNPCConfig.responded.showNameNull"));
                    return;
                }
                rsNpcConfig.setShowName(showName);
                rsNpcConfig.setNameTagAlwaysVisible(formResponseCustom.getToggleResponse(1));
                //物品
                rsNpcConfig.setHand(Item.fromString(formResponseCustom.getInputResponse(2)));
                Item[] items = new Item[4];
                items[0] = Item.fromString(formResponseCustom.getInputResponse(3));
                items[1] = Item.fromString(formResponseCustom.getInputResponse(4));
                items[2] = Item.fromString(formResponseCustom.getInputResponse(5));
                items[3] = Item.fromString(formResponseCustom.getInputResponse(6));
                rsNpcConfig.setArmor(items);
                //皮肤
                String skinName = skinOptions.get(formResponseCustom.getDropdownResponse(7).getElementID());
                rsNpcConfig.setSkinName(skinName);
                rsNpcConfig.setSkin(RsNPC.getInstance().getSkinByName(skinName));
                //实体NetworkId
                try {
                    rsNpcConfig.setNetworkId(Integer.parseInt(formResponseCustom.getInputResponse(8)));
                }catch (Exception e) {
                    player.sendMessage(language.translateString("gui.adminNPCConfig.responded.networkIdError"));
                }
                //实体大小
                String scaleString = formResponseCustom.getInputResponse(9);
                float scale = rsNpcConfig.getScale();
                try {
                    scale = (float) Double.parseDouble(scaleString);
                } catch (Exception ignored) {
                    try {
                        scale = Integer.parseInt(scaleString);
                    } catch (Exception e) {
                        player.sendMessage(language.translateString("gui.adminNPCConfig.responded.scaleError"));
                    }
                }
                rsNpcConfig.setScale(scale);
                rsNpcConfig.setLookAtThePlayer(formResponseCustom.getToggleResponse(10));
                rsNpcConfig.setCanProjectilesTrigger(formResponseCustom.getToggleResponse(11));
                rsNpcConfig.setEnabledDialogPages(formResponseCustom.getToggleResponse(12));
                rsNpcConfig.setDialogPagesName(formResponseCustom.getDropdownResponse(13).getElementContent());
                //保存并重载
                rsNpcConfig.save();
                if (rsNpcConfig.getEntityRsNpc() != null) {
                    rsNpcConfig.getEntityRsNpc().close();
                }
                rsNpcConfig.checkEntity();
                AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                        language.translateString("gui.adminNPCConfig.title"),
                        language.translateString("gui.adminNPCConfig.respondedWindowModal.content", rsNpcConfig.getName()),
                        language.translateString("gui.adminNPCConfig.respondedWindowModal.button.true"),
                        language.translateString("gui.adminNPCConfig.respondedWindowModal.button.false"));
                modal.onClickedTrue(cp2 -> sendAdminNpc(cp2, rsNpcConfig));
                cp.showFormWindow(modal);
            }catch (Exception e) { //针对漏掉的错误部分
                cp.sendMessage(language.translateString("gui.adminNPCConfig.responded.error"));
                RsNPC.getInstance().getLogger().error(language.translateString("gui.adminNPCConfig.responded.errorConsoleMessage"), e);
            }
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
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(language.translateString("gui.adminNPCConfigEmote.title"));

        custom.addElement(new ElementLabel(language.translateString("gui.adminNPCConfigEmote.label.tip"))); //0
        custom.addElement(new ElementToggle(language.translateString("gui.adminNPCConfigEmote.toggle.enableEmote"), rsNpcConfig.isEnableEmote())); //1

        StringBuilder ids = new StringBuilder();
        for (String id : rsNpcConfig.getEmoteIDs()) {
            ids.append(id).append(";");
        }
        ids.deleteCharAt(ids.length() - 1);
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfigEmote.input.emoteID"), "",ids.toString())); //2
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfigEmote.input.emoteInterval"), "", rsNpcConfig.getShowEmoteInterval() + "")); //3

        custom.onResponded((formResponseCustom, cp) -> {
            rsNpcConfig.setEnableEmote(formResponseCustom.getToggleResponse(1));
            rsNpcConfig.getEmoteIDs().clear();
            String[] emoteIDs = formResponseCustom.getInputResponse(2).split(";");
            for (String id : emoteIDs) {
                if (!"".equals(id.trim())) {
                    rsNpcConfig.getEmoteIDs().add(id);
                }
            }
            int showEmoteInterval;
            try {
                showEmoteInterval = Integer.parseInt(formResponseCustom.getInputResponse(3));
                if (showEmoteInterval <= 0) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                cp.sendMessage(language.translateString("gui.adminNPCConfigEmote.responded.emoteIntervalError"));
                return;
            }
            rsNpcConfig.setShowEmoteInterval(showEmoteInterval);
            rsNpcConfig.save();
            AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                    language.translateString("gui.adminNPCConfigEmote.title"),
                    language.translateString("gui.adminNPCConfigEmote.respondedWindowModal.content", rsNpcConfig.getName()),
                    language.translateString("gui.adminNPCConfigEmote.respondedWindowModal.button.true"),
                    language.translateString("gui.adminNPCConfigEmote.respondedWindowModal.button.false"));
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
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(language.translateString("gui.adminNPCConfigCommand.title"));
        simple.setContent(language.translateString("gui.adminNPCConfigCommand.content", rsNpcConfig.getName()));

        simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPCConfigCommand.button.addNewCommand"))
                .onClicked(cp -> sendAdminNpcConfigCommandAdd(cp, rsNpcConfig)));
        if (!rsNpcConfig.getCmds().isEmpty()) {
            simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPCConfigCommand.button.deleteCommand"))
                    .onClicked(cp -> sendAdminNpcConfigCommandDelete(cp, rsNpcConfig)));
        }

        player.showFormWindow(simple);
    }

    public static void sendAdminNpcConfigCommandAdd(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(language.translateString("gui.adminNPCConfigCommandAdd.title"));

        custom.addElement(new ElementLabel(language.translateString("gui.adminNPCConfigCommandAdd.label.tip", rsNpcConfig.getName()))); //0
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfigCommandAdd.input.command"), "", "me 萌萌哒~")); //1
        custom.addElement(new ElementDropdown(
                language.translateString("gui.adminNPCConfigCommandAdd.dropdown.permission"),
                Arrays.asList(
                        language.translateString("gui.adminNPCConfigCommandAdd.dropdown.permission.player"),
                        language.translateString("gui.adminNPCConfigCommandAdd.dropdown.permission.op"),
                        language.translateString("gui.adminNPCConfigCommandAdd.dropdown.permission.console")
                )
        )); //2

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
                    language.translateString("gui.adminNPCConfigCommandAdd.title"),
                    language.translateString("gui.adminNPCConfigCommandAdd.respondedWindowModal.content", cmd),
                    language.translateString("gui.adminNPCConfigCommandAdd.respondedWindowModal.button.true"),
                    language.translateString("gui.adminNPCConfigCommandAdd.respondedWindowModal.button.false"));
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
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(language.translateString("gui.adminNPCConfigCommandDelete.title"));
        simple.setContent(language.translateString("gui.adminNPCConfigCommandDelete.content", rsNpcConfig.getName()));

        for (String cmd : rsNpcConfig.getCmds()) {
            simple.addButton(new ResponseElementButton(cmd)
                    .onClicked(cp -> {
                        rsNpcConfig.getCmds().remove(cmd);
                        rsNpcConfig.save();

                        AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                                language.translateString("gui.adminNPCConfigCommandDelete.title"),
                                language.translateString("gui.adminNPCConfigCommandDelete.respondedWindowModal.content", cmd),
                                language.translateString("gui.adminNPCConfigCommandDelete.respondedWindowModal.button.true"),
                                language.translateString("gui.adminNPCConfigCommandDelete.respondedWindowModal.button.false"));
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
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(language.translateString("gui.adminNPCConfigMessage.title"));
        simple.setContent(language.translateString("gui.adminNPCConfigMessage.content", rsNpcConfig.getName()));

        simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPCConfigMessage.button.addNewMessage"))
                .onClicked(cp -> sendAdminNpcConfigMessageAdd(cp, rsNpcConfig)));
        if (!rsNpcConfig.getMessages().isEmpty()) {
            simple.addButton(new ResponseElementButton(language.translateString("gui.adminNPCConfigMessage.button.deleteMessage"))
                    .onClicked(cp -> sendAdminNpcConfigMessageDelete(cp, rsNpcConfig)));
        }

        player.showFormWindow(simple);
    }

    public static void sendAdminNpcConfigMessageAdd(@NotNull Player player, @NotNull RsNpcConfig rsNpcConfig) {
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(language.translateString("gui.adminNPCConfigMessageAdd.title"));

        custom.addElement(new ElementLabel(language.translateString("gui.adminNPCConfigMessageAdd.content", rsNpcConfig.getName()))); //0
        custom.addElement(new ElementInput(language.translateString("gui.adminNPCConfigMessageAdd.input.message"), "", "@p 你好！我是%npcName%")); //1

        custom.onResponded((formResponseCustom, cp) -> {
            String message = formResponseCustom.getInputResponse(1);
            if ("".equals(message.trim())) {
                cp.sendMessage(language.translateString("gui.adminNPCConfigMessageAdd.responded.messageNull"));
                return;
            }
            rsNpcConfig.getMessages().add(message);
            rsNpcConfig.save();
            AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                    language.translateString("gui.adminNPCConfigMessageAdd.title"),
                    language.translateString("gui.adminNPCConfigMessageAdd.respondedWindowModal.content", message),
                    language.translateString("gui.adminNPCConfigMessageAdd.respondedWindowModal.button.true"),
                    language.translateString("gui.adminNPCConfigMessageAdd.respondedWindowModal.button.false"));
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
        Language language = RsNPC.getInstance().getLanguage();

        AdvancedFormWindowSimple simple = new AdvancedFormWindowSimple(language.translateString("gui.AdminNPCConfigMessageDelete.title"));
        simple.setContent(language.translateString("gui.adminNPCConfigMessageDelete.content", rsNpcConfig.getName()));

        for (String message : rsNpcConfig.getMessages()) {
            simple.addButton(new ResponseElementButton(message)
                    .onClicked(cp -> {
                        rsNpcConfig.getMessages().remove(message);
                        rsNpcConfig.save();

                        AdvancedFormWindowModal modal = new AdvancedFormWindowModal(
                                language.translateString("gui.AdminNPCConfigMessageDelete.title"),
                                language.translateString("gui.AdminNPCConfigMessageDelete.respondedWindowModal.content", message),
                                language.translateString("gui.AdminNPCConfigMessageDelete.respondedWindowModal.button.true"),
                                language.translateString("gui.AdminNPCConfigMessageDelete.respondedWindowModal.button.false"));
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
            "是的，RsNPC终于添加GUI了！",
            "如果因为害怕失败，而不去努力，那就不会有能够成功的人。",
            "不辜负年华，做自己想做的事，唱自己想唱的歌",
            "RsNpcX(RsNPC2.0)最初只是修了RsNpc的一个bug",
            "114514...这个菜单还是丢掉吧！",
            "若水！永远滴神！",
            "人类的赞歌就是勇气的赞歌。",
            "RsNPC是开源免费的插件！",
            "即使从梦中醒来，还会有回忆留下。",
            "放火烧山可莉完蛋",
            "RsNPC的寻路只是辅助性质的哦！",
            "路径点距离较远时，RsNPC可能会占用更多的性能进行寻路！",
            "创造模式无法正常使用对话框功能，RsNPC会尝试把您的游戏模式暂时改为冒险模式"
    );

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd");

    /**
     * 获取随机信息
     *
     * @return 随机信息
     */
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
            case "09-03":
                return "中国人民抗日战争胜利纪念日！珍惜现在的和平生活，同时不要忘了那些为现在和平生活付出努力甚至生命的人，铭记历史，勿忘国耻，吾辈自强，奋勇前进！";
            case "10-01":
                return "灿烂的烟花绽放，欢乐的歌声飞扬，我的祝福乘着洁白的月光，悄悄来到你身旁。每逢佳节倍思友，愿你的幸福乐无忧。国庆佳节，为朋友祝福。";
            case "12-13":
                return "南京大屠杀，国民难忘记。六朝古都城，断壁残垣地。三十万同胞，顷刻魂归西。血淋淋历史，后辈永牢记。国弱被人欺，自强是真理。南京大屠杀纪念日，不忘国耻，自强不息！";
            default:
                return RANDOM_MESSAGE.get(RsNPC.RANDOM.nextInt(RANDOM_MESSAGE.size()));
        }
    }

}
