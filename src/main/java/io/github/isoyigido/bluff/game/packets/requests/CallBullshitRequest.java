package io.github.isoyigido.bluff.game.packets.requests;

public final class CallBullshitRequest extends Request {
    private int accusedPlayerID;

    public CallBullshitRequest(int turnID, int accusedPlayerID) {
        super(turnID);

        this.accusedPlayerID = accusedPlayerID;
    }

    public int getAccusedPlayerID() {
        return this.accusedPlayerID;
    }

    private CallBullshitRequest() {
        super(0);
    }
}