package de.cubeattack.api.language;

import de.cubeattack.api.logger.LogManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class Localization {
    private final String resourceBundlePrefix;

    private final Locale defaultLocale;

    private final ClassLoader loader;

    public Localization(String resourceBundlePrefix, Locale defaultLocale) {
        this(resourceBundlePrefix, defaultLocale, Localization.class.getClassLoader());
    }

    public Localization(String resourceBundlePrefix, Locale defaultLocale, ClassLoader loader) {
        this.resourceBundlePrefix = resourceBundlePrefix;
        this.defaultLocale = defaultLocale;
        this.loader = loader;
    }

    public Localization(String resourceBundlePrefix, Locale defaultLocale, File file) {
        ClassLoader tempLoader;
        this.resourceBundlePrefix = resourceBundlePrefix;
        this.defaultLocale = defaultLocale;
        try {
            tempLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
        }catch (MalformedURLException ex){
            ex.printStackTrace();
            tempLoader = Localization.class.getClassLoader();
        }
        this.loader = tempLoader;
    }

    public String get(String key, Object... format) {
        return get(key, this.defaultLocale, format);
    }

    public String get(String key, Locale locale, Object... format) {
        try {
            ResourceBundle bundle = getResourceBundle(locale);
            if (!bundle.containsKey(key)) {
                LogManager.getLogger().error("The '" + locale.getDisplayLanguage(Locale.ENGLISH) + "' language file does not contain key '" + key + "'");
                bundle = getDefaultResourceBundle();
                if (!bundle.containsKey(key)) {
                    bundle = getResourceBundle(Locale.ENGLISH);
                    if (!bundle.containsKey(key)) {
                        return key;
                    }
                }
            }
            return format.length == 0 ? bundle.getString(key) : MessageFormat.format(bundle.getString(key), format);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
            return key;
        }
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle(this.resourceBundlePrefix, locale, this.loader);
        } catch (MissingResourceException ex) {
            return getDefaultResourceBundle();
        }
    }

    public ResourceBundle getDefaultResourceBundle() {
        return ResourceBundle.getBundle(this.resourceBundlePrefix, this.defaultLocale, this.loader);
    }
}