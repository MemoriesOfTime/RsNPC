package com.smallaswater.npc.dialog;

import cn.lanink.gamecore.form.windows.AdvancedFormWindowDialog;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.entitys.EntityRsNPC;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.variable.VariableManage;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                RsNPC.getInstance().getLogger().error("加载对话页面失败：" + this.name + "." + page.get("key"), e);
            }
        });
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
        private final String content;
        private final ArrayList<Button> buttons = new ArrayList<>();

        private String closeGo;

        public DialogPage (@NotNull DialogPages dialogPages, @NotNull Map<String, Object> map) {
            this.dialogPages = dialogPages;
            this.key = (String) map.get("key");
            this.title = (String) map.get("title");
            this.content = (String) map.get("content");
            ((List<Map<String, Object>>) map.get("buttons")).forEach(button -> this.buttons.add(new Button(button)));
            if (map.containsKey("close")) {
                Map<String, Object> closeMap = (Map<String, Object>) map.get("close");
                if (closeMap.containsKey("go")) {
                    this.closeGo = (String) closeMap.get("go");
                }
            }
        }

        public void send(@NotNull EntityRsNPC entityRsNpc, @NotNull Player player) {
            //RsNPC的对话框没有实现编辑界面，创造玩家先转为冒险模式，再发送对话框，最后恢复玩家的游戏模式
            int beforeGameMode = -1;
            if (player.getGamemode() == Player.CREATIVE) {
                beforeGameMode = player.getGamemode();
                player.setGamemode(Player.ADVENTURE);
            }
            final int finalBeforeGameMode = beforeGameMode;

            AdvancedFormWindowDialog windowDialog = new AdvancedFormWindowDialog(
                    VariableManage.stringReplace(player, this.title, entityRsNpc.getConfig()),
                    VariableManage.stringReplace(player, this.content, entityRsNpc.getConfig()),
                    entityRsNpc
            );

            windowDialog.setSkinData("{\"picker_offsets\":{\"scale\":[1.75,1.75,1.75],\"translate\":[0,0,0]},\"portrait_offsets\":{\"scale\":[1.75,1.75,1.75],\"translate\":[0,-50,0]}}");

            this.buttons.forEach(button -> {
                windowDialog.addAdvancedButton(button.getText()).onClicked((p, response) -> {
                    if (finalBeforeGameMode != -1) {
                        p.setGamemode(finalBeforeGameMode);
                    }

                    for (Button.ButtonAction buttonAction : button.getButtonActions()) {
                        if (buttonAction.getType() == Button.ButtonActionType.ACTION_CLOSE) {
                            windowDialog.close(p, response);
                        }else if (buttonAction.getType() == Button.ButtonActionType.GOTO) {
                            dialogPages.getDialogPage(buttonAction.getData()).send(entityRsNpc, player);
                        }else if (buttonAction.getType() == Button.ButtonActionType.EXECUTE_COMMAND) {
                            Utils.executeCommand(p, entityRsNpc.getConfig(), buttonAction.getListData());
                        }
                        //TODO 其他点击操作

                    }
                });
            });

            windowDialog.onClosed((p, response) -> {
                if (finalBeforeGameMode != -1) {
                    p.setGamemode(finalBeforeGameMode);
                }
                if (this.closeGo != null) {
                    this.dialogPages.getDialogPage(this.closeGo).send(entityRsNpc, player);
                }
            });

            windowDialog.send(player);
        }

        public static class Button {

            @Getter
            private final String text;

            @Getter
            private final List<ButtonAction> buttonActions = new ArrayList<>();

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
                    ButtonAction buttonAction = new ButtonAction(ButtonActionType.EXECUTE_COMMAND);
                    buttonAction.getListData().clear();
                    buttonAction.getListData().addAll((List<String>) map.get("cmd"));
                    this.buttonActions.add(buttonAction);
                }

                if (this.buttonActions.isEmpty()) {
                    this.buttonActions.add(new ButtonAction(ButtonActionType.ACTION_CLOSE));
                }
            }

            public static class ButtonAction {

                @Getter
                @Setter
                private ButtonActionType type;

                @Getter
                @Setter
                private String data;

                @Getter
                @Setter
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
