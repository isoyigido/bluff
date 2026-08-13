package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.utils.ImageUtils;

import java.awt.image.BufferedImage;

public final class CardImage extends Component {
    private CardImage() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    public static BufferedImage of(Card card, int width, int height) {
        char suitChar = switch(card.suit()) {
            case SPADES -> 'S';
            case CLUBS -> 'C';
            case HEARTS -> 'H';
            case DIAMONDS -> 'D';
        };

        char rankChar = switch (card.rank()) {
            case ACE -> 'A';
            case TWO -> '2';
            case THREE -> '3';
            case FOUR -> '4';
            case FIVE -> '5';
            case SIX -> '6';
            case SEVEN -> '7';
            case EIGHT -> '8';
            case NINE -> '9';
            case TEN -> 'T';
            case JACK -> 'J';
            case QUEEN -> 'Q';
            case KING -> 'K';
        };

        String path = "/game/cards/" + suitChar + rankChar + ".png";

        return ImageUtils.readImage(path).map(image -> ImageUtils.resizeImage(image, width, height, true)).orElse(empty(width, height));
    }

    public static BufferedImage back(int width, int height) {
        return ImageUtils.readImage("/game/cards/BACK.png").map(image -> ImageUtils.resizeImage(image, width, height, true)).orElse(empty(width, height));
    }

    private static BufferedImage empty(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
}