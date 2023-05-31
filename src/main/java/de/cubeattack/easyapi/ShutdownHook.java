package de.cubeattack.easyapi;

@SuppressWarnings("unused")
public class ShutdownHook
{
    public static void register(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }
}