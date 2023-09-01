package de.cubeattack.api;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class API {

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static ScheduledExecutorService getExecutorService() {
        return executorService;
    }
}
