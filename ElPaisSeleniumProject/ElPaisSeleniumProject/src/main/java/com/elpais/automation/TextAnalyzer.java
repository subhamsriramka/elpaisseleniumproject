package com.elpais.automation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TextAnalyzer handles analysis of translated text
 * Finds repeated words and their frequencies
 */
public class TextAnalyzer {
    
    // Common English words to exclude from analysis (stop words)
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", 
        "of", "with", "by", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "could",
        "should", "may", "might", "can", "this", "that", "these", "those",
        "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them"
    ));
    
    /**
     * Analyze translated headers and find repeated words
     */
    public static Map<String, Integer> analyzeRepeatedWords(List<String> translatedHeaders) {
        if (translatedHeaders == null || translatedHeaders.isEmpty()) {
            System.out.println("No translated headers to analyze");
            return new HashMap<>();
        }
        
        System.out.println("\n=== ANALYZING TRANSLATED HEADERS ===");
        
        // Count word frequencies
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        for (String header : translatedHeaders) {
            if (header != null && !header.trim().isEmpty()) {
                // Process each header
                processHeader(header, wordFrequency);
            }
        }
        
        // Filter words that appear more than twice
        Map<String, Integer> repeatedWords = wordFrequency.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 2)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
        
        // Sort by frequency (descending)
        Map<String, Integer> sortedRepeatedWords = repeatedWords.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
        
        return sortedRepeatedWords;
    }
    
    /**
     * Process a single header and update word frequency map
     */
    private static void processHeader(String header, Map<String, Integer> wordFrequency) {
        // Clean and normalize the header text
        String cleanHeader = cleanText(header);
        
        // Split into words
        String[] words = cleanHeader.split("\\s+");
        
        for (String word : words) {
            word = word.trim().toLowerCase();
            
            // Skip if word is too short, is a stop word, or contains numbers
            if (isValidWord(word)) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
    }
    
    /**
     * Clean text by removing punctuation and special characters
     */
    private static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        
        // Remove punctuation and special characters, keep only letters and spaces
        String cleaned = text.replaceAll("[^a-zA-ZáéíóúñÁÉÍÓÚÑ\\s]", " ");
        
        // Replace multiple spaces with single space
        cleaned = cleaned.replaceAll("\\s+", " ");
        
        return cleaned.trim();
    }
    
    /**
     * Check if a word is valid for analysis
     */
    private static boolean isValidWord(String word) {
        // Skip if word is too short
        if (word.length() < 3) {
            return false;
        }
        
        // Skip if word is a common stop word
        if (STOP_WORDS.contains(word.toLowerCase())) {
            return false;
        }
        
        // Skip if word contains numbers
        if (word.matches(".*\\d.*")) {
            return false;
        }
        
        // Skip if word is all uppercase (might be acronym)
        if (word.equals(word.toUpperCase()) && word.length() < 5) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Print analysis results in a formatted way
     */
    public static void printAnalysisResults(Map<String, Integer> repeatedWords) {
        System.out.println("\n=== REPEATED WORDS ANALYSIS RESULTS ===");
        
        if (repeatedWords.isEmpty()) {
            System.out.println("No words found that are repeated more than twice.");
            return;
        }
        
        System.out.println("Words repeated more than twice across all translated headers:");
        System.out.println("--------------------------------------------------------");
        
        int rank = 1;
        for (Map.Entry<String, Integer> entry : repeatedWords.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            System.out.printf("%d. %-20s : %d occurrences%n", rank++, word, count);
        }
        
        System.out.println("--------------------------------------------------------");
        System.out.println("Total unique repeated words: " + repeatedWords.size());
    }
    
    /**
     * Get summary statistics of the word analysis
     */
    public static void printSummaryStatistics(List<String> translatedHeaders, Map<String, Integer> repeatedWords) {
        System.out.println("\n=== SUMMARY STATISTICS ===");
        
        // Count total words
        int totalWords = 0;
        Set<String> uniqueWords = new HashSet<>();
        
        for (String header : translatedHeaders) {
            if (header != null && !header.trim().isEmpty()) {
                String cleanHeader = cleanText(header);
                String[] words = cleanHeader.split("\\s+");
                
                for (String word : words) {
                    word = word.trim().toLowerCase();
                    if (isValidWord(word)) {
                        totalWords++;
                        uniqueWords.add(word);
                    }
                }
            }
        }
        
        System.out.println("Total articles analyzed: " + translatedHeaders.size());
        System.out.println("Total valid words: " + totalWords);
        System.out.println("Total unique words: " + uniqueWords.size());
        System.out.println("Words repeated more than twice: " + repeatedWords.size());
        
        if (totalWords > 0) {
            double repetitionRate = (double) repeatedWords.size() / uniqueWords.size() * 100;
            System.out.printf("Repetition rate: %.2f%%%n", repetitionRate);
        }
        
        System.out.println("=============================");
    }
} 