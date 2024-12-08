package com.example.palabramatch;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Firework {
    private int x, y;
    private Bitmap[] frames;
    private int currentFrame = 0;
    private long lastFrameTime = System.currentTimeMillis();
    private static final int FRAME_DELAY = 00; // Milliseconds between frames
    private static final int FRAME_SKIP = 8;

    public Firework(int x, int y, Bitmap[] frames) {
        this.x = x;
        this.y = y;
        this.frames = frames;
    }

    public boolean update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > FRAME_DELAY) { // Update frame based on delay
            currentFrame += FRAME_SKIP ;
            lastFrameTime = currentTime;
        }
        return currentFrame >= frames.length; // Return true when animation finishes
    }

    public void draw(Canvas canvas) {
        if (currentFrame < frames.length) {
            canvas.drawBitmap(frames[currentFrame], x, y, null);
        }
    }
}