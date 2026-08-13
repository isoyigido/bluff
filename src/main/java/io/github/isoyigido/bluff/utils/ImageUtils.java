package io.github.isoyigido.bluff.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/// Utility class for image processing operations
/// @see BufferedImage
public final class ImageUtils {
    /// Private constructor to prevent instantiation
    private ImageUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    /// Saves a BufferedImage into a PNG file
    ///
    /// **Special cases:**
    /// - Returns an empty {@link Optional} if the input `image` is null or an {@link IOException} is caught
    ///
    /// @param image the BufferedImage that is saved
    /// @param directory the directory (folder) where the image is saved
    /// @param filename the name of the saved image file (e.g., image.png)
    /// @return an {@link Optional} containing the saved {@link File} object,
    ///         or an empty {@link Optional} if the given image is null or an {@link IOException} is caught
    public static Optional<File> saveImage(RenderedImage image, File directory, String filename) {
        // If the input image is null
        if (image == null) {
            // Log error
            logger.error("Cannot save null image.");

            // Return empty optional
            return Optional.empty();
        }

        // Create the file with the input directory and file name
        File file = new File(directory, filename);

        // Ensure the file name is unique
        file = FileUtils.getNonDuplicateFile(file);

        try {
            // Save image as png
            ImageIO.write(image, "png", file);

            // Log saved image
            logger.info("Image saved successfully. path={}", file.getAbsolutePath());

            // Return an optional containing the saved file
            return Optional.of(file);

        } catch (IOException e) {
            // Log error
            logger.error("Error while saving image. path={}", file.getAbsolutePath(), e);

            // Return empty optional
            return Optional.empty();
        }
    }

    /// Reads and returns the image at the given path.
    ///
    /// **Special cases:**
    /// - Returns an empty {@link Optional} if the input `path` is null, there is no image at the given path, or an {@link IOException} is caught
    ///
    /// @param path the path to the image file relative to the resources folder
    /// @return an {@link Optional} containing the image from the input path as a {@link BufferedImage} object,
    ///         or an empty {@link Optional} if the given path is null, there is no image at the given path, or an {@link IOException} is caught
    public static Optional<BufferedImage> readImage(String path) {
        // If the given path is null
        if (path == null) {
            // Log error
            logger.error("Cannot read image from null path.");

            // Return empty optional
            return Optional.empty();
        }

        // Try to read the image file from the input path
        try (InputStream is = ImageUtils.class.getResourceAsStream(path)) {
            // If the input stream is null
            if (is == null) {
                // Log warning
                logger.warn("Unable to find image. path={}", path);

                // Return empty optional
                return Optional.empty();
            }

            // Read the image from the input stream
            return Optional.of(ImageIO.read(is));

        } catch (IOException e) {
            // Log error
            logger.error("Unable to read image. path={}", path, e);

            // Return empty optional
            return Optional.empty();
        }
    }

