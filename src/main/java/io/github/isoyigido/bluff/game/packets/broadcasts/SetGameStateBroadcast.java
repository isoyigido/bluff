package io.github.isoyigido.bluff.game.packets.broadcasts;

import io.github.isoyigido.bluff.game.server.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SetGameStateBroadcast {
    private static final Logger logger = LoggerFactory.getLogger(SetGameStateBroadcast.class);

    private int gameStateOrdinal;

    public SetGameStateBroadcast(int gameStateOrdinal) {
        this.gameStateOrdinal = gameStateOrdinal;
    }

    public Optional<GameServer.GameState> getGameState() {
        GameServer.GameState[] values = GameServer.GameState.values();

        if ((this.gameStateOrdinal < 0) || (this.gameStateOrdinal >= values.length)) {
            SetGameStateBroadcast.logger.warn("Encountered invalid game state ordinal. ordinal={}", this.gameStateOrdinal);

            return Optional.empty();
        }

        return Optional.of(values[this.gameStateOrdinal]);
    }

    private SetGameStateBroadcast() {}
}