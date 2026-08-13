package io.github.isoyigido.bluff.game.packets.broadcasts;

import io.github.isoyigido.bluff.game.cards.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AnonymousPlayedCardsBroadcast {
    private static final Logger logger = LoggerFactory.getLogger(AnonymousPlayedCardsBroadcast.class);

    private int playerID;
    private int currentRankOrdinal;
    private int numberOfCards;

    public AnonymousPlayedCardsBroadcast(int playerID, int currentRankOrdinal, int numberOfCards) {
        this.playerID = playerID;
        this.currentRankOrdinal = currentRankOrdinal;
        this.numberOfCards = numberOfCards;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public Optional<Rank> getCurrentRank() {
        Rank[] values = Rank.values();

        if ((this.currentRankOrdinal < 0) || (this.currentRankOrdinal >= values.length)) {
            AnonymousPlayedCardsBroadcast.logger.warn("Encountered invalid rank ordinal. ordinal={}", this.currentRankOrdinal);

            return Optional.empty();
        }

        return Optional.of(values[this.currentRankOrdinal]);
    }

    public int getNumberOfCards() {
        return this.numberOfCards;
    }

    private AnonymousPlayedCardsBroadcast() {}
}