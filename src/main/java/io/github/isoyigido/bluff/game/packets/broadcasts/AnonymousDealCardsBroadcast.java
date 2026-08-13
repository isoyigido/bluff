package io.github.isoyigido.bluff.game.packets.broadcasts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnonymousDealCardsBroadcast {
    private static final Logger logger = LoggerFactory.getLogger(AnonymousDealCardsBroadcast.class);

    private int playerID;
    private int numberOfCards;

    public AnonymousDealCardsBroadcast(int playerID, int numberOfCards) {
        this.playerID = playerID;
        this.numberOfCards = numberOfCards;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public int getNumberOfCards() {
        return this.numberOfCards;
    }

    private AnonymousDealCardsBroadcast() {}
}