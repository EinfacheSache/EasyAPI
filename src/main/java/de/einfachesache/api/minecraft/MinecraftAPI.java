package de.einfachesache.api.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.einfachesache.api.logger.LogManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class MinecraftAPI
{

    public static UUID loadUUID(String playerName) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + playerName;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "Java");

            int responseCode = conn.getResponseCode();
            if (responseCode == 204) {
                LogManager.getLogger().warn("Player '" + playerName + "' not found (204 No Content)");
                return null;
            }

            if (responseCode != 200) {
                LogManager.getLogger().error("Failed to fetch UUID for '" + playerName + "'. HTTP response code: " + responseCode);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String json = reader.lines().collect(Collectors.joining());

                if (json.isEmpty())
                    return null;

                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                return getUniqueIdFromString(obj.get("id").getAsString());
            }

        } catch (Exception ex) {
            LogManager.getLogger().error("Error loading UUID for '" + playerName + "': " + ex.getMessage());
            return null;
        }
    }

    private static UUID getUniqueIdFromString(String uuid) {
        return UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}
