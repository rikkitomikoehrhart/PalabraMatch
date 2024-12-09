package com.example.palabramatch;

public class Card {
    private int id;
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
    public Card(int id, String englishWord, String spanishWord, Boolean isFlipped, Boolean isMatched, int x, int y, int width, int height, String setTo) {
        this.id = id;
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
    public int getId() { return id; }

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
    public void setId(int id) { this.id = id; }

    public void setEnglishWord(String englishWord) { this.englishWord = englishWord; }

    public void setSpanishWord(String spanishWord) { this.spanishWord = spanishWord; }

    public void setIsFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public void setIsMatched(boolean matched) {
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

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", englishWord='" + englishWord + '\'' +
                ", spanishWord='" + spanishWord + '\'' +
                ", isFlipped=" + isFlipped +
                ", isMatched=" + isMatched +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", setTo='" + setTo + '\'' +
                ", currentFrame=" + currentFrame +
                '}';
    }
}
