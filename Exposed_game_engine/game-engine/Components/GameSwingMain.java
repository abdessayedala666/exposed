import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameSwingMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            List<Player> players = askPlayers();
            if (players == null) {
                return;
            }

            Team teamOne = new Team(List.of(players.get(0), players.get(2)));
            Team teamTwo = new Team(List.of(players.get(1), players.get(3)));

            GameEngineV2 engine = new GameEngineV2(teamOne, teamTwo);
            engine.startGame();

            GameFrame frame = new GameFrame(engine);
            frame.setVisible(true);
        });
    }

    private static List<Player> askPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String name = JOptionPane.showInputDialog(
                    null,
                    "Player " + (i + 1) + " name:",
                    "Create Players",
                    JOptionPane.QUESTION_MESSAGE);
            if (name == null) {
                return null;
            }
            name = name.trim();
            if (name.isEmpty()) {
                name = "Player " + (i + 1);
            }
            players.add(new Player(name, i));
        }
        return players;
    }

    private static class GameFrame extends JFrame {
        private static final Color BG = new Color(22, 120, 76);
        private static final Color PANEL = new Color(242, 242, 242);
        private static final Color HIGHLIGHT = new Color(255, 214, 102);

        // Seat mapping: teammates face each other (team1: seats 0 and 2, team2: seats 1 and 3).
        private static final int BOTTOM_SEAT = 0;
        private static final int TOP_SEAT = 2;
        private static final int LEFT_SEAT = 1;
        private static final int RIGHT_SEAT = 3;

        private final GameEngineV2 engine;

        private final JLabel headerLabel = new JLabel();
        private final JLabel tableLabel = new JLabel("Drop here to capture from pile", SwingConstants.CENTER);
        private final JLabel scoreLabel = new JLabel();
        private final JLabel hintLabel = new JLabel();

        private final JLabel[] playerInfo = new JLabel[4];
        private final JPanel[] seatPanels = new JPanel[4];

        private final JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        private GameFrame(GameEngineV2 engine) {
            this.engine = engine;
            setTitle("Exposed - Board UI (Drag and Drop)");
            setSize(1200, 820);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            root.setBackground(BG);

            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.setOpaque(false);
            headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            scoreLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            hintLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            top.add(headerLabel);
            top.add(scoreLabel);
            top.add(hintLabel);
            root.add(top, BorderLayout.NORTH);

            JPanel board = new JPanel(new BorderLayout(12, 12));
            board.setOpaque(false);

            board.add(buildSeatPanel(TOP_SEAT), BorderLayout.NORTH);
            board.add(buildSeatPanel(LEFT_SEAT), BorderLayout.WEST);
            board.add(buildSeatPanel(RIGHT_SEAT), BorderLayout.EAST);
            board.add(buildSeatPanel(BOTTOM_SEAT), BorderLayout.SOUTH);

            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            tableLabel.setOpaque(true);
            tableLabel.setBackground(new Color(255, 255, 255));
            tableLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 70, 70), 2),
                    BorderFactory.createEmptyBorder(25, 25, 25, 25)));
            tableLabel.setPreferredSize(new Dimension(280, 160));
            tableLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            tableLabel.setTransferHandler(new DropZoneTransferHandler(CaptureType.CAPTURE_FROM_PILE, -1));
            center.add(tableLabel, BorderLayout.CENTER);
            board.add(center, BorderLayout.CENTER);

            root.add(board, BorderLayout.CENTER);

            JPanel handWrapper = new JPanel(new BorderLayout());
            handWrapper.setOpaque(false);
            handWrapper.setBorder(BorderFactory.createTitledBorder("Drag A Card From Current Player Hand"));
            handPanel.setOpaque(false);
            JScrollPane scroll = new JScrollPane(handPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            handWrapper.add(scroll, BorderLayout.CENTER);
            root.add(handWrapper, BorderLayout.SOUTH);

            setContentPane(root);

            refreshUi();
        }

        private void onCardDrop(CaptureType type, int targetSeat, int handIndex) {
            if (engine.isGameOver()) {
                return;
            }
            Player current = engine.getCurrentPlayer();
            Player target = null;
            if (type == CaptureType.CAPTURE_FROM_OPPONENT) {
                if (targetSeat < 0 || targetSeat >= engine.getGameState().getPlayers().size()) {
                    return;
                }
                target = engine.getGameState().getPlayers().get(targetSeat);
            }
            try {
                GameEngineV2.MoveResult result = engine.playTurn(current, handIndex + 1, type, target);

                StringBuilder message = new StringBuilder();
                message.append("Played: ").append(result.getPlayedCard());
                if (!result.getCapturedCards().isEmpty()) {
                    message.append("\nCaptured: ").append(result.getCapturedCards());
                }
                if (result.isRoundEnded()) {
                    message.append("\nRound ended.");
                }
                if (result.isSetEnded()) {
                    message.append("\nSet ended.");
                }
                if (result.isGameEnded()) {
                    Team winner = engine.getWinnerTeamOrNull();
                    if (winner != null) {
                        message.append("\nWinner team: ")
                                .append(winner.getPlayer1().getName())
                                .append(" & ")
                                .append(winner.getPlayer2().getName());
                    }
                }

                JOptionPane.showMessageDialog(this, message.toString());
                refreshUi();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Invalid move: " + ex.getMessage());
            }
        }

        private void refreshUi() {
            GameState state = engine.getGameState();
            RoundState round = state.getCurrentRound();
            List<Player> players = state.getPlayers();

            String status = engine.isGameOver() ? "FINISHED" : "IN_PROGRESS";
            headerLabel.setText("Status: " + status
                    + " | Round: " + (round == null ? "-" : round.getRoundNumber())
                    + " | Current Player: " + (engine.isGameOver() ? "-" : engine.getCurrentPlayer().getName()));
            hintLabel.setText("Drag a card to CENTER pile, opponent seat to steal, or your own seat to add matching stack.");

            List<Team> teams = state.getTeams();
            scoreLabel.setText("Team 1 (" + teams.get(0).getPlayer1().getName() + ", " + teams.get(0).getPlayer2().getName()
                    + ") = " + teams.get(0).getTotalScore()
                    + " | Team 2 (" + teams.get(1).getPlayer1().getName() + ", " + teams.get(1).getPlayer2().getName()
                    + ") = " + teams.get(1).getTotalScore());

            if (round == null || round.getTablePile().isEmpty()) {
                tableLabel.setText("Table pile: []\nDrop here to capture from pile");
            } else {
                Card top = round.getTopTableCard();
                tableLabel.setText("<html><center>Table pile size: " + round.getTablePile().size()
                        + "<br/>Top: " + formatCardInline(top)
                        + "<br/><br/>Drop here to capture from pile</center></html>");
            }

            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                String txt = "<html><b>Seat " + i + " - " + p.getName() + "</b><br/>"
                        + "Hand: " + p.getHand().size() + " cards<br/>"
                        + "Captured: " + p.getCapturedCards().size() + " cards";
                if (!p.getCapturedCards().isEmpty()) {
                    txt += "<br/>Top captured: " + formatCardInline(p.getTopcapturedCard());
                    txt += "<br/>All captured: " + formatCardListInline(p.getCapturedCards());
                }
                txt += "</html>";
                playerInfo[i].setText(txt);

                if (!engine.isGameOver() && i == engine.getCurrentPlayerIndex()) {
                    seatPanels[i].setBorder(BorderFactory.createLineBorder(HIGHLIGHT, 3));
                } else {
                    seatPanels[i].setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
                }
            }

            rebuildHandPanel();
        }

        private void rebuildHandPanel() {
            handPanel.removeAll();

            if (engine.isGameOver()) {
                handPanel.revalidate();
                handPanel.repaint();
                return;
            }

            Player current = engine.getCurrentPlayer();
            List<Card> hand = current.getHand();
            for (int i = 0; i < hand.size(); i++) {
                handPanel.add(createCardTile(hand.get(i), i));
            }

            if (hand.isEmpty()) {
                JLabel empty = new JLabel("No cards left in hand.");
                empty.setForeground(Color.WHITE);
                handPanel.add(empty);
            }

            handPanel.revalidate();
            handPanel.repaint();
        }

        private JPanel buildSeatPanel(int seatIndex) {
            JPanel panel = new JPanel(new BorderLayout(6, 6));
            panel.setPreferredSize(new Dimension(250, 170));
            panel.setBackground(PANEL);
            panel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

            JLabel title = new JLabel("Seat " + seatIndex, SwingConstants.CENTER);
            title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            panel.add(title, BorderLayout.NORTH);

            JLabel info = new JLabel("", SwingConstants.CENTER);
            info.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            panel.add(info, BorderLayout.CENTER);
            playerInfo[seatIndex] = info;
            seatPanels[seatIndex] = panel;

            JLabel dropHint = new JLabel("Drop here to steal", SwingConstants.CENTER);
            dropHint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            dropHint.setForeground(new Color(70, 70, 70));
            panel.add(dropHint, BorderLayout.SOUTH);

            panel.setTransferHandler(new DropZoneTransferHandler(CaptureType.CAPTURE_FROM_OPPONENT, seatIndex));
            return panel;
        }

        private JLabel createCardTile(Card card, int handIndex) {
            JLabel tile = new JLabel(formatCardTile(card), SwingConstants.CENTER);
            tile.setOpaque(true);
            tile.setBackground(Color.WHITE);
            tile.setPreferredSize(new Dimension(140, 74));
            tile.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2));
            tile.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tile.putClientProperty("handIndex", handIndex);
            tile.setTransferHandler(new CardTransferHandler());
            tile.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    tile.getTransferHandler().exportAsDrag(tile, e, TransferHandler.COPY);
                }
            });
            return tile;
        }

        private class CardTransferHandler extends TransferHandler {
            @Override
            protected Transferable createTransferable(javax.swing.JComponent c) {
                Object idx = c.getClientProperty("handIndex");
                return new StringSelection(String.valueOf(idx));
            }

            @Override
            public int getSourceActions(javax.swing.JComponent c) {
                return COPY;
            }
        }

        private class DropZoneTransferHandler extends TransferHandler {
            private final CaptureType captureType;
            private final int targetSeat;

            private DropZoneTransferHandler(CaptureType captureType, int targetSeat) {
                this.captureType = captureType;
                this.targetSeat = targetSeat;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support) || engine.isGameOver()) {
                    return false;
                }
                try {
                    String raw = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    int handIndex = Integer.parseInt(raw);

                    onCardDrop(captureType, targetSeat, handIndex);
                    return true;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(GameFrame.this, "Drop failed: " + ex.getMessage());
                    return false;
                }
            }
        }

        private String formatCardTile(Card card) {
            String color = isRedSuit(card) ? "#C62828" : "#1A1A1A";
            return "<html><div style='text-align:center;line-height:1.25;'>"
                    + "<span style='font-size:18px;font-weight:700;color:" + color + ";'>" + card.getNumber() + " " + suitSymbol(card) + "</span>"
                    + "<br/><span style='font-size:11px;color:#555;'>" + card.getSuit() + "</span>"
                    + "</div></html>";
        }

        private String formatCardInline(Card card) {
            String color = isRedSuit(card) ? "#C62828" : "#1A1A1A";
            return "<span style='color:" + color + ";font-weight:700;'>" + card.getNumber() + " " + suitSymbol(card) + "</span>";
        }

        private String formatCardListInline(List<Card> cards) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cards.size(); i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(formatCardInline(cards.get(i)));
            }
            return sb.toString();
        }

        private boolean isRedSuit(Card card) {
            return card.getSuit() == Suit.HEARTS || card.getSuit() == Suit.DIAMONDS;
        }

        private String suitSymbol(Card card) {
            switch (card.getSuit()) {
                case HEARTS:
                    return "♥";
                case DIAMONDS:
                    return "♦";
                case CLUBS:
                    return "♣";
                case SPADES:
                    return "♠";
                default:
                    return "?";
            }
        }
    }
}
