package io.github.isoyigido.bluff.gui.components;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.constants.Cursors;
import io.github.isoyigido.basic.gui.core.MouseButton;
import io.github.isoyigido.basic.gui.core.components.ImageComponent;
import io.github.isoyigido.basic.gui.core.components.Trigger;
import io.github.isoyigido.bluff.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/// Special trigger with a button display
public class Button extends Trigger {
    // --- Colors ---
    private final Color backgroundColor = Theme.getColor("button.background");
    private final Color backgroundColorHovered = Theme.getColor("button.background-hovered");
    private Color baseColor = Theme.getColor("button.base");
    private Color baseColorHovered = Theme.getColor("button.base-hovered");
    private Color baseColorInactive = Theme.getColor("button.base-inactive");
    private Color textColor = Theme.getColor("button.text");
    private Color textColorInactive = Theme.getColor("button.text-inactive");

    // --- Numerical parameters ---
    private static final int BORDER_THICKNESS = 4;
    private static final int ICON_MARGIN = 16;

    private static final int FONT_SIZE = 32;

    private final Font font = Theme.getFont(Button.FONT_SIZE);
    private final FontMetrics fm = new Canvas().getFontMetrics(this.font);

    private final int labelY = ((super.getHeight() + this.fm.getAscent()) / 2) - 2;

    private final int arc;
    private final int innerArc;
    private boolean filled;
    private String label;

    private final Collection<Character> boundKeys = new HashSet<>(4);

    /// Constructs an active button.
    /// @param width the width of the button
    /// @param height the height of the button
    /// @param arc the arc of the button corners
    /// @param filled whether the button display is filled (false for border-only)
    /// @param label the label of the button
    /// @param iconPath the path to the button icon image file relative to the resources folder (null for no icon)
    /// @param action the action that is performed when left-clicked on the button
    public Button(int width, int height, int arc, boolean filled, String label, String iconPath, Runnable action) {
        this(
                width, height,
                arc,
                filled,
                label,
                Optional.ofNullable(iconPath).flatMap(ImageUtils::readImage).orElse(null),
                action
        );
    }

    public Button setBaseColor(Color baseColor, Color baseColorHovered, Color baseColorInactive) {
        this.baseColor = baseColor;
        this.baseColorHovered = baseColorHovered;
        this.baseColorInactive = baseColorInactive;

        return this;
    }

    public Button setTextColor(Color textColor, Color textColorInactive) {
        this.textColor = textColor;
        this.textColorInactive = textColorInactive;

        return this;
    }

    /// Constructs an active button.
    /// @param width the width of the button
    /// @param height the height of the button
    /// @param arc the arc of the button corners
    /// @param filled whether the button display is filled (false for border-only)
    /// @param label the label of the button
    /// @param iconImage the button icon image (null for no icon)
    /// @param action the action that is performed when left-clicked on the button
    private Button(int width, int height, int arc, boolean filled, String label, BufferedImage iconImage, Runnable action) {
        super(width, height);

        super.setMouseButtonAction(MouseButton.LEFT, action);
        super.setSpecialCursor(Cursors.HAND);

        // Set the arc
        this.arc = arc;
        // Set the inner arc
        this.innerArc = arc - (Button.BORDER_THICKNESS * 2);
        // Set whether the button is filled
        this.filled = filled;
        // Set the label of the button
        this.label = label;

        // --- Setting the icon ---
        // If the icon image is not null
        if (iconImage != null) {
            // Calculate icon size
            int iconSize = height - (Button.ICON_MARGIN * 2);
            // Add a new image element widget for the icon
            this.addWidget(new ImageComponent(ImageUtils.resizeImageToFitBounds(iconImage, iconSize, iconSize, true)).topLeft(Button.ICON_MARGIN, Button.ICON_MARGIN));
        }
    }

    public Button bind(char key) {
        this.boundKeys.add(key);
        return this;
    }

    public Button unbind(char key) {
        this.boundKeys.remove(key);
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        int width = this.getWidth();
        int height = this.getHeight();

        boolean active = super.isActive();
        boolean hovered = super.isHovered();

        // --- Rendering the background ---
        // If the button is filled
        if (this.filled) {
            // If the button is active, set the color based on whether the mouse is over the button
            if (active) g.setColor(hovered ? this.baseColorHovered : this.baseColor);
                // Else, draw the background with the inactive color
            else g.setColor(this.baseColorInactive);
            // Draw the filled background
            g.fillRoundRect(0, 0, width, height, this.arc, this.arc);
        }
        // If the button is not filled
        else {
            // Set the color based on whether the button is active
            g.setColor(active ? this.baseColor : this.baseColorInactive);
            // Draw the border of the button
            g.fillRoundRect(0, 0, width, height, this.arc, this.arc);

            // Set the color based on whether the button is active and the mouse is over the button
            g.setColor((hovered && active) ? this.backgroundColorHovered : this.backgroundColor);
            // Draw the filled background
            g.fillRoundRect(
                    Button.BORDER_THICKNESS, Button.BORDER_THICKNESS,
                    width - (Button.BORDER_THICKNESS * 2), height - (Button.BORDER_THICKNESS * 2),
                    this.innerArc, this.innerArc
            );
        }

        // --- Rendering the label ---
        // Calculate the x-coordinate of the label
        int labelX = (width - this.fm.stringWidth(this.label)) / 2;
        // Set the color based on whether the button is active
        g.setColor(active ? this.textColor : this.textColorInactive);
        // Set the font
        g.setFont(this.font);
        // Draw the text
        g.drawString(this.label, labelX, this.labelY);
    }

    @Override
    public void keyTypingEvent(KeyEvent e) {
        // If the pressed key is bound to this button
        if (this.boundKeys.contains(e.getKeyChar())) {
            // Register a click on this button
            this.click(MouseButton.LEFT);
            // Consume the event to prevent further activity
            e.consume();
        }
    }

    /// Sets whether the button display is filled
    /// @param filled whether the button display is filled
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}