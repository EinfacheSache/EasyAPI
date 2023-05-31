package de.cubeattack.easyapi;

@SuppressWarnings("unused")
public class ShutdownHookManager
{
    public static void register(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }
}