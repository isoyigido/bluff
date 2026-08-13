package io.github.isoyigido.bluff.gui.instances;

import io.github.isoyigido.basic.gui.core.GUI;
import io.github.isoyigido.basic.gui.core.GUIManager;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.game.client.GameEventListener;
import io.github.isoyigido.bluff.gui.components.game.ActionPanel;
import io.github.isoyigido.bluff.gui.components.game.MiddleCardsComponent;
import io.github.isoyigido.bluff.gui.components.game.PlayerCardsOverlay;
import io.github.isoyigido.bluff.gui.components.game.PlayerNamesOverlay;

import java.util.List;

public class GameGUI extends GUI {
    public GameGUI(GameClient gameClient) {
        ActionPanel actionPanel = new ActionPanel(gameClient, 240, 60, 24, 20);

        PlayerNamesOverlay playerNamesOverlay = new PlayerNamesOverlay(gameClient, 40);
        PlayerCardsOverlay playerCardsOverlay = new PlayerCardsOverlay(gameClient, actionPanel, 80);
        MiddleCardsComponent middleCardsComponent = new MiddleCardsComponent(gameClient);

        super.addWidget(playerNamesOverlay.center());
        super.addWidget(playerCardsOverlay.center());
        super.addWidget(middleCardsComponent.center());
        super.addWidget(actionPanel.top(ScreenConfig.xCenter, ScreenConfig.yCenter + 100));

        gameClient.setGameEventListener(new GameEventListener(){
            @Override
            public void playerDisconnected() {
                playerNamesOverlay.updatePlayerNames();
                playerCardsOverlay.updatePlayerCards();
                middleCardsComponent.updateCards();
                actionPanel.updateButtons();
            }

            @Override
            public void setCards() {
                playerCardsOverlay.updatePlayerCards();
            }

            @Override
            public void setTurn() {
                actionPanel.updateButtons();
            }

            @Override
            public void playedCards() {
                playerCardsOverlay.updatePlayerCards();
                middleCardsComponent.updateCards();
                actionPanel.updateButtons();
            }

            @Override
            public void calledBullshit(GameClient.Player accuser, GameClient.Player accused, List<Card> cards, boolean bluff) {
                playerCardsOverlay.updatePlayerCards();
                middleCardsComponent.updateCards();
                actionPanel.updateButtons();
            }

            @Override
            public void setAllPassed() {
                actionPanel.updateButtons();
            }
        });

        playerNamesOverlay.updatePlayerNames();
        playerCardsOverlay.updatePlayerCards();
        middleCardsComponent.updateCards();
        actionPanel.updateButtons();

        GUIManager.setGlobalKeyBind('u', () -> {
            playerNamesOverlay.updatePlayerNames();
            playerCardsOverlay.updatePlayerCards();
            middleCardsComponent.updateCards();
            actionPanel.updateButtons();
        });
    }
}