package de.cubeattack.api.util;

import de.cubeattack.api.API;
import de.cubeattack.api.logger.LogManager;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class FileUtils
{
    private final YamlConfiguration configuration;
    private final InputStream inputStream;
    private final String fileName;
    private final File file;

    public FileUtils(InputStream inputStream, String folder, String fileName) {
        this(inputStream, folder, fileName, false);
    }

    public FileUtils(InputStream inputStream, String folder, String fileName, boolean skipLoading) {
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.configuration = new YamlConfiguration();
        this.file = new File (folder +  "/" + fileName);
        copyToFile(skipLoading);
    }

    private void copyToFile(boolean skipLoading) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                Files.copy(inputStream, file.toPath());
            }

            if (!skipLoading) {
                configuration.load(file);
            }

            LogManager.getLogger().info("The file " + fileName + " has been successfully loaded");
        } catch (IOException | InvalidConfigurationException ex) {
            LogManager.getLogger().error("Error whiles creating : " + fileName + " " + ex.getLocalizedMessage());
        }
    }

    public void save() {
       API.getExecutorService().submit(() -> {
            try {
                configuration.save(file);
            } catch (IOException ex) {
                LogManager.getLogger().error("Error whiles saving " + fileName + " " + ex.getLocalizedMessage());
            }
        });
    }

    public void set(String path, Object value) {
        this.getConfig().set(path, value);
    }

    public String get(String path) {
        return this.getConfig().getString(path);
    }
    public int getInt(String path) {
        return this.getConfig().getInt(path);
    }
    public boolean getBoolean(String path) {
        return this.getConfig().getBoolean(path);
    }
    public double getDouble(String path) {
        return this.getConfig().getDouble(path);
    }
    public List<?> getList(String path) {
        return this.getConfig().getList(path);
    }
    public List<String> getListAsList(String path) {
        return this.getConfig().getStringList(path);
    }

    public int getInt(String path, int def) {
        return this.getConfig().getInt(path, def);
    }
    public boolean getBoolean(String path, boolean def) {
        return this.getConfig().getBoolean(path, def);
    }
    public String getString(String path, String def) {
        return this.getConfig().getString(path, def);
    }
    public double getDouble(String path, double def) {
        return this.getConfig().getDouble(path, def);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return this.getConfig().getConfigurationSection(path);
    }
    public YamlConfiguration getConfig() {
        return configuration;
    }
    public InputStream getInput() {
        return inputStream;
    }
    public String getFileName() {
        return fileName;
    }
    public File getFile() {
        return file;
    }
}


