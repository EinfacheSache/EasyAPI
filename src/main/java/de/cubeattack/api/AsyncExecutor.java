package de.cubeattack.api;

import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.shutdown.ShutdownHook;

import java.util.concurrent.*;

@SuppressWarnings("unused")
public class AsyncExecutor {

    private static final ScheduledThreadPoolExecutor EXECUTOR =
            new ScheduledThreadPoolExecutor(1, r -> {
                Thread t = new Thread(r, "async-task-1");
                t.setDaemon(true);
                return t;
            }) {
                @Override
                protected void afterExecute(Runnable r, Throwable t) {
                    super.afterExecute(r, t);
                    if (t == null && r instanceof Future<?> f) {
                        try {
                            if (f.isDone()) f.get();
                        } catch (CancellationException ignored) {
                            return;
                        } catch (ExecutionException e) {
                            t = e.getCause();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (t != null) LogManager.getLogger().error("Async task failed", t);
                }
            };

    static {
        EXECUTOR.setRemoveOnCancelPolicy(true);
        ShutdownHook.register(EXECUTOR::shutdown);
    }

    private AsyncExecutor() {}

    public static ScheduledExecutorService getService() {
        return EXECUTOR;
    }

    public static Runnable safe(Runnable r) {
        return () -> {
            try {
                r.run();
            } catch (Throwable t) {
                LogManager.getLogger().error("Async task failed", t);
            }
        };
    }
}