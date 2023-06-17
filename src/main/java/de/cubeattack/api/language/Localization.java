package de.cubeattack.api.language;

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

    public String get(String key, Object... format) {
        return get(key, this.defaultLocale, format);
    }

    public String get(String key, Locale locale, Object... format) {
        try {
            ResourceBundle bundle = getResourceBundle(locale);
            if (!bundle.containsKey(key)) {
                bundle = getDefaultResourceBundle();
                if (!bundle.containsKey(key))
                    return key;
            }
            if (format.length == 0)
                return bundle.getString(key);
            return MessageFormat.format(bundle.getString(key), format);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle(this.resourceBundlePrefix, locale, this.loader);
        } catch (MissingResourceException e) {
            return getDefaultResourceBundle();
        }
    }

    public ResourceBundle getDefaultResourceBundle() {
        return ResourceBundle.getBundle(this.resourceBundlePrefix, this.defaultLocale, this.loader);
    }
}