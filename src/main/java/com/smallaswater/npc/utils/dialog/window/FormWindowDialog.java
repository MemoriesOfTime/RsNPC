package com.smallaswater.npc.utils.dialog.window;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.utils.dialog.element.ElementDialogButton;
import com.smallaswater.npc.utils.dialog.packet.NPCRequestPacket;
import com.smallaswater.npc.utils.dialog.response.FormResponseDialog;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class FormWindowDialog implements WindowDialog {

    protected static final Gson GSON = new Gson();

    private static long dialogId = 0;

    private String title;

    private String content;

    private String skinData = "{\"picker_offsets\":{\"scale\":[1.70,1.70,1.70],\"translate\":[0,20,0]},\"portrait_offsets\":{\"scale\":[1.750,1.750,1.750],\"translate\":[-7,50,0]},\"skin_list\":[{\"variant\":0},{\"variant\":1},{\"variant\":2},{\"variant\":3},{\"variant\":4},{\"variant\":5},{\"variant\":6},{\"variant\":7},{\"variant\":8},{\"variant\":9},{\"variant\":10},{\"variant\":11},{\"variant\":12},{\"variant\":13},{\"variant\":14},{\"variant\":15},{\"variant\":16},{\"variant\":17},{\"variant\":18},{\"variant\":19},{\"variant\":20},{\"variant\":21},{\"variant\":22},{\"variant\":23},{\"variant\":24},{\"variant\":25},{\"variant\":26},{\"variant\":27},{\"variant\":28},{\"variant\":29},{\"variant\":30},{\"variant\":31},{\"variant\":32},{\"variant\":33},{\"variant\":34}]}";

    //usually you shouldn't edit this
    //in pnx this value is used to be an identifier
    private String sceneName = String.valueOf(dialogId++);

    private List<ElementDialogButton> buttons;

    private final Entity bindEntity;

    protected BiConsumer<Player, FormResponseDialog> formClosedListener;

    private boolean isClosed = false;

    public FormWindowDialog(String title, String content, Entity bindEntity) {
        this(title, content,bindEntity, new ArrayList<>());
    }

    public FormWindowDialog(String title, String content, Entity bindEntity, List<ElementDialogButton> buttons) {
        this.title = title;
        this.content = content;
        this.buttons = buttons;
        this.bindEntity = bindEntity;
        if (this.bindEntity == null) {
            throw new IllegalArgumentException("bindEntity cannot be null!");
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ElementDialogButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<ElementDialogButton> buttons) {
        this.buttons = buttons;
    }

    public ElementDialogButton addButton(String text) {
        return this.addButton(new ElementDialogButton(text, text));
    }

    public ElementDialogButton addButton(ElementDialogButton button) {
        this.buttons.add(button);
        return button;
    }

    public long getEntityId() {
        return this.getBindEntity().getId();
    }

    public Entity getBindEntity() {
        return bindEntity;
    }

    public String getSkinData(){
        return this.skinData;
    }

    public void setSkinData(String data){
        this.skinData = data;
    }

    public String getButtonJSONData() {
        return GSON.toJson(this.buttons);
    }

    public void setButtonJSONData(String json){
        this.setButtons(GSON.fromJson(json, new TypeToken<List<ElementDialogButton>>(){}.getType()));
    }

    public String getSceneName() {
        return sceneName;
    }

    public void updateSceneName() {
        this.sceneName = String.valueOf(dialogId++);
    }

    public FormWindowDialog onClosed(@NotNull BiConsumer<Player, FormResponseDialog> listener) {
        this.formClosedListener = Objects.requireNonNull(listener);
        return this;
    }

    protected void callClosed(@NotNull Player player, FormResponseDialog response) {
        if (this.formClosedListener != null && !this.isClosed) {
            this.formClosedListener.accept(player, response);
        }
    }

    @Override
    public void send(Player player){
        Utils.sendDialogWindows(player, this);
    }

    public static void onEvent(@NotNull NPCRequestPacket packet, @NotNull Player player) {
        FormWindowDialog dialog = Utils.WINDOW_DIALOG_CACHE.getIfPresent(packet.getSceneName());
        if (dialog == null) {
            return;
        }

        if (packet.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_CLOSING_COMMANDS) {
            Utils.WINDOW_DIALOG_CACHE.invalidate(packet.getSceneName());
        }

        FormResponseDialog response = new FormResponseDialog(packet, dialog);

        ElementDialogButton clickedButton = response.getClickedButton();
        if (packet.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_ACTION && clickedButton != null) {
            clickedButton.callClicked(player, response);
            //点击按钮后，需要关闭当前窗口或者跳转新的窗口，否则对话框会卡住玩家，所以可以认为当前对话框已经关闭
            dialog.isClosed = true;
        }

        if (packet.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_CLOSING_COMMANDS) {
            dialog.callClosed(player, response);
        }
    }

}
