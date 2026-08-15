package io.github.isoyigido.bluff.gui.instances;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.GUI;
import io.github.isoyigido.basic.gui.core.GUIManager;
import io.github.isoyigido.basic.gui.core.components.TextComponent;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.game.server.GameServer;
import io.github.isoyigido.bluff.gui.GUIUtils;
import io.github.isoyigido.bluff.gui.components.Button;
import io.github.isoyigido.bluff.gui.components.TextInput;
import io.github.isoyigido.bluff.gui.components.VerticalContainer;

import java.util.Set;

public class PlayMenuGUI extends GUI {
    private static String savedAddress = null;
    private static String savedName = null;

    private final TextInput addressInput;
    private final TextInput nameInput;

    public PlayMenuGUI() {
        super.addWidget(new VerticalContainer(
                MainMenuGUI.BUTTON_GAP,
                false,
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        true,
                        Translator.get("play_menu.buttons.host"),
                        null,
                        this::hostGame
                ),
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        true,
                        Translator.get("play_menu.buttons.join"),
                        null,
                        this::enterAddress
                )
        ).center());

        super.addWidget(
                new Button(
                        MainMenuGUI.BUTTON_WIDTH / 3, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        true,
                        Translator.get("menu.buttons.back"),
                        null,
                        () -> GUIManager.setGUI(MainMenuGUI::new)
                )
                        .bind('\u001B')
                        .bottomRight(ScreenConfig.screenWidth - MainMenuGUI.BACK_BUTTON_MARGIN, ScreenConfig.screenHeight - MainMenuGUI.BACK_BUTTON_MARGIN)
        );

        super.addWidget(new TextComponent(
                GUIUtils.getNavigationText(
                        Translator.get("menu_names.main_menu"),
                        Translator.get("menu_names.play_menu")
                ),
                Theme.getColor("navigation-text"),
                Theme.getFont(36)
        ).center(ScreenConfig.xCenter, ScreenConfig.screenHeight - MainMenuGUI.BACK_BUTTON_MARGIN - (MainMenuGUI.BUTTON_HEIGHT / 2)));

        this.addressInput = new TextInput(512, 96, 32, Theme.getFont(36), "", "Server IP Address", 32, Set.of('a', 'b', 'c', 'ç', 'd', 'e', 'f', 'g', 'ğ', 'h', 'ı', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'ö', 'p', 'q', 'r', 's', 'ş', 't', 'u', 'ü', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'Ç', 'D', 'E', 'F', 'G', 'Ğ', 'H', 'I', 'İ', 'J', 'K', 'L', 'M', 'N', 'O', 'Ö', 'P', 'Q', 'R', 'S', 'Ş', 'T', 'U', 'Ü', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '-', '.', ' '))
                .setOnEnterPressed(this::enterName);
        this.nameInput = new TextInput(512, 96, 32, Theme.getFont(36), "", "Player Name", 32, Set.of('a', 'b', 'c', 'ç', 'd', 'e', 'f', 'g', 'ğ', 'h', 'ı', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'ö', 'p', 'q', 'r', 's', 'ş', 't', 'u', 'ü', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'Ç', 'D', 'E', 'F', 'G', 'Ğ', 'H', 'I', 'İ', 'J', 'K', 'L', 'M', 'N', 'O', 'Ö', 'P', 'Q', 'R', 'S', 'Ş', 'T', 'U', 'Ü', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '-', '.', ' '));

        if (PlayMenuGUI.savedAddress != null) this.addressInput.setText(PlayMenuGUI.savedAddress);
        if (PlayMenuGUI.savedName != null) this.nameInput.setText(PlayMenuGUI.savedName);

        super.addWidget(this.addressInput.center().hide());
        super.addWidget(this.nameInput.center().hide());
    }

    private void enterAddress() {
        super.disableInput(this.addressInput.getWidget());

        this.addressInput.getWidget().show();
    }

    private void enterName() {
        this.addressInput.getWidget().hide();

        this.nameInput.setOnEnterPressed(() -> GUIUtils.doWithLoadingScreen(() -> {
            GameClient.get(this.addressInput.getText(), this.nameInput.getText()).map(GameLobbyGUI::new).ifPresent(GUIManager::setGUI);
            this.nameInput.getWidget().hide();
            super.enableInput();

            PlayMenuGUI.savedAddress = this.addressInput.getText();
            PlayMenuGUI.savedName = this.nameInput.getText();
        }));

        this.nameInput.getWidget().show();

        super.disableInput(this.nameInput.getWidget());
    }

    private void hostGame() {
        GameServer.host(4);

        this.nameInput.setOnEnterPressed(() -> GUIUtils.doWithLoadingScreen(() -> {
            GameClient.get("localhost", this.nameInput.getText()).map(GameLobbyGUI::new).ifPresent(GUIManager::setGUI);
            this.nameInput.getWidget().hide();
            super.enableInput();

            PlayMenuGUI.savedName = this.nameInput.getText();
        }));

        this.nameInput.getWidget().show();

        super.disableInput(this.nameInput.getWidget());
    }
}