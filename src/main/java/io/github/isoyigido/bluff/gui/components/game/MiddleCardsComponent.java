package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.core.Widget;
import io.github.isoyigido.basic.gui.core.components.ImageComponent;
import io.github.isoyigido.basic.gui.core.components.TextComponent;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import io.github.isoyigido.bluff.game.client.GameClient;

import java.awt.*;
import java.util.List;
import java.util.Locale;

public class MiddleCardsComponent extends Component {
    private final Color textColor = Theme.getColor("text");

    private final GameClient gameClient;

    private final Widget widget;
    private final TextComponent cardCount;

    private final TextComponent actionText;

    public MiddleCardsComponent(GameClient gameClient, int gap) {
        this.gameClient = gameClient;

        ImageComponent cardImage = new ImageComponent(PlayerCardsOverlay.BACK_IMAGE);

        Color textColor = Color.BLUE;
        Font textFont = Theme.getFont(48, true, false);

        this.cardCount = new TextComponent("0", textColor, textFont);

        cardImage.addWidget(this.cardCount.center(cardImage.getWidth() / 2, cardImage.getHeight() / 2));

        this.widget = cardImage.topLeft(0, 0).hide();

        super.addWidget(this.widget);

        this.actionText = new TextComponent(
                "...",
                this.textColor,
                Theme.getFont(36, true, false)
        );

        super.addWidget(this.actionText.top(cardImage.getWidth() / 2, cardImage.getHeight() + gap));

        this.updateCards();

        super.setDimensions(PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT + gap + this.actionText.getHeight());
    }

    public void updateCards() {
        int numberOfCards = this.gameClient.getNumberOfCardsInTheMiddle();

        this.cardCount.setText(Integer.toString(numberOfCards));

        this.widget.setVisible(numberOfCards > 0);
    }

    public void playCards(GameClient.Player player, Rank rank, int numberOfCards) {
        this.actionText.setText(Translator.get("game.played_cards").formatted(player.getName(), numberOfCards, MiddleCardsComponent.getTranslatedRankName(rank)));
        this.actionText.setColor(this.textColor);
    }

    public void callBullshit(GameClient.Player accuser, GameClient.Player accused, List<Card> cards, boolean bluff) {
        this.actionText.setText((bluff ? Translator.get("game.called_bullshit.right") : Translator.get("game.called_bullshit.wrong")).formatted(accuser.getName(), accused.getName()));
        this.actionText.setColor(Color.RED);
    }

    private static String getTranslatedRankName(Rank rank) {
        return Translator.get("cards." + rank.name().toLowerCase(Locale.ENGLISH)).toLowerCase();
    }
}