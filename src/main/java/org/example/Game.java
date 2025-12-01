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

public class Game extends JFrame {

    private final char[][] board = new char[3][3];
    private final JButton[][] buttons = new JButton[3][3];
    private final JLabel[] scoreLabels = new JLabel[9];
    private final char human, ai;
    private final String difficulty, evalMode;
    private MultiLayerNetwork neuralNet;
    private final Random rand = new Random();

    private static final String CSV_PATH = "C:\\Users\\HP\\Downloads\\tictactoe_dataset.csv";

    public static void startGame(char humanSymbol, String difficultyLevel, String evaluationMode) {
        SwingUtilities.invokeLater(() -> new Game(humanSymbol, difficultyLevel, evaluationMode));
    }

    private Game(char humanSymbol, String difficultyLevel, String evaluationMode) {
        this.human = humanSymbol;
        this.ai = (humanSymbol == 'X') ? 'O' : 'X';
        this.difficulty = difficultyLevel;
        this.evalMode = evaluationMode;

        for (char[] row : board) Arrays.fill(row, ' ');
        if (evalMode.equals("NeuralNet")) trainNeuralNet();
        setupGUI();
        if (ai == 'X') aiMove();
    }

    private void trainNeuralNet() {
        try {
            System.out.println("Training Neural Network...");
            List<String> lines = Files.readAllLines(new File(CSV_PATH).toPath());
            INDArray X = Nd4j.zeros(lines.size() - 1, 9);
            INDArray y = Nd4j.zeros(lines.size() - 1, 1);
            int idx = 0;
            for (int i = 1; i < lines.size(); i++) {
                String[] p = lines.get(i).split(",");
                if (p.length < 10) continue;
                for (int j = 0; j < 9; j++) {
                    String c = p[j].trim().replace("\"", "");
                    X.put(idx, j, c.equals("x") ? 1 : c.equals("o") ? -1 : 0);
                }
                y.put(idx++, 0, p[9].contains("positive") ? 1 : -1);
            }
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(123).weightInit(WeightInit.XAVIER).updater(new Adam(0.002))
                    .list()
                    .layer(new DenseLayer.Builder().nIn(9).nOut(128).activation(Activation.RELU).build())
                    .layer(new DenseLayer.Builder().nOut(64).activation(Activation.RELU).build())
                    .layer(new OutputLayer.Builder().nOut(1).activation(Activation.TANH).build())
                    .build();
            neuralNet = new MultiLayerNetwork(conf);
            neuralNet.init();
            neuralNet.fit((DataSetIterator) new DataSet(X, y), 40);
            System.out.println("Neural Network ready!");
        } catch (Exception e) { neuralNet = null; }
    }

    private void setupGUI() {
        setTitle("Tic-Tac-Toe AI • " + difficulty);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 950);
        getContentPane().setBackground(new Color(15, 25, 50));

