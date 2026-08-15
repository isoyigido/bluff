package io.github.isoyigido.bluff.gui.instances;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.GUI;
import io.github.isoyigido.basic.gui.core.GUIManager;
import io.github.isoyigido.basic.gui.core.components.TextComponent;
import io.github.isoyigido.basic.gui.window.ScreenConfig;
import io.github.isoyigido.bluff.gui.components.Button;
import io.github.isoyigido.bluff.gui.components.VerticalContainer;

import java.awt.*;
import java.awt.event.KeyEvent;

public class MainMenuGUI extends GUI {
    public static final int BUTTON_WIDTH = 480;
    public static final int BUTTON_HEIGHT = 80;
    public static final int BUTTON_ARC = 40;
    public static final int BUTTON_GAP = 20;

    public static final int BACK_BUTTON_MARGIN = 20;

    private static boolean textChanged = false;

    public MainMenuGUI() {
        super.addWidget(new TextComponent(
                MainMenuGUI.textChanged ? "Tezgâh" : Translator.get("title"),
                Color.WHITE,
                Theme.getFont(64, true, false)
        ){
            @Override
            public void keyTypingEvent(KeyEvent e) {
                if (e.getKeyChar() == 't') {
                    MainMenuGUI.textChanged = true;
                    this.setText("Tezgâh");
                }
            }
        }.top(ScreenConfig.xCenter, 240));

        super.addWidget(new VerticalContainer(
                MainMenuGUI.BUTTON_GAP,
                false,
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        true,
                        Translator.get("main_menu.buttons.play"),
                        null,
                        () -> GUIManager.setGUI(PlayMenuGUI::new)
                ),
                new Button(
                        MainMenuGUI.BUTTON_WIDTH, MainMenuGUI.BUTTON_HEIGHT,
                        MainMenuGUI.BUTTON_ARC,
                        true,
                        Translator.get("main_menu.buttons.settings"),
                        null,
                        () -> GUIManager.setGUI(SettingsMenuGUI::new)
                )
        ).center());

        super.addWidget(new TextComponent(
                Translator.get("menu_names.main_menu"),
                Theme.getColor("navigation-text"),
                Theme.getFont(36)
        ).center(ScreenConfig.xCenter, ScreenConfig.screenHeight - MainMenuGUI.BACK_BUTTON_MARGIN - (MainMenuGUI.BUTTON_HEIGHT / 2)));
    }
}