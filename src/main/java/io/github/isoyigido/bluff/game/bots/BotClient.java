package io.github.isoyigido.bluff.game.bots;

import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.game.client.GameEventListener;
import io.github.isoyigido.bluff.game.server.GameServer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BotClient {
    private static final String[] BOT_NAMES = new String[] {
            "John", "Mike", "Dave", "Chris", "James", "Tom", "Dan", "Rob", "Alex", "Joe",
            "Mark", "Paul", "Ryan", "Kevin", "Luke", "Matt", "Will", "Sam", "Nick", "Ben",
            "Mary", "Lisa", "Anna", "Sarah", "Emily", "Emma", "Jess", "Amy", "Kate", "Mia",
            "Jack", "Harry", "Leo", "Charlie", "George", "Oliver", "Noah", "Jacob", "Liam", "Ethan",
            "Sophia", "Olivia", "Ava", "Isabella", "Chloe", "Lily", "Zoe", "Grace", "Ruby", "Ella",
            "Frank", "Fred", "Harry", "Arthur", "Walter", "Albert", "Louis", "Clara", "Alice", "Rose",
            "Tony", "Vinnie", "Sal", "Carmine", "Rocco", "Bruno", "Sonny", "Silvio", "Paulie", "Gino",
            "Hank", "Chet", "Duke", "Mac", "Spike", "Buster", "Buddy", "Rusty", "Skip", "Sonny",
            "Abby", "Becky", "Cindy", "Donna", "Elena", "Fiona", "Gina", "Heidi", "Iris", "Judy"
    };

    public static void add() {
        GameClient.get("localhost", "[BOT] " + BOT_NAMES[BotClient.random.nextInt(0, BOT_NAMES.length)]).map(BotClient::new);
    }

    private static final Random random = new Random();

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private final GameClient gameClient;

    private boolean inDelay = false;

    private int numberOfMatchingCards = -1;

    private BotClient(GameClient gameClient) {
        this.gameClient = gameClient;

        gameClient.setGameEventListener(new GameEventListener(){
            @Override
            public void setTurn() {
                if (!gameClient.isThisPlayerInTurn() || BotClient.this.inDelay) return;

                BotClient.this.inDelay = true;
                BotClient.scheduler.schedule(BotClient.this::play, BotClient.getDelay(), TimeUnit.MILLISECONDS);
            }

            @Override
            public void calledBullshit(GameClient.Player accuser, GameClient.Player accused, List<Card> cards, boolean bluff) {
                BotClient.this.numberOfMatchingCards = -1;
            }

            @Override
            public void setGameState() {
                if (gameClient.getGameState() == GameServer.GameState.CONCLUDED) {
                    long delay = BotClient.random.nextInt(3000, 5000);
                    BotClient.scheduler.schedule(gameClient::close, delay, TimeUnit.MILLISECONDS);
                }
            }
        });

        if (!gameClient.isThisPlayerInTurn() || this.inDelay) return;

        this.inDelay = true;
        BotClient.scheduler.schedule(this::play, BotClient.getDelay(), TimeUnit.MILLISECONDS);
    }

    private void play() {
        this.makeMove();

        this.inDelay = false;

        if (!this.gameClient.isThisPlayerInTurn()) return;

        this.inDelay = true;
        BotClient.scheduler.schedule(this::play, BotClient.getDelay(), TimeUnit.MILLISECONDS);
    }

    private void makeMove() {
        if (!this.gameClient.isThisPlayerInTurn()) return;

        int lastPlayedCardNumber = this.gameClient.getLastPlayedCardNumber();

        if (lastPlayedCardNumber > 4) {
            this.gameClient.callBullshit();

            return;
        }

        List<Card> cards = this.gameClient.getThisCards();

        if (cards.isEmpty()) return;

        Rank currentRank = this.gameClient.getCurrentRank();

        List<Card> matchingCards = cards.stream().filter(card -> card.rank() == currentRank).toList();

        if (this.numberOfMatchingCards == -1) this.numberOfMatchingCards = matchingCards.size();

        if ((this.numberOfMatchingCards + lastPlayedCardNumber) > 4) {
            this.gameClient.callBullshit();

            return;
        }

        if (BotClient.random.nextInt(0, 4) == 0) this.gameClient.callBullshit();

        if (!matchingCards.isEmpty()) {
            if (BotClient.random.nextBoolean()) this.gameClient.playCards(matchingCards);

            else {
                List<Card> notMatchingCards = cards.stream().filter(card -> card.rank() != currentRank).toList();

                this.gameClient.playCards(notMatchingCards.subList(0, Math.min(notMatchingCards.size(), matchingCards.size())));
            }

            return;
        }

        if (!this.gameClient.didAllPass()) {
            this.gameClient.pass();

            return;
        }

        Card firstCard = cards.getFirst();

        Rank firstCardRank = firstCard.rank();

        matchingCards = cards.stream().filter(card -> card.rank() == firstCardRank).toList();

        if (BotClient.random.nextBoolean()) this.gameClient.changeRank(firstCardRank, matchingCards);

        else {
            List<Card> notMatchingCards = cards.stream().filter(card -> card.rank() != firstCardRank).toList();

            this.gameClient.changeRank(firstCardRank, notMatchingCards.subList(0, Math.min(notMatchingCards.size(), matchingCards.size())));
        }
    }

    private static int getDelay() {
        return BotClient.random.nextInt(3000, 5000);
    }
}