package io.github.isoyigido.bluff.game.packets.broadcasts;

import io.github.isoyigido.bluff.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GatherMiddleBroadcast {
    private static final Logger logger = LoggerFactory.getLogger(GatherMiddleBroadcast.class);

    private int accuserID;
    private int accusedID;
    private int[] playedCardIDs;
    private boolean bluff;
    private int[] middleCardIDs;

    public GatherMiddleBroadcast(int accuserID, int accusedID, List<Card> playedCards, boolean bluff, List<Card> middleCards) {
        this(accuserID, accusedID, GatherMiddleBroadcast.getCardIDs(playedCards), bluff, GatherMiddleBroadcast.getCardIDs(middleCards));
    }

    private static int[] getCardIDs(List<Card> cards) {
        int numberOfCards = cards.size();

        int[] cardIDs = new int[numberOfCards];

        for (int i = 0; i < numberOfCards; i++) {
            cardIDs[i] = cards.get(i).getID();
        }

        return cardIDs;
    }

    public GatherMiddleBroadcast(int accuserID, int accusedID, int[] playedCardIDs, boolean bluff, int[] middleCardIDs) {
        this.accuserID = accuserID;
        this.accusedID = accusedID;
        this.playedCardIDs = playedCardIDs;
        this.bluff = bluff;
        this.middleCardIDs = middleCardIDs;
    }

    public int getAccuserID() {
        return this.accuserID;
    }

    public int getAccusedID() {
        return this.accusedID;
    }

    public boolean isBluff() {
        return this.bluff;
    }

    public List<Card> getPlayedCards() {
        return GatherMiddleBroadcast.getCards(this.playedCardIDs);
    }

    public List<Card> getMiddleCards() {
        return GatherMiddleBroadcast.getCards(this.middleCardIDs);
    }

    private static List<Card> getCards(int[] cardIDs) {
        List<Card> cards = new ArrayList<>(cardIDs.length);

        for (Integer cardID : cardIDs) {
            Card.get(cardID).ifPresentOrElse(
                    cards::add,
                    () -> GatherMiddleBroadcast.logger.warn("Encountered invalid card ID number. id={}", cardID)
            );
        }

        return cards;
    }

    private GatherMiddleBroadcast() {}
}