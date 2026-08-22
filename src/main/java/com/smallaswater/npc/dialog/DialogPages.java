package com.smallaswater.npc.dialog;

import cn.lanink.gamecore.form.windows.AdvancedFormWindowDialog;
import cn.lanink.gamecore.utils.packet.ProtocolVersion;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.entitys.EntityRsNPC;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.variable.VariableManage;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author LT_Name
 */
public class DialogPages {

    private final String name;
    private final Config config;

    private String defaultPage;
    private final HashMap<String, DialogPage> dialogPageMap = new HashMap<>();

    public DialogPages(@NotNull String name, @NotNull Config config) {
        this.name = name;
        this.config = config;
        this.load();
    }

    private void load() {
        this.defaultPage = config.getString("defaultPage");
        this.config.getMapList("pages").forEach(page -> {
            try {
                DialogPage dialogPage = new DialogPage(this, page);
                this.dialogPageMap.put(dialogPage.getKey(), dialogPage);
            } catch (Exception e) {
                RsNPC.getInstance().getLogger().error(RsNPC.getInstance().getLanguage().translateString("plugin.load.dialog.dataError", this.name + "." + page.get("key")), e);
            }
        });
        Objects.requireNonNull(getDefaultDialogPage(), "Default dialog page cannot be null");
    }

    public DialogPage getDefaultDialogPage() {
        return this.getDialogPage(this.defaultPage);
    }

    public DialogPage getDialogPage(@NotNull String key) {
        return this.dialogPageMap.get(key);
    }

    public static class DialogPage {

        private final DialogPages dialogPages;
        @Getter
        private final String key;
        private final String title;
        private final ArrayList<String> contents = new ArrayList<>();
        private final Sound sound;
        private final ArrayList<Button> buttons = new ArrayList<>();

        private String closeGo;

        public DialogPage (@NotNull DialogPages dialogPages, @NotNull Map<String, Object> map) {
            this.dialogPages = dialogPages;
            this.key = (String) map.get("key");
            this.title = (String) map.get("title");
            //content 支持单条字符串或字符串列表（列表时每次展示随机一条）
            Object contentObj = map.get("content");
            if (contentObj instanceof List) {
                for (Object o : (List<?>) contentObj) {
                    if (o != null) {
                        this.contents.add(String.valueOf(o));
                    }
                }
            } else if (contentObj != null) {
                this.contents.add(String.valueOf(contentObj));
            }
            if (this.contents.isEmpty()) {
                this.contents.add("");
            }
            this.sound = new Sound((Map<String, Object>) map.getOrDefault("sound", new HashMap<>()));
            ((List<Map<String, Object>>) map.get("buttons")).forEach(button -> this.buttons.add(new Button(button)));
            if (map.containsKey("close")) {
                Map<String, Object> closeMap = (Map<String, Object>) map.get("close");
                if (closeMap.containsKey("go")) {
                    this.closeGo = (String) closeMap.get("go");
                }
            }
        }

        /**
         * 等概率随机返回一条正文原始文本（未经变量替换）。
         * contents 至少有一个元素（构造时已兜底），不会越界。
         */
        public String getContent() {
            return this.contents.get(ThreadLocalRandom.current().nextInt(this.contents.size()));
        }

