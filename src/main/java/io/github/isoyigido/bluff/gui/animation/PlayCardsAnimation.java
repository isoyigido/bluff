package io.github.isoyigido.bluff.gui.animation;

import io.github.isoyigido.basic.gui.core.Anchor;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.gui.CardImage;
import io.github.isoyigido.bluff.gui.components.game.MiddleCardsComponent;
import io.github.isoyigido.bluff.gui.components.game.PlayerCardsOverlay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class PlayCardsAnimation extends Component {
    private final Collection<CardAnimation> activeAnimations = new ArrayList<>(16);

    private final GameClient gameClient;

    private final MiddleCardsComponent middleCardsComponent;
    private final PlayerCardsOverlay playerCardsOverlay;

    private final int duration;

    public PlayCardsAnimation(GameClient gameClient, MiddleCardsComponent middleCardsComponent, PlayerCardsOverlay playerCardsOverlay, int duration) {
        this.gameClient = gameClient;

        this.middleCardsComponent = middleCardsComponent;
        this.playerCardsOverlay = playerCardsOverlay;

        this.duration = duration;
    }

    public void play(GameClient.Player player, int numberOfCards) {
        Optional<int[]> optX1 = this.playerCardsOverlay.getCardsX(player, player.getNumberOfCards() + numberOfCards);
        Optional<int[]> optX2 = this.playerCardsOverlay.getCardsX(player);

        if (optX1.isEmpty() || optX2.isEmpty()) return;

        int[] x1 = optX1.get();
        int[] x2 = optX2.get();

        this.middleCardsComponent.setNumberOfCards(this.gameClient.getNumberOfCardsInTheMiddle() - numberOfCards);

        int y1 = this.playerCardsOverlay.getCardsY(player);

        float theta1 = this.playerCardsOverlay.getCardsTheta(player);

        Collection<CardAnimation> animations = new ArrayList<>(x1.length);

        for (int i = 0; i < (x1.length - numberOfCards); i++) {
            animations.add(new CardAnimation(x1[i], y1, x2[i], y1, theta1, theta1, this.duration, false));
        }

        int midX = this.middleCardsComponent.getWidget().getX(Anchor.LEFT);
        int midY = this.middleCardsComponent.getWidget().getY(Anchor.TOP);

        for (int i = x1.length - numberOfCards; i < x1.length; i++) {
            animations.add(new CardAnimation(x1[i], y1, midX, midY, theta1, 0.0f, this.duration, false));
        }

        this.activeAnimations.addAll(animations);

        this.playerCardsOverlay.hidePlayer(player);

        if (CardAnimation.CARD_SFX != null) CardAnimation.CARD_SFX.play();
    }

    public void play(Collection<Card> cards) {
        GameClient.Player player = this.gameClient.getThisPlayer();

        Optional<int[]> optX1 = this.playerCardsOverlay.getCardsX(player, player.getNumberOfCards() + cards.size());
        Optional<int[]> optX2 = this.playerCardsOverlay.getCardsX(player);

        if (optX1.isEmpty() || optX2.isEmpty()) return;

        int[] x1 = optX1.get();
        int[] x2 = optX2.get();

        List<Card> remainingCards = this.playerCardsOverlay.getThisCards();

        List<Card> allCards = new ArrayList<>(remainingCards);
        allCards.addAll(cards);
        allCards.sort(Comparator.comparing(Card::rank));

        Collection<Integer> playedCardIndices = new ArrayList<>(cards.size());

        for (Card playedCard : cards) {
            playedCardIndices.add(allCards.indexOf(playedCard));
        }

        List<Integer> remainingCardIndices = new ArrayList<>(remainingCards.size());

        for (Card remainingCard : remainingCards) {
            remainingCardIndices.add(allCards.indexOf(remainingCard));
        }

        this.middleCardsComponent.setNumberOfCards(this.gameClient.getNumberOfCardsInTheMiddle() - cards.size());

        int y1 = this.playerCardsOverlay.getCardsY(player);

        Collection<CardAnimation> animations = new ArrayList<>(allCards.size());

        BufferedImage[] cardImages = new BufferedImage[remainingCardIndices.size()];

        for (int i = 0; i < cardImages.length; i++) {
            cardImages[i] = CardImage.of(remainingCards.get(i), PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT);
        }

        for (int i = 0; i < remainingCardIndices.size(); i++) {
            animations.add(new CardAnimation(cardImages[i], x1[remainingCardIndices.get(i)], y1, x2[i], y1, 0.0f, 0.0f, this.duration, false));
        }

        int midX = this.middleCardsComponent.getWidget().getX(Anchor.LEFT);
        int midY = this.middleCardsComponent.getWidget().getY(Anchor.TOP);

        for (Integer playedCardIndex : playedCardIndices) {
            animations.add(new CardAnimation(x1[playedCardIndex], y1, midX, midY, 0.0f, 0.0f, this.duration, false));
        }

        this.activeAnimations.addAll(animations);

        this.playerCardsOverlay.hidePlayer(player);

        if (CardAnimation.CARD_SFX != null) CardAnimation.CARD_SFX.play();
    }

    @Override
    public void update() {
        this.activeAnimations.forEach(CardAnimation::update);

        boolean updated = false;

        for (CardAnimation animation : new ArrayList<>(this.activeAnimations)) {
            if (animation.isConcluded()) {
                this.activeAnimations.remove(animation);

                if (!updated) {
                    this.playerCardsOverlay.showAll();

                    this.middleCardsComponent.updateCards();

                    updated = true;
                }
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        new ArrayList<>(this.activeAnimations).forEach(cardAnimation -> cardAnimation.render((Graphics2D) g.create()));
    }
}