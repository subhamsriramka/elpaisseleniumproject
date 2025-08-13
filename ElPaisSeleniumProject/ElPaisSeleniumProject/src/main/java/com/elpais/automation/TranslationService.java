package com.elpais.automation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * TranslationService handles translation of text from Spanish to English
 * Uses MyMemory Translation API (free service)
 */
public class TranslationService {
    
    private final String apiUrl;
    
    public TranslationService() {
        this.apiUrl = ConfigManager.getTranslationApiUrl();
    }
    
    /**
     * Translate text from Spanish to English
     * @param spanishText the text to translate
     * @return translated English text
     */
    public String translateToEnglish(String spanishText) {
        if (spanishText == null || spanishText.trim().isEmpty()) {
            return "";
        }
        
        try {
            // Clean the text - remove extra spaces
            String cleanText = spanishText.trim().replaceAll("\\s+", " ");
            
            // Build the API request URL with proper encoding
            String requestUrl = buildRequestUrl(cleanText);
            
            // Make HTTP request to translation API
            String response = makeHttpRequest(requestUrl);
            
            // Parse the JSON response
            String translatedText = parseTranslationResponse(response);
            
            System.out.println("Translated: '" + cleanText + "' -> '" + translatedText + "'");
            return translatedText;
            
        } catch (Exception e) {
            System.out.println("Translation error: " + e.getMessage());
            return spanishText; // Return original text if translation fails
        }
    }
    
    private String buildRequestUrl(String cleanText) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(this.apiUrl);
        builder.addParameter("q", cleanText);
        builder.addParameter("langpair", "es|en");
        return builder.build().toString();
    }
    
    /**
     * Make HTTP GET request to the translation API
     * @param url the API URL
     * @return response body as string
     */
    private String makeHttpRequest(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            
            // Set user agent to avoid blocking
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity, StandardCharsets.UTF_8);
                }
            }
        }
        return "";
    }
    
    /**
     * Parse the JSON response from translation API
     * @param jsonResponse the JSON response string
     * @return translated text
     */
    private String parseTranslationResponse(String jsonResponse) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            // MyMemory API response structure: {"responseData":{"translatedText":"..."}}
            if (jsonObject.has("responseData")) {
                JsonObject responseData = jsonObject.getAsJsonObject("responseData");
                if (responseData.has("translatedText")) {
                    return responseData.get("translatedText").getAsString();
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error parsing translation response: " + e.getMessage());
        }
        
        return "Translation not available";
    }
} 