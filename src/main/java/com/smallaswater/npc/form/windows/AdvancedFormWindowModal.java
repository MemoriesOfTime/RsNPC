package com.smallaswater.npc.form.windows;

import cn.nukkit.Player;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowModal;
import com.smallaswater.npc.RsNPC;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class AdvancedFormWindowModal extends FormWindowModal {

    protected Consumer<Player> buttonTrueClickedListener, buttonFalseClickedListener, formClosedListener;

    public AdvancedFormWindowModal(String title, String content, String trueButtonText, String falseButtonText) {
        super(title, content, trueButtonText, falseButtonText);
    }

    public AdvancedFormWindowModal onClickedTrue(@NotNull Consumer<Player> listener) {
        this.buttonTrueClickedListener = Objects.requireNonNull(listener);
        return this;
    }

    public AdvancedFormWindowModal onClickedFalse(@NotNull Consumer<Player> listener) {
        this.buttonFalseClickedListener = Objects.requireNonNull(listener);
        return this;
    }

    public AdvancedFormWindowModal onClosed(@NotNull Consumer<Player> listener) {
        this.formClosedListener = Objects.requireNonNull(listener);
        return this;
    }

    protected void callClickedTrue(@NotNull Player player) {
        if (this.buttonTrueClickedListener != null) {
            this.buttonTrueClickedListener.accept(player);
        }
    }

    protected void callClickedFalse(@NotNull Player player) {
        if (this.buttonFalseClickedListener != null) {
            this.buttonFalseClickedListener.accept(player);
        }
    }

    protected void callClosed(@NotNull Player player) {
        if (this.formClosedListener != null) {
            this.formClosedListener.accept(player);
        }
    }

    public static boolean onEvent(@NotNull FormWindow formWindow, @NotNull Player player) {
        if (formWindow instanceof AdvancedFormWindowModal advancedFormWindowModal) {
            if (advancedFormWindowModal.wasClosed() || advancedFormWindowModal.getResponse() == null) {
                advancedFormWindowModal.callClosed(player);
            }else {
                if (advancedFormWindowModal.getResponse().getClickedButtonId() == 0) {
                    advancedFormWindowModal.callClickedTrue(player);
                }else {
                    advancedFormWindowModal.callClickedFalse(player);
                }
            }
            return true;
        }
        return false;
    }

    public String getJSONData() {
        return RsNPC.GSON.toJson(this, FormWindowModal.class);
    }

}
