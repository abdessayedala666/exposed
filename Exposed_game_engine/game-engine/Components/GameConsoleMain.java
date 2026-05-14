import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameConsoleMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Exposed Console Game ===");
        System.out.println("Enter 4 player names in seat order (P1, P2, P3, P4).");

        String p1Name = askNonEmpty(scanner, "Player 1 name: ");
        String p2Name = askNonEmpty(scanner, "Player 2 name: ");
        String p3Name = askNonEmpty(scanner, "Player 3 name: ");
        String p4Name = askNonEmpty(scanner, "Player 4 name: ");

        Player p1 = new Player(p1Name, 0);
        Player p2 = new Player(p2Name, 1);
        Player p3 = new Player(p3Name, 2);
        Player p4 = new Player(p4Name, 3);

        Team teamOne = new Team(List.of(p1, p3));
        Team teamTwo = new Team(List.of(p2, p4));

        GameEngineV2 engine = new GameEngineV2(teamOne, teamTwo);
        engine.startGame();

        while (!engine.isGameOver()) {
            Player current = engine.getCurrentPlayer();
            RoundState roundState = engine.getGameState().getCurrentRound();

            System.out.println("\n------------------------------");
            System.out.println("Round " + roundState.getRoundNumber());
            System.out.println("Current player: " + current.getName());
            printTable(roundState);
            printHand(current);

            int cardChoice = askCardChoice(scanner, current);
            CaptureType captureType = askCaptureType(scanner);
            Player targetOpponent = null;

            if (captureType == CaptureType.CAPTURE_FROM_OPPONENT) {
                targetOpponent = askOpponentTarget(scanner, engine.getGameState().getPlayers(), current);
            }

            try {
                GameEngineV2.MoveResult result = engine.playTurn(current, cardChoice, captureType, targetOpponent);
                System.out.println("Played: " + result.getPlayedCard());
                if (!result.getCapturedCards().isEmpty()) {
                    System.out.println("Captured cards: " + result.getCapturedCards());
                }
                if (result.isRoundEnded()) {
                    System.out.println("Round ended.");
                }
                if (result.isSetEnded()) {
                    System.out.println("Set ended.");
                }
            } catch (RuntimeException ex) {
                System.out.println("Invalid move: " + ex.getMessage());
                System.out.println("Please try this turn again.");
            }
        }

        Team winner = engine.getWinnerTeamOrNull();
        System.out.println("\n=== Game Finished ===");
        if (winner != null) {
            System.out.println("Winner team: " + winner.getPlayer1().getName() + " and " + winner.getPlayer2().getName());
        }

        List<Team> teams = engine.getGameState().getTeams();
        System.out.println("Final scores:");
        System.out.println("Team 1: " + teams.get(0).getTotalScore());
        System.out.println("Team 2: " + teams.get(1).getTotalScore());
    }

    private static String askNonEmpty(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Name cannot be empty.");
        }
    }

    private static int askCardChoice(Scanner scanner, Player player) {
        while (true) {
            System.out.print("Choose card index to play (1-" + player.getHand().size() + "): ");
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value >= 1 && value <= player.getHand().size()) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid card index.");
        }
    }

    private static CaptureType askCaptureType(Scanner scanner) {
        while (true) {
            System.out.print("Capture type [1=PILE, 2=OPPONENT]: ");
            String raw = scanner.nextLine().trim();
            if ("1".equals(raw)) {
                return CaptureType.CAPTURE_FROM_PILE;
            }
            if ("2".equals(raw)) {
                return CaptureType.CAPTURE_FROM_OPPONENT;
            }
            System.out.println("Invalid choice.");
        }
    }

    private static Player askOpponentTarget(Scanner scanner, List<Player> players, Player current) {
        List<Player> candidates = new ArrayList<>();
        for (Player player : players) {
            if (player != current && !player.getCapturedCards().isEmpty()) {
                candidates.add(player);
            }
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException("No opponent has a captured stack to steal from");
        }

        System.out.println("Choose opponent to steal from:");
        for (int i = 0; i < candidates.size(); i++) {
            Player p = candidates.get(i);
            System.out.println((i + 1) + ". " + p.getName() + " (top: " + p.getTopcapturedCard() + ")");
        }

        while (true) {
            System.out.print("Opponent choice (1-" + candidates.size() + "): ");
            String raw = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(raw);
                if (choice >= 1 && choice <= candidates.size()) {
                    return candidates.get(choice - 1);
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid opponent choice.");
        }
    }

    private static void printTable(RoundState roundState) {
        List<Card> pile = roundState.getTablePile();
        if (pile.isEmpty()) {
            System.out.println("Table pile: []");
            return;
        }
        Card top = pile.get(pile.size() - 1);
        System.out.println("Table pile size: " + pile.size() + " | top: " + top);
    }

    private static void printHand(Player player) {
        List<Card> hand = player.getHand();
        System.out.println("Your hand:");
        for (int i = 0; i < hand.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + hand.get(i));
        }
    }
}
