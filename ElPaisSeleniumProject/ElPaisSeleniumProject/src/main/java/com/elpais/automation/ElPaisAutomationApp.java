package com.elpais.automation;

import org.openqa.selenium.MutableCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ElPaisAutomationApp - Main application class
 * Orchestrates the entire automation process:
 * 1. Scrape articles from El Pais Opinion section
 * 2. Translate headers to English
 * 3. Analyze repeated words
 * 4. Run tests on BrowserStack (parallel execution)
 */
public class ElPaisAutomationApp {
    
    public static void main(String[] args) {
        System.out.println("=== EL PAIS SELENIUM AUTOMATION PROJECT ===");
        System.out.println("Starting automated scraping and analysis...\n");
        
        ElPaisAutomationApp app = new ElPaisAutomationApp();
        
        try {
            // Step 1: Run local test first
            System.out.println("STEP 1: Running local test...");
            List<Article> articles = app.runLocalTest();
            
            if (articles.isEmpty()) {
                System.out.println("No articles found. Exiting...");
                return;
            }
            
            // Step 2: Translate and analyze
            System.out.println("\nSTEP 2: Translating and analyzing articles...");
            app.translateAndAnalyze(articles);
            
            // Step 3: Run BrowserStack tests (parallel)
            System.out.println("\nSTEP 3: Running BrowserStack parallel tests...");
            app.runBrowserStackTests();
            
            System.out.println("\n=== AUTOMATION COMPLETED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            System.out.println("Error during automation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run local test to scrape articles
     */
    public List<Article> runLocalTest() {
        ElPaisWebScraper scraper = new ElPaisWebScraper();
        List<Article> articles = new ArrayList<>();
        
        try {
            // Initialize local Chrome driver
            scraper.initializeLocalDriver();
            
            // Navigate to El Pais
            scraper.navigateToElPais();
            
            // Navigate to Opinion section
            scraper.navigateToOpinionSection();
            
            // Scrape articles
            int maxArticles = ConfigManager.getMaxArticles();
            articles = scraper.scrapeArticles(maxArticles);
            
            // Print scraped articles
            printScrapedArticles(articles);
            
        } catch (Exception e) {
            System.out.println("Error during local test: " + e.getMessage());
        } finally {
            scraper.close();
        }
        
        return articles;
    }
    
    /**
     * Translate articles and analyze repeated words
     */
    public void translateAndAnalyze(List<Article> articles) {
        if (articles.isEmpty()) {
            System.out.println("No articles to translate and analyze");
            return;
        }
        
        TranslationService translator = new TranslationService();
        List<String> translatedHeaders = new ArrayList<>();
        
        System.out.println("\n=== TRANSLATING ARTICLE HEADERS ===");
        
        // Translate each article header
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            String spanishTitle = article.getTitle();
            
            if (spanishTitle != null && !spanishTitle.trim().isEmpty()) {
                System.out.println("\nArticle " + (i + 1) + ":");
                System.out.println("Spanish: " + spanishTitle);
                
                String englishTitle = translator.translateToEnglish(spanishTitle);
                article.setTranslatedTitle(englishTitle);
                translatedHeaders.add(englishTitle);
                
                System.out.println("English: " + englishTitle);
            }
        }
        
        // Analyze repeated words
        System.out.println("\n=== ANALYZING REPEATED WORDS ===");
        Map<String, Integer> repeatedWords = TextAnalyzer.analyzeRepeatedWords(translatedHeaders);
        
        // Print results
        TextAnalyzer.printAnalysisResults(repeatedWords);
        TextAnalyzer.printSummaryStatistics(translatedHeaders, repeatedWords);
    }
    
    /**
     * Run tests on BrowserStack with parallel execution
     */
    public void runBrowserStackTests() {
        try {
            // Check if BrowserStack credentials are configured
            String username = ConfigManager.getBrowserStackUsername();
            if (username == null || username.contains("YOUR_")) {
                System.out.println("⚠ BrowserStack credentials not configured.");
                System.out.println("Please update config.properties with your BrowserStack credentials to run parallel tests.");
                System.out.println("For now, skipping BrowserStack tests...");
                return;
            }
            
            List<MutableCapabilities> browserConfigs = BrowserStackConfig.getBrowserConfigurations();
            int parallelThreads = Math.min(ConfigManager.getParallelThreads(), browserConfigs.size());
            
            System.out.println("Running tests on " + parallelThreads + " parallel threads...");
            BrowserStackConfig.printAvailableConfigurations();
            
            // Create thread pool for parallel execution
            ExecutorService executor = Executors.newFixedThreadPool(parallelThreads);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            // Submit tasks for parallel execution
            for (int i = 0; i < parallelThreads; i++) {
                MutableCapabilities config = browserConfigs.get(i);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    runSingleBrowserStackTest(config);
                }, executor);
                futures.add(future);
            }
            
            // Wait for all tasks to complete
            CompletableFuture<Void> allTests = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allTests.get(); // Wait for completion
            executor.shutdown();
            
            System.out.println("✓ All BrowserStack parallel tests completed");
            
        } catch (Exception e) {
            System.out.println("Error running BrowserStack tests: " + e.getMessage());
        }
    }
    
