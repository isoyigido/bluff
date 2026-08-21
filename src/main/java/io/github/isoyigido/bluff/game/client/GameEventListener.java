package io.github.isoyigido.bluff.game.client;

import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;

import java.util.Collection;
import java.util.List;

public class GameEventListener {
    public void playerConnected() {}

    public void playerDisconnected() {}

    public void setHost() {}

    public void setGameState() {}

    public void startGame() {}

    public void setCards() {}

    public void setTurn() {}

    public void playedCards(Rank rank, Collection<Card> playedCards) {}

    public void playedCards(GameClient.Player player, Rank rank, int numberOfCards) {}

    public void calledBullshit(GameClient.Player accuser, GameClient.Player accused, List<Card> playedCards, boolean bluff, int numberOfMiddleCards, List<Card> middleCards) {}

    public void setAllPassed() {}

    public void setWinner() {}
}