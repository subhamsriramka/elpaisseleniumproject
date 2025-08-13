package com.elpais.automation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager handles reading configuration from properties file
 * This is a utility class that loads settings from config.properties
 */
public class ConfigManager {
    
    private static Properties properties;
    private static final String CONFIG_FILE = "config.properties";
    
    // Static block - runs once when class is first loaded
    static {
        loadProperties();
    }
    
    /**
     * Load properties from config.properties file
     */
    private static void loadProperties() {
        properties = new Properties();
        try {
            // Load the properties file from resources folder
            InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (input != null) {
                properties.load(input);
                System.out.println("Configuration loaded successfully");
            } else {
                System.out.println("Warning: config.properties file not found");
            }
        } catch (IOException e) {
            System.out.println("Error loading configuration: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    // Convenient methods for specific configurations
    public static String getBrowserStackUsername() {
        return getProperty("browserstack.username");
    }
    
    public static String getBrowserStackAccessKey() {
        return getProperty("browserstack.accesskey");
    }
    
    public static String getBrowserStackHubUrl() {
        return getProperty("browserstack.hub.url");
    }
    
    public static String getElPaisBaseUrl() {
        return getProperty("elpais.base.url");
    }
    
    public static String getElPaisOpinionSection() {
        return getProperty("elpais.opinion.section");
    }
    
    public static String getTranslationApiUrl() {
        return getProperty("translation.api.url");
    }
    
    public static String getDownloadDirectory() {
        return getProperty("download.directory", "downloads");
    }
    
    public static int getMaxArticles() {
        return Integer.parseInt(getProperty("max.articles", "5"));
    }
    
    public static int getParallelThreads() {
        return Integer.parseInt(getProperty("parallel.threads", "5"));
    }
    
    public static int getImplicitWait() {
        return Integer.parseInt(getProperty("implicit.wait", "10"));
    }
    
    public static int getPageLoadTimeout() {
        return Integer.parseInt(getProperty("page.load.timeout", "30"));
    }
} 