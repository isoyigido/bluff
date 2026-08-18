package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.gui.CardImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PlayerCardsOverlay extends Component {
    public static final int CARD_WIDTH = 120;
    public static final int CARD_HEIGHT = 168;

    public static final BufferedImage BACK_IMAGE = CardImage.back(PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT);

    private final GameClient gameClient;

    private final int margin;

    private final int[] otherPlayerCardNumbers;

    private int numberOfOtherPlayers = 0;

    private final CardSelectionPanel cardSelectionPanel;

    public PlayerCardsOverlay(GameClient gameClient, ActionPanel actionPanel, int margin) {
        super(ScreenConfig.screenWidth, ScreenConfig.screenHeight);

        this.gameClient = gameClient;

        this.margin = margin;

        this.otherPlayerCardNumbers = new int[4];

        this.cardSelectionPanel = new CardSelectionPanel(actionPanel, 1280, 24);

        this.updatePlayerCards();

        super.addWidget(this.cardSelectionPanel.bottom(ScreenConfig.xCenter, ScreenConfig.screenHeight - margin));
    }

    public void updatePlayerCards() {
        this.cardSelectionPanel.setCards(this.gameClient.getThisCards());

        List<GameClient.Player> otherPlayers = new ArrayList<>(this.gameClient.getOtherPlayers().sequencedValues());

        this.numberOfOtherPlayers = otherPlayers.size();

        for (int i = 0; i < this.otherPlayerCardNumbers.length; i++) {
            this.otherPlayerCardNumbers[i] = (i < this.numberOfOtherPlayers) ? otherPlayers.get(i).getNumberOfCards() : 0;
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (this.numberOfOtherPlayers == 0) return;

        int leftIndex = 0;
        int topIndex = 1;
        int rightIndex = 2;

        switch (this.numberOfOtherPlayers) {
            case 1 -> {
                topIndex = 0;
                leftIndex = 1;
            }
            case 2 -> {
                rightIndex = 1;
                topIndex = 2;
            }
        }

        int gap = 20;

        // --- TOP (OTHER PLAYER 3) ---
        int width = ((this.otherPlayerCardNumbers[topIndex] - 1) * gap) + PlayerCardsOverlay.CARD_WIDTH;

        int x = ScreenConfig.xCenter - (width / 2);

        for (int i = 0; i < this.otherPlayerCardNumbers[topIndex]; i++) {
            g.drawImage(PlayerCardsOverlay.BACK_IMAGE, x, this.margin, null);

            x += gap;
        }

        // --- LEFT (OTHER PLAYER 1) ---
        g.rotate(Math.PI / 2);

        int y = -this.margin - PlayerCardsOverlay.CARD_HEIGHT;

        width = ((this.otherPlayerCardNumbers[leftIndex] - 1) * gap) + PlayerCardsOverlay.CARD_WIDTH;

        x = ScreenConfig.yCenter - (width / 2);

        for (int i = 0; i < this.otherPlayerCardNumbers[leftIndex]; i++) {
            g.drawImage(PlayerCardsOverlay.BACK_IMAGE, x, y, null);

            x += gap;
        }

        // --- RIGHT (OTHER PLAYER 2) ---
        g.rotate(-Math.PI);

        y = ScreenConfig.screenWidth - this.margin - PlayerCardsOverlay.CARD_HEIGHT;

        width = ((this.otherPlayerCardNumbers[rightIndex] - 1) * gap) + PlayerCardsOverlay.CARD_WIDTH;

        x = -ScreenConfig.yCenter - (width / 2);

        for (int i = 0; i < this.otherPlayerCardNumbers[rightIndex]; i++) {
            g.drawImage(PlayerCardsOverlay.BACK_IMAGE, x, y, null);

            x += gap;
        }

        // - Fix rotation -
        g.rotate(Math.PI / 2);
    }
}