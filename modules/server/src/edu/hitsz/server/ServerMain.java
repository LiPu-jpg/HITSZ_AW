package edu.hitsz.server;

public class ServerMain {

    public static void main(String[] args) throws InterruptedException {
        ServerLaunchConfig launchConfig = ServerLaunchConfig.fromArgs(args);
        LocalAuthorityServer server = new LocalAuthorityServer(
                launchConfig.getBindHost(),
                launchConfig.getPort(),
                launchConfig.getBacklog()
        );
        try {
            server.start();
        } catch (IllegalStateException e) {
            System.err.println(ServerStartupMessage.formatStartFailure(launchConfig.getPort(), e));
            return;
        }
        System.out.println(ServerStartupMessage.formatStarted(server.getBindHost(), server.getPort()));

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        synchronized (ServerMain.class) {
            ServerMain.class.wait();
        }
    }
}
