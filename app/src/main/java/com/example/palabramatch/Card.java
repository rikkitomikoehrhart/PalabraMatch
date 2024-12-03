package com.example.palabramatch;

public class Card {
    private String englishWord;
    private String spanishWord;
    private boolean isFlipped;
    private boolean isMatched;
    private int x;
    private int y;
    private int width;
    private int height;

    // Constructor
    public Card(String englishWord, String spanishWord, boolean isFlipped, boolean isMatched, int x, int y, int width, int height) {
        this.englishWord = englishWord;
        this.spanishWord = spanishWord;
        this.isFlipped = isFlipped;
        this.isMatched = isMatched;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Getters
    public String getEnglishWord() {
        return englishWord;
    }

    public String getSpanishWord() {
        return spanishWord;
    }

    public boolean getIsFlipped() {
        return isFlipped;
    }

    public boolean getIsMatched() {
        return isMatched;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Setters
    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }

    // METHODS
    // Check if a touch event happens to this card
    public boolean isTapped(float touchX, float touchY) {
        return touchX >= x && touchX <= x + width && touchY >= y && touchY <= y + height;
    }

}