    /// @param image the BufferedImage that is resized
    /// @param width the new width of the image
    /// @param height the new height of the image
    /// @param doInterpolation whether the image should be interpolated
    /// @throws NullPointerException if the input `image` is null
    public static BufferedImage resizeImage(BufferedImage image, int width, int height, boolean doInterpolation) {
        // If the given image is null, throw a null pointer exception
        Objects.requireNonNull(image, "Image to resize is null.");

        // Initialize the new image
        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        // Create the image graphics
        Graphics2D g = newImage.createGraphics();
        // If the image should be interpolated
        if (doInterpolation) {
            // Enable bicubic interpolation
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
        // Draw the input image on the new image with the input dimensions
        g.drawImage(image, 0, 0, width, height, null);
        // Dispose of the graphics
        g.dispose();
        // Return the new, resized image
        return newImage;
    }

    /// Resizes the input image to fit the mask height while keeping the aspect ratio the same, clips areas outside the mask
    /// @param image the BufferedImage that is masked
    /// @param mask the mask outside which the image is clipped
    /// @return the masked image
    /// @throws NullPointerException if the input `image` or `mask` is null
    public static BufferedImage maskImage(BufferedImage image, Shape mask, boolean doInterpolation) {
        // If the given image is null, throw a null pointer exception
        Objects.requireNonNull(image, "Image to mask is null.");

        // If the given mask is null, throw a null pointer exception
        Objects.requireNonNull(mask, "Mask to apply is null.");

        // Get mask position and dimensions
        int maskX = mask.getBounds().x;
        int maskY = mask.getBounds().y;
        int maskWidth = mask.getBounds().width;
        int maskHeight = mask.getBounds().height;

        // Resize the image to fit the height of the mask
        image = resizeImageToHeight(image, maskHeight, doInterpolation);

        // Initialize the new image
        BufferedImage newImage = new BufferedImage(maskWidth, maskHeight, BufferedImage.TYPE_INT_ARGB);
        // Create the image graphics
        Graphics2D g = newImage.createGraphics();

        // Set the clip to mask the image
        g.setClip(mask);

        // Calculate the image position
        int imageX = maskX + ((maskWidth - image.getWidth()) / 2);

        // Draw the input image on the new image with the mask dimensions
        g.drawImage(image, imageX, maskY, null);

        // Dispose of the graphics
        g.dispose();

        // Return the new, masked image
        return newImage;
    }

    /// Resizes the input image to the specified width without changing the aspect ratio
    /// @param image the BufferedImage that is resized
    /// @param width the new width of the image
    /// @param doInterpolation whether the image should be interpolated
    /// @throws NullPointerException if the input `image` is null
    public static BufferedImage resizeImageToWidth(BufferedImage image, int width, boolean doInterpolation) {
        // If the given image is null, throw a null pointer exception
        Objects.requireNonNull(image, "Image to resize is null.");

        // Calculate the aspect ratio of the input image
        double aspectRatio = (double) image.getWidth() / image.getHeight();
        // Calculate the height corresponding to the input width for the aspect ratio
        int height = (int) Math.round(width / aspectRatio);

        // Return the resized image
        return resizeImage(image, width, height, doInterpolation);
    }

    /// Resizes the input image to the specified height without changing the aspect ratio
    /// @param image the BufferedImage that is resized
    /// @param height the new height of the image
    /// @param doInterpolation whether the image should be interpolated
    /// @throws NullPointerException if the input `image` is null
    public static BufferedImage resizeImageToHeight(BufferedImage image, int height, boolean doInterpolation) {
        // If the given image is null, throw a null pointer exception
        Objects.requireNonNull(image, "Image to resize is null.");

        // Calculate the aspect ratio of the input image
        double aspectRatio = (double) image.getWidth() / image.getHeight();
        // Calculate the width corresponding to the input height for the aspect ratio
        int width = (int) Math.round(height * aspectRatio);

        // Return the resized image
        return resizeImage(image, width, height, doInterpolation);
    }

    /// Resizes the input image to the maximum size which fits within the specified bounds without changing the aspect ratio
    /// @param image the BufferedImage that is resized
    /// @param maxWidth the maximum width of the new image
    /// @param maxHeight the maximum height of the new image
    /// @param doInterpolation whether the image should be interpolated
    /// @throws NullPointerException if the input `image` is null
    public static BufferedImage resizeImageToFitBounds(BufferedImage image, int maxWidth, int maxHeight, boolean doInterpolation) {
        // If the given image is null, throw a null pointer exception
        Objects.requireNonNull(image, "Image to resize is null.");

        // Calculate the aspect ratio of the input image
        double aspectRatio = (double) image.getWidth() / image.getHeight();

        // Calculate the width corresponding to the max height for the aspect ratio
        int width = (int) Math.round(maxHeight * aspectRatio);

        // If the calculated width is within bounds
        if (width <= maxWidth) {
            // Resize the image to the calculated width and max height
            return resizeImage(image, width, maxHeight, doInterpolation);
        }

        // The width is not within bounds:
        // Calculate the height corresponding to the max width for the aspect ratio
        int height = (int) Math.round(maxWidth / aspectRatio);

        // Resize the image to the calculated height and max width
        return resizeImage(image, maxWidth, height, doInterpolation);
    }
}