package io.github.isoyigido.bluff.gui.overlays;

import io.github.isoyigido.basic.gui.core.Overlay;
import io.github.isoyigido.basic.gui.window.ScreenConfig;

import java.awt.*;

public final class LoadingOverlay implements Overlay {
    // --- Colors ---
    private static final Color BG_COLOR = new Color(0, 0, 0, 150);
    private static final Color LOADING_CIRCLE_BG_COLOR = new Color(120, 120, 120);
    private static final Color LOADING_CIRCLE_SPINNER_COLOR = new Color(255, 255, 255);

    // --- Custom parameters ---
    private static final int LOADING_CIRCLE_SIZE = 80;

    // --- Loading circle parameters ---
    private final int loadingCircleX = ScreenConfig.xCenter - (LoadingOverlay.LOADING_CIRCLE_SIZE / 2);
    private final int loadingCircleY = ScreenConfig.yCenter - (LoadingOverlay.LOADING_CIRCLE_SIZE / 2);

    private int angle;

    public LoadingOverlay() {
        this.angle = 0;
    }

    @Override
    public void render(Graphics2D g) {
        // --- Render the semi-transparent background ---
        g.setColor(LoadingOverlay.BG_COLOR);
        g.fillRect(0, 0, ScreenConfig.screenWidth, ScreenConfig.screenHeight);

        // --- Render the loading circle ---
        // Increase stroke for a thicker loading circle
        g.setStroke(new BasicStroke(5));
        // Set loading circle background color
        g.setColor(LoadingOverlay.LOADING_CIRCLE_BG_COLOR);
        // Draw the loading circle background
        g.drawOval(this.loadingCircleX, this.loadingCircleY, LoadingOverlay.LOADING_CIRCLE_SIZE, LoadingOverlay.LOADING_CIRCLE_SIZE);
        // Set loading circle spinner color
        g.setColor(LoadingOverlay.LOADING_CIRCLE_SPINNER_COLOR);
        // Draw the loading circle spinner
        g.drawArc(this.loadingCircleX, this.loadingCircleY, LoadingOverlay.LOADING_CIRCLE_SIZE, LoadingOverlay.LOADING_CIRCLE_SIZE, this.angle, 90);
        // Set default stroke
        g.setStroke(new BasicStroke(1));
    }

    @Override
    public void update() {
        // Update the angle for spin animation
        this.angle = (this.angle - 5) % 360;
    }
}