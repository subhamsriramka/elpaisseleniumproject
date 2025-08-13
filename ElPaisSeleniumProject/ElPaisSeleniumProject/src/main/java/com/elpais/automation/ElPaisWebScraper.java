package com.elpais.automation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.PageLoadStrategy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * ElPaisWebScraper handles the web scraping of El Pais articles
 * Uses Selenium WebDriver to navigate and extract content
 */
public class ElPaisWebScraper {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl;
    private final String opinionSection;
    private final String downloadDir;
    
    public ElPaisWebScraper() {
        this.baseUrl = ConfigManager.getElPaisBaseUrl();
        this.opinionSection = ConfigManager.getElPaisOpinionSection();
        this.downloadDir = ConfigManager.getDownloadDirectory();
        
        // Create download directory if it doesn't exist
        createDownloadDirectory();
    }
    
    /**
     * Initialize WebDriver for local testing
     */
    public void initializeLocalDriver() {
        try {
            // WebDriverManager automatically downloads and sets up ChromeDriver
            WebDriverManager.chromedriver().setup();
            
            // Configure Chrome options
            ChromeOptions options = new ChromeOptions();
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            options.addArguments("--headless=new"); // Run in background (no GUI)
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            
            // Set language to Spanish
            options.addArguments("--lang=es");
            options.addArguments("--accept-lang=es-ES,es");
            
            driver = new ChromeDriver(options);
            
            // Set timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigManager.getPageLoadTimeout()));
            
            // Initialize WebDriverWait
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            System.out.println("Local Chrome driver initialized successfully");
            
        } catch (Exception e) {
            System.out.println("Error initializing local driver: " + e.getMessage());
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }
    
    /**
     * Initialize WebDriver for BrowserStack (remote testing)
     */
    public void initializeBrowserStackDriver(MutableCapabilities capabilities) {
        try {
            String username = ConfigManager.getBrowserStackUsername();
            String accessKey = ConfigManager.getBrowserStackAccessKey();
            String hubUrl = ConfigManager.getBrowserStackHubUrl();
            
            if (username == null || accessKey == null || username.contains("YOUR_")) {
                throw new RuntimeException("Please set your BrowserStack credentials in config.properties");
            }
            
            // Inject credentials using W3C-compliant bstack:options
            Object existing = capabilities.getCapability("bstack:options");
            MutableCapabilities bstackOptions;
            if (existing instanceof MutableCapabilities) {
                bstackOptions = (MutableCapabilities) existing;
            } else {
                bstackOptions = new MutableCapabilities();
            }
            bstackOptions.setCapability("userName", username);
            bstackOptions.setCapability("accessKey", accessKey);
            capabilities.setCapability("bstack:options", bstackOptions);
            
            // Create remote WebDriver
            driver = new RemoteWebDriver(new URL(hubUrl), capabilities);
            
            // Set timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));
            
            // Initialize WebDriverWait
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            System.out.println("BrowserStack driver initialized successfully");
            
        } catch (Exception e) {
            System.out.println("Error initializing BrowserStack driver: " + e.getMessage());
            throw new RuntimeException("Failed to initialize BrowserStack WebDriver", e);
        }
    }
    
    /**
     * Navigate to El Pais website and verify Spanish language
     */
    public void navigateToElPais() {
        try {
            System.out.println("Navigating to El Pais website...");
            try {
                driver.get(baseUrl);
            } catch (WebDriverException loadError) {
                System.out.println("Page load issue detected (will attempt to proceed): " + loadError.getMessage());
                try {
                    ((JavascriptExecutor) driver).executeScript("window.stop();");
                } catch (Exception ignore) {}
            }
            
            // Wait for page to load sufficiently
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Check if page is in Spanish by looking for common Spanish words
            String pageSource = driver.getPageSource().toLowerCase();
            if (pageSource.contains("opinión") || pageSource.contains("política") || pageSource.contains("españa")) {
                System.out.println("✓ Website is displaying content in Spanish");
            } else {
                System.out.println("⚠ Warning: Website might not be in Spanish");
            }
            
            System.out.println("Successfully navigated to: " + driver.getCurrentUrl());
            
        } catch (Exception e) {
            System.out.println("Error navigating to El Pais: " + e.getMessage());
            throw new RuntimeException("Failed to navigate to El Pais", e);
        }
    }
    
    /**
     * Navigate to Opinion section
     */
    public void navigateToOpinionSection() {
        try {
            System.out.println("Navigating to Opinion section...");
            
            // Try to find and click opinion link
            List<WebElement> opinionLinks = driver.findElements(By.partialLinkText("Opinión"));
            if (opinionLinks.isEmpty()) {
                opinionLinks = driver.findElements(By.partialLinkText("Opinion"));
            }
            
            if (!opinionLinks.isEmpty()) {
                opinionLinks.get(0).click();
                System.out.println("✓ Clicked on Opinion section link");
            } else {
                // Fallback: navigate directly to opinion URL
                String opinionUrl = baseUrl + opinionSection;
                driver.get(opinionUrl);
                System.out.println("✓ Navigated directly to Opinion section: " + opinionUrl);
            }
            
            // Wait for opinion page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("article")));
            
        } catch (Exception e) {
            System.out.println("Error navigating to Opinion section: " + e.getMessage());
            // Try direct navigation as fallback
            try {
                String opinionUrl = baseUrl + opinionSection;
                driver.get(opinionUrl);
                System.out.println("✓ Fallback navigation to Opinion section successful");
            } catch (Exception fallbackError) {
                throw new RuntimeException("Failed to navigate to Opinion section", fallbackError);
            }
        }
    }
    
    /**
     * Scrape articles from the Opinion section
     */
    public List<Article> scrapeArticles(int maxArticles) {
        List<Article> articles = new ArrayList<>();
        
        try {
            System.out.println("Starting to scrape articles...");
            
            // Find article elements - try multiple selectors
            List<WebElement> articleElements = findArticleElements();
            
            if (articleElements.isEmpty()) {
                System.out.println("No articles found on the page");
                return articles;
            }
            
            System.out.println("Found " + articleElements.size() + " article elements");
            
            // Process up to maxArticles
            int articlesToProcess = Math.min(maxArticles, articleElements.size());
            
            for (int i = 0; i < articlesToProcess; i++) {
                try {
                    WebElement articleElement = articleElements.get(i);
                    Article article = extractArticleData(articleElement, i + 1);
                    
                    if (article != null && article.getTitle() != null && !article.getTitle().trim().isEmpty()) {
                        articles.add(article);
                        System.out.println("✓ Successfully scraped article " + (i + 1));
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error scraping article " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            System.out.println("Successfully scraped " + articles.size() + " articles");
            
        } catch (Exception e) {
            System.out.println("Error during article scraping: " + e.getMessage());
        }
        
        return articles;
    }
    
    /**
     * Find article elements using multiple selectors
     */
    private List<WebElement> findArticleElements() {
        // Try different selectors commonly used by El Pais
        String[] selectors = {
            "article",
            ".c_t",
            ".articulo-item",
            ".story",
            "[data-dtm-region='articulo_portada']",
            ".elemento-multimedia"
        };
        
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    System.out.println("Found articles using selector: " + selector);
                    return elements;
                }
            } catch (Exception e) {
                // Continue with next selector
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Extract data from a single article element
     */
    private Article extractArticleData(WebElement articleElement, int articleNumber) {
        Article article = new Article();
        
        try {
            // Extract title
            String title = extractTitle(articleElement);
            article.setTitle(title);
            
            // Extract content preview
            String content = extractContent(articleElement);
            article.setContent(content);
            
            // Extract image URL
            String imageUrl = extractImageUrl(articleElement);
            article.setImageUrl(imageUrl);
            
            // Download image if available
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String imagePath = downloadImage(imageUrl, articleNumber);
                article.setImagePath(imagePath);
            }
            
        } catch (Exception e) {
            System.out.println("Error extracting article data: " + e.getMessage());
        }
        
        return article;
    }
    
    /**
     * Extract title from article element
     */
    private String extractTitle(WebElement articleElement) {
        String[] titleSelectors = {
            "h2", "h1", "h3", ".titulo", ".headline", ".title",
            "a[title]", ".c_t_a", ".story-title"
        };
        
        for (String selector : titleSelectors) {
            try {
                WebElement titleElement = articleElement.findElement(By.cssSelector(selector));
                String title = titleElement.getText().trim();
                if (!title.isEmpty()) {
                    return title;
                }
                
                // Try title attribute if text is empty
                title = titleElement.getAttribute("title");
                if (title != null && !title.trim().isEmpty()) {
                    return title.trim();
                }
                
            } catch (Exception e) {
                // Continue with next selector
            }
        }
        
        return "Title not found";
    }
    
    /**
     * Extract content from article element
     */
    private String extractContent(WebElement articleElement) {
        String[] contentSelectors = {
            "p", ".entradilla", ".summary", ".excerpt", 
            ".description", ".c_e", ".story-excerpt"
        };
        
        for (String selector : contentSelectors) {
            try {
                WebElement contentElement = articleElement.findElement(By.cssSelector(selector));
                String content = contentElement.getText().trim();
                if (!content.isEmpty() && content.length() > 20) {
                    return content;
                }
            } catch (Exception e) {
                // Continue with next selector
            }
        }
        
        return "Content preview not available";
    }
    
    /**
     * Extract image URL from article element
     */
    private String extractImageUrl(WebElement articleElement) {
        String[] imageSelectors = {
            "img", ".imagen img", ".photo img", ".c_m_e img"
        };
        
        for (String selector : imageSelectors) {
            try {
                WebElement imgElement = articleElement.findElement(By.cssSelector(selector));
                String src = imgElement.getAttribute("src");
                if (src != null && !src.isEmpty() && src.startsWith("http")) {
                    return src;
                }
                
                // Try data-src for lazy loading
                src = imgElement.getAttribute("data-src");
                if (src != null && !src.isEmpty() && src.startsWith("http")) {
                    return src;
                }
                
            } catch (Exception e) {
                // Continue with next selector
            }
        }
        
        return null;
    }
    
    /**
     * Download image from URL
     */
    private String downloadImage(String imageUrl, int articleNumber) {
        try {
            // Create filename
            String extension = imageUrl.substring(imageUrl.lastIndexOf("."));
            if (extension.length() > 5) {
                extension = ".jpg"; // Default extension
            }
            String fileName = "article_" + articleNumber + "_image" + extension;
            String filePath = downloadDir + File.separator + fileName;
            
            // Download image
            URL url = new URL(imageUrl);
            File file = new File(filePath);
            FileUtils.copyURLToFile(url, file, 5000, 5000); // 5 second timeout
            
            System.out.println("✓ Downloaded image: " + fileName);
            return filePath;
            
        } catch (Exception e) {
            System.out.println("Error downloading image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create download directory
     */
    private void createDownloadDirectory() {
        try {
            File dir = new File(downloadDir);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("Created download directory: " + downloadDir);
            }
        } catch (Exception e) {
            System.out.println("Error creating download directory: " + e.getMessage());
        }
    }
    
    /**
     * Close the WebDriver
     */
    public void close() {
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("WebDriver closed successfully");
            } catch (Exception e) {
                System.out.println("Error closing WebDriver: " + e.getMessage());
            }
        }
    }
} 