package io.github.isoyigido.bluff.game.packets.requests;

public sealed class Request permits JoinLobbyRequest, StartGameRequest, PlayCardsRequest, CallBullshitRequest, PassRequest, ChangeRankRequest {
    private final int turnID;

    Request(int turnID) {
        this.turnID = turnID;
    }

    public boolean isInSync(int turnID) {
        // TODO: change
        // return this.turnID == turnID;

        return true;
    }

    private Request() {
        this(0);
    }
}