package io.github.isoyigido.bluff.game.packets.broadcasts;

import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayedCardsBroadcast {
    private static final Logger logger = LoggerFactory.getLogger(PlayedCardsBroadcast.class);

    private int currentRankOrdinal;
    private int numberOfCards;
    private int[] remainingCardIDs;

    public PlayedCardsBroadcast(int currentRankOrdinal, int numberOfCards, List<Card> remainingCards) {
        this(currentRankOrdinal, numberOfCards, PlayedCardsBroadcast.getCardIDs(remainingCards));
    }

    private static int[] getCardIDs(List<Card> cards) {
        int[] cardIDs = new int[cards.size()];

        for (int i = 0; i < cardIDs.length; i++) {
            cardIDs[i] = cards.get(i).getID();
        }

        return cardIDs;
    }

    public PlayedCardsBroadcast(int currentRankOrdinal, int numberOfCards, int[] remainingCardIDs) {
        this.currentRankOrdinal = currentRankOrdinal;
        this.numberOfCards = numberOfCards;
        this.remainingCardIDs = remainingCardIDs;
    }

    public Optional<Rank> getCurrentRank() {
        Rank[] values = Rank.values();

        if ((this.currentRankOrdinal < 0) || (this.currentRankOrdinal >= values.length)) {
            PlayedCardsBroadcast.logger.warn("Encountered invalid rank ordinal. ordinal={}", this.currentRankOrdinal);

            return Optional.empty();
        }

        return Optional.of(values[this.currentRankOrdinal]);
    }

    public int getNumberOfCards() {
        return this.numberOfCards;
    }

    public List<Card> getRemainingCards() {
        int numberOfCards = this.remainingCardIDs.length;

        List<Card> cards = new ArrayList<>(numberOfCards);

        for (Integer cardID : this.remainingCardIDs) {
            Card.get(cardID).ifPresentOrElse(
                    cards::add,
                    () -> PlayedCardsBroadcast.logger.warn("Encountered invalid card ID number. id={}", cardID)
            );
        }

        return cards;
    }

    private PlayedCardsBroadcast() {}
}