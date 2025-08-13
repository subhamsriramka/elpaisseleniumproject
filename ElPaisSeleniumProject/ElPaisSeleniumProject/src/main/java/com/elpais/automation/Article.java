package com.elpais.automation;

/**
 * Article class represents a news article from El Pais
 */
public class Article {
    
    // Private fields to store article information
    private String title;           // Article title in Spanish
    private String content;         // Article content in Spanish
    private String translatedTitle; // Article title translated to English
    private String imageUrl;        // URL of the article's cover image
    private String imagePath;       // Local path where image is saved
    
    // Default constructor (creates empty article)
    public Article() {
    }
    
    // Constructor with all fields
    public Article(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }
    
    // Getter methods (to retrieve values)
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getTranslatedTitle() {
        return translatedTitle;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    // Setter methods (to set values)
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    // toString method for easy printing
    @Override
    public String toString() {
        return "Article{" +
                "title='" + title + '\'' +
                ", translatedTitle='" + translatedTitle + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
} 