package io.github.isoyigido.bluff.main;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.app.Translator;
import io.github.isoyigido.basic.gui.core.GUIManager;
import io.github.isoyigido.basic.gui.window.BasicWindow;
import io.github.isoyigido.basic.gui.window.FullScreenWindow;
import io.github.isoyigido.bluff.gui.ScreenConstants;
import io.github.isoyigido.bluff.gui.instances.MainMenuGUI;
import io.github.isoyigido.bluff.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
    private Main() {
        throw new UnsupportedOperationException("Main class cannot be instantiated.");
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String APP_TITLE = "Bullshit!";
    private static final String ICON_PATH = "/game/cards/SA.png";

    public static void main(String[] args) {
        Theme.registerColorTheme("/app/themes", "light", "dark");

        Theme.setColorTheme("dark");

        Translator.register("/app/language", "en", "tr");

        Translator.setLanguage("en");

        BasicWindow window = new FullScreenWindow()
                .setVirtualScreenDimensions(1920, 1080)
                .setTitle(APP_TITLE);

        ImageUtils.readImage(ICON_PATH).ifPresentOrElse(
                window::setIconImage,
                () -> logger.warn("Unable to read application icon. path={}", ICON_PATH)
        );

        window.show(ScreenConstants.FPS, ScreenConstants.UPS);

        GUIManager.setGUI(MainMenuGUI::new);
    }
}