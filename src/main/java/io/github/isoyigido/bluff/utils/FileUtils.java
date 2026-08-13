package io.github.isoyigido.bluff.utils;

import io.github.isoyigido.basic.gui.app.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/// Utility class for file operations
/// @see File
public final class FileUtils {
    /// Private constructor to prevent instantiation
    private FileUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /// Creates the input directory if it doesn't exist
    /// @param dir the input directory
    public static void createIfNotExists(File dir) {
        // If the input directory is null
        if (dir == null) {
            // Log error
            logger.error("Directory to create is null.");
            // Return
            return;
        }

        // If the directory exists, return
        if (dir.exists()) return;

        // Create the directory
        if (dir.mkdir()) logger.info ("Successfully created directory. path={}", dir.getAbsolutePath());
        else             logger.error("Unable to create directory. path={}",     dir.getAbsolutePath());
    }

    /// If the input file already exists, numbers are appended to the file name to avoid duplicate file names.
    /// @param file the file with the potentially duplicate name
    /// @return the file with the non-duplicate name
    public static File getNonDuplicateFile(File file) {
        // If the file doesn't exist, return it
        if (!file.exists()) return file;

        // Get the full path of the file
        String fullPath = file.getAbsolutePath();

        // Get the index of the dot
        int dotIndex = fullPath.lastIndexOf('.');

        // The base file name without the dot and extension (e.g., "image")
        String baseName;
        // The extension with the dot (e.g., ".png")
        String extension;

        // If the file name has no extension
        if (dotIndex < 0) {
            baseName = fullPath;
            extension = "";
        } else {
            baseName = fullPath.substring(0, dotIndex);
            extension = fullPath.substring(dotIndex);
        }

        // Start at 2 (i.e. "image.png", "image (2).png", ...)
        int count = 2;
        // Until a non-existing file name is found
        while (file.exists()) {
            // Construct: directory + base + (count) + extension
            String newName = baseName + " (" + count + ')' + extension;
            // Update the file
            file = new File(newName);
            // Increment the count
            count++;
        }

        // Return the file
        return file;
    }

    /// Shows a new file choosing window and returns the selected file as a {@link File} object.
    ///
    /// **Special cases:**
    /// - Returns an empty {@link Optional} if no file is selected
    ///
    /// @param windowTitle the title of the file choosing window
    /// @param fileType the type of the file to be selected (e.g., `Bash Script`)
    /// @param extensions extensions allowed to be selected (e.g., `bash, sh`)
    /// @return an {@link Optional} containing the selected {@link File} object,
    ///         or an empty {@link Optional} if no file is selected
    public static Optional<File> selectFile(String windowTitle, String fileType, String... extensions) {
        // Create the file chooser
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        // Create the description of the file name extension filter
        String joinedExtensions = Arrays.stream(extensions)
                .map(ext -> '.' + ext)
                .collect(Collectors.joining(", "));
        String description = fileType + " (" + joinedExtensions + ')';

        // Set the file name extension filter
        chooser.setFileFilter(new FileNameExtensionFilter(description, extensions));

        // Set the title of the file choosing window
        chooser.setDialogTitle(windowTitle);

        // Show the file choosing window
        int result = chooser.showOpenDialog(null);

        // If the result is approved by JFileChooser
        if (result == JFileChooser.APPROVE_OPTION) {
            // Return an optional containing the selected file object
            return Optional.of(chooser.getSelectedFile());
        }

        // Return empty optional
        return Optional.empty();
    }

    /// Shows a new folder choosing window and returns the selected folder as a {@link File} object.
    ///
    /// **Special cases:**
    /// - Returns an empty {@link Optional} if no folder is selected
    ///
    /// @param windowTitle the title of the folder choosing window
    /// @return an {@link Optional} containing the selected folder as a {@link File} object,
    ///         or an empty {@link Optional} if no folder is selected
    public static Optional<File> selectFolder(String windowTitle) {
        // Create the file chooser
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        // Set the selection mode to ONLY directories
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Set the title of the folder choosing window
        chooser.setDialogTitle(windowTitle);

        // Show the folder choosing window
        int result = chooser.showOpenDialog(null);

        // If the result is approved by JFileChooser
        if (result == JFileChooser.APPROVE_OPTION) {
            // Return an optional containing the selected file object
            return Optional.of(chooser.getSelectedFile());
        }

        // Return empty optional
        return Optional.empty();
    }

    /// Shows a file save dialog and returns the selected file.
    ///
    /// **Special cases:**
    /// - Returns an empty {@link Optional} if no file is selected
    ///
    /// @param defaultName the default name of the save file (e.g., "New Text File")
    /// @param fileType the description of the file type (e.g., "Text Files")
    /// @param extension the extension of the file (e.g., "txt")
    /// @return an {@link Optional} containing the selected {@link File} object,
    ///         or an empty {@link Optional} if no file is selected
    public static Optional<File> getSaveLocation(String defaultName, String fileType, String extension) {
        // Initialize the file chooser
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory()) {
            @Override
            public void approveSelection() {
                // Get the selected file
                File file = this.getSelectedFile();

                // If the file already exists and the dialog is a save dialog
                if (file.exists() && (this.getDialogType() == JFileChooser.SAVE_DIALOG)) {
                    // Show overwrite confirmation dialog
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            Translator.get("file_dialog.dialog.overwrite.message"),
                            Translator.get("file_dialog.dialog.overwrite.title"),
                            JOptionPane.YES_NO_CANCEL_OPTION
                    );

                    // Based on the result
                    switch (result) {
                        // YES -> Approve selection as usual and return
                        case JOptionPane.YES_OPTION: super.approveSelection(); return;
                        // NO, CLOSE -> Return
                        case JOptionPane.NO_OPTION, JOptionPane.CLOSED_OPTION: return;
                        // CANCEL -> Cancel file selection and return
                        case JOptionPane.CANCEL_OPTION:
                            this.cancelSelection(); return;
                    }
                }

                // Approve selection as usual
                super.approveSelection();
            }
        };

        // Pre-fill the selected file for convenience
        chooser.setSelectedFile(new File(defaultName + '.' + extension));

        // Set the file name extension filter
        chooser.setFileFilter(new FileNameExtensionFilter(fileType + " (." + extension + ')', extension));

        // Show save dialog window
        int result = chooser.showSaveDialog(null);

        // If selection is not approved, return empty optional
        if (result != JFileChooser.APPROVE_OPTION) return Optional.empty();

        // Get the selected file
        File selectedFile = chooser.getSelectedFile();

        // Get the absolute path of the selected file
        String filePath = selectedFile.getAbsolutePath();
        // If the user forgets the extension
        if (!filePath.toLowerCase().endsWith('.' + extension.toLowerCase())) {
            // Append the extension
            selectedFile = new File(filePath + '.' + extension);
        }

        // Return an optional containing the selected file
        return Optional.of(selectedFile);
    }
}