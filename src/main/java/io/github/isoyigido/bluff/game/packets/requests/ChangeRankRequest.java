package io.github.isoyigido.bluff.game.packets.requests;

import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ChangeRankRequest extends Request {
    private static final Logger logger = LoggerFactory.getLogger(ChangeRankRequest.class);

    private int rankOrdinal;
    private int[] cardIDs;

    public ChangeRankRequest(int turnID, int rankOrdinal, List<Card> cards) {
        this(turnID, rankOrdinal, ChangeRankRequest.getCardIDs(cards));
    }

    private static int[] getCardIDs(List<Card> cards) {
        int[] cardIDs = new int[cards.size()];

        for (int i = 0; i < cardIDs.length; i++) {
            cardIDs[i] = cards.get(i).getID();
        }

        return cardIDs;
    }

    public ChangeRankRequest(int turnID, int rankOrdinal, int[] cardIDs) {
        super(turnID);

        this.rankOrdinal = rankOrdinal;
        this.cardIDs = cardIDs;
    }

    public Optional<Rank> getRank() {
        Rank[] values = Rank.values();

        if ((this.rankOrdinal < 0) || (this.rankOrdinal >= values.length)) {
            ChangeRankRequest.logger.warn("Encountered invalid rank ordinal. ordinal={}", this.rankOrdinal);

            return Optional.empty();
        }

        return Optional.of(values[this.rankOrdinal]);
    }

    public List<Card> getCards() {
        int numberOfCards = this.cardIDs.length;

        List<Card> cards = new ArrayList<>(numberOfCards);

        for (Integer cardID : this.cardIDs) {
            Card.get(cardID).ifPresentOrElse(
                    cards::add,
                    () -> ChangeRankRequest.logger.warn("Encountered invalid card ID number. id={}", cardID)
            );
        }

        return cards;
    }

    private ChangeRankRequest() {
        super(0);
    }
}