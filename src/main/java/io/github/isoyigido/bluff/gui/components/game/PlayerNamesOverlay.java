package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.game.client.GameClient;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerNamesOverlay extends Component {
    private final Color textColor = Theme.getColor("text");
    private final Font textFont = Theme.getFont(32);
    private final FontMetrics textFontMetrics = new Canvas().getFontMetrics(this.textFont);

    private final GameClient gameClient;

    private final int margin;

    private final String[] playerNames;

    public PlayerNamesOverlay(GameClient gameClient, int margin) {
        super(ScreenConfig.screenWidth, ScreenConfig.screenHeight);

        this.gameClient = gameClient;

        this.margin = margin;

        this.playerNames = new String[4];

        this.updatePlayerNames();
    }

    public void updatePlayerNames() {
        List<GameClient.Player> otherPlayers = new ArrayList<>(this.gameClient.getOtherPlayers().sequencedValues());

        this.playerNames[0] = this.gameClient.getThisPlayer().getName();

        int numberOfPlayers = otherPlayers.size() + 1;

        for (int i = 1; i < this.playerNames.length; i++) {
            this.playerNames[i] = (i < numberOfPlayers) ? (otherPlayers.get(i - 1).getName()) : "";
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setFont(this.textFont);
        g.setColor(this.textColor);

        // --- BOTTOM (THIS PLAYER) ---
        g.drawString(this.playerNames[0], ScreenConfig.xCenter - (this.textFontMetrics.stringWidth(this.playerNames[0]) / 2), ScreenConfig.screenHeight - this.margin);

        // --- TOP (OTHER PLAYER 3) ---
        g.drawString(this.playerNames[3], ScreenConfig.xCenter - (this.textFontMetrics.stringWidth(this.playerNames[3]) / 2), this.margin + this.textFontMetrics.getAscent());

        // --- LEFT (OTHER PLAYER 1) ---
        g.rotate(Math.PI / 2);

        g.drawString(this.playerNames[1], ScreenConfig.yCenter - (this.textFontMetrics.stringWidth(this.playerNames[1]) / 2), -this.margin);

        // --- RIGHT (OTHER PLAYER 2) ---
        g.rotate(-Math.PI);

        g.drawString(this.playerNames[2], - (this.textFontMetrics.stringWidth(this.playerNames[2]) / 2) - ScreenConfig.yCenter, ScreenConfig.screenWidth - this.margin);
    }
}