package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.MouseButton;
import io.github.isoyigido.bluff.game.cards.Rank;
import io.github.isoyigido.bluff.gui.components.Button;

import java.awt.event.MouseWheelEvent;
import java.util.function.Consumer;

public class RankSelectionComponent extends Button {
    private final String[] ranks = new String[]{
            Translator.get("cards.ace") + " (A)",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            Translator.get("cards.jack") + " (J)",
            Translator.get("cards.queen") + " (Q)",
            Translator.get("cards.king") + " (K)",
    };

    private int selectedIndex = 0;

    public RankSelectionComponent(int width, int height, int arc, Consumer<Rank> onSelection) {
        super(
                width, height,
                arc,
                true,
                "",
                null,
                () -> {}
        );

        this.setSelectedIndex(0);

        super.setMouseButtonAction(MouseButton.LEFT, () -> {
            onSelection.accept(Rank.values()[this.selectedIndex]);
            this.setSelectedIndex(0);
        });
    }

    @Override
    public void mouseWheelEvent(MouseWheelEvent e) {
        if (!super.isActive()) return;

        this.setSelectedIndex(Math.clamp(this.selectedIndex - e.getWheelRotation(), 0, this.ranks.length - 1));
    }

    private void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        this.updateLabel();
    }

    private void updateLabel() {
        super.setLabel(this.ranks[this.selectedIndex]);
    }
}