package org.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class Prop {
    public static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            InputStream in = Prop.class.getClassLoader().getResourceAsStream("data/config.properties");
            if (in == null) {
                Logging.logError("[Properties] Could not load properties file");
                throw new FileNotFoundException();
            }
            properties.load(in);
            Logging.logSuccess("[Properties] Properties file loaded");
        } catch (Exception e) {
            Logging.logError("[Properties] Could not load properties file");
        }
        return properties;
    }
}
