package io.github.isoyigido.bluff.game.packets.broadcasts;

public class PlayerDisconnectedBroadcast {
    private int playerID;

    public PlayerDisconnectedBroadcast(int playerID) {
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    private PlayerDisconnectedBroadcast() {}
}