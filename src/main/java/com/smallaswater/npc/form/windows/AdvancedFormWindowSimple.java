package com.smallaswater.npc.form.windows;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import com.smallaswater.npc.RsNPC;
import com.smallaswater.npc.form.element.ResponseElementButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class AdvancedFormWindowSimple extends FormWindowSimple {

    protected BiConsumer<ElementButton, Player> buttonClickedListener;
    protected Consumer<Player> formClosedListener;

    public AdvancedFormWindowSimple(String title) {
        this(title, "");
    }

    public AdvancedFormWindowSimple(String title, String content) {
        super(title, content);
    }

    public AdvancedFormWindowSimple(String title, String content, List<ElementButton> buttons) {
        super(title, content, buttons);
    }

    public void addButton(String text, Consumer<Player> listener) {
        this.addButton(new ResponseElementButton(text).onClicked(listener));
    }

    public AdvancedFormWindowSimple onClicked(@NotNull BiConsumer<ElementButton, Player> listener) {
        this.buttonClickedListener = Objects.requireNonNull(listener);
        return this;
    }

    public AdvancedFormWindowSimple onClosed(@NotNull Consumer<Player> listener) {
        this.formClosedListener = Objects.requireNonNull(listener);
        return this;
    }

    protected void callClicked(@NotNull ElementButton elementButton, @NotNull Player player) {
        if (this.buttonClickedListener != null) {
            this.buttonClickedListener.accept(elementButton, player);
        }
    }

    protected void callClosed(@NotNull Player player) {
        if (this.formClosedListener != null) {
            this.formClosedListener.accept(player);
        }
    }

    public static boolean onEvent(@NotNull FormWindow formWindow, @NotNull Player player) {
        if (formWindow instanceof AdvancedFormWindowSimple) {
            AdvancedFormWindowSimple advancedFormWindowSimple = (AdvancedFormWindowSimple) formWindow;
            if (advancedFormWindowSimple.wasClosed() || advancedFormWindowSimple.getResponse() == null) {
                advancedFormWindowSimple.callClosed(player);
            }else {
                ElementButton elementButton = advancedFormWindowSimple.getResponse().getClickedButton();
                if (elementButton instanceof ResponseElementButton && ((ResponseElementButton) elementButton).callClicked(player)) {
                    return true;
                }else {
                    advancedFormWindowSimple.callClicked(elementButton, player);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String getJSONData() {
        return RsNPC.GSON.toJson(this, FormWindowSimple.class);
    }

}
