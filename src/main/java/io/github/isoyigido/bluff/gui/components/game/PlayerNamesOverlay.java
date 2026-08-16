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

    private static final Color IN_TURN_COLOR = Color.GREEN;

    private final GameClient gameClient;

    private final int margin;

    private final String[] playerNames;

    private int numberOfOtherPlayers = 0;

    private int playerInTurnIndex = -1;

    public PlayerNamesOverlay(GameClient gameClient, int margin) {
        super(ScreenConfig.screenWidth, ScreenConfig.screenHeight);

        this.gameClient = gameClient;

        this.margin = margin;

        this.playerNames = new String[4];

        this.updatePlayerNames();
    }

    public void updatePlayerNames() {
        List<GameClient.Player> otherPlayers = new ArrayList<>(this.gameClient.getOtherPlayers().sequencedValues());

        this.numberOfOtherPlayers = otherPlayers.size();

        this.playerNames[0] = this.gameClient.getThisPlayer().getName();

        int numberOfPlayers = otherPlayers.size() + 1;

        for (int i = 1; i < this.playerNames.length; i++) {
            this.playerNames[i] = (i < numberOfPlayers) ? (otherPlayers.get(i - 1).getName()) : "";
        }

        this.playerInTurnIndex = -1;

        if (this.gameClient.isThisPlayerInTurn()) {
            this.playerInTurnIndex = 0;

            return;
        }

        GameClient.Player playerInTurn = this.gameClient.getPlayerInTurn();

        for (int i = 0; i < otherPlayers.size(); i++) {
            if (otherPlayers.get(i) == playerInTurn) {
                this.playerInTurnIndex = i + 1;

                return;
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        int leftIndex = 1;
        int topIndex = 2;
        int rightIndex = 3;

        switch (this.numberOfOtherPlayers) {
            case 1 -> {
                topIndex = 1;
                leftIndex = 2;
            }
            case 2 -> {
                rightIndex = 2;
                topIndex = 3;
            }
        }

        g.setFont(this.textFont);
        g.setColor(this.textColor);

        // --- BOTTOM (THIS PLAYER) ---
        g.setColor((this.playerInTurnIndex == 0) ? PlayerNamesOverlay.IN_TURN_COLOR : this.textColor);
        g.drawString(this.playerNames[0], ScreenConfig.xCenter - (this.textFontMetrics.stringWidth(this.playerNames[0]) / 2), ScreenConfig.screenHeight - this.margin);

        // --- TOP (OTHER PLAYER 3) ---
        g.setColor((this.playerInTurnIndex == topIndex) ? PlayerNamesOverlay.IN_TURN_COLOR : this.textColor);
        g.drawString(this.playerNames[topIndex], ScreenConfig.xCenter - (this.textFontMetrics.stringWidth(this.playerNames[topIndex]) / 2), this.margin + this.textFontMetrics.getAscent());

        // --- LEFT (OTHER PLAYER 1) ---
        g.rotate(Math.PI / 2);

        g.setColor((this.playerInTurnIndex == leftIndex) ? PlayerNamesOverlay.IN_TURN_COLOR : this.textColor);
        g.drawString(this.playerNames[leftIndex], ScreenConfig.yCenter - (this.textFontMetrics.stringWidth(this.playerNames[leftIndex]) / 2), -this.margin);

        // --- RIGHT (OTHER PLAYER 2) ---
        g.rotate(-Math.PI);

        g.setColor((this.playerInTurnIndex == rightIndex) ? PlayerNamesOverlay.IN_TURN_COLOR : this.textColor);
        g.drawString(this.playerNames[rightIndex], - (this.textFontMetrics.stringWidth(this.playerNames[rightIndex]) / 2) - ScreenConfig.yCenter, ScreenConfig.screenWidth - this.margin);
    }
}