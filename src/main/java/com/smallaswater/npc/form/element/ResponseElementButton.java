package com.smallaswater.npc.form.element;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author lt_name
 */
@SuppressWarnings("unused")
public class ResponseElementButton extends ElementButton {

    private Consumer<Player> clickedListener;

    public ResponseElementButton(String text) {
        super(text);
    }

    public ResponseElementButton(String text, ElementButtonImageData image) {
        super(text, image);
    }

    public ResponseElementButton onClicked(@NotNull Consumer<Player> clickedListener) {
        this.clickedListener = Objects.requireNonNull(clickedListener);
        return this;
    }

    public boolean callClicked(@NotNull Player player) {
        if (this.clickedListener != null) {
            this.clickedListener.accept(player);
            return true;
        }
        return false;
    }

}
