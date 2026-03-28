package edu.hitsz.server;

public class ServerLaunchConfigTest {

    public static void main(String[] args) {
        defaultsToPublicListenerProfile();
        supportsLegacyPortOnlyArgs();
        supportsExplicitHostPortAndBacklog();
    }

    private static void defaultsToPublicListenerProfile() {
        ServerLaunchConfig config = ServerLaunchConfig.fromArgs(new String[0]);

        assert "0.0.0.0".equals(config.getBindHost()) : "Default bind host should listen on all interfaces";
        assert config.getPort() == 20123 : "Default port should remain 20123";
        assert config.getBacklog() == 128 : "Default backlog should be production-friendly";
    }

    private static void supportsLegacyPortOnlyArgs() {
        ServerLaunchConfig config = ServerLaunchConfig.fromArgs(new String[]{"20124"});

        assert "0.0.0.0".equals(config.getBindHost()) : "Port-only startup should preserve the default bind host";
        assert config.getPort() == 20124 : "Single CLI arg should still be treated as the port";
        assert config.getBacklog() == 128 : "Backlog should default when omitted";
    }

    private static void supportsExplicitHostPortAndBacklog() {
        ServerLaunchConfig config = ServerLaunchConfig.fromArgs(new String[]{"47.112.172.41", "20125", "256"});

        assert "47.112.172.41".equals(config.getBindHost()) : "Explicit bind host should be preserved";
        assert config.getPort() == 20125 : "Explicit port should be parsed";
        assert config.getBacklog() == 256 : "Explicit backlog should be parsed";
    }
}
