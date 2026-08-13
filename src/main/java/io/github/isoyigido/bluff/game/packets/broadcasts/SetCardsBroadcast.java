package io.github.isoyigido.bluff.game.packets.broadcasts;

import io.github.isoyigido.bluff.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SetCardsBroadcast {
    private static final Logger logger = LoggerFactory.getLogger(SetCardsBroadcast.class);

    private int[] cardIDs;

    public SetCardsBroadcast(List<Card> cards) {
        this(SetCardsBroadcast.getCardIDs(cards));
    }

    private static int[] getCardIDs(List<Card> cards) {
        int[] cardIDs = new int[cards.size()];

        for (int i = 0; i < cardIDs.length; i++) {
            cardIDs[i] = cards.get(i).getID();
        }

        return cardIDs;
    }

    public SetCardsBroadcast(int[] cardIDs) {
        this.cardIDs = cardIDs;
    }

    public List<Card> getCards() {
        int numberOfCards = this.cardIDs.length;

        List<Card> cards = new ArrayList<>(numberOfCards);

        for (Integer cardID : this.cardIDs) {
            Card.get(cardID).ifPresentOrElse(
                    cards::add,
                    () -> SetCardsBroadcast.logger.warn("Encountered invalid card ID number. id={}", cardID)
            );
        }

        return cards;
    }

    private SetCardsBroadcast() {}
}