package edu.hitsz.client;

import edu.hitsz.common.GameConstants;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ClientMain {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 20123;
    private static final String DEFAULT_SESSION_ID = "session-local";

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        String sessionId = args.length > 2 ? args[2] : DEFAULT_SESSION_ID;
        SwingUtilities.invokeLater(() -> startWindowedClient("Aircraft War Client", host, port, sessionId, null));
    }

    static void startWindowedClient(String title, String host, int port, String sessionId, Runnable onClose) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame frame = new JFrame(title);
        frame.setSize(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        frame.setResizable(false);
        frame.setBounds(
                ((int) screenSize.getWidth() - GameConstants.WINDOW_WIDTH) / 2,
                0,
                GameConstants.WINDOW_WIDTH,
                GameConstants.WINDOW_HEIGHT
        );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onClose != null) {
                    onClose.run();
                }
                AudioManager.getInstance().shutdown();
            }
        });
        LauncherPanel launcherPanel = new LauncherPanel(selection ->
                launchGame(frame, host, port, sessionId, selection, onClose)
        );
        frame.add(launcherPanel);
        frame.setVisible(true);
        launcherPanel.requestFocusInWindow();
    }

    private static void launchGame(
            JFrame frame,
            String host,
            int port,
            String sessionId,
            LauncherSelectionModel selection,
            Runnable onClose
    ) {
        frame.getContentPane().removeAll();
        Game game = new Game();
        SocketClientSession clientSession = new SocketClientSession(host, port, sessionId, game);
        game.attachCommandPublisher(clientSession);
        frame.add(game);
        for (WindowListener listener : frame.getWindowListeners()) {
            frame.removeWindowListener(listener);
        }
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clientSession.stop();
                if (onClose != null) {
                    onClose.run();
                }
                AudioManager.getInstance().shutdown();
            }
        });
        frame.revalidate();
        frame.repaint();
        game.requestFocusInWindow();
        clientSession.start();
        if ("JOIN".equals(selection.getEntryMode())) {
            clientSession.publishJoinRoom(selection.getRoomCode());
        } else {
            clientSession.publishCreateRoom(selection.getDifficulty());
        }
        game.action();
    }
}
