package com.smallaswater.npc.form.windows;

import cn.nukkit.Player;
import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import com.smallaswater.npc.RsNPC;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class AdvancedFormWindowCustom extends FormWindowCustom {

    protected BiConsumer<FormResponseCustom, Player> buttonClickedListener;
    protected Consumer<Player> formClosedListener;

    public AdvancedFormWindowCustom(String title) {
        super(title);
    }

    public AdvancedFormWindowCustom(String title, List<Element> contents) {
        super(title, contents);
    }

    public AdvancedFormWindowCustom(String title, List<Element> contents, String icon) {
        super(title, contents, icon);
    }

    public AdvancedFormWindowCustom(String title, List<Element> contents, ElementButtonImageData icon) {
        super(title, contents, icon);
    }

    public AdvancedFormWindowCustom onResponded(@NotNull BiConsumer<FormResponseCustom, Player> listener) {
        this.buttonClickedListener = listener;
        return this;
    }

    public AdvancedFormWindowCustom onClosed(@NotNull Consumer<Player> listener) {
        this.formClosedListener = Objects.requireNonNull(listener);
        return this;
    }

    protected void callResponded(@NotNull FormResponseCustom formResponseCustom, @NotNull Player player) {
        if (this.buttonClickedListener != null) {
            this.buttonClickedListener.accept(formResponseCustom, player);
        }
    }

    protected void callClosed(@NotNull Player player) {
        if (this.formClosedListener != null) {
            this.formClosedListener.accept(player);
        }
    }

    public static boolean onEvent(@NotNull FormWindow formWindow, @NotNull Player player) {
        if (formWindow instanceof AdvancedFormWindowCustom advancedFormWindowCustom) {
            if (advancedFormWindowCustom.wasClosed() || advancedFormWindowCustom.getResponse() == null) {
                advancedFormWindowCustom.callClosed(player);
            }else {
                advancedFormWindowCustom.callResponded(advancedFormWindowCustom.getResponse(), player);
            }
            return true;
        }
        return false;
    }

    @Override
    public String getJSONData() {
        return RsNPC.GSON.toJson(this, FormWindowCustom.class);
    }

}
