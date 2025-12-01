package org.example;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createGUI());
    }

    private void createGUI() {
        JFrame frame = new JFrame("Tic-Tac-Toe AI • Welcome");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 20, 60), 0, getHeight(), new Color(5, 10, 40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setLayout(null);
        frame.add(panel);

        // Your Image
        JLabel imageLabel = new JLabel();
        try {
            Image img = ImageIO.read(new File("C:\\Users\\HP\\Downloads\\tac_tac_toe.png"))
                    .getScaledInstance(320, 320, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imageLabel.setText("Image Not Found");
            imageLabel.setForeground(Color.RED);
        }
        imageLabel.setBounds(70, 80, 320, 320);
        panel.add(imageLabel);

        // Title & Authors (same as before)
        JLabel title = new JLabel("<html><center>NEURAL NETWORK<br>& ALPHA-BETA AI</center></html>", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 56));
        title.setForeground(new Color(130, 210, 255));
        title.setBounds(420, 80, 660, 150);
        panel.add(title);

        JLabel a1 = new JLabel("Basil Barakat", JLabel.CENTER);
        a1.setFont(new Font("Consolas", Font.BOLD, 30));
        a1.setForeground(new Color(120, 220, 255));
        a1.setBounds(420, 250, 660, 50);
        panel.add(a1);

        JLabel a2 = new JLabel("Hosni Badran", JLabel.CENTER);
        a2.setFont(new Font("Consolas", Font.BOLD, 30));
        a2.setForeground(new Color(120, 220, 255));
        a2.setBounds(420, 300, 660, 50);
        panel.add(a2);

        // Symbol Choice
        JRadioButton xBtn = new JRadioButton("Play as X (First Move)", true);
        JRadioButton oBtn = new JRadioButton("Play as O (Second Move)", false);
        ButtonGroup group = new ButtonGroup();
        group.add(xBtn); group.add(oBtn);
        xBtn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        oBtn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        xBtn.setForeground(Color.CYAN);
        oBtn.setForeground(Color.LIGHT_GRAY);
        xBtn.setOpaque(false); oBtn.setOpaque(false);

        JPanel symbolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 10));
        symbolPanel.setOpaque(false);
        symbolPanel.add(xBtn); symbolPanel.add(oBtn);
        symbolPanel.setBounds(420, 380, 660, 70);
        panel.add(symbolPanel);

        // Difficulty
        String[] difficulties = {"Easy", "Normal", "Hard"};
        JComboBox<String> diffBox = new JComboBox<>(difficulties);
        diffBox.setFont(new Font("Segoe UI", Font.BOLD, 24));
        diffBox.setForeground(Color.WHITE);
        diffBox.setBackground(new Color(35, 45, 85));
        diffBox.setBorder(BorderFactory.createLineBorder(new Color(100, 160, 255), 3));
        diffBox.setSelectedIndex(2);

        // Evaluation
        String[] evals = {"Classical Heuristic", "Neural Network"};
        JComboBox<String> evalBox = new JComboBox<>(evals);
        evalBox.setFont(new Font("Segoe UI", Font.BOLD, 24));
        evalBox.setForeground(Color.WHITE);
        evalBox.setBackground(new Color(35, 45, 85));
        evalBox.setBorder(BorderFactory.createLineBorder(new Color(100, 160, 255), 3));
        evalBox.setSelectedIndex(1);

        panel.add(new JLabel("Difficulty Level:"){{
            setFont(new Font("Segoe UI", Font.BOLD, 28));
            setForeground(new Color(160, 210, 255));
            setBounds(420, 480, 300, 50);
        }});
        panel.add(diffBox); diffBox.setBounds(720, 480, 350, 55);

        panel.add(new JLabel("AI Evaluation:"){{
            setFont(new Font("Segoe UI", Font.BOLD, 28));
            setForeground(new Color(160, 210, 255));
            setBounds(420, 560, 300, 50);
        }});
        panel.add(evalBox); evalBox.setBounds(720, 560, 350, 55);

        // Small Elegant Start Button
        JButton start = new JButton("START GAME");
        start.setFont(new Font("Segoe UI", Font.BOLD, 28));
        start.setForeground(Color.WHITE);
        start.setBackground(new Color(0, 140, 255));
        start.setFocusPainted(false);
        start.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        start.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 35));
        start.setBounds(480, 650, 240, 60);

        start.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { start.setBackground(new Color(80, 180, 255)); }
            public void mouseExited(java.awt.event.MouseEvent e) { start.setBackground(new Color(0, 140, 255)); }
        });

        start.addActionListener(e -> {
            char playerSymbol = xBtn.isSelected() ? 'X' : 'O';
            String difficulty = (String) diffBox.getSelectedItem();
            String evalMode = evalBox.getSelectedIndex() == 1 ? "NeuralNet" : "Heuristic";

            frame.dispose();

            // IF NEURAL NETWORK SELECTED → SHOW TRAINING SCREEN FIRST
            if (evalMode.equals("NeuralNet")) {
                showTrainingScreen(() -> Game.startGame(playerSymbol, difficulty, evalMode));
            } else {
                Game.startGame(playerSymbol, difficulty, evalMode);
            }
        });

        panel.add(start);
        frame.setVisible(true);
    }

    // BEAUTIFUL TRAINING SCREEN
    private void showTrainingScreen(Runnable onComplete) {
        JFrame trainFrame = new JFrame("Training Neural Network...");
        trainFrame.setSize(700, 400);
        trainFrame.setLocationRelativeTo(null);
        trainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        trainFrame.getContentPane().setBackground(new Color(10, 20, 50));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(10, 20, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Training AI Brain...", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(100, 220, 255));

        JLabel status = new JLabel("Loading dataset & training model...", JLabel.CENTER);
        status.setFont(new Font("Consolas", Font.BOLD, 20));
        status.setForeground(Color.CYAN);

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setForeground(new Color(0, 200, 255));

        panel.add(title, BorderLayout.NORTH);
        panel.add(status, BorderLayout.CENTER);
        panel.add(progress, BorderLayout.SOUTH);

        trainFrame.add(panel);
        trainFrame.setVisible(true);

        // Simulate training time (2–4 seconds) then open game
        new Thread(() -> {
            try {
                Thread.sleep(3000); // You can remove this if real training is fast
            } catch (Exception ignored) {}
            SwingUtilities.invokeLater(() -> {
                trainFrame.dispose();
                onComplete.run();
            });
        }).start();
    }
}