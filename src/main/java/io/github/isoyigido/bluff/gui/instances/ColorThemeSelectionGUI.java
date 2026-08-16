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

public final class ColorThemeSelectionGUI extends GUI {
    private static final String[] colorThemes = new String[]{"dark", "light"};

    private static int colorThemeIndex = 0;

    public ColorThemeSelectionGUI() {
        super.addWidget(new VerticalContainer(
                MainMenuGUI.BUTTON_GAP,
                false,
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        ColorThemeSelectionGUI.colorThemeIndex == 0,
                        Translator.get("color_themes.dark"),
                        null,
                        () -> ColorThemeSelectionGUI.setColorTheme(0)
                ),
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        ColorThemeSelectionGUI.colorThemeIndex == 1,
                        Translator.get("color_themes.light"),
                        null,
                        () -> ColorThemeSelectionGUI.setColorTheme(1)
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
                        Translator.get("menu_names.color_theme_menu")
                ),
                Theme.getColor("navigation-text"),
                Theme.getFont(36)
        ).center(ScreenConfig.xCenter, ScreenConfig.screenHeight - MainMenuGUI.BACK_BUTTON_MARGIN - (MainMenuGUI.BUTTON_HEIGHT / 2)));
    }

    private static void setColorTheme(int colorThemeIndex) {
        // Set the color theme index
        ColorThemeSelectionGUI.colorThemeIndex = colorThemeIndex;

        // With a loading screen
        GUIUtils.doWithLoadingScreen(() -> {
            // Set the color theme
            Theme.setColorTheme(colorThemes[colorThemeIndex]);

            // Set the GUI to a new color theme selection GUI to refresh elements
            GUIManager.setGUI(ColorThemeSelectionGUI::new);
        });
    }
}