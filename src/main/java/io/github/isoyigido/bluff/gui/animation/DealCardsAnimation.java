package io.github.isoyigido.bluff.gui.animation;

import io.github.isoyigido.basic.gui.core.Anchor;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.bluff.gui.components.game.MiddleCardsComponent;
import io.github.isoyigido.bluff.gui.components.game.PlayerCardsOverlay;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DealCardsAnimation extends Component {
    private final List<CardAnimation> animationPool;
    private final List<CardAnimation> addedAnimations;
    private final List<CardAnimation> activeAnimations;

    private int animationIndex = 0;

    private boolean addedAllAnimations = false;
    private boolean concluded = false;

    private final MiddleCardsComponent middleCardsComponent;
    private final PlayerCardsOverlay playerCardsOverlay;

    public DealCardsAnimation(MiddleCardsComponent middleCardsComponent, PlayerCardsOverlay playerCardsOverlay, int individualDuration) {
        this.middleCardsComponent = middleCardsComponent;
        this.playerCardsOverlay = playerCardsOverlay;

        playerCardsOverlay.getWidget().hide();

        int x1 = middleCardsComponent.getWidget().getX(Anchor.LEFT);
        int y1 = middleCardsComponent.getWidget().getY(Anchor.TOP);

        int[] bottomX = playerCardsOverlay.getBottomX();
        int bottomY = playerCardsOverlay.getBottomY();

        int[] rightX = playerCardsOverlay.getRightX();
        int rightY = playerCardsOverlay.getRightY();

        int[] topX = playerCardsOverlay.getTopX();
        int topY = playerCardsOverlay.getTopY();

        int[] leftX = playerCardsOverlay.getLeftX();
        int leftY = playerCardsOverlay.getLeftY();

        int totalNumberOfAnimations = rightX.length + topX.length + leftX.length;

        this.animationPool = new ArrayList<>(totalNumberOfAnimations);
        this.addedAnimations = new ArrayList<>(totalNumberOfAnimations);
        this.activeAnimations = new ArrayList<>(totalNumberOfAnimations);

        boolean playAudio = true;

        for (int x : bottomX) {
            this.animationPool.add(new CardAnimation(x1, y1, x, bottomY, 0.0f, 0.0f, individualDuration, playAudio));
            playAudio = false;
        }

        playAudio = true;

        float halfPi = (float) (Math.PI / 2);

        for (int x : rightX) {
            this.animationPool.add(new CardAnimation(x1, y1, x, rightY, 0.0f, -halfPi, individualDuration, playAudio));
            playAudio = false;
        }

        playAudio = true;

        for (int x : topX) {
            this.animationPool.add(new CardAnimation(x1, y1, x, topY, 0.0f, 0.0f, individualDuration, playAudio));
            playAudio = false;
        }

        playAudio = true;

        for (int x : leftX) {
            this.animationPool.add(new CardAnimation(x1, y1, x, leftY, 0.0f, halfPi, individualDuration, playAudio));
            playAudio = false;
        }
    }

    @Override
    public void update() {
        if (!this.addedAllAnimations) {
            if (this.animationIndex >= this.animationPool.size()) this.addedAllAnimations = true;
            else {
                CardAnimation cardAnimation = this.animationPool.get(this.animationIndex++);
                this.addedAnimations.add(cardAnimation);
                this.activeAnimations.add(cardAnimation);
            }

            this.middleCardsComponent.setNumberOfCards(this.animationPool.size() - this.animationIndex);
        }

        if (this.concluded) return;

        this.activeAnimations.forEach(CardAnimation::update);

        this.activeAnimations.removeIf(CardAnimation::isConcluded);

        if (this.addedAllAnimations && this.activeAnimations.isEmpty()) {
            this.concluded = true;

            this.middleCardsComponent.updateCards();
            this.playerCardsOverlay.getWidget().show();

            super.getWidget().hide();
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (this.concluded) return;

        new ArrayList<>(this.addedAnimations).forEach(cardAnimation -> cardAnimation.render((Graphics2D) g.create()));
    }
}