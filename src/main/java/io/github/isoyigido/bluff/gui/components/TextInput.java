package io.github.isoyigido.bluff.gui.components;

import io.github.isoyigido.basic.gui.app.Theme;
import io.github.isoyigido.basic.gui.constants.Cursors;
import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.core.MouseButton;
import io.github.isoyigido.basic.gui.core.components.Trigger;
import io.github.isoyigido.bluff.gui.ScreenConstants;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;

public class TextInput extends Component {
    // --- Colors ---
    private final Color backgroundColor = Theme.getColor("text-input.background");
    private final Color borderColor = Theme.getColor("text-input.border");
    private final Color borderColorFocused = Theme.getColor("text-input.border-focused");
    private final Color textColor = Theme.getColor("text-input.text");
    private final Color placeholderTextColor = Theme.getColor("text-input.placeholder-text");

    // --- Horizontal gaps ---
    private static final int CARET_GAP = 2;

    // --- Other ---
    private static final int CARET_BLINKS_PER_SECOND = 2;
    private static final int UPDATES_PER_CARET_BLINK = ScreenConstants.UPS / CARET_BLINKS_PER_SECOND;

    private final int textAreaWidth;
    private final int textX;
    private final int textY;
    private final int textAreaEndX;
    private final int caretY1;
    private final int caretY2;

    // Rectangle arc
    final int arc;
    // Input text
    private final StringBuilder text;
    // Text font
    private final Font font;
    // Font metrics for the text font
    private final FontMetrics fm;
    // Placeholder text
    private final String placeholder;
    // Whether the box is being input to
    boolean focused = false;
    // Caret timer
    private int caretTimer = 0;
    // Whether the caret is visible
    private boolean caretVisible = true;
    // Maximum input text length
    private final int maxTextLength;
    // The set of allowed characters
    private final Set<Character> allowedCharacters;
    // What happens when enter is pressed
    private Runnable onEnterPressed = null;
    // What happens when the box is exited (when the box loses focus)
    private Runnable onExited = null;
    // What happens when the text content is updated
    private Runnable onUpdated = null;

    // The clip of the text area
    private final Rectangle textAreaClip;

    private final Trigger trigger;

    /// TextInput constructor: Constructs a box where text can be input
    /// @param width the width of the box
    /// @param height the height of the box
    /// @param arc the arc of the box corners
    /// @param font the font of the displayed text
    /// @param initialText the initial text content of the box
    /// @param placeholder the placeholder text that is displayed when the box is empty
    /// @param maxTextLength the maximum character length of the input text
    /// @param allowedCharacters the set of allowed characters
    public TextInput(int width, int height, int arc, Font font, String initialText, String placeholder, int maxTextLength, Set<Character> allowedCharacters) {
        super(width, height);

        // --- Set parameters ---
        this.arc = arc;
        this.font = font;
        this.text = new StringBuilder(initialText);
        this.placeholder = placeholder;
        this.maxTextLength = maxTextLength;
        this.allowedCharacters = allowedCharacters;

        this.fm = new Canvas().getFontMetrics(font);
        final int TEXT_PADDING = width / 10;
        this.textAreaWidth = width - (TEXT_PADDING * 2);
        this.textX = TEXT_PADDING;
        this.textY = ((height + this.fm.getAscent()) / 2) - 2;
        this.textAreaEndX = this.textX + this.textAreaWidth;
        this.caretY1 = this.textY - this.fm.getAscent();
        this.caretY2 = this.textY + 2;

        // Add the trigger for the text input
        this.trigger = new Trigger(width, height).setSpecialCursor(Cursors.TEXT);
        this.addWidget(this.trigger.topLeft(0, 0));

        // Set the clip of the text area
        this.textAreaClip = new Rectangle(
                TEXT_PADDING - (TextInput.CARET_GAP / 2),
                0,
                this.textAreaWidth + TextInput.CARET_GAP,
                height
        );
    }

    /// @return the text content of the box
    public String getText() {
        return this.text.toString();
    }

    /// Sets the text content of the box
    /// @param text the new text content of the box
    public void setText(String text) {
        // Clear the text
        this.text.setLength(0);
        // Append the input text
        this.text.append(text);

        // Register an update to the text content
        this.registerUpdate();
    }

    /// Clears the input text
    public void clear() {
        // Set the text length to 0
        this.text.setLength(0);

        // Register an update to the text content
        this.registerUpdate();
    }

    @Override
    public void render(Graphics2D g) {
        // Render the box background
        g.setColor(this.backgroundColor);
        g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.arc, this.arc);

        // Render the border
        this.renderBorder(g);

