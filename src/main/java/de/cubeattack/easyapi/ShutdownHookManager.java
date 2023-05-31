package de.cubeattack.easyapi;

@SuppressWarnings("unused")
public class ShutdownHookManager
{

    public ShutdownHookManager(){
        register(() -> Runtime.getRuntime().halt(0));
    }
    public void register(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }
}