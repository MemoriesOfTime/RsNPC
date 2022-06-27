package com.smallaswater.npc.utils.dialog.response;

import com.smallaswater.npc.utils.dialog.element.AdvancedElementDialogButton;
import com.smallaswater.npc.utils.dialog.packet.NPCRequestPacket;
import com.smallaswater.npc.utils.dialog.window.AdvancedFormWindowDialog;
import lombok.Getter;

@Getter
public class FormResponseDialog {

    private long entityRuntimeId;
    private String data;
    private AdvancedElementDialogButton clickedButton;//can be null
    private String sceneName;
    private NPCRequestPacket.RequestType requestType;
    private int skinType;

    public FormResponseDialog(NPCRequestPacket packet, AdvancedFormWindowDialog dialog) {
        this.entityRuntimeId = packet.getRequestedEntityRuntimeId();
        this.data = packet.getData();
        try {
            this.clickedButton = dialog.getButtons().get(packet.getSkinType());
        }catch (IndexOutOfBoundsException e){
            this.clickedButton = null;
        }
        this.sceneName = packet.getSceneName();
        this.requestType = packet.getRequestType();
        this.skinType = packet.getSkinType();
    }
}