        // Render the text and the caret
        this.renderTextAndCaret(g);
    }

    /// Helper method: Renders the border
    void renderBorder(Graphics2D g) {
        // Increase stroke for thicker border
        g.setStroke(new BasicStroke(5));
        // Set the color of the border based on whether the box is focused on
        g.setColor(this.focused ? this.borderColorFocused : this.borderColor);
        // Draw the round rectangle
        g.drawRoundRect(0, 0, this.getWidth(), this.getHeight(), this.arc, this.arc);
        // Restore the default stroke
        g.setStroke(new BasicStroke(1));
    }

    /// Helper method: Renders the text and the caret
    private void renderTextAndCaret(Graphics2D g) {
        Graphics2D gLocal = (Graphics2D) g.create();

        // Clip text rendering area
        gLocal.clip(this.textAreaClip);

        // Set the font
        gLocal.setFont(this.font);

        // Initialize the displayed text
        String displayText;
        // If the box is empty and not focused on
        if (this.text.isEmpty() && !this.focused) {
            // Display placeholder
            gLocal.setColor(this.placeholderTextColor);
            displayText = this.placeholder;
        }
        // Otherwise
        else {
            // Display the input text
            gLocal.setColor(this.textColor);
            displayText = this.text.toString();
        }

        // Calculate the width of the displayed text
        int textWidth = this.fm.stringWidth(displayText) + TextInput.CARET_GAP;
        // Check whether the text width exceeds the text area width
        boolean overflow = textWidth > this.textAreaWidth;
        // Render the displayed text, offsetting it if necessary
        gLocal.drawString(displayText, overflow ? (this.textAreaEndX - textWidth) : this.textX, this.textY);

        // If focused and caret is visible
        if (this.focused && this.caretVisible) {
            // Calculate the x coordinate of the caret
            int caretX = overflow ? this.textAreaEndX : (this.textX + textWidth);
            // Draw the caret
            gLocal.drawLine(caretX, this.caretY1, caretX, this.caretY2);
        }
    }

    @Override
    public void update() {
        this.trigger.setActive((super.getWidget() != null) && super.getWidget().isVisible());

        // If focused
        if (this.focused) {
            // Update caret timer
            this.caretTimer++;
            // If the caret timer is completed
            if (this.caretTimer > TextInput.UPDATES_PER_CARET_BLINK) {
                // Invert caret visibility
                this.caretVisible = !this.caretVisible;
                // Reset the caret timer
                this.caretTimer = 0;
            }
        }
    }

    /// Sets what happens when enter is pressed
    /// @param onEnterPressed what happens when enter is pressed
    public TextInput setOnEnterPressed(Runnable onEnterPressed) {
        this.onEnterPressed = onEnterPressed;

        return this;
    }

    /// Sets what happens when the box is exited (when the box loses focus)
    /// @param onExited what happens when the box is exited (when the box loses focus)
    public void setOnExited(Runnable onExited) {
        this.onExited = onExited;
    }

    /// Sets what happens when the text content is updated
    /// @param onUpdated what happens when the text content is updated
    public void setOnUpdated(Runnable onUpdated) {
        this.onUpdated = onUpdated;
    }

    /// Sets whether the box is currently being input to (whether the box is focused on)
    /// @param focused whether the box is currently being input to (whether the box is focused on)
    private void setFocused(boolean focused) {
        // If lost focus and onExited is not null
        if (!focused && this.focused && (this.onExited != null)) {
            // Run onExited
            this.onExited.run();
        }

        // Set focused
        this.focused = focused;
    }

    @Override
    public void mouseClickEvent(int x, int y, MouseButton mouseButton) {
        if ((super.getWidget() == null) || !super.getWidget().isVisible()) return;

        // Update focused state based on whether clicked on the text input
        this.setFocused(this.contains(x, y));
    }

    @Override
    public void keyTypingEvent(KeyEvent e) {
        // If not focused, return
        if (!this.focused || (super.getWidget() == null) || !super.getWidget().isVisible()) return;

        // Save the old text
        String oldText = this.text.toString();

        // Get the typed character
        char c = e.getKeyChar();

        // If enter is typed and enter action is not null
        if ((c == KeyEvent.VK_ENTER) && (this.onEnterPressed != null)) {
            // Set focused to false
            this.setFocused(false);
            // Run the enter action
            this.onEnterPressed.run();
        }

        // If the character is backspace and the text is not empty
        if ((c == '\b') && !this.text.isEmpty()) {
            // Delete the last character
            this.text.deleteCharAt(this.text.length() - 1);
        }
        // Else if the typed character is allowed
        // and the current text length is less than the maximum text length
        else if (this.allowedCharacters.contains(c) && (this.text.length() < this.maxTextLength)) {
            // Append the character to the text
            this.text.append(c);
        }

        // Consume the key typing event
        e.consume();

        // If the new text is different from the old text, register an update to the text
        if (!this.text.toString().equals(oldText)) this.registerUpdate();
    }

    /// Registers an update to the text
    void registerUpdate() {
        // If on updated is not null, run onUpdated
        if (this.onUpdated != null) this.onUpdated.run();
    }
}