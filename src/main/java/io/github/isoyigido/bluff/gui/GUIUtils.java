package io.github.isoyigido.bluff.gui;

import io.github.isoyigido.basic.gui.core.GUIManager;
import io.github.isoyigido.bluff.gui.overlays.LoadingOverlay;

public final class GUIUtils {
    /// Private constructor to prevent instantiation
    private GUIUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /// Performs the specified action with a loading overlay to avoid freezing the GUI.
    /// @param action the action that is performed
    public static void doWithLoadingScreen(Runnable action) {
        // Set the overlay to a new loading overlay
        GUIManager.setOverlay(new LoadingOverlay());

        // On a new thread
        new Thread(() -> {
            // Prevent getting permanently locked out on error
            try {
                // Run the action
                action.run();
            }
            finally {
                // Remove the loading overlay
                GUIManager.removeOverlay();
            }
        }).start();
    }

    /// Constructs and returns the navigation text for the given array of GUI names.
    ///
    /// **Example navigation text: `Main Menu > Settings > App`**
    ///
    /// @param navigation the array of GUI names
    /// @return the navigation text
    public static String getNavigationText(String... navigation) {
        return String.join(" > ", navigation);
    }
}