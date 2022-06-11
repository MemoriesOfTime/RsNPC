package com.smallaswater.npc.dialog;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import com.smallaswater.npc.RsNpcX;
import com.smallaswater.npc.entitys.EntityRsNpc;
import com.smallaswater.npc.utils.dialog.window.FormWindowDialog;
import lombok.Getter;

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
    private HashMap<String, DialogPage> dialogPageMap = new HashMap<>();

    public DialogPages(String name, Config config) {
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
                RsNpcX.getInstance().getLogger().error("加载对话页面失败：" + this.name + "." + page.get("key"), e);
            }
        });
    }

    public DialogPage getDefaultDialogPage() {
        return this.getDialogPage(this.defaultPage);
    }

    public DialogPage getDialogPage(String key) {
        return this.dialogPageMap.get(key);
    }

    public static class DialogPage {

        private final DialogPages dialogPages;
        @Getter
        private final String key;
        private String title;
        private String content;
        private ArrayList<Button> buttons = new ArrayList<>();

        public DialogPage (DialogPages dialogPages, Map<String, Object> map) {
            this.dialogPages = dialogPages;
            this.key = (String) map.get("key");
            this.title = (String) map.get("title");
            this.content = (String) map.get("content");
            ((List<Map<String, Object>>) map.get("buttons")).forEach(button -> this.buttons.add(new Button(button)));
        }

        public void send(EntityRsNpc entityRsNpc, Player player) {
            FormWindowDialog windowDialog = new FormWindowDialog(this.title, this.content, entityRsNpc);

            this.buttons.forEach(button -> {
                windowDialog.addButton(button.getText()).onClicked(p -> {
                    if (button.getType() == Button.ButtonType.GOTO) {
                        Server.getInstance().getScheduler().scheduleDelayedTask(RsNpcX.getInstance(), () -> {
                            dialogPages.getDialogPage(button.getData()).send(entityRsNpc, player);
                            //TODO 检查第三页无法正常显示的问题
                        }, 15);
                    }
                    //TODO 其他点击操作
                });
            });

            windowDialog.send(player);
        }

        public static class Button {

            @Getter
            private String text;
            @Getter
            private ButtonType type;
            @Getter
            private String data;

            public Button(Map<String, Object> map) {
                this.text = (String) map.get("text");
                if (map.containsKey("action")) {
                    this.type = ButtonType.ACTION;
                    this.data = String.valueOf(map.get("action"));
                    if ("close".equalsIgnoreCase(this.data)) {
                        this.type = ButtonType.ACTION_CLOSE;
                    }
                }else if (map.containsKey("go")) {
                    this.type = ButtonType.GOTO;
                    this.data = String.valueOf(map.get("go"));
                }
                //TODO
            }

            public enum ButtonType {
                ACTION,
                ACTION_CLOSE,
                GOTO,
                ;
            }
        }

    }

}
