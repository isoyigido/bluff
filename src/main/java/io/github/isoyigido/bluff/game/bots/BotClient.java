package io.github.isoyigido.bluff.game.bots;

import io.github.isoyigido.bluff.game.cards.Card;
import io.github.isoyigido.bluff.game.cards.Rank;
import io.github.isoyigido.bluff.game.client.GameClient;
import io.github.isoyigido.bluff.game.client.GameEventListener;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

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

    private static final RandomGenerator random = new Random();

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private BotClient(GameClient gameClient) {
        gameClient.setGameEventListener(new GameEventListener(){
            private int numberOfMatchingCards = -1;

            @Override
            public void setTurn() {
                if (!gameClient.isThisPlayerInTurn()) return;

                long delay = BotClient.random.nextInt(3000, 5000);
                BotClient.scheduler.schedule(this::play, delay, TimeUnit.MILLISECONDS);
            }

            @Override
            public void calledBullshit(GameClient.Player accuser, GameClient.Player accused, List<Card> cards, boolean bluff) {
                this.numberOfMatchingCards = -1;
            }

            @Override
            public void setWinner() {
                long delay = BotClient.random.nextInt(3000, 5000);
                BotClient.scheduler.schedule(gameClient::close, delay, TimeUnit.MILLISECONDS);
            }

            private void play() {
                int lastPlayedCardNumber = gameClient.getLastPlayedCardNumber();

                if (lastPlayedCardNumber > 4) {
                    gameClient.callBullshit();

                    return;
                }

                List<Card> cards = gameClient.getThisCards();

                if (cards.isEmpty()) return;

                Rank currentRank = gameClient.getCurrentRank();

                List<Card> matchingCards = cards.stream().filter(card -> card.rank() == currentRank).toList();

                if (this.numberOfMatchingCards == -1) this.numberOfMatchingCards = matchingCards.size();

                if ((this.numberOfMatchingCards + lastPlayedCardNumber) > 4) {
                    gameClient.callBullshit();

                    return;
                }

                if ((lastPlayedCardNumber > 0) && (BotClient.random.nextInt(0, 4) == 0)) {
                    gameClient.callBullshit();

                    return;
                }

                if (!matchingCards.isEmpty()) {
                    if (BotClient.random.nextBoolean()) gameClient.playCards(matchingCards);

                    else {
                        List<Card> notMatchingCards = cards.stream().filter(card -> card.rank() != currentRank).toList();

                        gameClient.playCards(notMatchingCards.subList(0, Math.min(notMatchingCards.size(), matchingCards.size())));
                    }

                    return;
                }

                if (!gameClient.didAllPass()) {
                    gameClient.pass();

                    return;
                }

                Card firstCard = cards.getFirst();

                Rank firstCardRank = firstCard.rank();

                matchingCards = cards.stream().filter(card -> card.rank() == firstCardRank).toList();

                if (BotClient.random.nextBoolean()) gameClient.changeRank(firstCardRank, matchingCards);

                else {
                    List<Card> notMatchingCards = cards.stream().filter(card -> card.rank() != firstCardRank).toList();

                    gameClient.changeRank(firstCardRank, notMatchingCards.subList(0, Math.min(notMatchingCards.size(), matchingCards.size())));
                }
            }
        });
    }
}