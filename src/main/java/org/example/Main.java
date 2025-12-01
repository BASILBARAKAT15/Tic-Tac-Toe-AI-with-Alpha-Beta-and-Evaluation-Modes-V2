package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {

    private JFrame frame;
    private JComboBox<String> difficultyBox, evalBox;
    private JRadioButton xButton, oButton;
    private ButtonGroup symbolGroup;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().show());
    }

    public void show() {
        frame = new JFrame("Tic-Tac-Toe AI • Welcome");
        frame.setSize(1000, 650);
        frame.setMinimumSize(new Dimension(800, 550));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // MAIN PANEL WITH GRADIENT BACKGROUND
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();  // ← This was missing the 'd'!
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(20, 30, 70),
                        0, getHeight(), new Color(5, 10, 35)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        mainPanel.setLayout(null);
        frame.add(mainPanel);

        // LOGO
        JLabel imgLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("C:\\Users\\HP\\Downloads\\tac_tac_toe.png");
            Image img = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imgLabel.setText("TIC-TAC-TOE");

            imgLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 48));
            imgLabel.setForeground(new Color(100, 200, 255));
        }
        imgLabel.setBounds(80, 80, 220, 220);
        mainPanel.add(imgLabel);

        // TITLE
        JLabel titleLbl = new JLabel("<html><center>NEURAL NETWORK<br>TIC-TAC-TOE</center></html>", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 52));
        titleLbl.setForeground(new Color(120, 200, 255));
        titleLbl.setBounds(340, 80, 580, 120);
        mainPanel.add(titleLbl);

        JLabel subtitleLbl = new JLabel("By Basil Barakat & Hosni Badran", SwingConstants.CENTER);
        subtitleLbl.setFont(new Font("Consolas", Font.PLAIN, 22));
        subtitleLbl.setForeground(new Color(180, 220, 255));
        subtitleLbl.setBounds(340, 190, 580, 40);
        mainPanel.add(subtitleLbl);

        // SYMBOL SELECTION
        JPanel symbolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        symbolPanel.setOpaque(false);
        symbolPanel.setBounds(300, 280, 600, 70);

        xButton = createRadioButton("Play as X (First)", true);
        oButton = createRadioButton("Play as O (Second)", false);

        symbolGroup = new ButtonGroup();
        symbolGroup.add(xButton);
        symbolGroup.add(oButton);

        symbolPanel.add(xButton);
        symbolPanel.add(oButton);
        mainPanel.add(symbolPanel);

        // DIFFICULTY & EVAL
        difficultyBox = createComboBox(new String[]{"Easy", "Normal", "Hard", "ML-Powered"});
        difficultyBox.setSelectedIndex(3);
        evalBox = createComboBox(new String[]{"Classical Heuristic", "Neural Network"});
        evalBox.setSelectedIndex(1);

        placeComponent(mainPanel, createLabel("Difficulty Level:"), 380, 370, 200, 40);
        placeComponent(mainPanel, difficultyBox, 580, 370, 280, 50);
        placeComponent(mainPanel, createLabel("AI Brain:"), 380, 440, 200, 40);
        placeComponent(mainPanel, evalBox, 580, 440, 280, 50);

        // LAUNCH BUTTON — GLOWING MASTERPIECE
        JButton goBtn = new JButton("GO!");
        styleLaunchButton(goBtn);
        goBtn.setBounds(400, 530, 300, 70);
        goBtn.addActionListener(e -> {
            char playerSymbol = xButton.isSelected() ? 'X' : 'O';
            String difficulty = (String) difficultyBox.getSelectedItem();
            String evalFunc = (String) evalBox.getSelectedItem();

            if ("ML-Powered".equals(difficulty)) difficulty = "ML";
            if ("Neural Network".equals(evalFunc)) evalFunc = "NeuralNet";

            frame.dispose();
            Game.startTicTacToe(playerSymbol, difficulty, evalFunc);
        });
        mainPanel.add(goBtn);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JRadioButton createRadioButton(String text, boolean selected) {
        JRadioButton btn = new JRadioButton(text, selected);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btn.setForeground(selected ? new Color(100, 220, 255) : new Color(200, 200, 240));
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), 3),
                BorderFactory.createEmptyBorder(12, 30, 12, 30)
        ));
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        box.setForeground(Color.WHITE);
        box.setBackground(new Color(40, 50, 90));
        box.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 255), 2));
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setForeground(Color.WHITE);
                label.setBackground(isSelected ? new Color(0, 120, 215) : new Color(40, 50, 90));
                label.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                return label;
            }
        });
        return box;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl.setForeground(new Color(150, 200, 255));
        return lbl;
    }

    private void placeComponent(Container container, Component comp, int x, int y, int w, int h) {
        comp.setBounds(x, y, w, h);
        container.add(comp);
    }

    private void styleLaunchButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 34));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 160, 255));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(80, 200, 255));
                btn.setBounds(btn.getX() - 5, btn.getY() - 5, btn.getWidth() + 10, btn.getHeight() + 10);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0, 160, 255));
                btn.setBounds(btn.getX() + 5, btn.getY() + 5, btn.getWidth() - 10, btn.getHeight() - 10);
            }
        });
    }
}