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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketServerTransport implements Transport {

    private final String configuredBindHost;
    private final int configuredPort;
    private final int configuredBacklog;
    private final MessageCodec codec;
    private final LineMessageFramer framer;
    private final List<ClientConnection> clients;

    private volatile ProtocolMessageListener listener;
    private volatile ServerConnectionListener connectionListener;
    private volatile boolean running;
    private ServerSocket serverSocket;
    private Thread acceptThread;

    public SocketServerTransport(int configuredPort, MessageCodec codec) {
        this("0.0.0.0", configuredPort, 128, codec);
    }

    public SocketServerTransport(String configuredBindHost, int configuredPort, int configuredBacklog, MessageCodec codec) {
        this.configuredBindHost = configuredBindHost == null || configuredBindHost.trim().isEmpty()
                ? "0.0.0.0"
                : configuredBindHost.trim();
        this.configuredPort = configuredPort;
        this.configuredBacklog = configuredBacklog;
        this.codec = codec;
        this.framer = new LineMessageFramer();
        this.clients = new CopyOnWriteArrayList<>();
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(configuredBindHost, configuredPort), configuredBacklog);
            running = true;
            acceptThread = new Thread(this::acceptLoop, "socket-server-acceptor");
            acceptThread.setDaemon(true);
            acceptThread.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start server transport", e);
        }
    }

    @Override
    public synchronized void stop() {
        running = false;
        closeQuietly(serverSocket);
        for (ClientConnection client : clients) {
            client.close();
        }
        clients.clear();
        serverSocket = null;
    }

    @Override
    public void send(ProtocolMessage message) {
        String framed = framer.frame(codec.encode(message));
        for (ClientConnection client : clients) {
            client.send(framed);
        }
    }

    public void sendToSessions(ProtocolMessage message, Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return;
        }
        HashSet<String> targetSessionIds = new HashSet<>(sessionIds);
        String framed = framer.frame(codec.encode(message));
        for (ClientConnection client : clients) {
            if (client.isBoundToAny(targetSessionIds)) {
                client.send(framed);
            }
        }
    }

    @Override
    public void setListener(ProtocolMessageListener listener) {
        this.listener = listener;
    }

    public void setConnectionListener(ServerConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public int getPort() {
        return serverSocket == null ? configuredPort : serverSocket.getLocalPort();
    }

    public String getBindHost() {
        if (serverSocket == null || serverSocket.getInetAddress() == null) {
            return configuredBindHost;
        }
        return serverSocket.getInetAddress().getHostAddress();
    }

    ServerSocket getServerSocketForTest() {
        return serverSocket;
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                ClientConnection clientConnection = new ClientConnection(socket);
                clients.add(clientConnection);
                clientConnection.start();
            } catch (SocketException e) {
                if (running) {
                    throw new IllegalStateException("Server socket accept failed", e);
                }
            } catch (IOException e) {
                if (running) {
                    throw new IllegalStateException("Server transport accept loop failed", e);
                }
            }
        }
    }

    private void closeQuietly(ServerSocket serverSocket) {
        if (serverSocket == null) {
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    private final class ClientConnection {

        private final Socket socket;
        private volatile String boundSessionId;
        private PrintWriter writer;

        private ClientConnection(Socket socket) {
            this.socket = socket;
        }

        private void start() throws IOException {
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            Thread readerThread = new Thread(() -> readLoop(reader), "socket-server-client-reader");
            readerThread.setDaemon(true);
            readerThread.start();
        }

        private void readLoop(BufferedReader reader) {
            try {
                String line;
                while (running && (line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    ProtocolMessage decoded = codec.decode(line);
                    if (decoded.getSessionId() != null && !decoded.getSessionId().trim().isEmpty()) {
                        boundSessionId = decoded.getSessionId();
                    }
                    ProtocolMessageListener currentListener = listener;
                    if (currentListener != null) {
                        currentListener.onMessage(decoded);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    throw new IllegalStateException("Server client read loop failed", e);
                }
            } finally {
                close();
                clients.remove(this);
                notifyDisconnected();
            }
        }

        private synchronized void send(String framed) {
            if (writer == null) {
                return;
            }
            writer.write(framed);
            writer.flush();
        }

        private boolean isBoundToAny(Collection<String> sessionIds) {
            return boundSessionId != null && sessionIds.contains(boundSessionId);
        }

        private void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        private void notifyDisconnected() {
            if (boundSessionId == null) {
                return;
            }
            ServerConnectionListener currentConnectionListener = connectionListener;
            if (currentConnectionListener != null) {
                currentConnectionListener.onClientDisconnected(boundSessionId);
            }
        }
    }
}
