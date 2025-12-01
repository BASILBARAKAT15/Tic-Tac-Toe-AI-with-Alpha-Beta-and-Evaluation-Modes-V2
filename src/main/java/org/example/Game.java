package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class Game extends JPanel {

    private static final int SIZE = 3;
    private JFrame frame;
    private final JButton[][] buttons = new JButton[SIZE][SIZE];
    private final char[][] board = new char[SIZE][SIZE];
    private final char player;
    private final char ai;
    private final String difficulty;
    private MultiLayerNetwork model;
    private JLabel statusLabel;

    private static final String CSV_PATH = "C:\\Users\\HP\\Downloads\\tictactoe_dataset.csv";

    public static void main(String[] args) {
        startTicTacToe('X', "ML", "NeuralNet");
    }

    public static void startTicTacToe(char playerSymbol, String difficulty, String evalFunc) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new Game(playerSymbol, difficulty, evalFunc).createAndShowGUI();
        });
    }

    public Game(char playerSymbol, String difficulty, String evalFunc) {
        this.player = playerSymbol;
        this.ai = (playerSymbol == 'X') ? 'O' : 'X';
        this.difficulty = difficulty;
        initializeBoard();

        if ("ML".equals(difficulty) || "NeuralNet".equals(evalFunc)) {
            loadAndTrainModel();
        }
    }

    private void initializeBoard() {
        for (char[] row : board) Arrays.fill(row, ' ');
    }

    private void loadAndTrainModel() {
        try {
            System.out.println("Loading and training neural network from dataset...");
            File file = new File(CSV_PATH);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null,
                        "Dataset not found!\nExpected: " + CSV_PATH,
                        "ML Model Error", JOptionPane.ERROR_MESSAGE);
                model = createDefaultModel(); // fallback
                return;
            }

            List<String> lines = Files.readAllLines(file.toPath());
            INDArray features = Nd4j.create(lines.size() - 1, 9);
            INDArray labels = Nd4j.create(lines.size() - 1, 9);

            int row = 0;
            for (int i = 1; i < lines.size(); i++) { // skip header
                String[] values = lines.get(i).split(",");
                if (values.length < 10) continue;

                // Convert board: x,o,b → 1, -1, 0
                for (int j = 0; j < 9; j++) {
                    char c = values[j].charAt(1); // "x" or "o" or "b"
                    double val = (c == 'x') ? 1.0 : (c == 'o') ? -1.0 : 0.0;
                    if (player == 'O') val = -val; // normalize: AI is always X (1.0)
                    features.put(row, j, val);
                }

                // Best move from dataset (positive = win for x)
                String result = values[9];
                int bestMove = -1;
                double maxScore = -10;
                for (int j = 0; j < 9; j++) {
                    if (values[j].equals("\"b\"")) {
                        double score = result.contains("positive") ? 1.0 : -1.0;
                        if (score > maxScore) {
                            maxScore = score;
                            bestMove = j;
                        }
                    }
                }
                if (bestMove != -1) {
                    labels.put(row, bestMove, 1.0);
                }
                row++;
            }

            DataSet dataSet = new DataSet(features, labels);
            model = createNeuralNetwork();
            model.fit((DataSetIterator) dataSet, 20); // train 20 epochs
            System.out.println("ML Model trained successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            model = createDefaultModel();
        }
    }

    private MultiLayerNetwork createNeuralNetwork() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.01))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(9).nOut(64).activation(Activation.RELU).build())
                .layer(1, new DenseLayer.Builder().nOut(64).activation(Activation.RELU).build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX).nIn(64).nOut(9).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        return net;
    }

    private MultiLayerNetwork createDefaultModel() {
        // Very small dummy model if training fails
        MultiLayerNetwork net = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .list()
                .layer(new OutputLayer.Builder().nIn(9).nOut(9).activation(Activation.SOFTMAX).build())
                .build());
        net.init();
        return net;
    }

    private void createAndShowGUI() {
        // SAME BEAUTIFUL BIG GUI AS BEFORE (copy from previous version)
        frame = new JFrame("TIC-TAC-TOE AI • ML POWERED");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(10, 20, 60));
        JLabel titleLabel = new JLabel("NEURAL NETWORK TIC-TAC-TOE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(new Color(100, 220, 255));
        titlePanel.add(titleLabel);
        frame.add(titlePanel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 20, 20));
        boardPanel.setBackground(new Color(15, 15, 40));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        Font font = new Font("Arial", Font.BOLD, 100);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                final int row = r, col = c;
                buttons[r][c] = new JButton(" ");
                buttons[r][c].setFont(font);
                buttons[r][c].setBackground(Color.WHITE);
                buttons[r][c].setBorder(BorderFactory.createLineBorder(new Color(70, 70, 150), 6));
                buttons[r][c].setCursor(new Cursor(Cursor.HAND_CURSOR));

                buttons[r][c].addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        if (board[row][col] == ' ') buttons[row][col].setBackground(new Color(200, 240, 255));
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        if (board[row][col] == ' ') buttons[row][col].setBackground(Color.WHITE);
                    }
                });

                buttons[r][c].addActionListener(e -> playerMove(row, col));
                boardPanel.add(buttons[r][c]);
            }
        }
        frame.add(boardPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Your turn (" + player + ")", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        statusLabel.setForeground(Color.CYAN);
        statusLabel.setBackground(new Color(10, 20, 60));
        statusLabel.setOpaque(true);
        frame.add(statusLabel, BorderLayout.SOUTH);

        frame.pack();
        frame.setSize(850, 950);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (ai == 'X') aiTurn();
    }

    private void playerMove(int r, int c) {
        if (board[r][c] != ' ') return;
        makeMove(r, c, player);
        buttons[r][c].setForeground(new Color(0, 180, 255));
        if (checkGameOver(player)) return;
        statusLabel.setText("AI thinking with Neural Network...");
        SwingUtilities.invokeLater(this::aiTurn);
    }

    private void aiTurn() {
        if (isGameOver()) return;

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override protected Integer doInBackground() {
                return getNeuralMove();
            }
            @Override protected void done() {
                try {
                    int move = get();
                    int r = move / 3, c = move % 3;
                    makeMove(r, c, ai);
                    buttons[r][c].setForeground(new Color(255, 70, 70));
                    checkGameOver(ai);
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private int getNeuralMove() {
        INDArray input = Nd4j.create(1, 9);
        for (int i = 0; i < 9; i++) {
            int r = i / 3, c = i % 3;
            char cell = board[r][c];
            double val = (cell == ai) ? 1.0 : (cell == player) ? -1.0 : 0.0;
            input.put(0, i, val);
        }

        INDArray output = model.output(input);
        List<Integer> moves = getAvailableMoves();

        int bestMove = moves.get(0);
        double bestScore = -1;

        for (int move : moves) {
            double score = output.getDouble(0, move);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    // Keep your existing makeMove, checkWinner, isDraw, etc. from previous code...
    // (Just copy them here – too long to repeat)

    private void makeMove(int r, int c, char s) {
        board[r][c] = s;
        buttons[r][c].setText(String.valueOf(s));
    }

    private List<Integer> getAvailableMoves() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (board[i/3][i%3] == ' ') list.add(i);
        return list;
    }

    private boolean checkWinner(char s) {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] line : lines) {
            if (board[line[0]/3][line[0]%3] == s &&
                    board[line[1]/3][line[1]%3] == s &&
                    board[line[2]/3][line[2]%3] == s) return true;
        }
        return false;
    }

    private boolean isGameOver() {
        return checkWinner(player) || checkWinner(ai) || getAvailableMoves().isEmpty();
    }

    private boolean checkGameOver(char s) {
        if (checkWinner(s)) {
            String msg = s == player ? "YOU WIN!" : "NEURAL NETWORK WINS!";
            JOptionPane.showMessageDialog(frame, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            return true;
        } else if (getAvailableMoves().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "DRAW!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            return true;
        }
        statusLabel.setText("Your turn (" + player + ")");
        return false;
    }
}