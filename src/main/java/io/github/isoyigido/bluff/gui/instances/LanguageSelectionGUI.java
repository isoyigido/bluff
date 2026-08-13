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

public final class LanguageSelectionGUI extends GUI {
    private static final String[] languageCodes = new String[]{"en", "tr"};

    private static int languageIndex = 0;

    public LanguageSelectionGUI() {
        super.addWidget(new VerticalContainer(
                MainMenuGUI.BUTTON_GAP,
                false,
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        LanguageSelectionGUI.languageIndex == 0,
                        "English",
                        "/app/icons/flags/uk.png",
                        () -> LanguageSelectionGUI.setLanguage(0)
                ),
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        LanguageSelectionGUI.languageIndex == 1,
                        "Türkçe",
                        "/app/icons/flags/turkey.png",
                        () -> LanguageSelectionGUI.setLanguage(1)
                )
        ).center());

        super.addWidget(
                new Button(
                        MainMenuGUI.BUTTON_WIDTH / 3, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        true,
                        Translator.get("menu.buttons.back"),
                        null,
                        () -> GUIManager.setGUI(SettingsMenuGUI::new)
                )
                        .bind('\u001B')
                        .bottomRight(ScreenConfig.screenWidth - MainMenuGUI.BACK_BUTTON_MARGIN, ScreenConfig.screenHeight - MainMenuGUI.BACK_BUTTON_MARGIN)
        );

        super.addWidget(new TextComponent(
                GUIUtils.getNavigationText(
                        Translator.get("menu_names.main_menu"),
                        Translator.get("menu_names.settings_menu"),
                        Translator.get("menu_names.language_menu")
                ),
                Theme.getColor("navigation-text"),
                Theme.getFont(36)
        ).center(ScreenConfig.xCenter, ScreenConfig.screenHeight - MainMenuGUI.BACK_BUTTON_MARGIN - (MainMenuGUI.BUTTON_HEIGHT / 2)));
    }

    private static void setLanguage(int languageIndex) {
        // Set the language index
        LanguageSelectionGUI.languageIndex = languageIndex;

        // With a loading screen
        GUIUtils.doWithLoadingScreen(() -> {
            // Set the language
            Translator.setLanguage(languageCodes[languageIndex]);

            // Set the GUI to a new language selection GUI to refresh elements
            GUIManager.setGUI(LanguageSelectionGUI::new);
        });
    }
}