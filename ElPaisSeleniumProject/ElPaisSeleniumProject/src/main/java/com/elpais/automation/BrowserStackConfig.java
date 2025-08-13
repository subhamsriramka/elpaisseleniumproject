package com.elpais.automation;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.edge.EdgeOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowserStackConfig provides browser configurations for BrowserStack testing
 * Defines different browser and device combinations for parallel testing
 */
public class BrowserStackConfig {
    public static List<MutableCapabilities> getBrowserConfigurations() {
        List<MutableCapabilities> configs = new ArrayList<>();
        
        // Desktop Browsers
        configs.add(getChromeDesktopConfig());
        configs.add(getFirefoxDesktopConfig());
        configs.add(getEdgeDesktopConfig());
        
        // Mobile Browsers
        configs.add(getChromeMobileConfig());
        configs.add(getSafariMobileConfig());
        
        return configs;
    }
    
    /**
     * Chrome Desktop Configuration
     */
    private static MutableCapabilities getChromeDesktopConfig() {
        ChromeOptions options = new ChromeOptions();
        
        // Browser version at top-level per W3C
        options.setCapability("browserVersion", "latest");
        
        // bstack:options for BrowserStack-specific caps
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("os", "Windows");
        bstackOptions.setCapability("osVersion", "11");
        bstackOptions.setCapability("resolution", "1920x1080");
        bstackOptions.setCapability("projectName", "El Pais Selenium Project");
        bstackOptions.setCapability("buildName", "Build 1.0");
        bstackOptions.setCapability("sessionName", "El Pais Scraping - Chrome Desktop");
        options.setCapability("bstack:options", bstackOptions);
        
        // Browser specific options
        options.addArguments("--lang=es");
        options.addArguments("--accept-lang=es-ES,es");
        
        System.out.println("Configured Chrome Desktop");
        return options;
    }
    
    /**
     * Firefox Desktop Configuration
     */
    private static MutableCapabilities getFirefoxDesktopConfig() {
        FirefoxOptions options = new FirefoxOptions();
        
        // Browser version at top-level per W3C
        options.setCapability("browserVersion", "latest");
        
        // bstack:options for BrowserStack-specific caps
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("os", "Windows");
        bstackOptions.setCapability("osVersion", "11");
        bstackOptions.setCapability("resolution", "1920x1080");
        bstackOptions.setCapability("projectName", "El Pais Selenium Project");
        bstackOptions.setCapability("buildName", "Build 1.0");
        bstackOptions.setCapability("sessionName", "El Pais Scraping - Firefox Desktop");
        options.setCapability("bstack:options", bstackOptions);
        
        System.out.println("Configured Firefox Desktop");
        return options;
    }
    
    /**
     * Edge Desktop Configuration
     */
    private static MutableCapabilities getEdgeDesktopConfig() {
        EdgeOptions options = new EdgeOptions();
        
        // Browser version at top-level per W3C
        options.setCapability("browserVersion", "latest");
        
        // bstack:options for BrowserStack-specific caps
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("os", "Windows");
        bstackOptions.setCapability("osVersion", "11");
        bstackOptions.setCapability("resolution", "1920x1080");
        bstackOptions.setCapability("projectName", "El Pais Selenium Project");
        bstackOptions.setCapability("buildName", "Build 1.0");
        bstackOptions.setCapability("sessionName", "El Pais Scraping - Edge Desktop");
        options.setCapability("bstack:options", bstackOptions);
        
        System.out.println("Configured Edge Desktop");
        return options;
    }
    
    /**
     * Chrome Mobile Configuration
     */
    private static MutableCapabilities getChromeMobileConfig() {
        ChromeOptions options = new ChromeOptions();
        
        // bstack:options for real mobile device
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("deviceName", "Samsung Galaxy S22");
        bstackOptions.setCapability("osVersion", "12.0");
        bstackOptions.setCapability("realMobile", "true");
        bstackOptions.setCapability("projectName", "El Pais Selenium Project");
        bstackOptions.setCapability("buildName", "Build 1.0");
        bstackOptions.setCapability("sessionName", "El Pais Scraping - Chrome Mobile");
        options.setCapability("bstack:options", bstackOptions);
        
        System.out.println("Configured Chrome Mobile");
        return options;
    }
    
    /**
     * Safari Mobile Configuration
     */
    private static MutableCapabilities getSafariMobileConfig() {
        SafariOptions options = new SafariOptions();
        
        // bstack:options for real mobile device
        MutableCapabilities bstackOptions = new MutableCapabilities();
        bstackOptions.setCapability("deviceName", "iPhone 14");
        bstackOptions.setCapability("osVersion", "16");
        bstackOptions.setCapability("realMobile", "true");
        bstackOptions.setCapability("projectName", "El Pais Selenium Project");
        bstackOptions.setCapability("buildName", "Build 1.0");
        bstackOptions.setCapability("sessionName", "El Pais Scraping - Safari Mobile");
        options.setCapability("bstack:options", bstackOptions);
        
        System.out.println("Configured Safari Mobile");
        return options;
    }

    public static MutableCapabilities getSimpleConfig() {
        return getChromeDesktopConfig();
    }
    
    /**
     * Print all available configurations
     */
    public static void printAvailableConfigurations() {
        System.out.println("\n=== AVAILABLE BROWSER CONFIGURATIONS ===");
        System.out.println("1. Chrome Desktop (Windows 11)");
        System.out.println("2. Firefox Desktop (Windows 11)");
        System.out.println("3. Edge Desktop (Windows 11)");
        System.out.println("4. Chrome Mobile (Samsung Galaxy S22)");
        System.out.println("5. Safari Mobile (iPhone 14)");
        System.out.println("==========================================");
    }
} 