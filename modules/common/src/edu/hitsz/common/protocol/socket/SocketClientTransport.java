package edu.hitsz.common.protocol.socket;

import edu.hitsz.common.protocol.MessageCodec;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.ProtocolMessageListener;
import edu.hitsz.common.protocol.Transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketClientTransport implements Transport {

    private final String host;
    private final int port;
    private final MessageCodec codec;
    private final LineMessageFramer framer;

    private volatile ProtocolMessageListener listener;
    private volatile boolean running;
    private Socket socket;
    private PrintWriter writer;
    private Thread readThread;

    public SocketClientTransport(String host, int port, MessageCodec codec) {
        this.host = host;
        this.port = port;
        this.codec = codec;
        this.framer = new LineMessageFramer();
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            running = true;
            readThread = new Thread(() -> readLoop(reader), "socket-client-reader");
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start client transport", e);
        }
    }

    @Override
    public synchronized void stop() {
        running = false;
        closeQuietly(socket);
        socket = null;
        writer = null;
    }

    @Override
    public synchronized void send(ProtocolMessage message) {
        if (writer == null) {
            throw new IllegalStateException("Client transport is not started");
        }
        writer.write(framer.frame(codec.encode(message)));
        writer.flush();
    }

    @Override
    public void setListener(ProtocolMessageListener listener) {
        this.listener = listener;
    }

    private void readLoop(BufferedReader reader) {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                ProtocolMessageListener currentListener = listener;
                if (currentListener != null) {
                    currentListener.onMessage(codec.decode(line));
                }
            }
        } catch (IOException e) {
            if (running) {
                throw new IllegalStateException("Client read loop failed", e);
            }
        } finally {
            stop();
        }
    }

    private void closeQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
