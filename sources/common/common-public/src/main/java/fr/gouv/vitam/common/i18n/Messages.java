/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.common.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import fr.gouv.vitam.common.CharsetUtils;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.logging.SysErrLogger;

/**
 * Internationalization Messages support
 */
public class Messages {
    private final String bundleName;

    /**
     * Default Locale
     */
    public static final Locale DEFAULT_LOCALE = Locale.FRENCH;

    private final ResourceBundle resourceBundle;
    private Locale locale;

    /**
     * Constructor using default Locale (FRENCH)
     *
     * @param bundleName
     */
    public Messages(String bundleName) {
        this(bundleName, DEFAULT_LOCALE);
    }

    /**
     * Constructor
     *
     * @param bundleName
     * @param locale
     */
    public Messages(String bundleName, Locale locale) {
        this.bundleName = bundleName;
        this.locale = locale;
        resourceBundle = init();
    }

    /**
     * Enable UTF-8 Property files
     */
    private static final class UTF8Control extends Control {
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
            boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, CharsetUtils.UTF8));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }

    /**
     * Change the Message to the given Locale
     *
     * @param locale
     */
    private final ResourceBundle init() {
        if (locale == null) {
            locale = Locale.FRENCH;
        }
        // First check if this file is in config directory
        File bundleFile =
            PropertiesUtils.fileFromConfigFolder(bundleName + "_" + locale.toLanguageTag() + ".properties");
        if (bundleFile.canRead()) {
            try (FileInputStream inputStream = new FileInputStream(bundleFile)) {
                return new PropertyResourceBundle(new InputStreamReader(inputStream, CharsetUtils.UTF8));
            } catch (IOException e) {
                SysErrLogger.FAKE_LOGGER.ignoreLog(e);
            }
        }
        // If necessary update Static enum of VitamCode
        return ResourceBundle.getBundle(bundleName, locale, new UTF8Control());
    }


    /**
     * Retrieve all the messages
     * 
     * @return map of messages
     */
    public Map<String, String> getAllMessages() {
        final Map<String, String> bundleMap = new HashMap<>();
        for (String key : resourceBundle.keySet()) {
            final String value = resourceBundle.getString(key);
            bundleMap.put(key, value);
        }
        return bundleMap;
    }

    /**
     *
     * @param key the key of the message
     * @return the associated message
     */
    public final String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (final MissingResourceException e) {
            SysErrLogger.FAKE_LOGGER.ignoreLog(e);
            return getFakeMessage(key);
        }
    }

    /**
     *
     * @param key the key of the message
     * @param args the arguments to use as MessageFormat.format(mesg, args)
     * @return the associated message
     */
    public final String getString(String key, Object... args) {
        try {
            final String source = resourceBundle.getString(key);
            return MessageFormat.format(source, args);
        } catch (final MissingResourceException e) {
            SysErrLogger.FAKE_LOGGER.ignoreLog(e);
            return getFakeMessage(key, args);
        }
    }

    private String getFakeMessage(String key, Object... args) {
        StringBuilder builder = new StringBuilder("!").append(key).append('!');
        if (args != null) {
            for (Object object : args) {
                builder.append(" ").append(object);
            }
        }
        return builder.toString();
    }

    /**
     *
     * @param key the key of the message
     * @param args the arguments to use as MessageFormat.format(mesg, args)
     * @return the associated message, !key! if value is null or empty
     */
    public final String getStringNotEmpty(String key, Object... args) {
        try {
            final String source = resourceBundle.getString(key);
            if (source == null || source.isEmpty()) {
                // Cannot find any resource or value for this key
                return getFakeMessage(key, args);
            }
            return MessageFormat.format(source, args);
        } catch (final MissingResourceException e) {
            SysErrLogger.FAKE_LOGGER.ignoreLog(e);
            return getFakeMessage(key, args);
        }
    }

    /**
     *
     * @return the current Locale
     */
    public Locale getLocale() {
        return locale;
    }
}
