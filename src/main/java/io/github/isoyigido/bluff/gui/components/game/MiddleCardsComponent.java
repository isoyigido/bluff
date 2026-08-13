package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.core.Widget;
import io.github.isoyigido.basic.gui.core.components.ImageComponent;
import io.github.isoyigido.basic.gui.core.components.TextComponent;
import io.github.isoyigido.bluff.game.client.GameClient;

import java.awt.*;

public class MiddleCardsComponent extends Component {
    private final GameClient gameClient;

    private final Widget widget;
    private final TextComponent cardCount;

    public MiddleCardsComponent(GameClient gameClient) {
        super(PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT);

        this.gameClient = gameClient;

        ImageComponent cardImage = new ImageComponent(PlayerCardsOverlay.BACK_IMAGE);

        Color textColor = Color.BLUE;
        Font textFont = Theme.getFont(48, true, false);

        this.cardCount = new TextComponent("0", textColor, textFont);

        cardImage.addWidget(this.cardCount.center(cardImage.getWidth() / 2, cardImage.getHeight() / 2));

        this.widget = cardImage.topLeft(0, 0).hide();

        super.addWidget(this.widget);

        this.updateCards();
    }

    public void updateCards() {
        int numberOfCards = this.gameClient.getNumberOfCardsInTheMiddle();

        this.cardCount.setText(Integer.toString(numberOfCards));

        this.widget.setVisible(numberOfCards > 0);
    }
}