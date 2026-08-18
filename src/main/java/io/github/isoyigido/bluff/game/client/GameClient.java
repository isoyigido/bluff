package io.github.isoyigido.bluff.game.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import io.github.isoyigido.bluff.game.packets.broadcasts.*;
import io.github.isoyigido.bluff.game.packets.requests.*;
import io.github.isoyigido.bluff.game.server.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class GameClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GameClient.class);

    public static final class Player {
        private final int connectionID;
        private final String name;
        private int turnIndex = -1;

        private int numberOfCards = 0;

        private Player(int connectionID, String name) {
            this.connectionID = connectionID;
            this.name = name;
        }

        public int getConnectionID() {
            return this.connectionID;
        }

        public String getName() {
            return this.name;
        }

        public int getTurnIndex() {
            return this.turnIndex;
        }

        public int getNumberOfCards() {
            return this.numberOfCards;
        }
    }

    public static Optional<GameClient> get(String host, String name) {
        Client client = new Client();

        Kryo kryo = client.getKryo();
        GameServer.registerClasses(kryo);

        client.start();

        GameClient gameClient = new GameClient(client);

        int tcpPort = 54555;
        int udpPort = 54777;

        try {
            client.connect(10000, host, tcpPort, udpPort);
        } catch (Exception e) {
            logger.error("Encountered an error while connecting to client.", e);

            client.stop();

            return Optional.empty();
        }

        logger.info("Client connected. host={} tcp={} udp={}", host, tcpPort, udpPort);

        gameClient.thisPlayer = new Player(client.getID(), name);

        client.sendTCP(new JoinLobbyRequest(name));

        return Optional.of(gameClient);
    }

    private final Client client;

    private GameEventListener gameEventListener = null;

    private int turnID = 0;

    private Player thisPlayer;
    private List<Card> thisCards = new ArrayList<>(13);

    private final LinkedHashMap<Integer, Player> otherPlayers = new LinkedHashMap<>(4);

    private Player host = null;

    private GameServer.GameState gameState = GameServer.GameState.WAITING_FOR_PLAYERS;

    // - PLAYING -
    private Rank currentRank = Rank.TWO;
    private int numberOfCardsInTheMiddle = 0;

    private Player playerInTurn = null;

    private boolean allPassed = false;

    private Player lastPlayer = null;
    private int lastPlayedCardNumber = 0;

    // - CONCLUDED -
    private Player winner = null;

    private GameClient(Client client) {
        this.client = client;

        this.client.addListener(new Listener() {
            @Override
            public void disconnected(Connection connection) {
                GameClient.logger.info("Client disconnected. id={}", connection.getID());

                GameClient.this.close();
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof FrameworkMessage) {
                    GameClient.logger.debug("Received framework message. id={}", connection.getID());

                    return;
                }

                switch (object) {
                    case PlayerJoinedBroadcast playerJoinedBroadcast -> {
                        GameClient.logger.info("Client received player joined broadcast. id={} name={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name);

                        this.handlePlayerJoined(playerJoinedBroadcast.getPlayerID(), playerJoinedBroadcast.getPlayerName());
                    }

                    case PlayerDisconnectedBroadcast playerDisconnectedBroadcast -> {
                        GameClient.logger.info("Client received player disconnected broadcast. id={} name={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name);

                        this.handlePlayerDisconnected(playerDisconnectedBroadcast.getPlayerID());
                    }

                    case SetHostBroadcast setHostBroadcast -> {
                        GameClient.logger.info("Client received set host broadcast. id={} name={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name);

                        this.handleSetHost(setHostBroadcast.getHostID());
                    }

                    case SetGameStateBroadcast setGameStateBroadcast -> setGameStateBroadcast.getGameState().ifPresent(this::handleSetGameState);

                    case StartGameBroadcast startGameBroadcast -> {
                        int[] playerIDs = startGameBroadcast.getPlayerIDs();
                        int[] dealtCards = startGameBroadcast.getDealtCards();

                        GameClient.logger.info("Client received start game broadcast. id={} name={} dealtPlayerID={} numberOfCards={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name, Arrays.toString(playerIDs), Arrays.toString(dealtCards));

                        this.handleStartGame(playerIDs, dealtCards);
                    }

                    case SetCardsBroadcast setCardsBroadcast -> {
                        List<Card> cards = setCardsBroadcast.getCards();

                        GameClient.logger.info("Client received set cards broadcast. id={} name={} cards={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name, cards);

                        this.handleSetCards(cards);
                    }

                    case SetTurnBroadcast setTurnBroadcast -> {
                        int playerID = setTurnBroadcast.getPlayerID();

                        GameClient.logger.info("Client received set turn broadcast. id={} name={} playerInTurnID={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name, playerID);

                        this.handleSetTurn(playerID);
                    }

                    case PlayedCardsBroadcast playedCardsBroadcast -> {
                        GameClient.logger.info("Client received played cards broadcast. id={} name={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name);

                        playedCardsBroadcast.getCurrentRank().ifPresent(rank ->
                                this.handlePlayedCards(rank, playedCardsBroadcast.getNumberOfCards(),playedCardsBroadcast.getPlayedCards(), playedCardsBroadcast.getRemainingCards()));
                    }

                    case AnonymousPlayedCardsBroadcast anonymousPlayedCardsBroadcast -> {
                        GameClient.logger.info("Client received anonymous played cards broadcast. id={} name={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name);

                        anonymousPlayedCardsBroadcast.getCurrentRank().ifPresent(rank ->
                                this.handleAnonymousPlayedCards(anonymousPlayedCardsBroadcast.getPlayerID(), rank, anonymousPlayedCardsBroadcast.getNumberOfCards(), null));
                    }

                    case SetAllPassedBroadcast setAllPassedBroadcast -> this.handleSetAllPassed(setAllPassedBroadcast.didAllPass());

                    case CallBullshitBroadcast callBullshitBroadcast -> {
                        int accuserID = callBullshitBroadcast.getAccuserID();
                        int accusedID = callBullshitBroadcast.getAccusedID();
                        List<Card> playedCards = callBullshitBroadcast.getPlayedCards();
                        boolean bluff = callBullshitBroadcast.isBluff();

                        GameClient.logger.info("Client received call bullshit broadcast. id={} name={} accuserID={} accusedID={} playedCards={} bluff={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name, accuserID, accusedID, playedCards, bluff);

                        this.handleCallBullshit(accuserID, accusedID, playedCards, bluff);
                    }

                    case SetWinnerBroadcast setWinnerBroadcast -> {
                        int playerID = setWinnerBroadcast.getPlayerID();

                        GameClient.logger.info("Client received set winner broadcast. id={} name={} winnerID={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name, playerID);

                        this.handleSetWinner(playerID);
                    }

                    default -> GameClient.logger.warn("Client received invalid broadcast. id={} name={}", GameClient.this.thisPlayer.connectionID, GameClient.this.thisPlayer.name);
                }
            }

            private void handlePlayerJoined(int playerID, String playerName) {
                GameClient.this.otherPlayers.put(playerID, new Player(playerID, playerName));

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.playerConnected();
            }

            private void handlePlayerDisconnected(int playerID) {
                Player player = GameClient.this.otherPlayers.remove(playerID);

                if (player == null) return;

                if (GameClient.this.gameState == GameServer.GameState.PLAYING) {
                    GameClient.this.numberOfCardsInTheMiddle += player.numberOfCards;
                }

                if (player == GameClient.this.lastPlayer) {
                    GameClient.this.lastPlayer = null;
                    GameClient.this.lastPlayedCardNumber = 0;
                }

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.playerDisconnected();
            }

            private void handleSetHost(int hostID) {
                Player host = this.getPlayer(hostID);

                if (host == null) return;

                GameClient.this.host = host;

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.setHost();
            }

            private void handleSetGameState(GameServer.GameState gameState) {
                GameClient.this.gameState = gameState;

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.setGameState();
            }

            private void handleStartGame(int[] playerIDs, int[] dealtCards) {
                for (int i = 0; i < playerIDs.length; i++) {
                    Player player = this.getPlayer(playerIDs[i]);

                    if (player == null) continue;

                    player.numberOfCards = dealtCards[i];

                    player.turnIndex = i;
                }

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.startGame();
            }

            private void handleSetCards(Collection<Card> cards) {
                GameClient.this.thisCards = new ArrayList<>(cards);

                GameClient.this.thisPlayer.numberOfCards = GameClient.this.thisCards.size();

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.setCards();
            }

            private void handleSetTurn(int playerID) {
                Player playerInTurn = this.getPlayer(playerID);

                if (playerInTurn == null) return;

                GameClient.this.playerInTurn = playerInTurn;

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.setTurn();
            }

            private void handlePlayedCards(Rank currentRank, int numberOfCards, Collection<Card> playedCards, Collection<Card> remainingCards) {
                GameClient.this.thisCards = new ArrayList<>(remainingCards);

                this.handleAnonymousPlayedCards(GameClient.this.thisPlayer.connectionID, currentRank, numberOfCards, playedCards);
            }

            private void handleAnonymousPlayedCards(int playerID, Rank currentRank, int numberOfCards, Collection<Card> playedCards) {
                Player player = this.getPlayer(playerID);

                if (player == null) return;

                player.numberOfCards -= numberOfCards;

                GameClient.this.currentRank = currentRank;
                GameClient.this.numberOfCardsInTheMiddle += numberOfCards;

                GameClient.this.turnID++;

                GameClient.this.lastPlayer = player;
                GameClient.this.lastPlayedCardNumber = numberOfCards;

                if (GameClient.this.gameEventListener != null) {
                    if (playedCards == null) GameClient.this.gameEventListener.playedCards(player, currentRank, numberOfCards);
                    else GameClient.this.gameEventListener.playedCards(currentRank, playedCards);
                }
            }

            private void handleSetAllPassed(boolean allPassed) {
                GameClient.this.allPassed = allPassed;

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.setAllPassed();
            }

            private void handleCallBullshit(int accuserID, int accusedID, List<Card> playedCards, boolean bluff) {
                Player accuser = this.getPlayer(accuserID);

                if (accuser == null) return;

                Player accused = this.getPlayer(accusedID);

                if (accused == null) return;

                if (bluff) accused.numberOfCards += GameClient.this.numberOfCardsInTheMiddle;
                else       accuser.numberOfCards += GameClient.this.numberOfCardsInTheMiddle;

                GameClient.this.numberOfCardsInTheMiddle = 0;

                GameClient.this.lastPlayer = null;
                GameClient.this.lastPlayedCardNumber = 0;

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.calledBullshit(accuser, accused, playedCards, bluff);
            }

            private void handleSetWinner(int playerID) {
                Player winner = this.getPlayer(playerID);

                if (winner == null) return;

                GameClient.this.winner = winner;

                if (GameClient.this.gameEventListener != null) GameClient.this.gameEventListener.setWinner();
            }

            private Player getPlayer(int playerID) {
                Player player = (playerID == GameClient.this.thisPlayer.connectionID) ? GameClient.this.thisPlayer : GameClient.this.otherPlayers.get(playerID);

                if (player == null) GameClient.logger.warn("Received player ID is invalid. id={}", playerID);

                return player;
            }
        });
    }

    public void setGameEventListener(GameEventListener gameEventListener) {
        this.gameEventListener = gameEventListener;
    }

    @Override
    public void close() {
        this.client.stop();
    }

    public void startGame() {
        if ((this.thisPlayer != this.host) || (this.gameState != GameServer.GameState.WAITING_FOR_START)) return;

        this.client.sendTCP(new StartGameRequest());
    }

    public void playCards(List<Card> cards) {
        if (cards.isEmpty() || (this.gameState != GameServer.GameState.PLAYING) || !this.isThisPlayerInTurn()) return;

        this.client.sendTCP(new PlayCardsRequest(this.turnID, cards));
    }

    public void callBullshit() {
        if ((this.gameState != GameServer.GameState.PLAYING) || !this.isThisPlayerInTurn() || (this.lastPlayer == null) || (this.lastPlayer == this.thisPlayer)) return;

        this.client.sendTCP(new CallBullshitRequest(this.turnID, this.lastPlayer.connectionID));
    }

    public void pass() {
        if ((this.gameState != GameServer.GameState.PLAYING) || !this.isThisPlayerInTurn() || this.allPassed) return;

        this.client.sendTCP(new PassRequest(this.turnID));
    }

    public void changeRank(Rank rank, List<Card> cards) {
        if ((this.gameState != GameServer.GameState.PLAYING) || !this.isThisPlayerInTurn() || !this.allPassed) return;

        this.client.sendTCP(new ChangeRankRequest(this.turnID, rank.ordinal(), cards));
    }

    // --- GETTERS ---
    public Client getClient() {
        return this.client;
    }

    public Player getThisPlayer() {
        return this.thisPlayer;
    }

    public SequencedMap<Integer, Player> getOtherPlayers() {
        return this.otherPlayers;
    }

    public Player getHost() {
        return this.host;
    }

    public boolean isThisPlayerHost() {
        return this.thisPlayer == this.host;
    }

    public GameServer.GameState getGameState() {
        return this.gameState;
    }

    public List<Card> getThisCards() {
        return this.thisCards;
    }

    public Rank getCurrentRank() {
        return this.currentRank;
    }

    public int getNumberOfCardsInTheMiddle() {
        return this.numberOfCardsInTheMiddle;
    }

    public Player getPlayerInTurn() {
        return this.playerInTurn;
    }

    public boolean isThisPlayerInTurn() {
        return this.thisPlayer == this.playerInTurn;
    }

    public boolean didAllPass() {
        return this.allPassed;
    }

    public Player getLastPlayer() {
        return this.lastPlayer;
    }

    public boolean isThisPlayerTheLastPlayer() {
        return this.thisPlayer == this.lastPlayer;
    }

    public int getLastPlayedCardNumber() {
        return this.lastPlayedCardNumber;
    }

    public Player getWinner() {
        return this.winner;
    }
}