        JLabel title = new JLabel("TIC-TAC-TOE AI", JLabel.CENTER);
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 48));
        title.setForeground(new Color(100, 200, 255));
        title.setBackground(new Color(10, 15, 40));
        title.setOpaque(true);
        title.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3,3,25,25));
        grid.setBorder(BorderFactory.createEmptyBorder(40,80,80,80));
        grid.setBackground(new Color(10,20,60));

        Font sym = new Font("Arial", Font.BOLD, 140);
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(new Color(240,248,255));
            cell.setBorder(BorderFactory.createLineBorder(new Color(50,70,120), 8, true));

            buttons[i][j] = new JButton(" ");
            buttons[i][j].setFont(sym);
            buttons[i][j].setFocusPainted(false);
            buttons[i][j].setContentAreaFilled(false);
            int r=i, c=j;
            buttons[i][j].addActionListener(e -> playerMove(r,c));

            scoreLabels[i*3+j] = new JLabel("", JLabel.CENTER);
            scoreLabels[i*3+j].setFont(new Font("Consolas", Font.BOLD, 18));
            scoreLabels[i*3+j].setForeground(new Color(80,120,180));

            cell.add(buttons[i][j], BorderLayout.CENTER);
            cell.add(scoreLabels[i*3+j], BorderLayout.SOUTH);
            grid.add(cell);
        }
        add(grid, BorderLayout.CENTER);

        JLabel status = new JLabel("Your turn • You are " + human, JLabel.CENTER);
        status.setFont(new Font("Segoe UI", Font.BOLD, 28));
        status.setForeground(Color.CYAN);
        status.setBackground(new Color(10,15,40));
        status.setOpaque(true);
        add(status, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void playerMove(int r, int c) {
        if (board[r][c] != ' ') return;
        board[r][c] = human;
        buttons[r][c].setText(String.valueOf(human));
        buttons[r][c].setForeground(human == 'X' ? new Color(30,144,255) : new Color(255,69,90));
        if (!checkEnd()) aiMove();
    }

    private void aiMove() {
        if (checkEnd()) return;

        int move;
        if (difficulty.equals("Easy")) {
            move = rand.nextInt(100) < 75 ? getRandomMove() : getBestMove();
        } else if (difficulty.equals("Normal")) {
            move = rand.nextInt(100) < 20 ? getRandomMove() : getBestMove();
        } else {
            move = getBestMove();
        }

        int r = move / 3, c = move % 3;
        board[r][c] = ai;
        buttons[r][c].setText(String.valueOf(ai));
        buttons[r][c].setForeground(ai == 'X' ? new Color(30,144,255) : new Color(255,69,90));

        if (!difficulty.equals("Easy")) showScores();
        checkEnd();
    }

    private int getRandomMove() {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (board[i/3][i%3] == ' ') moves.add(i);
        return moves.get(rand.nextInt(moves.size()));
    }

    private int getBestMove() {
        Map<Integer, Double> scores = new HashMap<>();
        int best = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int p = 0; p < 9; p++) {
            int r = p/3, c = p%3;
            if (board[r][c] != ' ') continue;
            board[r][c] = ai;
            double score = minimax(getDepth(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
            board[r][c] = ' ';
            scores.put(p, score);
            if (score > bestScore) { bestScore = score; best = p; }
        }
        if (!difficulty.equals("Easy")) {
            for (int i = 0; i < 9; i++) {
                if (board[i/3][i%3] == ' ') {
                    double s = scores.getOrDefault(i, 0.0);
                    scoreLabels[i].setText(String.format("%.2f", s));
                    scoreLabels[i].setForeground(s > 0 ? Color.GREEN.darker() : Color.RED.darker());
                } else scoreLabels[i].setText("");
            }
        }
        return best;
    }

    private int getDepth() {
        return switch (difficulty) {
            case "Easy" -> 1;
            case "Normal" -> 5;
            case "Hard" -> 10;
            default -> 8;
        };
    }

    private double minimax(int depth, double a, double b, boolean max) {
        if (depth == 0 || checkWin(ai) || checkWin(human) || isFull()) return evaluate();
        if (max) {
            double v = Double.NEGATIVE_INFINITY;
            for (int i=0; i<9; i++) if (board[i/3][i%3]==' ') {
                board[i/3][i%3]=ai; v = Math.max(v, minimax(depth-1,a,b,false)); board[i/3][i%3]=' '; a=Math.max(a,v);
                if (b <= a) break;
            } return v;
        } else {
            double v = Double.POSITIVE_INFINITY;
            for (int i=0; i<9; i++) if (board[i/3][i%3]==' ') {
                board[i/3][i%3]=human; v = Math.min(v, minimax(depth-1,a,b,true)); board[i/3][i%3]=' '; b=Math.min(b,v);
                if (b <= a) break;
            } return v;
        }
    }

    private double evaluate() {
        if (evalMode.equals("NeuralNet") && neuralNet != null) {
            INDArray in = Nd4j.create(1,9);
            for (int i=0;i<9;i++) in.put(0,i, board[i/3][i%3]==ai ? 1 : board[i/3][i%3]==human ? -1 : 0);
            return neuralNet.output(in).getDouble(0);
        }
        return heuristic();
    }

    private double heuristic() {
        double s = 0;
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] l : lines) {
            int a=0, h=0, e=0;
            for (int p : l) { char c=board[p/3][p%3]; if(c==ai)a++; else if(c==human)h++; else e++; }
            if(a==3)s+=10000; if(h==3)s-=10000;
            if(a==2&&e==1)s+=120; if(h==2&&e==1)s-=120;
        }
        return s;
    }

    private boolean checkWin(char p) {
        int[][] w = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] l : w) if (board[l[0]/3][l[0]%3]==p && board[l[1]/3][l[1]%3]==p && board[l[2]/3][l[2]%3]==p) return true;
        return false;
    }

    private boolean isFull() { for (char[] r:board) for (char c:r) if(c==' ')return false; return true; }

    private void showScores() { /* already in getBestMove */ }

    private boolean checkEnd() {
        if (checkWin(human)) { JOptionPane.showMessageDialog(this,"YOU WIN!","Victory",1); System.exit(0); }
        if (checkWin(ai))    { JOptionPane.showMessageDialog(this,"AI WINS!","Defeat",2); System.exit(0); }
        if (isFull())        { JOptionPane.showMessageDialog(this,"DRAW!","Tie",1); System.exit(0); }
        return false;
    }
}
