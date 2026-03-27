package edu.hitsz.e2e;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EntryPointAvailabilityTest {

    public static void main(String[] args) throws Exception {
        assertHasMain("edu.hitsz.client.ClientMain");
        assertHasMain("edu.hitsz.server.ServerMain");
    }

    private static void assertHasMain(String className) throws Exception {
        Class<?> type = Class.forName(className);
        Method main = type.getMethod("main", String[].class);
        assert Modifier.isStatic(main.getModifiers()) : className + " should expose static main(String[])";
    }
}
