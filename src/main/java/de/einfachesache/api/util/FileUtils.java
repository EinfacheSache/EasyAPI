package de.einfachesache.api.util;

import de.einfachesache.api.AsyncExecutor;
import de.einfachesache.api.logger.LogManager;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class FileUtils {

    private final AtomicReference<CompletableFuture<Void>> readyRef = new AtomicReference<>(CompletableFuture.completedFuture(null));
    private final YamlConfiguration configuration = new YamlConfiguration();
    private final String fileName;
    private final URL inputURL;
    private final Path path;

    public FileUtils(URL inputURL, String folder, String fileName) {
        this(inputURL, folder, fileName, false);
    }

    public FileUtils(URL inputURL, String folder, String fileName, boolean skipLoading) {
        this.fileName = fileName;
        this.inputURL = inputURL;
        this.path = Paths.get(folder).resolve(fileName);

        ensureFileExists();
        if (!skipLoading) {
            loadConfigurationFromDisk();
            LogManager.getLogger().info("Loaded file: " + fileName);
        } else {
            LogManager.getLogger().info("File created (skipped load): " + fileName);
        }
    }

    private void ensureFileExists() {
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                try (InputStream in = this.inputURL.openStream()) {
                    if (in == null) {
                        throw new IOException("InputStream is null for " + fileName);
                    }
                    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException ex) {
            LogManager.getLogger().error("Error while ensuring file " + fileName, ex);
        }
    }

    private void loadConfigurationFromDisk() {
        try {
            configuration.load(path.toFile());
        } catch (InvalidConfigurationException ex) {
            LogManager.getLogger().error("Invalid YAML in " + fileName, ex);
        } catch (IOException ex) {
            LogManager.getLogger().error("I/O error while loading " + fileName, ex);
        }
    }

    /**
     * Synchronously reloads the file from disk (blocking I/O).
     * Prefer {@link #reloadConfigurationAsync()} for non-blocking usage.
     *
     * @deprecated since 1.1 – use {@link #reloadConfigurationAsync()}.
     */
    public void reloadConfiguration() {
        ensureFileExists();
        loadConfigurationFromDisk();
        LogManager.getLogger().info("Reload file: " + fileName);
    }

    public CompletableFuture<Void> reloadConfigurationAsync() {
        return readyRef.updateAndGet(prev ->
                prev.thenRunAsync(() -> {
                    ensureFileExists();
                    try {
                        configuration.load(path.toFile());
                        LogManager.getLogger().info("Reload file async: " + fileName);
                    } catch (InvalidConfigurationException | IOException ex) {
                        LogManager.getLogger().error("Failed to reload " + fileName, ex);
                    }
                }, AsyncExecutor.getService())
        );
    }

    public CompletableFuture<Void> saveAsync() {
        return readyRef.updateAndGet(prev ->
                prev.thenRunAsync(() -> {
                    try {
                        configuration.save(path.toFile());
                    } catch (IOException ex) {
                        LogManager.getLogger().error("Failed to save " + fileName, ex);
                    }
                }, AsyncExecutor.getService())
        );
    }

    public void saveAsync(String key, Object value) {
        this.getConfig().set(key, value);
        this.saveAsync();
    }

    public void remove(String path) {
        this.getConfig().set(path, null);
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

    public float getFloat(String path) {
        return this.getConfig().getFloat(path);
    }

    public double getDouble(String path) {
        return this.getConfig().getDouble(path);
    }

    public long getLong(String path) {
        return this.getConfig().getLong(path);
    }

    public Object getObject(String path) {
        return this.getConfig().get(path);
    }

    public Map<String, Object> getMap(String path, boolean deep) {
        var section = getConfig().getConfigurationSection(path);
        return section == null ? Collections.emptyMap() : section.getValues(deep);
    }

    public List<?> getList(String path) {
        return this.getConfig().getList(path);
    }

    public List<String> getStringList(String path) {
        return this.getConfig().getStringList(path);
    }

    public List<Map<?, ?>> getMapList(String path) {
        return this.getConfig().getMapList(path);
    }

    public String get(String path, String def) {
        return this.getConfig().getString(path, def);
    }

    public int getInt(String path, int def) {
        return this.getConfig().getInt(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return this.getConfig().getBoolean(path, def);
    }

    public float getFloat(String path, float def) {
        return this.getConfig().getFloat(path, def);
    }

    public double getDouble(String path, double def) {
        return this.getConfig().getDouble(path, def);
    }

    public long getLong(String path, long def) {
        return this.getConfig().getLong(path, def);
    }

    public Object getObject(String path, Object def) {
        return this.getConfig().get(path, def);
    }

    public List<?> getList(String path, List<?> def) {
        return this.getConfig().getList(path, def);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return this.getConfig().getConfigurationSection(path);
    }

    public YamlConfiguration getConfig() {
        return configuration;
    }

    public URL getInput() {
        return inputURL;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return path.toFile();
    }
}