    /**
     * Run a single test on BrowserStack
     */
    private void runSingleBrowserStackTest(MutableCapabilities capabilities) {
        ElPaisWebScraper scraper = new ElPaisWebScraper();
        String browserName = "BrowserStack Session";
        try {
            Object explicitName = capabilities.getCapability("name");
            if (explicitName != null) {
                browserName = explicitName.toString();
            } else {
                Object bstack = capabilities.getCapability("bstack:options");
                if (bstack instanceof MutableCapabilities) {
                    Object sessionName = ((MutableCapabilities) bstack).getCapability("sessionName");
                    if (sessionName != null) {
                        browserName = sessionName.toString();
                    }
                }
                if (browserName.equals("BrowserStack Session")) {
                    Object topBrowser = capabilities.getCapability("browserName");
                    if (topBrowser != null) {
                        browserName = topBrowser.toString();
                    }
                }
            }
        } catch (Exception ignored) {}
        
        try {
            System.out.println("Starting test: " + browserName);
            
            // Initialize BrowserStack driver
            scraper.initializeBrowserStackDriver(capabilities);
            
            // Navigate to El Pais
            scraper.navigateToElPais();
            
            // Navigate to Opinion section
            scraper.navigateToOpinionSection();
            
            // Scrape a few articles (reduced for parallel testing)
            List<Article> articles = scraper.scrapeArticles(2);
            
            System.out.println("✓ " + browserName + " - Scraped " + articles.size() + " articles");
            
            // Quick translation test
            if (!articles.isEmpty()) {
                TranslationService translator = new TranslationService();
                String firstTitle = articles.get(0).getTitle();
                String translated = translator.translateToEnglish(firstTitle);
                System.out.println("✓ " + browserName + " - Translation test successful");
            }
            
        } catch (Exception e) {
            System.out.println("✗ " + browserName + " - Test failed: " + e.getMessage());
        } finally {
            scraper.close();
        }
    }
    
    /**
     * Print scraped articles in a formatted way
     */
    private void printScrapedArticles(List<Article> articles) {
        System.out.println("\n=== SCRAPED ARTICLES ===");
        
        if (articles.isEmpty()) {
            System.out.println("No articles were scraped.");
            return;
        }
        
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            System.out.println("\n--- Article " + (i + 1) + " ---");
            System.out.println("Title: " + article.getTitle());
            System.out.println("Content: " + truncateText(article.getContent(), 100));
            System.out.println("Image URL: " + (article.getImageUrl() != null ? article.getImageUrl() : "No image"));
            System.out.println("Image Path: " + (article.getImagePath() != null ? article.getImagePath() : "Not downloaded"));
        }
        
        System.out.println("\n========================");
    }
    
    /**
     * Truncate text to specified length
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "N/A";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
} 