package de.cubeattack.api.util;

import de.cubeattack.api.logger.LogManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@SuppressWarnings("unused")
public class Logs {

    public static void write(Path path,String message) {
        try {

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            Files.writeString(
                    path,
                    message + "\n",
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
            );

        } catch (IOException e) {
            LogManager.getLogger().error("Error writing to log file", e);
        }
    }
}
