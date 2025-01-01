package de.cubeattack.api.minecraft.stats;

import com.google.gson.Gson;
import de.cubeattack.api.logger.LogManager;
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


    public static void runStatsUpdateSchedule(String ID, String address, Stats stats, long updatePeriodInSec) {

        LogManager.getLogger().info("StatsUpdate scheduler started");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                RequestBody requestBody = RequestBody.create(new Gson().toJson(stats), MediaType.parse("application/json"));
                if(!updateStats(requestBody, String.valueOf(UUID.nameUUIDFromBytes((ID + ":" + address).getBytes(StandardCharsets.UTF_8)))))
                    LogManager.getLogger().info("Request to update stats failed");
                else
                    LogManager.getLogger().debug("Request to update stats was successful");
            }
        }, 1000, 1000 * updatePeriodInSec);
    }

    private static boolean updateStats(RequestBody requestBody, String identifier) {
        try {
            Request request = new Request.Builder()
                    .url(statsServer)
                    .addHeader("accept", "*/*")
                    .addHeader("Content-Type", "application/json")
                    .header("identifier", identifier)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.code() == 200;
            }
        } catch (IOException exception) {
            return false;
        }
    }
}
