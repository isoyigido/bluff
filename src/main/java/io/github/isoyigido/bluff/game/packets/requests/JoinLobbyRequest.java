package io.github.isoyigido.bluff.game.packets.requests;

public final class JoinLobbyRequest extends Request {
    private String playerName;

    public JoinLobbyRequest(String playerName) {
        super(-1);

        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    private JoinLobbyRequest() {
        super(-1);
    }
}