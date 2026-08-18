package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.core.Widget;
import io.github.isoyigido.basic.gui.core.components.TextComponent;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.game.server.GameServer;
import io.github.isoyigido.bluff.gui.components.Button;
import io.github.isoyigido.bluff.gui.components.HorizontalContainer;
import io.github.isoyigido.bluff.gui.components.VerticalContainer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ActionPanel extends Component {
    private final GameClient gameClient;

    private final Button bullshitButton;
    private final Button playButton;
    private final Button rankButton;
    private final Button passButton;

    private final Widget panelWidget;

    private final Widget rankSelectionWidget;

    private boolean selectingRank = false;
    private boolean selectedRank = true;

    private List<Card> selectedCards = new ArrayList<>(0);

    private final TextComponent waitingText;

    public ActionPanel(GameClient gameClient, int buttonWidth, int buttonHeight, int buttonArc, int gap) {
        this.gameClient = gameClient;

        this.bullshitButton = new Button(
                buttonWidth, buttonHeight,
                buttonArc,
                true,
                Translator.get("game_gui.buttons.call_bullshit"),
                null,
                this::callBullshit
        ).setBaseColor(
                new Color(133, 0, 0),
                new Color(155, 22, 22),
                new Color(82, 82, 82)
        ).setTextColor(
                Color.WHITE,
                Color.WHITE
        );

        this.playButton = new Button(
                buttonWidth, buttonHeight,
                buttonArc,
                true,
                Translator.get("game_gui.buttons.play_cards"),
                null,
                this::playCards
        ).setBaseColor(
                new Color(36, 180, 12),
                new Color(60, 204, 36),
                new Color(153, 153, 153)
        ).setTextColor(
                Color.WHITE,
                Color.WHITE
        );

        this.rankButton = new Button(
                buttonWidth, buttonHeight,
                buttonArc,
                true,
                Translator.get("game_gui.buttons.change_rank"),
                null,
                this::changeRank
        ).setBaseColor(
                new Color(0, 38, 156),
                new Color(22, 60, 178),
                new Color(105, 105, 105)
        ).setTextColor(
                Color.WHITE,
                Color.WHITE
        );

        this.passButton = new Button(
                buttonWidth, buttonHeight,
                buttonArc,
                true,
                Translator.get("game_gui.buttons.pass"),
                null,
                this::pass
        );

        VerticalContainer buttonContainer = new VerticalContainer(
                gap,
                false,
                new HorizontalContainer(
                        gap,
                        false,
                        this.bullshitButton,
                        this.playButton
                ),
                new HorizontalContainer(
                        gap,
                        false,
                        this.rankButton,
                        this.passButton
                )
        );

        this.panelWidget = buttonContainer.topLeft(0, 0);

        super.addWidget(this.panelWidget);

        super.setDimensions(buttonContainer.getWidth(), buttonContainer.getHeight());

        this.rankSelectionWidget = new RankSelectionComponent(
                160, 80,
                80,
                this::changeRank
        ).center(super.getWidth() / 2, super.getHeight() / 2).hide();

        super.addWidget(this.rankSelectionWidget);

        this.waitingText = new TextComponent(
                "",
                Theme.getColor("text"),
                Theme.getFont(36, true, false)
        );

        super.addWidget(this.waitingText.center(super.getWidth() / 2, super.getHeight() / 2).hide());

        this.updateElements();
    }

    public void updateElements() {
        if (this.selectingRank) return;

        boolean inTurn = this.gameClient.isThisPlayerInTurn();

        if (this.selectedRank) {
            if (inTurn) return;

            this.selectedRank = false;
        }

        if (!inTurn || (this.gameClient.getGameState() != GameServer.GameState.PLAYING)) {
            this.panelWidget.hide();

            this.rankSelectionWidget.hide();

            GameClient.Player playerInTurn = this.gameClient.getPlayerInTurn();

            if (this.gameClient.getGameState() == GameServer.GameState.PLAYING) {
                if (playerInTurn != null) {
                    this.waitingText.setText(Translator.get("game.waiting_text").formatted(playerInTurn.getName()));

                    this.waitingText.getWidget().show();
                }
            } else {
                this.waitingText.getWidget().hide();
            }

            return;
        }

        this.waitingText.getWidget().hide();

        this.panelWidget.show();

        this.playButton.setActive(!this.selectedCards.isEmpty());

        GameClient.Player lastPlayer = this.gameClient.getLastPlayer();
        boolean isThisPlayerTheLastPlayer = this.gameClient.isThisPlayerTheLastPlayer();
        boolean allPassed = this.gameClient.didAllPass();

        this.passButton.setActive((lastPlayer != null) && !allPassed);
        this.bullshitButton.setActive((lastPlayer != null) && !isThisPlayerTheLastPlayer);

        this.rankButton.setActive(!this.selectedCards.isEmpty() && allPassed);
    }

    public void setSelectedCards(List<Card> selectedCards) {
        this.selectedCards = selectedCards;
        this.updateElements();
    }

    private void playCards() {
        this.gameClient.playCards(this.selectedCards);
    }

    private void changeRank() {
        this.panelWidget.hide();

        this.rankSelectionWidget.show();

        this.selectingRank = true;
    }

    private void changeRank(Rank rank) {
        this.rankSelectionWidget.hide();

        this.selectedRank = true;
        this.selectingRank = false;

        this.gameClient.changeRank(rank, this.selectedCards);
    }

    private void callBullshit() {
        this.gameClient.callBullshit();
    }

    private void pass() {
        this.gameClient.pass();
    }
}