package com.smallaswater.npc.utils.dialog.packet;

import cn.nukkit.network.protocol.DataPacket;
import lombok.ToString;

@ToString
public class NPCRequestPacket extends DataPacket {
    
    public static final byte NETWORK_ID = 0x62;
    
    public long entityRuntimeId;
    
    public RequestType requestType;
    
    public String data = "";
    
    public int skinType = 0;

    
    public String sceneName = "";

    public long getRequestedEntityRuntimeId() {
        return entityRuntimeId;
    }

    public void setRequestedEntityRuntimeId(long entityRuntimeId) {
        this.entityRuntimeId = entityRuntimeId;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSkinType() {
        return skinType;
    }

    public void setSkinType(int skinType) {
        this.skinType = skinType;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public enum RequestType {

        SET_ACTIONS,
        EXECUTE_ACTION,
        EXECUTE_CLOSING_COMMANDS,
        SET_NAME,
        SET_SKIN,
        SET_INTERACTION_TEXT,
        EXECUTE_OPENING_COMMANDS
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.entityRuntimeId = super.getEntityRuntimeId();
        this.requestType = RequestType.values()[this.getByte()];
        this.data = this.getString();
        this.skinType = this.getByte();
        this.sceneName = this.getString();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.entityRuntimeId);
        this.putByte((byte) requestType.ordinal());
        this.putString(this.data);
        this.putByte((byte) this.skinType);
        this.putString(this.sceneName);
    }
}
