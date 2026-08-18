package io.github.isoyigido.bluff.gui.instances;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.core.GUI;
import io.github.isoyigido.basic.gui.core.GUIManager;
import io.github.isoyigido.basic.gui.core.components.TextComponent;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.game.client.GameEventListener;
import io.github.isoyigido.bluff.gui.animation.DealCardsAnimation;
import io.github.isoyigido.bluff.gui.components.game.ActionPanel;
import io.github.isoyigido.bluff.gui.components.game.MiddleCardsComponent;
import io.github.isoyigido.bluff.gui.components.game.PlayerCardsOverlay;
import io.github.isoyigido.bluff.gui.components.game.PlayerNamesOverlay;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameGUI extends GUI {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public GameGUI(GameClient gameClient) {
        ActionPanel actionPanel = new ActionPanel(gameClient, 240, 60, 24, 16);

        PlayerNamesOverlay playerNamesOverlay = new PlayerNamesOverlay(gameClient, 40);
        PlayerCardsOverlay playerCardsOverlay = new PlayerCardsOverlay(gameClient, actionPanel, 80, 20);
        MiddleCardsComponent middleCardsComponent = new MiddleCardsComponent(gameClient, 20);

        super.addWidget(playerNamesOverlay.center());
        super.addWidget(playerCardsOverlay.center());
        super.addWidget(middleCardsComponent.center());
        super.addWidget(actionPanel.top(ScreenConfig.xCenter, ScreenConfig.yCenter + 120));

        TextComponent winnerText = new TextComponent(
                "",
                Color.YELLOW,
                Theme.getFont(48, true, false)
        );

        super.addWidget(winnerText.center().hide());

        gameClient.setGameEventListener(new GameEventListener(){
            @Override
            public void playerDisconnected() {
                playerNamesOverlay.updatePlayerNames();
                playerCardsOverlay.updatePlayerCards();
                middleCardsComponent.updateCards();
                actionPanel.updateElements();
            }

            @Override
            public void setCards() {
                playerCardsOverlay.updatePlayerCards();
            }

            @Override
            public void setTurn() {
                playerNamesOverlay.updatePlayerNames();
                actionPanel.updateElements();
            }

            @Override
            public void playedCards(GameClient.Player player, Rank rank, int numberOfCards) {
                playerCardsOverlay.updatePlayerCards();
                middleCardsComponent.updateCards();
                actionPanel.updateElements();

                middleCardsComponent.playCards(player, rank, numberOfCards);
            }

            @Override
            public void calledBullshit(GameClient.Player accuser, GameClient.Player accused, List<Card> cards, boolean bluff) {
                playerCardsOverlay.updatePlayerCards();
                middleCardsComponent.updateCards();
                actionPanel.updateElements();

                middleCardsComponent.callBullshit(accuser, accused, cards, bluff);
            }

            @Override
            public void setAllPassed() {
                actionPanel.updateElements();
            }

            @Override
            public void setWinner() {
                playerCardsOverlay.updatePlayerCards();
                middleCardsComponent.updateCards();
                actionPanel.updateElements();

                this.concludeGame();
            }

            private void concludeGame() {
                winnerText.setText(Translator.get("game.win_message").formatted(gameClient.getWinner().getName()));

                winnerText.getWidget().show();

                GameGUI.scheduler.schedule(() -> {
                    GUIManager.setGUI(PlayMenuGUI::new);
                    gameClient.close();
                }, 5, TimeUnit.SECONDS);
            }
        });

        playerNamesOverlay.updatePlayerNames();
        playerCardsOverlay.updatePlayerCards();
        middleCardsComponent.updateCards();
        actionPanel.updateElements();

        super.addWidget(new Component(){
            @Override
            public void keyTypingEvent(KeyEvent e) {
                if (e.getKeyChar() == 'u') {
                    playerNamesOverlay.updatePlayerNames();
                    playerCardsOverlay.updatePlayerCards();
                    middleCardsComponent.updateCards();
                    actionPanel.updateElements();
                }
            }
        }.center());

        super.addWidget(new DealCardsAnimation(middleCardsComponent, playerCardsOverlay, 3).top(0, 0));
    }
}