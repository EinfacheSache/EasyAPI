package de.cubeattack.api.minecraft.stats;

import com.google.gson.Gson;
import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.shutdown.ShutdownHook;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@SuppressWarnings("unused")
public class StatsManager {

    private static final OkHttpClient client = new OkHttpClient();
    private static final String statsServer = "https://metrics.einfachesache.de/api/stats/plugin";

    public static void runStatsUpdateSchedule(String ID, String address, StatsProvider stats, long updatePeriodInSec) {

        LogManager.getLogger().info("StatsUpdate scheduler started");

        Timer statsUpdateTimer = new Timer();
        statsUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                RequestBody requestBody = RequestBody.create(new Gson().toJson(stats.getStats()), MediaType.parse("application/json"));
                int code = updateStats(requestBody, String.valueOf(UUID.nameUUIDFromBytes((ID + ":" + address).getBytes(StandardCharsets.UTF_8))));
                if(code == 200)
                    LogManager.getLogger().debug("Request to update stats was successful");
                else {
                    LogManager.getLogger().warn("Request to update stats failed (error-code: " + code + ")");
                }
            }
        }, 1000, 1000 * updatePeriodInSec);

        ShutdownHook.register(() -> {
            statsUpdateTimer.cancel();
            int code = sendOfflineStatus(String.valueOf(UUID.nameUUIDFromBytes((ID + ":" + address).getBytes(StandardCharsets.UTF_8))));
            if(code == 302)
                LogManager.getLogger().info("Request to send shutdown status to stats server was successful");
            else {
                LogManager.getLogger().warn("Request to send shutdown status to stats server failed (error-code: " + code + ")");
            }
        });

    }

    private static int sendOfflineStatus(String identifier) {
        Request request = new Request.Builder()
                .url(statsServer)
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "application/json")
                .header("identifier", identifier)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.code();
        }catch (IOException exception){
            LogManager.getLogger().error("Failed to send shutdown status to the stats server (exception: " + exception.getMessage() + ")");
            return 500;
        }
    }

    private static int updateStats(RequestBody requestBody, String identifier) {
        Request request = new Request.Builder()
                .url(statsServer)
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "application/json")
                .header("identifier", identifier)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.code();
        }catch (IOException exception){
            LogManager.getLogger().error("Failed to send stats update to the stats server (exception: " + exception.getMessage() + ")");
            return 500;
        }
    }
}
