package io.github.isoyigido.bluff.gui.components.game;

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

    private final int margin;

    private final int gap;

    private final CardSelectionPanel cardSelectionPanel;

    private LoopingIterator<GameClient.Player> players;

    private GameClient.Player thisPlayer;

    public PlayerCardsOverlay(GameClient gameClient, ActionPanel actionPanel, int margin, int gap) {
        super(ScreenConfig.screenWidth, ScreenConfig.screenHeight);

        this.gameClient = gameClient;

        this.margin = margin;

        this.gap = gap;

        this.cardSelectionPanel = new CardSelectionPanel(actionPanel, 1280, 24);

        this.updatePlayerCards();

        super.addWidget(this.cardSelectionPanel.bottom(ScreenConfig.xCenter, ScreenConfig.screenHeight - margin));
    }

    public void updatePlayerCards() {
        this.cardSelectionPanel.setCards(this.gameClient.getThisCards());

        this.thisPlayer = this.gameClient.getThisPlayer();

        List<GameClient.Player> players = new ArrayList<>(this.gameClient.getOtherPlayers().sequencedValues());

        players.add(this.thisPlayer);

        players.sort(Comparator.comparing(GameClient.Player::getTurnIndex));

        this.players = LoopingIterator.of(players);
    }

    @Override
    public void render(Graphics2D g) {
        LoopingIterator<GameClient.Player> players = this.players;

        int numberOfPlayers = players.size();

        if (numberOfPlayers < 2) return;

        players.set(this.thisPlayer);

        // --- NEXT PLAYER (TOP OR RIGHT) ---
        if (numberOfPlayers == 2) this.renderTop(g, players.next().getNumberOfCards());
        else this.renderRight(g, players.next().getNumberOfCards());

        if (numberOfPlayers < 3) return;

        // --- NEXT PLAYER (LEFT OR TOP) ---
        if (numberOfPlayers == 3) this.renderLeft(g, players.next().getNumberOfCards());
        else this.renderTop(g, players.next().getNumberOfCards());

        if (numberOfPlayers < 4) return;

        // --- NEXT PLAYER (LEFT) ---
        this.renderLeft(g, players.next().getNumberOfCards());
    }

    private void renderRight(Graphics2D g, int numberOfCards) {
        g.rotate(-Math.PI / 2);

        int y = ScreenConfig.screenWidth - this.margin - PlayerCardsOverlay.CARD_HEIGHT;
        int width = ((numberOfCards - 1) * this.gap) + PlayerCardsOverlay.CARD_WIDTH;
        int x = -ScreenConfig.yCenter - (width / 2);

        for (int i = 0; i < numberOfCards; i++) {
            g.drawImage(PlayerCardsOverlay.BACK_IMAGE, x, y, null);
            x += this.gap;
        }

        g.rotate(Math.PI / 2);
    }

    private void renderTop(Graphics2D g, int numberOfCards) {
        int width = ((numberOfCards - 1) * this.gap) + PlayerCardsOverlay.CARD_WIDTH;
        int x = ScreenConfig.xCenter - (width / 2);

        for (int i = 0; i < numberOfCards; i++) {
            g.drawImage(PlayerCardsOverlay.BACK_IMAGE, x, this.margin, null);
            x += this.gap;
        }
    }

    private void renderLeft(Graphics2D g, int numberOfCards) {
        g.rotate(Math.PI / 2);

        int y = -this.margin - PlayerCardsOverlay.CARD_HEIGHT;
        int width = ((numberOfCards - 1) * this.gap) + PlayerCardsOverlay.CARD_WIDTH;
        int x = ScreenConfig.yCenter - (width / 2);

        for (int i = 0; i < numberOfCards; i++) {
            g.drawImage(PlayerCardsOverlay.BACK_IMAGE, x, y, null);
            x += this.gap;
        }

        g.rotate(-Math.PI / 2);
    }
}