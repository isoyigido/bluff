package io.github.isoyigido.bluff.game.packets.broadcasts;

public class PlayerJoinedBroadcast {
    private int playerID;
    private String playerName;

    public PlayerJoinedBroadcast(int playerID, String playerName) {
        this.playerID = playerID;
        this.playerName = playerName;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    private PlayerJoinedBroadcast() {}
}