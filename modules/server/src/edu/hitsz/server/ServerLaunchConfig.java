package edu.hitsz.server;

public final class ServerLaunchConfig {

    private static final String DEFAULT_BIND_HOST = "0.0.0.0";
    private static final int DEFAULT_PORT = 20123;
    private static final int DEFAULT_BACKLOG = 128;

    private final String bindHost;
    private final int port;
    private final int backlog;

    private ServerLaunchConfig(String bindHost, int port, int backlog) {
        this.bindHost = bindHost;
        this.port = port;
        this.backlog = backlog;
    }

    public static ServerLaunchConfig fromArgs(String[] args) {
        if (args == null || args.length == 0) {
            return new ServerLaunchConfig(DEFAULT_BIND_HOST, DEFAULT_PORT, DEFAULT_BACKLOG);
        }
        if (args.length == 1) {
            return new ServerLaunchConfig(DEFAULT_BIND_HOST, Integer.parseInt(args[0]), DEFAULT_BACKLOG);
        }
        String bindHost = args[0] == null || args[0].trim().isEmpty() ? DEFAULT_BIND_HOST : args[0].trim();
        int port = Integer.parseInt(args[1]);
        int backlog = args.length >= 3 ? Integer.parseInt(args[2]) : DEFAULT_BACKLOG;
        return new ServerLaunchConfig(bindHost, port, backlog);
    }

    public String getBindHost() {
        return bindHost;
    }

    public int getPort() {
        return port;
    }

    public int getBacklog() {
        return backlog;
    }
}
