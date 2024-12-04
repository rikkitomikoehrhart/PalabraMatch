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
    private int currentFrame;
    private String setTo;

    // Constructor
    public Card(String englishWord, String spanishWord, boolean isFlipped, boolean isMatched, int x, int y, int width, int height, String setTo) {
        this.englishWord = englishWord;
        this.spanishWord = spanishWord;
        this.isFlipped = isFlipped;
        this.isMatched = isMatched;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.setTo = setTo;

        this.currentFrame = 0;
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

    public String getSetTo() {
        return setTo;
    }

    public int getCurrentFrame() { return currentFrame; }

    // Setters
    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }

    public void setX(int num) { x = num; }

    public void setY(int num) { y = num; }

    public void setWidth(int num) { width = num; }

    public void setHeight(int num) {height = num; }

    public void setSetTo(String language) {
        setTo = language;
    }

    public void setCurrentFrame(int num) { currentFrame = num; }

    // METHODS
    // Check if a touch event happens to this card
    public boolean isTapped(float touchX, float touchY) {
        return touchX >= x && touchX <= x + width && touchY >= y && touchY <= y + height;
    }

}
