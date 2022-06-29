package com.smallaswater.npc.utils.dialog.window;

import cn.nukkit.Player;
import cn.nukkit.dialog.element.ElementDialogButton;
import cn.nukkit.dialog.response.FormResponseDialog;
import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.NPCRequestPacket;
import com.smallaswater.npc.utils.Utils;
import com.smallaswater.npc.utils.dialog.element.AdvancedElementDialogButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class AdvancedFormWindowDialog extends cn.nukkit.dialog.window.FormWindowDialog {

    protected BiConsumer<Player, FormResponseDialog> formClosedListener;

    private boolean isClosed = false;

    public AdvancedFormWindowDialog(String title, String content, Entity bindEntity) {
        this(title, content,bindEntity, new ArrayList<>());
    }

    public AdvancedFormWindowDialog(String title, String content, Entity bindEntity, List<cn.nukkit.dialog.element.ElementDialogButton> buttons) {
        super(title, content, bindEntity, buttons);
        if (this.getBindEntity() == null) {
            throw new IllegalArgumentException("bindEntity cannot be null!");
        }
    }

    @Deprecated
    public void addButton(String text) {
        this.addButton(new AdvancedElementDialogButton(text, text));
    }

    public AdvancedElementDialogButton addAdvancedButton(String text) {
        return this.addButton(new AdvancedElementDialogButton(text, text));
    }

    public AdvancedElementDialogButton addButton(AdvancedElementDialogButton button) {
        super.addButton(button);
        return button;
    }

    public AdvancedFormWindowDialog onClosed(@NotNull BiConsumer<Player, FormResponseDialog> listener) {
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

    public static boolean onEvent(@NotNull NPCRequestPacket packet, @NotNull Player player) {
        AdvancedFormWindowDialog dialog = Utils.WINDOW_DIALOG_CACHE.getIfPresent(packet.getSceneName());
        if (dialog == null) {
            return false;
        }

        if (packet.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_CLOSING_COMMANDS) {
            Utils.WINDOW_DIALOG_CACHE.invalidate(packet.getSceneName());
        }

        FormResponseDialog response = new FormResponseDialog(packet, dialog);

        ElementDialogButton clickedButton = response.getClickedButton();
        if (packet.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_ACTION && clickedButton != null) {
            if (clickedButton instanceof AdvancedElementDialogButton advancedButton) {
                advancedButton.callClicked(player, response);
            }
            //点击按钮后，需要关闭当前窗口或者跳转新的窗口，否则对话框会卡住玩家，所以可以认为当前对话框已经关闭
            dialog.isClosed = true;
        }

        if (packet.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_CLOSING_COMMANDS) {
            dialog.callClosed(player, response);
        }
        return true;
    }

}
