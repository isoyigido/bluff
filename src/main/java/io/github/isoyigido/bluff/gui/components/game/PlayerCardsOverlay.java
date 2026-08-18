package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.core.Anchor;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.gui.CardImage;
import io.github.isoyigido.bluff.utils.LoopingIterator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerCardsOverlay extends Component {
    public static final int CARD_WIDTH = 120;
    public static final int CARD_HEIGHT = 168;

    public static final BufferedImage BACK_IMAGE = CardImage.back(PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT);

    private final GameClient gameClient;

    private final int gap;

    private final CardSelectionPanel cardSelectionPanel;

    private int numberOfPlayers;

    private int[] bottomX;
    private int[] rightX;
    private int[] topX;
    private int[] leftX;

    private final int bottomY;
    private final int rightY;
    private final int topY;
    private final int leftY;

    public PlayerCardsOverlay(GameClient gameClient, ActionPanel actionPanel, int margin, int gap) {
        super(ScreenConfig.screenWidth, ScreenConfig.screenHeight);

        this.gameClient = gameClient;

        this.gap = gap;

        this.bottomY = ScreenConfig.screenHeight - margin - PlayerCardsOverlay.CARD_HEIGHT;
        this.rightY = ScreenConfig.screenWidth - margin - PlayerCardsOverlay.CARD_HEIGHT;
        this.topY = margin;
        this.leftY = -margin - PlayerCardsOverlay.CARD_HEIGHT;

        this.cardSelectionPanel = new CardSelectionPanel(actionPanel, gameClient.getThisPlayer().getNumberOfCards(), 1280, 24);

        super.addWidget(this.cardSelectionPanel.bottom(ScreenConfig.xCenter, ScreenConfig.screenHeight - margin));

        this.updatePlayerCards();
    }

    public void updatePlayerCards() {
        this.cardSelectionPanel.setCards(this.gameClient.getThisCards());

        List<GameClient.Player> players = new ArrayList<>(this.gameClient.getOtherPlayers().sequencedValues());

        players.add(this.gameClient.getThisPlayer());

        players.sort(Comparator.comparing(GameClient.Player::getTurnIndex));

        this.numberOfPlayers = players.size();

        this.updateCoordinates(players);
    }

    private void updateCoordinates(List<GameClient.Player> players) {
        int[] bottomX = this.cardSelectionPanel.getXCoordinates().clone();

        int cardSelectionPanelX = this.cardSelectionPanel.getWidget().getX(Anchor.LEFT);

        for (int i = 0; i < bottomX.length; i++) {
            bottomX[i] += cardSelectionPanelX;
        }

        this.bottomX = bottomX;

        LoopingIterator<GameClient.Player> iterator = LoopingIterator.of(players);

        iterator.set(this.gameClient.getThisPlayer());

        switch (players.size()) {
            case 2 -> {
                this.rightX = new int[0];
                this.topX = this.getTopX(iterator.next().getNumberOfCards());
                this.leftX = new int[0];
            }
            case 3 -> {
                this.rightX = this.getRightX(iterator.next().getNumberOfCards());
                this.topX = new int[0];
                this.leftX = this.getLeftX(iterator.next().getNumberOfCards());
            }
            case 4 -> {
                this.rightX = this.getRightX(iterator.next().getNumberOfCards());
                this.topX = this.getTopX(iterator.next().getNumberOfCards());
                this.leftX = this.getLeftX(iterator.next().getNumberOfCards());
            }
            default -> {
                this.rightX = new int[0];
                this.topX = new int[0];
                this.leftX = new int[0];
            }
        }
    }

    private int[] getRightX(int numberOfCards) {
        int[] x = new int[numberOfCards];

        int xStart = -ScreenConfig.yCenter - (this.getTotalWidth(numberOfCards) / 2);

        for (int i = 0; i < x.length; i++) {
            x[i] = xStart + (i * this.gap);
        }

        return x;
    }

    private int[] getTopX(int numberOfCards) {
        int[] x = new int[numberOfCards];

        int xStart = ScreenConfig.xCenter - (this.getTotalWidth(numberOfCards) / 2);

        for (int i = 0; i < x.length; i++) {
            x[i] = xStart + (i * this.gap);
        }

        return x;
    }

    private int[] getLeftX(int numberOfCards) {
        int[] x = new int[numberOfCards];

        int xStart = ScreenConfig.yCenter - (this.getTotalWidth(numberOfCards) / 2);

        for (int i = 0; i < x.length; i++) {
            x[i] = xStart + (i * this.gap);
        }

        return x;
    }

    private int getTotalWidth(int numberOfCards) {
        return ((numberOfCards - 1) * this.gap) + PlayerCardsOverlay.CARD_WIDTH;
    }

    @Override
    public void render(Graphics2D g) {
        int numberOfPlayers = this.numberOfPlayers;

        if (numberOfPlayers < 2) return;

        int[] rightX = this.rightX;
        int[] topX = this.topX;
        int[] leftX = this.leftX;

        // --- NEXT PLAYER (TOP OR RIGHT) ---
        if (numberOfPlayers == 2) this.renderCardsTop(g, topX);
        else this.renderCardsRight(g, rightX);

        if (numberOfPlayers < 3) return;

        // --- NEXT PLAYER (LEFT OR TOP) ---
        if (numberOfPlayers == 3) this.renderCardsLeft(g, leftX);
        else this.renderCardsTop(g, topX);

        if (numberOfPlayers < 4) return;

        // --- NEXT PLAYER (LEFT) ---
        this.renderCardsLeft(g, leftX);
    }

    private static void renderCards(Graphics2D g, int[] x, int y) {
        for (int value : x) {
            g.drawImage(PlayerCardsOverlay.BACK_IMAGE, value, y, null);
        }
    }

    private void renderCardsRight(Graphics2D g, int[] x) {
        g.rotate(-Math.PI / 2);
        PlayerCardsOverlay.renderCards(g, x, this.rightY);
        g.rotate(Math.PI / 2);
    }

    private void renderCardsTop(Graphics2D g, int[] x) {
        PlayerCardsOverlay.renderCards(g, x, this.topY);
    }

    private void renderCardsLeft(Graphics2D g, int[] x) {
        g.rotate(Math.PI / 2);
        PlayerCardsOverlay.renderCards(g, x, this.leftY);
        g.rotate(-Math.PI / 2);
    }

    public int[] getBottomX() {
        return this.bottomX;
    }

    public int getBottomY() {
        return this.bottomY;
    }

    public int[] getRightX() {
        return this.rightX;
    }

    public int getRightY() {
        return this.rightY;
    }

    public int[] getTopX() {
        return this.topX;
    }

    public int getTopY() {
        return this.topY;
    }

    public int[] getLeftX() {
        return this.leftX;
    }

    public int getLeftY() {
        return this.leftY;
    }
}