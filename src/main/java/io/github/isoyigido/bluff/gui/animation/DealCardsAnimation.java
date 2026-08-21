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

    public DealCardsAnimation(MiddleCardsComponent middleCardsComponent, PlayerCardsOverlay playerCardsOverlay, float individualDuration) {
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

        float halfPi = (float) (Math.PI / 2);

        int idx1 = 0, idx2 = 0, idx3 = 0, idx4 = 0;

        while ((idx1 < bottomX.length) || (idx2 < rightX.length) || (idx3 < topX.length) || (idx4 < leftX.length)) {
            if (idx1 < bottomX.length) this.animationPool.add(new CardAnimation(x1, y1, bottomX[idx1++], bottomY, 0.0f, 0.0f, individualDuration, (idx1 % 4) == 0));
            if (idx2 < rightX.length)  this.animationPool.add(new CardAnimation(x1, y1, rightX[idx2++],  rightY,  0.0f, -halfPi,     individualDuration, false));
            if (idx3 < topX.length)    this.animationPool.add(new CardAnimation(x1, y1, topX[idx3++],    topY,    0.0f, 0.0f, individualDuration, false));
            if (idx4 < leftX.length)   this.animationPool.add(new CardAnimation(x1, y1, leftX[idx4++],   leftY,   0.0f, halfPi,      individualDuration, false));
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