        public void send(@NotNull EntityRsNPC entityRsNpc, @NotNull Player player) {
            //RsNPC的对话框没有实现编辑界面，创造玩家先转为冒险模式，再发送对话框，最后恢复玩家的游戏模式
            int beforeGameMode = -1;
            if (player.getGamemode() == Player.CREATIVE) {
                beforeGameMode = player.getGamemode();
                player.setGamemode(Player.ADVENTURE);
            }
            final int finalBeforeGameMode = beforeGameMode;

            //1.19.40 有两个关闭按钮，上面的关闭按钮无法监听，这里使用Task延迟处理
            Server.getInstance().getScheduler().scheduleDelayedTask(RsNPC.getInstance(), () -> {
                if (finalBeforeGameMode != -1) {
                    player.setGamemode(finalBeforeGameMode);
                }

                //修复 1.19.40+ 未知原因导致的不显示NPC名称问题
                if (ProtocolInfo.CURRENT_PROTOCOL >= ProtocolVersion.v1_19_40) {
                    String nameTag = entityRsNpc.getNameTag();
                    entityRsNpc.setNameTag("re" + nameTag);
                    entityRsNpc.setNameTag(nameTag);
                }
            }, 5);

            if (this.sound.isEnable() && !"".equals(this.sound.getIdentifier())) {
                Utils.playSound(player, this.sound.getIdentifier());
            }

            AdvancedFormWindowDialog windowDialog = new AdvancedFormWindowDialog(
                    VariableManage.stringReplace(player, this.title, entityRsNpc.getConfig()),
                    VariableManage.stringReplace(player, this.getContent(), entityRsNpc.getConfig()),
                    entityRsNpc
            );

            windowDialog.setSkinData("{\"picker_offsets\":{\"scale\":[1.75,1.75,1.75],\"translate\":[0,0,0]},\"portrait_offsets\":{\"scale\":[1.75,1.75,1.75],\"translate\":[0,-50,0]}}");

            this.buttons.forEach(button -> {
                windowDialog.addAdvancedButton(button.getText()).onClicked((p, response) -> {
                    for (Button.ButtonAction buttonAction : button.getButtonActions()) {
                        if (buttonAction.getType() == Button.ButtonActionType.ACTION_CLOSE) {
                            windowDialog.close(p, response);
                        } else if (buttonAction.getType() == Button.ButtonActionType.GOTO) {
                            DialogPage gotoPage = dialogPages.getDialogPage(buttonAction.getData());
                            if (gotoPage != null) {
                                gotoPage.send(entityRsNpc, player);
                            } else {
                                RsNPC.getInstance().getLogger().warning("对话框页面 " + this.key + " 按钮跳转失败！不存在名为 " + buttonAction.getData() + " 的页面！");
                            }
                        } else if (buttonAction.getType() == Button.ButtonActionType.EXECUTE_COMMAND) {
                            Server.getInstance().getScheduler().scheduleDelayedTask(RsNPC.getInstance(), () -> {
                                Utils.executeCommand(p, entityRsNpc.getConfig(), buttonAction.getListData());
                            }, 10);
                        }

                        if (button.getSound().isEnable() && !"".equals(button.getSound().getIdentifier())) {
                            Utils.playSound(player, button.getSound().getIdentifier());
                        }

                        //TODO 其他点击操作
                    }
                });
            });

            windowDialog.onClosed((p, response) -> {
                if (this.closeGo != null) {
                    DialogPage closeGoPage = this.dialogPages.getDialogPage(this.closeGo);
                    if (closeGoPage != null) {
                        closeGoPage.send(entityRsNpc, player);
                    } else {
                        RsNPC.getInstance().getLogger().warning("对话框页面 " + this.key + " 关闭跳转失败！不存在名为 " + this.closeGo + " 的页面！");
                    }
                }
            });

            windowDialog.send(player);
        }

        @Getter
        public static class Sound {

            private final boolean enable;
            private final String identifier;

            public Sound() {
                this.enable = false;
                this.identifier = "";
            }

            public Sound(@NotNull Map<String, Object> map) {
                this.enable = (boolean) map.getOrDefault("enable", false);
                this.identifier = (String) map.getOrDefault("identifier", "");
            }
        }

        @Getter
        public static class Button {

            private final String text;

            private final List<ButtonAction> buttonActions = new ArrayList<>();

            private final Sound sound;

            public Button(@NotNull Map<String, Object> map) {
                this.text = (String) map.get("text");
                if (map.containsKey("action")) {
                    ButtonAction buttonAction = new ButtonAction(ButtonActionType.ACTION, String.valueOf(map.get("action")));
                    if ("close".equalsIgnoreCase(buttonAction.getData())) {
                        buttonAction.setType(ButtonActionType.ACTION_CLOSE);
                    }
                    this.buttonActions.add(buttonAction);
                }
                if (map.containsKey("go")) {
                    ButtonAction buttonAction = new ButtonAction(ButtonActionType.GOTO, String.valueOf(map.get("go")));
                    this.buttonActions.add(buttonAction);
                }
                if (map.containsKey("cmd")) {
                    Object cmdObj = map.get("cmd");
                    if (cmdObj instanceof List) {
                        ButtonAction buttonAction = new ButtonAction(ButtonActionType.EXECUTE_COMMAND);
                        buttonAction.getListData().clear();
                        // YAML 中数字会被解析为 Integer，统一转为字符串避免点击时 ClassCastException
                        for (Object cmd : (List<?>) cmdObj) {
                            if (cmd != null) {
                                buttonAction.getListData().add(String.valueOf(cmd));
                            }
                        }
                        this.buttonActions.add(buttonAction);
                    } else {
                        RsNPC.getInstance().getLogger().warning("对话框按钮 \"" + this.text + "\" 的 cmd 配置不是列表（可能为空值），已忽略！");
                    }
                }

                if (this.buttonActions.isEmpty()) {
                    this.buttonActions.add(new ButtonAction(ButtonActionType.ACTION_CLOSE));
                }

                if (map.containsKey("sound")) {
                    this.sound = new Sound((Map<String, Object>) map.get("sound"));
                } else {
                    this.sound = new Sound();
                }
            }

            @Setter
            @Getter
            public static class ButtonAction {

                private ButtonActionType type;

                private String data;

                private List<String> listData = new ArrayList<>();

                public ButtonAction(@NotNull ButtonActionType type) {
                    this(type, null);
                }

                public ButtonAction(@NotNull ButtonActionType type, String data) {
                    this.type = type;
                    this.data = data;
                }

            }

            public enum ButtonActionType {
                ACTION,
                ACTION_CLOSE,
                GOTO,
                EXECUTE_COMMAND,
                ;
            }
        }

    }

}
