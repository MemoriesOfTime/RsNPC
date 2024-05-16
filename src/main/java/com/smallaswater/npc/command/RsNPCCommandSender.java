package com.smallaswater.npc.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.permission.PermissibleBase;
import cn.nukkit.permission.Permission;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.plugin.Plugin;
import com.smallaswater.npc.entitys.EntityRsNPC;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RsNPCCommandSender implements CommandSender {

    protected PermissibleBase perm = new PermissibleBase(this);
    private final Player initiator;
    private final EntityRsNPC npc;

    public RsNPCCommandSender(EntityRsNPC npc, Player initiator) {
        this.npc = npc;
        this.initiator = initiator;
    }

    public Player getInitiator() {
        return initiator;
    }

    public EntityRsNPC getNpc() {
        return npc;
    }

    @Override
    public void sendMessage(String message) {
    }

    @Override
    public void sendMessage(TextContainer message) {
    }

    @Override
    public Server getServer() {
        return npc.getServer();
    }

    @NotNull
    @Override
    public String getName() {
        return npc.getName();
    }

    public boolean isEntity() {
        return true;
    }

    @Nullable
    public Entity asEntity() {
        return npc;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Nullable
    public Player asPlayer() {
        return null;
    }

    @NotNull
    public Position getPosition() {
        return npc.getPosition();
    }

    @NotNull
    public Location getLocation() {
        return npc.getLocation();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return this.perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return this.perm.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String name) {
        return this.perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return this.perm.hasPermission(permission);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return this.perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name) {
        return this.perm.addAttachment(plugin, name);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, Boolean value) {
        return this.perm.addAttachment(plugin, name, value);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        this.perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        this.perm.recalculatePermissions();
    }

    @Override
    public Map<String, PermissionAttachmentInfo> getEffectivePermissions() {
        return this.perm.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {}
}
