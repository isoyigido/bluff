package io.github.isoyigido.bluff.game.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import io.github.isoyigido.bluff.game.cards.Suit;
import io.github.isoyigido.bluff.game.packets.broadcasts.*;
import io.github.isoyigido.bluff.game.packets.requests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public final class GameServer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);

    private static final class Player {
        private final int connectionID;
        private final String name;

        private final List<Card> cards = new ArrayList<>(8);
        private boolean pass = false;

        private Player(int connectionID, String name) {
            this.connectionID = connectionID;
            this.name = name;
        }
    }

    public enum GameState {
        WAITING_FOR_PLAYERS,
        WAITING_FOR_START,
        PLAYING,
        CONCLUDED
    }

    public static Optional<GameServer> host(int maximumNumberOfPlayers) {
        Server server = new Server();

        Kryo kryo = server.getKryo();
        registerClasses(kryo);

        int tcpPort = 54555;
        int udpPort = 54777;

        try {
            server.bind(tcpPort, udpPort);
        } catch (IOException e) {
            logger.error("Encountered an error while trying to bind server.", e);

            return Optional.empty();
        }

        logger.info("Started server. tcp={} udp={}", tcpPort, udpPort);

        server.start();

        return Optional.of(new GameServer(server, maximumNumberOfPlayers));
    }

    public static void registerClasses(Kryo kryo) {
        kryo.register(int[].class);
        kryo.register(Request.class);
        kryo.register(JoinLobbyRequest.class);
        kryo.register(StartGameRequest.class);
        kryo.register(PlayCardsRequest.class);
        kryo.register(CallBullshitRequest.class);
        kryo.register(PassRequest.class);
        kryo.register(ChangeRankRequest.class);
        kryo.register(PlayerJoinedBroadcast.class);
        kryo.register(PlayerDisconnectedBroadcast.class);
        kryo.register(SetHostBroadcast.class);
        kryo.register(SetGameStateBroadcast.class);
        kryo.register(StartGameBroadcast.class);
        kryo.register(SetCardsBroadcast.class);
        kryo.register(SetTurnBroadcast.class);
        kryo.register(PlayedCardsBroadcast.class);
        kryo.register(AnonymousPlayedCardsBroadcast.class);
        kryo.register(SetAllPassedBroadcast.class);
        kryo.register(CallBullshitBroadcast.class);
        kryo.register(SetWinnerBroadcast.class);
    }

    private final Server server;

    private Player host = null;

    private final LinkedHashMap<Integer, Player> lobby = new LinkedHashMap<>(4);

    private int turnID = -1;

    private GameState gameState = GameState.WAITING_FOR_PLAYERS;

    // - PLAYING -
    private Player playerInTurn = null;
    private int turn = -1;

    private Rank currentRank = Rank.TWO;

    private boolean allPassed = false;
    
    private final List<Card> cardsInTheMiddle = new ArrayList<>(8);

    private Player lastPlayer = null;
    private List<Card> lastPlayedCards = null;
    private boolean lastWasBluff = false;

    // - CONCLUDED -
    private Player winner = null;

    private GameServer(Server server, int maximumNumberOfPlayers) {
        this.server = server;

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                GameServer.logger.info("Client connected. id={}", connection.getID());
            }

            @Override
            public void disconnected(Connection connection) {
                int connectionID = connection.getID();

                GameServer.logger.info("Client disconnected. id={}", connectionID);

                this.handleDisconnection(connectionID);
            }

            @Override
            public void received(Connection connection, Object object) {
                int connectionID = connection.getID();

                if (object instanceof FrameworkMessage) {
                    GameServer.logger.debug("Received framework message. id={}", connectionID);

                    return;
                }

                if (!(object instanceof Request request)) {
                    GameServer.logger.warn("Server received invalid object.");

                    return;
                }

                if (!request.isInSync(GameServer.this.turnID)) {
                    GameServer.logger.warn("Received out-of-sync request.");

                    return;
                }

                if (request instanceof JoinLobbyRequest joinLobbyRequest) {
                    String name = joinLobbyRequest.getPlayerName();

                    GameServer.logger.info("Player wants to join the lobby. id={} name={}", connectionID, name);

                    this.handleJoinLobbyRequest(connection, name);

                    return;
                }

                Player player = GameServer.this.lobby.get(connectionID);

                if (player == null) {
                    GameServer.logger.warn("Unable to find player. id={}", connectionID);

                    return;
                }

                switch (request) {
                    case StartGameRequest _ -> {
                        GameServer.logger.info("Player wants to start the game. id={} name={}", connectionID, player.name);

                        this.handleStartGameRequest(player);
                    }

                    case PlayCardsRequest playCardsRequest -> {
                        List<Card> cards = playCardsRequest.getCards();

                        GameServer.logger.info("Player wants to play cards. id={} name={} cards={}", connectionID, player.name, cards);

                        this.handlePlayCardsRequest(player, cards);
                    }

                    case CallBullshitRequest callBullshitRequest -> {
                        int accusedPlayerID = callBullshitRequest.getAccusedPlayerID();

                        GameServer.logger.info("Player wants to call bullshit. id={} name={} accusedID={}", connectionID, player.name, accusedPlayerID);

                        this.handleCallBullshitRequest(player, accusedPlayerID);
                    }

                    case PassRequest _ -> {
                        GameServer.logger.info("Player wants to pass. id={} name={}", connectionID, player.name);

                        this.handlePassRequest(player);
                    }

                    case ChangeRankRequest changeRankRequest -> changeRankRequest.getRank().ifPresent(rank -> {
                        List<Card> cards = changeRankRequest.getCards();

                        GameServer.logger.info("Player wants to change rank. id={} name={} rank={} cards={}", connectionID, player.name, rank.name(), cards);

                        this.handleChangeRankRequest(player, rank, cards);
                    });

                    default -> GameServer.logger.warn("Server received invalid request.");
                }
            }

            private void handleJoinLobbyRequest(Connection connection, String name) {
                int connectionID = connection.getID();

                if ((GameServer.this.gameState != GameState.WAITING_FOR_PLAYERS) && (GameServer.this.gameState != GameState.WAITING_FOR_START)) {
                    GameServer.logger.warn("Player tried to join a game that has already started. Closing connection. id={}", connectionID);

                    connection.close();

                    return;
                }

                if (GameServer.this.lobby.size() >= maximumNumberOfPlayers) {
                    GameServer.logger.warn("Player tried to join a full lobby. Closing connection. id={}", connectionID);

                    connection.close();

                    return;
                }

                Player player = new Player(connectionID, name);

                GameServer.this.lobby.put(connectionID, player);

                GameServer.logger.info("Player joined the lobby. id={}", connectionID);

                for (Player otherPlayer : GameServer.this.lobby.sequencedValues()) {
                    if (otherPlayer == player) continue;

                    GameServer.this.server.sendToTCP(connectionID, new PlayerJoinedBroadcast(otherPlayer.connectionID, otherPlayer.name));
                }

                GameServer.this.server.sendToAllExceptTCP(connectionID, new PlayerJoinedBroadcast(connectionID, player.name));

                if (GameServer.this.host == null) this.setHost(player);
                else connection.sendTCP(new SetHostBroadcast(GameServer.this.host.connectionID));

                connection.sendTCP(new SetGameStateBroadcast(GameServer.this.gameState.ordinal()));

                if ((GameServer.this.lobby.size() > 1) && (GameServer.this.gameState == GameState.WAITING_FOR_PLAYERS)) {
                    this.setGameState(GameState.WAITING_FOR_START);
                }
            }

            private void handleDisconnection(int connectionID) {
                Player player = GameServer.this.lobby.remove(connectionID);

                if (player == null) return;

                GameServer.logger.info("Player left the lobby. id={} name={}", connectionID, player.name);

                int numberOfPlayers = GameServer.this.lobby.size();

                if (numberOfPlayers < 1) {
                    server.stop();

                    GameServer.logger.info("Terminated empty game server.");

                    return;
                }

                if (player == GameServer.this.host) {
                    this.setHost(GameServer.this.lobby.sequencedValues().getFirst());
                }

                if ((GameServer.this.gameState == GameState.WAITING_FOR_START) && (numberOfPlayers < 2)) {
                    this.setGameState(GameState.WAITING_FOR_PLAYERS);

                    GameServer.this.server.sendToAllExceptTCP(connectionID, new PlayerDisconnectedBroadcast(connectionID));

                    return;
                }

                if (GameServer.this.gameState == GameState.PLAYING) {
                    GameServer.this.cardsInTheMiddle.addAll(0, player.cards);

                    GameServer.logger.info("Added the cards of the disconnected player to the middle. cards={}", player.cards);

                    this.updateAllPassedStatus();

                    if (player == GameServer.this.lastPlayer) {
                        GameServer.this.lastPlayer = null;
                        GameServer.this.lastPlayedCards = null;
                        GameServer.this.lastWasBluff = false;
                    }

                    if (player == GameServer.this.playerInTurn) {
                        this.nextTurn();
                    }

                    GameServer.this.server.sendToAllExceptTCP(connectionID, new PlayerDisconnectedBroadcast(connectionID));

                    if (numberOfPlayers < 2) {
                        this.setWinner(GameServer.this.lobby.sequencedValues().getFirst());
                    }

                    return;
                }

                GameServer.this.server.sendToAllExceptTCP(connectionID, new PlayerDisconnectedBroadcast(connectionID));
            }

            private void handleStartGameRequest(Player host) {
                if (host != GameServer.this.host) {
                    GameServer.logger.warn("Only the host can start the game.");

                    return;
                }

                if (GameServer.this.gameState == GameState.WAITING_FOR_PLAYERS) {
                    GameServer.logger.warn("Cannot start game because there are not enough players.");

                    return;
                }

                if (GameServer.this.gameState != GameState.WAITING_FOR_START) {
                    GameServer.logger.warn("Cannot start game because it has already started.");

                    return;
                }

                this.setGameState(GameState.PLAYING);

                GameServer.logger.info("Game started. Dealing the cards.");

                Card[] shuffledDeck = Card.getShuffledDeck();

                List<Player> players = new ArrayList<>(GameServer.this.lobby.sequencedValues());

                int numberOfPlayers = players.size();

                List<List<Card>> cardsToDeal = new ArrayList<>(numberOfPlayers);

                for (int i = 0; i < numberOfPlayers; i++) {
                    cardsToDeal.add(new ArrayList<>((int) Math.ceil((float) shuffledDeck.length / numberOfPlayers)));
                }

                int firstPlayerIndex = 0;

                int playerIndex = 0;

                for (Card card : shuffledDeck) {
                    cardsToDeal.get(playerIndex).add(card);

                    if (card.equals(Card.of(Suit.CLUBS, Rank.TWO))) firstPlayerIndex = playerIndex;

                    playerIndex = (playerIndex + 1) % numberOfPlayers;
                }

                int[] playerIDs = new int[numberOfPlayers];
                int[] dealtCards = new int[numberOfPlayers];

                for (int i = 0; i < numberOfPlayers; i++) {
                    Player playerToDeal = players.get(i);

                    List<Card> cards = cardsToDeal.get(i);

                    playerIDs[i] = playerToDeal.connectionID;
                    dealtCards[i] = cards.size();

                    playerToDeal.cards.addAll(cards);

                    GameServer.logger.info("Dealt cards to player. id={} name={} cards={}", playerToDeal.connectionID, playerToDeal.name, cards);
                }

                GameServer.logger.info("Dealt all cards.");

                GameServer.this.server.sendToAllTCP(new StartGameBroadcast(playerIDs, dealtCards));

                for (Player player : players) {
                    GameServer.this.server.sendToTCP(player.connectionID, new SetCardsBroadcast(player.cards));
                }

                this.setTurn(players.get(firstPlayerIndex));
            }

            private boolean handlePlayCardsRequest(Player player, Collection<Card> cards) {
                if (GameServer.this.gameState == GameState.CONCLUDED) {
                    GameServer.logger.warn("Cannot play cards because the game has already concluded.");

                    return false;
                }

                if (GameServer.this.gameState != GameState.PLAYING) {
                    GameServer.logger.warn("Cannot play cards because the game has not started yet.");

                    return false;
                }

                if (player != GameServer.this.playerInTurn) {
                    GameServer.logger.warn("Cannot play cards because player is not in turn. id={} name={}", player.connectionID, player.name);

                    return false;
                }

                List<Card> cardsToPlay = new ArrayList<>(cards.size());

                for (Card card : cards) {
                    if (player.cards.contains(card)) cardsToPlay.add(card);
                    else GameServer.logger.warn("Player does not have the card to play. id={} name={} card={}", player.connectionID, player.name, card);
                }

                if (cardsToPlay.isEmpty()) {
                    GameServer.logger.warn("Players must play at least one card. id={} name={}", player.connectionID, player.name);

                    return false;
                }

                boolean finisher = player.cards.size() == cardsToPlay.size();

                boolean bluff = cardsToPlay.stream().map(Card::rank).anyMatch(rank -> rank != GameServer.this.currentRank);

                if (finisher && bluff) {
                    GameServer.logger.warn("Finishing move cannot be a bluff. id={} name={}", player.connectionID, player.name);

                    return false;
                }

                player.cards.removeAll(cardsToPlay);

                GameServer.this.cardsInTheMiddle.addAll(cardsToPlay);

                GameServer.this.lastPlayer = player;
                GameServer.this.lastPlayedCards = cardsToPlay;
                GameServer.this.lastWasBluff = bluff;

                int currentRankOrdinal = GameServer.this.currentRank.ordinal();
                int numberOfPlayedCards = cardsToPlay.size();

                this.resetPassStatusForAllPlayers();

                player.pass = true;

                GameServer.logger.info("Player played cards. id={} name={} cards={}", player.connectionID, player.name, cardsToPlay);

                GameServer.this.server.sendToAllExceptTCP(player.connectionID, new AnonymousPlayedCardsBroadcast(player.connectionID, currentRankOrdinal, numberOfPlayedCards));
                GameServer.this.server.sendToTCP(player.connectionID, new PlayedCardsBroadcast(currentRankOrdinal, numberOfPlayedCards, player.cards));

                if (finisher) this.setWinner(player);

                this.nextTurn();

                return true;
            }

            private void handleCallBullshitRequest(Player player, int accusedPlayerID) {
                if (GameServer.this.gameState == GameState.CONCLUDED) {
                    GameServer.logger.warn("Cannot call bullshit because the game has already concluded.");

                    return;
                }

                if (GameServer.this.gameState != GameState.PLAYING) {
                    GameServer.logger.warn("Cannot call bullshit because the game has not started yet.");

                    return;
                }

                if (player != GameServer.this.playerInTurn) {
                    GameServer.logger.warn("Cannot call bullshit because player is not in turn. id={} name={}", player.connectionID, player.name);

                    return;
                }

                if (GameServer.this.lastPlayedCards == null) {
                    GameServer.logger.warn("Cannot call bullshit because no cards have been played yet. id={} name={}", player.connectionID, player.name);

                    return;
                }

                Player accusedPlayer = GameServer.this.lobby.get(accusedPlayerID);

                if (accusedPlayer == null) {
                    GameServer.logger.warn("Cannot accuse player because no player exists with the given ID. accusedID={}", accusedPlayerID);

                    return;
                }

                if (accusedPlayer == player) {
                    GameServer.logger.warn("Players cannot accuse themselves. id={} name={}", player.connectionID, player.name);

                    return;
                }

                if (accusedPlayer != GameServer.this.lastPlayer) {
                    GameServer.logger.warn("Cannot accuse a player who did not make the last move. accusedID={} accusedName={}", accusedPlayerID, accusedPlayer.name);

                    return;
                }

                GameServer.logger.info("Player called bullshit. accuserID={} accuserName={} accusedID={} accusedName={}", player.connectionID, player.name, accusedPlayerID, accusedPlayer.name);

                Player rightPlayer = GameServer.this.lastWasBluff ? player : accusedPlayer;
                Player wrongPlayer = GameServer.this.lastWasBluff ? accusedPlayer : player;

                if (GameServer.this.lastWasBluff) GameServer.logger.info("Player has been caught. accuserID={} accuserName={} accusedID={} accusedName={}", player.connectionID, player.name, accusedPlayerID, accusedPlayer.name);
                else GameServer.logger.info("Player has been falsely accused. accuserID={} accuserName={} accusedID={} accusedName={}", player.connectionID, player.name, accusedPlayerID, accusedPlayer.name);

                wrongPlayer.cards.addAll(GameServer.this.cardsInTheMiddle);

                GameServer.this.cardsInTheMiddle.clear();

                GameServer.this.server.sendToAllTCP(new CallBullshitBroadcast(player.connectionID, accusedPlayerID, GameServer.this.lastPlayedCards, GameServer.this.lastWasBluff));

                GameServer.this.lastPlayer = null;
                GameServer.this.lastPlayedCards = null;
                GameServer.this.lastWasBluff = false;

                this.setAllPassedStatus(true);

                this.setTurn(rightPlayer);

                GameServer.this.server.sendToTCP(wrongPlayer.connectionID, new SetCardsBroadcast(wrongPlayer.cards));
            }

            private void handlePassRequest(Player player) {
                if (GameServer.this.gameState == GameState.CONCLUDED) {
                    GameServer.logger.warn("Cannot pass because the game has already concluded.");

                    return;
                }

                if (GameServer.this.gameState != GameState.PLAYING) {
                    GameServer.logger.warn("Cannot pass because the game has not started yet.");

                    return;
                }

                if (player != GameServer.this.playerInTurn) {
                    GameServer.logger.warn("Cannot pass because player is not in turn. id={} name={}", player.connectionID, player.name);

                    return;
                }

                if (GameServer.this.allPassed && (player == GameServer.this.lastPlayer)) {
                    GameServer.logger.warn("The last player to move cannot pass after everyone has passed. id={} name={}", player.connectionID, player.name);

                    return;
                }

                if (GameServer.this.lastPlayer == null) {
                    GameServer.logger.warn("Cannot pass on the first turn. id={} name={}", player.connectionID, player.name);

                    return;
                }

                GameServer.logger.info("Player passed. id={} name={}", player.connectionID, player.name);

                player.pass = true;

                this.updateAllPassedStatus();

                this.nextTurn();
            }

            private void handleChangeRankRequest(Player player, Rank rank, Collection<Card> cards) {
                if (GameServer.this.gameState == GameState.CONCLUDED) {
                    GameServer.logger.warn("Cannot change rank because the game has already concluded.");

                    return;
                }

                if (GameServer.this.gameState != GameState.PLAYING) {
                    GameServer.logger.warn("Cannot change rank because the game has not started yet.");

                    return;
                }

                if (player != GameServer.this.playerInTurn) {
                    GameServer.logger.warn("Cannot change rank because player is not in turn. id={} name={}", player.connectionID, player.name);

                    return;
                }

                if (!GameServer.this.allPassed) {
                    GameServer.logger.warn("Cannot change rank because not all players have passed. id={} name={}", player.connectionID, player.name);

                    return;
                }

                Rank oldRank = GameServer.this.currentRank;

                GameServer.this.currentRank = rank;

                if (!this.handlePlayCardsRequest(player, cards)) {
                    GameServer.this.currentRank = oldRank;
                }
            }

            private void setHost(Player player) {
                GameServer.this.host = player;

                GameServer.logger.info("Player is now host. id={} name={}", player.connectionID, player.name);

                GameServer.this.server.sendToAllTCP(new SetHostBroadcast(player.connectionID));
            }

            private void setGameState(GameState gameState) {
                GameServer.this.gameState = gameState;

                GameServer.logger.info("Changed game state. state={}", gameState.name());

                GameServer.this.server.sendToAllTCP(new SetGameStateBroadcast(gameState.ordinal()));
            }

            private void nextTurn() {
                List<Player> players = new ArrayList<>(GameServer.this.lobby.sequencedValues());

                if (players.isEmpty()) return;

                GameServer.this.playerInTurn = players.get(++GameServer.this.turn % players.size());

                GameServer.logger.info("Player is now in turn. id={} name={}", GameServer.this.playerInTurn.connectionID, GameServer.this.playerInTurn.name);

                GameServer.this.turnID++;

                GameServer.this.server.sendToAllTCP(new SetTurnBroadcast(GameServer.this.playerInTurn.connectionID));
            }

            private void setTurn(Player player) {
                List<Player> players = new ArrayList<>(GameServer.this.lobby.sequencedValues());

                GameServer.this.turn = players.indexOf(player) - 1;

                this.nextTurn();
            }

            private void resetPassStatusForAllPlayers() {
                GameServer.this.lobby.values().forEach(player -> player.pass = false);
                
                this.updateAllPassedStatus();
            }
            
            private void updateAllPassedStatus() {
                this.setAllPassedStatus(GameServer.this.lobby.values().stream().allMatch(player -> player.pass));
            }

            private void setAllPassedStatus(boolean allPassed) {
                GameServer.this.allPassed = allPassed;

                GameServer.this.server.sendToAllTCP(new SetAllPassedBroadcast(GameServer.this.allPassed));
            }

            private void setWinner(Player winner) {
                GameServer.logger.info("Player won the game. id={} name={}", winner.connectionID, winner.name);

                this.setGameState(GameState.CONCLUDED);

                GameServer.this.winner = winner;

                GameServer.this.server.sendToAllTCP(new SetWinnerBroadcast(winner.connectionID));
            }
        });
    }

    @Override
    public void close() {
        this.server.stop();
    }
}