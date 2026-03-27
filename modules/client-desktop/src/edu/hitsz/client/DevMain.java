package edu.hitsz.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class DevMain {

    public static void main(String[] args) {
        Object server = startLocalServer();
        int port = invokeInt(server, "getPort");
        String sessionId = invokeString(server, "getLocalSessionId");

        ClientMain.startWindowedClient(
                "Aircraft War Dev",
                "127.0.0.1",
                port,
                sessionId,
                () -> invokeVoid(server, "stop")
        );
    }

    private static Object startLocalServer() {
        try {
            Class<?> serverClass = loadServerClass();
            Constructor<?> constructor = serverClass.getConstructor(int.class);
            Object server = constructor.newInstance(0);
            invokeVoid(server, "start");
            return server;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to start local authority server for dev mode", e);
        }
    }

    private static Class<?> loadServerClass() throws ClassNotFoundException {
        try {
            return Class.forName("edu.hitsz.server.LocalAuthorityServer");
        } catch (ClassNotFoundException ignored) {
            return Class.forName("edu.hitsz.application.server.LocalAuthorityServer");
        }
    }

    private static void invokeVoid(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            method.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke " + methodName, e);
        }
    }

    private static int invokeInt(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return ((Integer) method.invoke(target)).intValue();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke " + methodName, e);
        }
    }

    private static String invokeString(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return (String) method.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke " + methodName, e);
        }
    }
}
