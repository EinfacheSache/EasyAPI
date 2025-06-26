package de.cubeattack.api.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cubeattack.api.logger.LogManager;

import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

@SuppressWarnings("unused")
public class MinecraftAPI
{

    public static UUID loadUUID(String playerName) {
        String API_URL = "https://api.mojang.com/users/profiles/minecraft/";

        try (Scanner scanner = new Scanner(new URL(API_URL + playerName).openConnection().getInputStream())) {
            String json = scanner.nextLine();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String id = jsonObject.get("id").getAsString();
            return getUniqueIdFromString(id);
        } catch (Exception ex) {
            LogManager.getLogger().error("Error whiles loading UUID from " + playerName + " : " + ex.getLocalizedMessage());
            return null;
        }
    }

    private static UUID getUniqueIdFromString(String uuid) {
        return UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}
