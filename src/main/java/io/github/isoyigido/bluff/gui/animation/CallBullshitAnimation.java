package io.github.isoyigido.bluff.gui.animation;

import io.github.isoyigido.basic.gui.core.Anchor;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.gui.CardImage;
import io.github.isoyigido.bluff.gui.ScreenConstants;
import io.github.isoyigido.bluff.gui.audio.Audio;
import io.github.isoyigido.bluff.gui.components.game.MiddleCardsComponent;
import io.github.isoyigido.bluff.gui.components.game.PlayerCardsOverlay;
import io.github.isoyigido.bluff.utils.AudioRegistry;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class CallBullshitAnimation extends Component {
    private final List<AnimationPack> activeAnimations = new ArrayList<>(4);

    private final GameClient gameClient;

    private final MiddleCardsComponent middleCardsComponent;
    private final PlayerCardsOverlay playerCardsOverlay;

    private final float cardRevealDuration;
    private final float waitDuration;
    private final float gatherCardsDuration;

    public CallBullshitAnimation(GameClient gameClient, MiddleCardsComponent middleCardsComponent, PlayerCardsOverlay playerCardsOverlay, float cardRevealDuration, float waitDuration, float gatherCardsDuration) {
        this.gameClient = gameClient;

        this.middleCardsComponent = middleCardsComponent;
        this.playerCardsOverlay = playerCardsOverlay;

        this.cardRevealDuration = cardRevealDuration;
        this.waitDuration = waitDuration;
        this.gatherCardsDuration = gatherCardsDuration;
    }

    public void callBullshit(GameClient.Player accuser, GameClient.Player accused, List<Card> playedCards, boolean bluff, int numberOfMiddleCards, List<Card> middleCards) {
        AudioRegistry.get("referee").ifPresent(Audio::play);

        this.middleCardsComponent.callBullshit(accuser, accused, bluff);

        this.activeAnimations.add(new AnimationPack(accuser, accused, playedCards, bluff, numberOfMiddleCards, middleCards));
    }

    @Override
    public void update() {
        new ArrayList<>(this.activeAnimations).forEach(AnimationPack::update);

        this.activeAnimations.removeIf(AnimationPack::isConcluded);
    }

    @Override
    public void render(Graphics2D g) {
        new ArrayList<>(this.activeAnimations).forEach(animation -> animation.render(g));
    }

    private final class AnimationPack {
        private boolean concluded = false;

        private final AnimationStage revealCardsAnimation;
        private final AnimationStage gatherCardsAnimation;

        private AnimationPack(GameClient.Player accuser, GameClient.Player accused, List<Card> playedCards, boolean bluff, int numberOfMiddleCards, List<Card> middleCards) {
            this.revealCardsAnimation = new RevealCardsAnimation(numberOfMiddleCards, playedCards);

            GameClient.Player wrong = bluff ? accused : accuser;

            this.gatherCardsAnimation = new GatherCardsAnimation(wrong, numberOfMiddleCards, middleCards);
        }

        private void update() {
            if (this.concluded) return;

            if (!this.revealCardsAnimation.isConcluded()) {
                this.revealCardsAnimation.update();

                return;
            }

            if (!this.gatherCardsAnimation.isConcluded()) {
                this.gatherCardsAnimation.update();

                return;
            }

            this.concluded = true;
        }

        private void render(Graphics2D g) {
            if (!this.gatherCardsAnimation.isConcluded()) this.gatherCardsAnimation.render(g);
            if (!this.revealCardsAnimation.isConcluded()) this.revealCardsAnimation.render(g);
        }

        private boolean isConcluded() {
            return this.concluded;
        }
    }

    private abstract static class AnimationStage {
        private boolean concluded = false;

        protected void conclude() {
            this.concluded = true;
        }

        protected boolean isConcluded() {
            return this.concluded;
        }

        abstract void update();
        abstract void render(Graphics2D g);
    }

    private final class RevealCardsAnimation extends AnimationStage {
        private final Collection<CardAnimation> activeAnimations = new ArrayList<>(8);

        private final int totalUpdates;

        private int currentUpdates = 0;

        private RevealCardsAnimation(int numberOfMiddleCards, Collection<Card> playedCards) {
            CallBullshitAnimation.this.middleCardsComponent.setNumberOfCards(numberOfMiddleCards - playedCards.size());

            int midX = CallBullshitAnimation.this.middleCardsComponent.getWidget().getX(Anchor.LEFT);
            int midY = CallBullshitAnimation.this.middleCardsComponent.getWidget().getY(Anchor.TOP);

            int x2 = midX;

            for (Card playedCard : playedCards) {
                BufferedImage cardImage = CardImage.of(playedCard, PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT);
                this.activeAnimations.add(new CardAnimation(cardImage, midX, midY, x2, midY, 0.0f, 0.0f, CallBullshitAnimation.this.cardRevealDuration, false));
                x2 += PlayerCardsOverlay.CARD_WIDTH + 10;
            }

            this.totalUpdates = (int) ((CallBullshitAnimation.this.cardRevealDuration + CallBullshitAnimation.this.waitDuration) * ScreenConstants.UPS);
        }

        @Override
        void update() {
            if (this.isConcluded()) return;

            if (this.currentUpdates >= this.totalUpdates) this.conclude();

            this.activeAnimations.forEach(CardAnimation::update);

            this.currentUpdates++;
        }

        @Override
        void render(Graphics2D g) {
            this.activeAnimations.forEach(cardAnimation -> cardAnimation.render((Graphics2D) g.create()));
        }
    }

    private final class GatherCardsAnimation extends AnimationStage {
        private final Collection<CardAnimation> activeAnimations = new ArrayList<>(8);

        private GatherCardsAnimation(GameClient.Player player, int numberOfMiddleCards, Collection<Card> middleCards) {
            if (middleCards == null) this.otherPlayerGathersCards(player, numberOfMiddleCards);
            else this.thisPlayerGathersCards(player, middleCards);
        }

        private void otherPlayerGathersCards(GameClient.Player player, int numberOfCards) {
            Optional<int[]> optX1 = CallBullshitAnimation.this.playerCardsOverlay.getCardsX(player, player.getNumberOfCards() - numberOfCards);
            Optional<int[]> optX2 = CallBullshitAnimation.this.playerCardsOverlay.getCardsX(player);

            if (optX1.isEmpty() || optX2.isEmpty()) return;

            int[] x1 = optX1.get();
            int[] x2 = optX2.get();

            CallBullshitAnimation.this.middleCardsComponent.setNumberOfCards(CallBullshitAnimation.this.gameClient.getNumberOfCardsInTheMiddle() - numberOfCards);

            int y2 = CallBullshitAnimation.this.playerCardsOverlay.getCardsY(player);

            float theta2 = CallBullshitAnimation.this.playerCardsOverlay.getCardsTheta(player);

            Collection<CardAnimation> animations = new ArrayList<>(x1.length);

            for (int i = 0; i < x1.length; i++) {
                animations.add(new CardAnimation(x1[i], y2, x2[i], y2, theta2, theta2, CallBullshitAnimation.this.gatherCardsDuration, false));
            }

            int midX = CallBullshitAnimation.this.middleCardsComponent.getWidget().getX(Anchor.LEFT);
            int midY = CallBullshitAnimation.this.middleCardsComponent.getWidget().getY(Anchor.TOP);

            for (int i = x1.length; i < x2.length; i++) {
                animations.add(new CardAnimation(midX, midY, x2[i], y2, 0.0f, theta2, CallBullshitAnimation.this.gatherCardsDuration, false));
            }

            this.activeAnimations.addAll(animations);

            CallBullshitAnimation.this.playerCardsOverlay.hidePlayer(player);
        }

        private void thisPlayerGathersCards(GameClient.Player player, Collection<Card> middleCards) {
            int numberOfCards = middleCards.size();

            Optional<int[]> optX1 = CallBullshitAnimation.this.playerCardsOverlay.getCardsX(player, player.getNumberOfCards() - numberOfCards);
            Optional<int[]> optX2 = CallBullshitAnimation.this.playerCardsOverlay.getCardsX(player);

            if (optX1.isEmpty() || optX2.isEmpty()) return;

            int[] x1 = optX1.get();
            int[] x2 = optX2.get();

            List<Card> allCards = CallBullshitAnimation.this.playerCardsOverlay.getThisCards();

            List<Card> previousCards = new ArrayList<>(allCards);
            previousCards.removeAll(middleCards);
            previousCards.sort(Comparator.comparing(Card::rank));

            Collection<Integer> gatheredCardIndices = new ArrayList<>(numberOfCards);

            for (Card gatheredCard : middleCards) {
                gatheredCardIndices.add(allCards.indexOf(gatheredCard));
            }

            List<Integer> previousCardIndices = new ArrayList<>(previousCards.size());

            for (Card previousCard : previousCards) {
                previousCardIndices.add(allCards.indexOf(previousCard));
            }

            CallBullshitAnimation.this.middleCardsComponent.setNumberOfCards(0);

            int y2 = CallBullshitAnimation.this.playerCardsOverlay.getCardsY(player);

            Collection<CardAnimation> animations = new ArrayList<>(allCards.size());

            BufferedImage[] cardImages = new BufferedImage[previousCards.size()];

            for (int i = 0; i < cardImages.length; i++) {
                cardImages[i] = CardImage.of(previousCards.get(i), PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT);
            }

            for (int i = 0; i < previousCards.size(); i++) {
                animations.add(new CardAnimation(cardImages[i], x1[i], y2, x2[previousCardIndices.get(i)], y2, 0.0f, 0.0f, CallBullshitAnimation.this.gatherCardsDuration, false));
            }

            int midX = CallBullshitAnimation.this.middleCardsComponent.getWidget().getX(Anchor.LEFT);
            int midY = CallBullshitAnimation.this.middleCardsComponent.getWidget().getY(Anchor.TOP);

            for (Integer gatheredCardIndex : gatheredCardIndices) {
                animations.add(new CardAnimation(midX, midY, x2[gatheredCardIndex], y2, 0.0f, 0.0f, CallBullshitAnimation.this.gatherCardsDuration, false));
            }

            this.activeAnimations.addAll(animations);

            CallBullshitAnimation.this.playerCardsOverlay.hidePlayer(player);
        }

        @Override
        void update() {
            this.activeAnimations.forEach(CardAnimation::update);

            boolean updated = false;

            for (CardAnimation animation : new ArrayList<>(this.activeAnimations)) {
                if (animation.isConcluded()) {
                    this.activeAnimations.remove(animation);

                    if (!updated) {
                        CallBullshitAnimation.this.playerCardsOverlay.showAll();

                        CallBullshitAnimation.this.middleCardsComponent.updateCards();

                        updated = true;

                        super.conclude();
                    }
                }
            }
        }

        @Override
        void render(Graphics2D g) {
            new ArrayList<>(this.activeAnimations).forEach(cardAnimation -> cardAnimation.render((Graphics2D) g.create()));
        }
    }
}