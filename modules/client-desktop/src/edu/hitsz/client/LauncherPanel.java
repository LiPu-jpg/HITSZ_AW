package edu.hitsz.client;

import edu.hitsz.common.Difficulty;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class LauncherPanel extends JPanel {

    private static final Color CARD_OVERLAY = new Color(0, 0, 0, 155);
    private static final Color SELECTED_BUTTON = new Color(206, 79, 44);
    private static final Color DEFAULT_BUTTON = new Color(238, 238, 238);

    private final LauncherSelectionModel selectionModel;
    private final Consumer<LauncherSelectionModel> onStart;
    private JButton createModeButton;
    private JButton joinModeButton;
    private JButton actionButton;
    private JTextField roomCodeField;
    private JLabel roomCodeLabel;
    private JLabel modeSummaryLabel;
    private JLabel difficultySummaryLabel;
    private JLabel roomCodeSummaryLabel;
    private final List<JButton> difficultyButtons = new LinkedList<>();

    public LauncherPanel(Consumer<LauncherSelectionModel> onStart) {
        this.selectionModel = new LauncherSelectionModel();
        this.onStart = onStart;
        setLayout(null);
        setOpaque(true);
        buildUi();
    }

    public void selectDifficulty(String difficulty) {
        selectionModel.setDifficulty(difficulty);
        syncStateUi();
    }

    public void selectCreateMode() {
        selectionModel.setEntryMode("CREATE");
        syncStateUi();
    }

    public void selectJoinMode() {
        selectionModel.setEntryMode("JOIN");
        syncStateUi();
    }

    public void setRoomCode(String roomCode) {
        selectionModel.setRoomCode(roomCode);
        if (roomCodeField != null) {
            roomCodeField.setText(selectionModel.getRoomCode());
        }
        syncStateUi();
    }

    public void submitSelections() {
        if ("JOIN".equals(selectionModel.getEntryMode())) {
            selectionModel.setRoomCode(roomCodeField.getText());
        }
        onStart.accept(selectionModel.copy());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(ImageManager.LAUNCHER_BACKGROUND_IMAGE, 0, 0, getWidth(), getHeight(), null);
        g.setColor(CARD_OVERLAY);
        g.fillRoundRect(48, 88, 416, 518, 28, 28);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 32));
        g.drawString("Aircraft War", 147, 148);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.drawString("Host creates the room and picks difficulty.", 95, 180);
        g.drawString("Teammates join with a room code.", 132, 206);
    }

    private void buildUi() {
        addLabel("Mode", 100, 225, 140, 28);
        createModeButton = addButton("Create", 100, 260, this::selectCreateMode);
        joinModeButton = addButton("Join", 100, 302, this::selectJoinMode);

        addLabel("Difficulty", 100, 350, 140, 28);
        difficultyButtons.add(addButton("Easy", 100, 385, () -> selectDifficulty(Difficulty.EASY.name())));
        difficultyButtons.add(addButton("Normal", 100, 427, () -> selectDifficulty(Difficulty.NORMAL.name())));
        difficultyButtons.add(addButton("Hard", 100, 469, () -> selectDifficulty(Difficulty.HARD.name())));

        roomCodeLabel = addLabel("Room Code", 280, 225, 140, 28);
        roomCodeField = new JTextField();
        roomCodeField.setBounds(280, 260, 130, 32);
        roomCodeField.addActionListener(e -> setRoomCode(roomCodeField.getText()));
        add(roomCodeField);

        modeSummaryLabel = addSummaryLabel("", 96, 525, 320, 24);
        difficultySummaryLabel = addSummaryLabel("", 96, 549, 320, 24);
        roomCodeSummaryLabel = addSummaryLabel("", 96, 573, 320, 24);

        actionButton = new JButton("Create Room");
        actionButton.setBounds(154, 630, 204, 42);
        actionButton.addActionListener(e -> submitSelections());
        add(actionButton);
        syncStateUi();
    }

    private JLabel addLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setBounds(x, y, width, height);
        add(label);
        return label;
    }

    private JButton addButton(String text, int x, int y, Runnable onClick) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 130, 32);
        button.addActionListener(e -> onClick.run());
        add(button);
        return button;
    }

    private JLabel addSummaryLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 17));
        label.setBounds(x, y, width, height);
        add(label);
        return label;
    }

    private void syncStateUi() {
        boolean joinMode = "JOIN".equals(selectionModel.getEntryMode());
        if (roomCodeLabel != null) {
            roomCodeLabel.setVisible(joinMode);
        }
        if (roomCodeField != null) {
            roomCodeField.setVisible(joinMode);
            roomCodeField.setEnabled(joinMode);
            roomCodeField.setText(selectionModel.getRoomCode());
        }
        if (actionButton != null) {
            actionButton.setText(joinMode ? "Join Room" : "Create Room");
        }
        if (createModeButton != null) {
            styleModeButton(createModeButton, !joinMode);
        }
        if (joinModeButton != null) {
            styleModeButton(joinModeButton, joinMode);
        }
        syncSelectionButtons();
        if (modeSummaryLabel != null) {
            modeSummaryLabel.setText("Mode: " + selectionModel.getEntryMode());
        }
        if (difficultySummaryLabel != null) {
            difficultySummaryLabel.setText("Difficulty: " + selectionModel.getDifficulty());
        }
        if (roomCodeSummaryLabel != null) {
            roomCodeSummaryLabel.setVisible(joinMode);
            roomCodeSummaryLabel.setText("Room Code: " + selectionModel.getRoomCode());
        }
        repaint();
    }

    private void syncSelectionButtons() {
        styleSelectionButton(difficultyButtons, selectionModel.getDifficulty(), "Easy", Difficulty.EASY.name());
        styleSelectionButton(difficultyButtons, selectionModel.getDifficulty(), "Normal", Difficulty.NORMAL.name());
        styleSelectionButton(difficultyButtons, selectionModel.getDifficulty(), "Hard", Difficulty.HARD.name());
    }

    private void styleModeButton(JButton button, boolean selected) {
        button.setOpaque(true);
        button.setBackground(selected ? SELECTED_BUTTON : DEFAULT_BUTTON);
        button.setForeground(selected ? Color.WHITE : Color.BLACK);
    }

    private void styleSelectionButton(List<JButton> buttons, String selectedValue, String buttonText, String value) {
        for (JButton button : buttons) {
            if (!buttonText.equals(button.getText())) {
                continue;
            }
            boolean selected = value.equals(selectedValue);
            button.setOpaque(true);
            button.setBackground(selected ? SELECTED_BUTTON : DEFAULT_BUTTON);
            button.setForeground(selected ? Color.WHITE : Color.BLACK);
            return;
        }
    }
}
