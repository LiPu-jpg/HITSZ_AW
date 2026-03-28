package edu.hitsz.common.protocol.socket;

import edu.hitsz.common.protocol.json.JsonMessageCodec;

import java.net.ServerSocket;
import java.net.Socket;

public class SocketTransportConfigurationTest {

    public static void main(String[] args) throws Exception {
        clientSocketEnablesLowLatencyOptions();
        serverTransportBindsRequestedHost();
    }

    private static void clientSocketEnablesLowLatencyOptions() throws Exception {
        Socket socket = new Socket();
        SocketClientTransport.applyClientSocketOptions(socket);

        assert socket.getTcpNoDelay() : "Client transport should enable TCP_NODELAY";
        assert socket.getKeepAlive() : "Client transport should enable keep-alive";

        socket.close();
    }

    private static void serverTransportBindsRequestedHost() throws Exception {
        SocketServerTransport transport = new SocketServerTransport("127.0.0.1", 0, 16, new JsonMessageCodec());
        transport.start();
        try {
            ServerSocket serverSocket = transport.getServerSocketForTest();
            assert serverSocket != null : "Transport should expose the bound server socket for verification";
            assert serverSocket.getInetAddress().isLoopbackAddress()
                    : "Explicit 127.0.0.1 binding should stay on the loopback interface";
            assert serverSocket.getLocalPort() > 0 : "Ephemeral port binding should allocate a real local port";
            assert serverSocket.getReuseAddress() : "Server socket should enable SO_REUSEADDR";
        } finally {
            transport.stop();
        }
    }
}
