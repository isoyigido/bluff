package io.github.isoyigido.bluff.game.packets.requests;

public final class PassRequest extends Request {
    public PassRequest(int turnID) {
        super(turnID);
    }

    private PassRequest() {
        super(0);
    }
}