package io.github.isoyigido.bluff.game.packets.broadcasts;

public class SetTurnBroadcast {
    private int playerID;

    public SetTurnBroadcast(int playerID) {
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    private SetTurnBroadcast() {}
}