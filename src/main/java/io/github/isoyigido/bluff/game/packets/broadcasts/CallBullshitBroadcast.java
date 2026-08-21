package io.github.isoyigido.bluff.game.packets.broadcasts;

import io.github.isoyigido.bluff.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CallBullshitBroadcast {
    private static final Logger logger = LoggerFactory.getLogger(CallBullshitBroadcast.class);

    private int accuserID;
    private int accusedID;
    private int[] playedCardIDs;
    private boolean bluff;
    private int numberOfMiddleCards;

    public CallBullshitBroadcast(int accuserID, int accusedID, List<Card> playedCards, boolean bluff, int numberOfMiddleCards) {
        this(accuserID, accusedID, CallBullshitBroadcast.getCardIDs(playedCards), bluff, numberOfMiddleCards);
    }

    private static int[] getCardIDs(List<Card> cards) {
        int numberOfCards = cards.size();

        int[] cardIDs = new int[numberOfCards];

        for (int i = 0; i < numberOfCards; i++) {
            cardIDs[i] = cards.get(i).getID();
        }

        return cardIDs;
    }

    public CallBullshitBroadcast(int accuserID, int accusedID, int[] playedCardIDs, boolean bluff, int numberOfMiddleCards) {
        this.accuserID = accuserID;
        this.accusedID = accusedID;
        this.playedCardIDs = playedCardIDs;
        this.bluff = bluff;
        this.numberOfMiddleCards = numberOfMiddleCards;
    }

    public int getAccuserID() {
        return this.accuserID;
    }

    public int getAccusedID() {
        return this.accusedID;
    }

    public List<Card> getPlayedCards() {
        int numberOfCards = this.playedCardIDs.length;

        List<Card> cards = new ArrayList<>(numberOfCards);

        for (Integer cardID : this.playedCardIDs) {
            Card.get(cardID).ifPresentOrElse(
                    cards::add,
                    () -> CallBullshitBroadcast.logger.warn("Encountered invalid card ID number. id={}", cardID)
            );
        }

        return cards;
    }

    public boolean isBluff() {
        return this.bluff;
    }

    public int getNumberOfMiddleCards() {
        return this.numberOfMiddleCards;
    }

    private CallBullshitBroadcast() {}
}