package io.github.isoyigido.bluff.gui.instances;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.GUI;
import io.github.isoyigido.basic.gui.core.GUIManager;
import io.github.isoyigido.basic.gui.core.components.TextComponent;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.gui.GUIUtils;
import io.github.isoyigido.bluff.gui.components.Button;
import io.github.isoyigido.bluff.gui.components.VerticalContainer;

public class SettingsMenuGUI extends GUI {
    public SettingsMenuGUI() {
        super.addWidget(new VerticalContainer(
                MainMenuGUI.BUTTON_GAP,
                false,
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        true,
                        Translator.get("settings_menu.buttons.languages"),
                        null,
                        () -> GUIManager.setGUI(LanguageSelectionGUI::new)
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
                        Translator.get("menu_names.settings_menu")
                ),
                Theme.getColor("navigation-text"),
                Theme.getFont(36)
        ).center(ScreenConfig.xCenter, ScreenConfig.screenHeight - MainMenuGUI.BACK_BUTTON_MARGIN - (MainMenuGUI.BUTTON_HEIGHT / 2)));
    }
}