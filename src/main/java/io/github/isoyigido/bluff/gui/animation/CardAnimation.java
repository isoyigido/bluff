package io.github.isoyigido.bluff.gui.animation;

import io.github.isoyigido.bluff.gui.ScreenConstants;
import io.github.isoyigido.bluff.gui.audio.Audio;
import io.github.isoyigido.bluff.gui.components.game.PlayerCardsOverlay;
import io.github.isoyigido.bluff.utils.AudioRegistry;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CardAnimation {
    public static final Audio CARD_SFX = AudioRegistry.get("card").orElse(null);

    private static final float AGGRESSIVENESS = 5.0f;

    private static final float POWER_CONSTANT = (float) (Math.exp(-AGGRESSIVENESS));
    private static final float DIVISOR_CONSTANT = 1.0f - (float) (Math.exp(-AGGRESSIVENESS));

    private final BufferedImage image;

    private final int startX;
    private final int startY;

    private final int deltaX;
    private final int deltaY;

    private float x;
    private float y;

    private final float startTheta;
    private final float deltaTheta;

    private float theta;

    private final int updates;

    private int updateCounter = 0;

    private boolean concluded = false;

    private final boolean playAudio;

    private boolean firstUpdate = true;

    public CardAnimation(int x1, int y1, int x2, int y2, float theta1, float theta2, float duration, boolean playAudio) {
        this(PlayerCardsOverlay.BACK_IMAGE, x1, y1, x2, y2, theta1, theta2, duration, playAudio);
    }

    public CardAnimation(BufferedImage image, int x1, int y1, int x2, int y2, float theta1, float theta2, float duration, boolean playAudio) {
        if (duration <= 0) throw new IllegalArgumentException("Card animation duration must be positive.");

        this.image = image;

        this.startX = x1;
        this.startY = y1;

        this.deltaX = x2 - x1;
        this.deltaY = y2 - y1;

        this.x = x1;
        this.y = y1;

        this.startTheta = theta1;
        this.deltaTheta = theta2 - theta1;
        this.theta = theta1;

        this.updates = Math.round(duration * ScreenConstants.UPS);

        this.playAudio = playAudio;
    }

    public void update() {
        if (this.firstUpdate) {
            if (this.playAudio && (CardAnimation.CARD_SFX != null)) CardAnimation.CARD_SFX.play();

            this.firstUpdate = false;
        }

        if (this.concluded) return;

        if (this.updateCounter >= this.updates) {
            this.concluded = true;
            return;
        }

        float t = (float) this.updateCounter / this.updates;

        float factor = (1.0f - (float) Math.pow(CardAnimation.POWER_CONSTANT, t)) / CardAnimation.DIVISOR_CONSTANT;

        this.x = this.startX + (this.deltaX * factor);
        this.y = this.startY + (this.deltaY * factor);

        this.theta = this.startTheta + (this.deltaTheta * factor);

        this.updateCounter++;
    }

    public void render(Graphics2D g) {
        g.rotate(this.theta);

        g.drawImage(this.image, Math.round(this.x), Math.round(this.y), null);
    }

    public boolean isConcluded() {
        return this.concluded;
    }
}