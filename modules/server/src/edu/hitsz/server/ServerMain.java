package edu.hitsz.server;

public class ServerMain {

    public static void main(String[] args) throws InterruptedException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 20123;
        LocalAuthorityServer server = new LocalAuthorityServer(port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        synchronized (ServerMain.class) {
            ServerMain.class.wait();
        }
    }
}
