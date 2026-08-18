package io.github.isoyigido.bluff.game.packets.broadcasts;

public class StartGameBroadcast {
    private int[] playerIDs;
    private int[] dealtCards;

    public StartGameBroadcast(int[] playerIDs, int[] dealtCards) {
        this.playerIDs = playerIDs;
        this.dealtCards = dealtCards;
    }

    public int[] getPlayerIDs() {
        return this.playerIDs;
    }

    public int[] getDealtCards() {
        return this.dealtCards;
    }

    private StartGameBroadcast() {}
}