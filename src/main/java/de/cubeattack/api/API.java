package de.cubeattack.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class API {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static ExecutorService getExecutorService() {
        return executorService;
    }
}
