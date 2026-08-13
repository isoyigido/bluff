package io.github.isoyigido.bluff.game.packets.broadcasts;

public class SetHostBroadcast {
    private int hostID;

    public SetHostBroadcast(int hostID) {
        this.hostID = hostID;
    }

    public int getHostID() {
        return this.hostID;
    }

    private SetHostBroadcast() {}
}