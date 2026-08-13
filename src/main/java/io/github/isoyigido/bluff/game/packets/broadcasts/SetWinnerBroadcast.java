package io.github.isoyigido.bluff.game.packets.broadcasts;

public class SetWinnerBroadcast {
    private int playerID;

    public SetWinnerBroadcast(int playerID) {
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    private SetWinnerBroadcast() {}
}