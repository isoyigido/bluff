package io.github.isoyigido.bluff.gui.components.game;

import io.github.isoyigido.basic.gui.core.Component;
import io.github.isoyigido.basic.gui.core.MouseButton;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.gui.CardImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CardSelectionPanel extends Component {
    private static final Color HIGHLIGHT_COLOR = new Color(0x80006FFF, true);

    private static final int HIGHLIGHT_ARC = 16;

    private final ActionPanel actionPanel;

    private final int maximumPanelWidth;
    private final int maximumGap;

    private List<Card> cards = new ArrayList<>(0);

    private List<Rectangle> hitboxes = new ArrayList<>(0);
    
    private final List<Card> selectedCards = new ArrayList<>(4);
    private boolean[] selectedMask = new boolean[0];

    private List<BufferedImage> cardImages = new ArrayList<>(0);

    private int[] xCoordinates;

    public CardSelectionPanel(ActionPanel actionPanel, int initialNumberOfCards, int maximumPanelWidth, int maximumGap) {
        super(maximumPanelWidth, PlayerCardsOverlay.CARD_HEIGHT);

        this.actionPanel = actionPanel;

        this.maximumPanelWidth = maximumPanelWidth;
        this.maximumGap = maximumGap;

        this.updateHitboxes(initialNumberOfCards);
    }

    public void setCards(List<Card> cards) {
        int numberOfCards = cards.size();

        this.updateHitboxes(numberOfCards);

        List<Card> sortedCards = new ArrayList<>(cards);

        sortedCards.sort(Comparator.comparing(card -> card.rank().ordinal()));

        this.cards = sortedCards;

        this.resolveSelectedCards();

        List<BufferedImage> cardImages = new ArrayList<>(numberOfCards);

        this.cards.stream()
                .map(card -> CardImage.of(card, PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT))
                .forEach(cardImages::add);

        this.cardImages = cardImages;
    }

    private void updateHitboxes(int numberOfCards) {
        this.xCoordinates = this.getXCoordinates(numberOfCards);

        List<Rectangle> hitboxes = new ArrayList<>(numberOfCards);

        for (int i = 0; i < numberOfCards; i++) {
            hitboxes.add(new Rectangle(this.xCoordinates[i], 0, PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT));
        }

        this.hitboxes = hitboxes;
    }

    public int[] getXCoordinates(int numberOfCards) {
        if (numberOfCards == 1) {
            return new int[]{(this.maximumPanelWidth - PlayerCardsOverlay.CARD_WIDTH) / 2};
        }

        int gap = Math.min(this.maximumGap, (this.maximumPanelWidth - (numberOfCards * PlayerCardsOverlay.CARD_WIDTH)) / (numberOfCards - 1));

        int width = (numberOfCards * (PlayerCardsOverlay.CARD_WIDTH + gap)) - gap;

        int xStart = (this.maximumPanelWidth - width) / 2;

        int[] x = new int[numberOfCards];

        for (int i = 0; i < x.length; i++) {
            x[i] = xStart + (i * (gap + PlayerCardsOverlay.CARD_WIDTH));
        }

        return x;
    }

    private void resolveSelectedCards() {
        this.selectedCards.removeIf(card -> !this.cards.contains(card));

        this.selectedMask = new boolean[this.cards.size()];

        for (Card selectedCard : this.selectedCards) {
            int selectedCardIndex = this.cards.indexOf(selectedCard);

            if (selectedCardIndex != -1) this.selectedMask[selectedCardIndex] = true;
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(CardSelectionPanel.HIGHLIGHT_COLOR);

        for (int i = 0; i < this.cardImages.size(); i++) {
            if (i >= this.hitboxes.size()) return;

            Rectangle hitbox = this.hitboxes.get(i);

            g.drawImage(this.cardImages.get(i), hitbox.x, 0, null);

            if (this.selectedMask[i]) g.fillRoundRect(
                    hitbox.x, 0,
                    PlayerCardsOverlay.CARD_WIDTH, PlayerCardsOverlay.CARD_HEIGHT,
                    CardSelectionPanel.HIGHLIGHT_ARC, CardSelectionPanel.HIGHLIGHT_ARC
            );
        }
    }

    @Override
    public void mouseClickEvent(int x, int y, MouseButton mouseButton) {
        for (int i = this.hitboxes.size() - 1; i >= 0; i--) {
            if (this.hitboxes.get(i).contains(x, y)) {
                this.toggleSelection(i);

                return;
            }
        }
    }

    private void toggleSelection(int index) {
        this.selectedMask[index] = !this.selectedMask[index];

        if (this.selectedMask[index]) this.selectedCards.add(this.cards.get(index));
        else this.selectedCards.remove(this.cards.get(index));

        this.actionPanel.setSelectedCards(this.selectedCards);
    }

    public List<Card> getCards() {
        return this.cards;
    }

    public int[] getXCoordinates() {
        return this.xCoordinates;
    }
}