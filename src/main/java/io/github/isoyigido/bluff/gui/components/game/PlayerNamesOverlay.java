package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.utils.LoopingIterator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerNamesOverlay extends Component {
    private final Color textColor = Theme.getColor("text");
    private final Font textFont = Theme.getFont(32);
    private final FontMetrics textFontMetrics = new Canvas().getFontMetrics(this.textFont);

    private static final Color IN_TURN_COLOR = Color.GREEN;

    private final GameClient gameClient;

    private final int margin;

    private LoopingIterator<GameClient.Player> players;

    private GameClient.Player thisPlayer;

    private GameClient.Player playerInTurn;

    public PlayerNamesOverlay(GameClient gameClient, int margin) {
        super(ScreenConfig.screenWidth, ScreenConfig.screenHeight);

        this.gameClient = gameClient;

        this.margin = margin;

        this.updatePlayerNames();
    }

    public void updatePlayerNames() {
        this.thisPlayer = this.gameClient.getThisPlayer();

        List<GameClient.Player> players = new ArrayList<>(this.gameClient.getOtherPlayers().sequencedValues());

        players.add(this.thisPlayer);

        players.sort(Comparator.comparing(GameClient.Player::getTurnIndex));

        this.players = LoopingIterator.of(players);

        this.playerInTurn = this.gameClient.getPlayerInTurn();
    }

    @Override
    public void render(Graphics2D g) {
        g.setFont(this.textFont);

        LoopingIterator<GameClient.Player> players = this.players;

        int numberOfPlayers = players.size();

        // --- THIS PLAYER (BOTTOM) ---
        this.setColor(g, this.thisPlayer);
        this.renderBottom(g, this.thisPlayer.getName());

        if (numberOfPlayers < 2) return;

        players.set(this.thisPlayer);

        // --- NEXT PLAYER (TOP OR RIGHT) ---
        GameClient.Player nextPlayer = players.next();
        this.setColor(g, nextPlayer);
        if (numberOfPlayers == 2) this.renderTop(g, nextPlayer.getName());
        else this.renderRight(g, nextPlayer.getName());

        if (numberOfPlayers < 3) return;

        // --- NEXT PLAYER (LEFT OR TOP) ---
        nextPlayer = players.next();
        this.setColor(g, nextPlayer);
        if (numberOfPlayers == 3) this.renderLeft(g, nextPlayer.getName());
        else this.renderTop(g, nextPlayer.getName());

        if (numberOfPlayers < 4) return;

        // --- NEXT PLAYER (LEFT) ---
        nextPlayer = players.next();
        this.setColor(g, nextPlayer);
        this.renderLeft(g, nextPlayer.getName());
    }

    private void setColor(Graphics2D g, GameClient.Player player) {
        g.setColor((this.playerInTurn == player) ? PlayerNamesOverlay.IN_TURN_COLOR : this.textColor);
    }

    private void renderBottom(Graphics2D g, String name) {
        g.drawString(name, ScreenConfig.xCenter - (this.textFontMetrics.stringWidth(name) / 2), ScreenConfig.screenHeight - this.margin);
    }

    private void renderRight(Graphics2D g, String name) {
        g.rotate(-Math.PI / 2);
        g.drawString(name, - (this.textFontMetrics.stringWidth(name) / 2) - ScreenConfig.yCenter, ScreenConfig.screenWidth - this.margin);
        g.rotate(Math.PI / 2);
    }

    private void renderTop(Graphics2D g, String name) {
        g.drawString(name, ScreenConfig.xCenter - (this.textFontMetrics.stringWidth(name) / 2), this.margin + this.textFontMetrics.getAscent());
    }

    private void renderLeft(Graphics2D g, String name) {
        g.rotate(Math.PI / 2);
        g.drawString(name, ScreenConfig.yCenter - (this.textFontMetrics.stringWidth(name) / 2), -this.margin);
        g.rotate(-Math.PI / 2);
    }
}