package io.github.isoyigido.bluff.game.client;

import io.github.isoyigido.bluff.game.cards.Card;

import java.util.List;

public class GameEventListener {
    public void playerConnected() {}

    public void playerDisconnected() {}

    public void setHost() {}

    public void setGameState() {}

    public void setCards() {}

    public void setTurn() {}

    public void playedCards() {}

    public void calledBullshit(GameClient.Player accuser, GameClient.Player accused, List<Card> cards, boolean bluff) {}

    public void setAllPassed() {}
}