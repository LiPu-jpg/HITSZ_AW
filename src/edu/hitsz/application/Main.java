package edu.hitsz.application;

import edu.hitsz.application.client.SocketClientSession;
import edu.hitsz.application.server.LocalAuthorityServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 程序入口
 * @author hitsz
 */
public class Main {

    public static final int WINDOW_WIDTH = 512;
    public static final int WINDOW_HEIGHT = 768;

    public static void main(String[] args) {

        System.out.println("Hello Aircraft War");

        LocalAuthorityServer authorityServer = new LocalAuthorityServer(0);
        authorityServer.start();

        // 获得屏幕的分辨率，初始化 Frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame frame = new JFrame("Aircraft War");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);
        //设置窗口的大小和位置,居中放置
        frame.setBounds(((int) screenSize.getWidth() - WINDOW_WIDTH) / 2, 0,
                WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Game game = new Game();
        SocketClientSession clientSession = new SocketClientSession(
                "127.0.0.1",
                authorityServer.getPort(),
                authorityServer.getLocalSessionId(),
                game
        );
        game.attachCommandPublisher(clientSession);
        frame.add(game);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clientSession.stop();
                authorityServer.stop();
            }
        });
        frame.setVisible(true);
        game.requestFocusInWindow();
        clientSession.start();
        game.action();
    }
}
