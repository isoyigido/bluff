package io.github.isoyigido.bluff.game.packets.requests;

import io.github.isoyigido.bluff.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class PlayCardsRequest extends Request {
    private static final Logger logger = LoggerFactory.getLogger(PlayCardsRequest.class);

    private int[] cardIDs;

    public PlayCardsRequest(int turnID, List<Card> cards) {
        this(turnID, PlayCardsRequest.getCardIDs(cards));
    }

    private static int[] getCardIDs(List<Card> cards) {
        int numberOfCards = cards.size();

        int[] cardIDs = new int[numberOfCards];

        for (int i = 0; i < numberOfCards; i++) {
            cardIDs[i] = cards.get(i).getID();
        }

        return cardIDs;
    }

    public PlayCardsRequest(int turnID, int[] cardIDs) {
        super(turnID);

        this.cardIDs = cardIDs;
    }

    public List<Card> getCards() {
        int numberOfCards = this.cardIDs.length;

        List<Card> cards = new ArrayList<>(numberOfCards);

        for (Integer cardID : this.cardIDs) {
            Card.get(cardID).ifPresentOrElse(
                    cards::add,
                    () -> PlayCardsRequest.logger.warn("Encountered invalid card ID number. id={}", cardID)
            );
        }

        return cards;
    }

    private PlayCardsRequest() {
        super(0);
    }
}