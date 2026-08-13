package io.github.isoyigido.bluff.game.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record Card(Suit suit, Rank rank) {
    private static final Card[] DECK = getDeck();

    private static Card[] getDeck() {
        Suit[] suitValues = Suit.values();
        Rank[] rankValues = Rank.values();

        int numberOfSuits = suitValues.length;
        int numberOfRanks = rankValues.length;

        Card[] deck = new Card[numberOfSuits * numberOfRanks];

        for (int id = 0; id < deck.length; id++) {

            int suitOrdinal = id / numberOfRanks;

            int rankOrdinal = id % numberOfRanks;

            deck[id] = new Card(suitValues[suitOrdinal], rankValues[rankOrdinal]);
        }

        return deck;
    }

    public static Card of(Suit suit, Rank rank) {
        return DECK[getID(suit, rank)];
    }

    private static int getID(Suit suit, Rank rank) {
        return (suit.ordinal() * Rank.values().length) + rank.ordinal();
    }

    public int getID() {
        return Card.getID(this.suit, this.rank);
    }

    public static Optional<Card> get(int id) {
        if ((id < 0) || (id >= DECK.length)) return Optional.empty();

        return Optional.of(DECK[id]);
    }

    public static Card[] getShuffledDeck() {
        List<Card> cards = new ArrayList<>(List.of(Card.DECK));

        Collections.shuffle(cards);

        return cards.toArray(new Card[0]);
    }

    @Override
    public String toString() {
        return this.rank.name().toUpperCase() + " OF " + this.suit.name().toUpperCase();
    }
}