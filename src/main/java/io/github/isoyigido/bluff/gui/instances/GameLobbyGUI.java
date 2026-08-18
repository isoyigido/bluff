package io.github.isoyigido.bluff.gui.instances;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.core.GUI;
import io.github.isoyigido.basic.gui.core.GUIManager;
import io.github.isoyigido.basic.gui.core.components.TextComponent;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.game.bots.BotClient;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.game.client.GameEventListener;
import io.github.isoyigido.bluff.game.server.GameServer;
import io.github.isoyigido.bluff.gui.components.Button;
import io.github.isoyigido.bluff.gui.components.HorizontalContainer;
import io.github.isoyigido.bluff.gui.components.VerticalContainer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GameLobbyGUI extends GUI {
    private final GameClient gameClient;

    private final TextComponent[] playerNames;

    private final Button startGameButton;

    public GameLobbyGUI(GameClient gameClient) {
        this.gameClient = gameClient;

        this.playerNames = new TextComponent[4];

        this.playerNames[0] = new TextComponent(
                gameClient.getThisPlayer().getName(),
                Theme.getColor("text"),
                Theme.getFont(36, true, false)
        );

        for (int i = 1; i < this.playerNames.length; i++) {
            this.playerNames[i] = new TextComponent(
                    "",
                    Theme.getColor("text"),
                    Theme.getFont(36)
            );
        }

        super.addWidget(new VerticalContainer(
                40,
                true,
                this.playerNames
        ).center());

        this.startGameButton = new Button(
                MainMenuGUI.BUTTON_WIDTH / 2, MainMenuGUI.BUTTON_HEIGHT,
                MainMenuGUI.BUTTON_ARC,
                true,
                Translator.get("game_lobby.buttons.start_game"),
                null,
                this.gameClient::startGame
        ).setBaseColor(
                new Color(36, 180, 12),
                new Color(60, 204, 36),
                new Color(153, 153, 153)
        ).setTextColor(
                Color.WHITE,
                Color.WHITE
        );

        super.addWidget(new HorizontalContainer(
                20,
                false,
                this.startGameButton,
                new Button(
                        MainMenuGUI.BUTTON_WIDTH / 2, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        true,
                        Translator.get("game_lobby.buttons.disconnect"),
                        null,
                        () -> {
                            this.gameClient.close();

                            GUIManager.setGUI(PlayMenuGUI::new);
                        }
                ).setBaseColor(
                        new Color(133, 0, 0),
                        new Color(155, 22, 22),
                        Color.BLACK
                ).setTextColor(
                        Color.WHITE,
                        Color.BLACK
                )
        ).bottom(ScreenConfig.xCenter, ScreenConfig.screenHeight - 120));

        gameClient.setGameEventListener(new GameEventListener() {
            @Override
            public void playerConnected() {
                GameLobbyGUI.this.updatePlayerList();
            }

            @Override
            public void playerDisconnected() {
                GameLobbyGUI.this.updatePlayerList();
            }

            @Override
            public void setHost() {
                GameLobbyGUI.this.updatePlayerList();
            }

            @Override
            public void setGameState() {
                if (gameClient.getGameState() == GameServer.GameState.PLAYING) return;

                GameLobbyGUI.this.updateStartGameButton();
            }

            @Override
            public void startGame() {
                GUIManager.setGUI(() -> new GameGUI(gameClient));
            }
        });

        this.updatePlayerList();

        super.addWidget(new Component(){
            @Override
            public void keyTypingEvent(KeyEvent e) {
                if (e.getKeyChar() == 'u') GameLobbyGUI.this.updatePlayerList();
                else if ((e.getKeyChar() == 'b') && gameClient.isThisPlayerHost()) BotClient.add();
            }
        }.center());
    }


    private void updatePlayerList() {
        List<GameClient.Player> allPlayers = new ArrayList<>(this.gameClient.getOtherPlayers().sequencedValues());

        allPlayers.addFirst(this.gameClient.getThisPlayer());

        int hostIndex = allPlayers.indexOf(this.gameClient.getHost());

        int numberOfPlayers = allPlayers.size();

        for (int i = 0; i < this.playerNames.length; i++) {
            boolean filled = i < numberOfPlayers;

            this.playerNames[i].setText(filled ? allPlayers.get(i).getName() : "...");

            boolean host = i == hostIndex;

            if (host) this.playerNames[i].setColor(Color.YELLOW);
            else this.playerNames[i].setColor(filled ? Theme.getColor("text") : Theme.getColor("placeholder-text"));
        }

        this.updateStartGameButton();
    }

    private void updateStartGameButton() {
        this.startGameButton.setActive(this.gameClient.isThisPlayerHost() && (this.gameClient.getGameState() == GameServer.GameState.WAITING_FOR_START));
    }
}