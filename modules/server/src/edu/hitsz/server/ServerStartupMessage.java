package edu.hitsz.server;

import java.net.BindException;

public final class ServerStartupMessage {

    private ServerStartupMessage() {
    }

    public static String formatStartFailure(int port, Throwable throwable) {
        if (containsBindException(throwable)) {
            return "服务器启动失败：端口 " + port + " 已被占用。\n"
                    + "请先关闭旧服务，或改用其他端口，例如：java -cp <运行目录> edu.hitsz.server.ServerMain " + (port + 1);
        }
        String message = throwable == null ? null : throwable.getMessage();
        return "服务器启动失败：" + (message == null || message.trim().isEmpty() ? "未知错误" : message);
    }

    private static boolean containsBindException(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor instanceof BindException) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }
}
