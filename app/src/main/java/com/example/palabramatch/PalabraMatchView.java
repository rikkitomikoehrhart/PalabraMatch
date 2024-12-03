

package com.example.palabramatch;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;

public class PalabraMatchView extends SurfaceView implements SurfaceHolder.Callback, Runnable {


    private final GameActivity gameActivity;

    private Thread _thread;

    private int screenWidth;    // screen width
    private int screenHeight;   // screen height

    Context _context;

    private SurfaceHolder _surfaceHolder;

    private boolean _run = false;






    //  All variables go in here

    private final static int MAX_FPS = 30; //desired fps
    private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period

    private Paint background = new Paint();
    private Paint dark = new Paint();

    private int frameWidth;
    private int frameHeight;
    private Bitmap spriteSheet;

    private int currentFrame = 0;
    private long lastFrameTime = System.currentTimeMillis();

    private List<Card> cards;




    public PalabraMatchView(Context context, List<Card> cards) {
        super(context);
        _surfaceHolder = getHolder();
        getHolder().addCallback(this);
        this.gameActivity = (GameActivity) context;
        this.cards = cards;

        _context = context;

        setFocusable(true);
        setFocusableInTouchMode(true);

        // Bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.card, options);

        // Calculate frame dimensions
        int xSprites = 4;
        int ySprites = 4;
        int xSheetSize = spriteSheet.getWidth();
        int ySheetSize = spriteSheet.getHeight();

        frameWidth = xSheetSize / xSprites;
        frameHeight = ySheetSize / ySprites;
    }



    @Override
    public void run() {
         float avg_sleep = 0.0f;
         float fcount = 0.0f;
         long fps = System.currentTimeMillis();

        Canvas c;
        while (_run) {
            c = null;
            long started = System.currentTimeMillis();
            try {
                c = _surfaceHolder.lockCanvas(null);


                synchronized (_surfaceHolder) {
                    // Update game state
                    update();
                }
                // draw image
                drawImage(c);

            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }

            }

            float deltaTime = (System.currentTimeMillis() - started);
            int sleepTime = (int) (FRAME_PERIOD - deltaTime);



            if (sleepTime > 0) {
                try {
                   _thread.sleep(sleepTime);
                }
                catch (InterruptedException e) {
                }
            }

        }
    }

    public void pause() {
        _run = false;
        boolean retry = true;
        while (retry) {
            try {
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
    }

    public void initialize(int w, int h) {
        screenWidth = w;
        screenHeight = h;

        // create paints, rectangles, init time, etc

        background.setColor(0xff200040);  // should really get this from resource file
        dark.setColor(0xffdddddd);


    }


    protected void update() {
        // game update goes here
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > 100) {
            currentFrame = (currentFrame +1) % 16;
            lastFrameTime = currentTime;
        }

    }


    protected void drawImage(Canvas canvas) {

        if (canvas == null) return;


        // Fill the Background
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        // Source rectangle: defines the part of the sprite sheet to draw
        int x = (currentFrame % 4) * frameWidth; // Assuming 4 frames across
        int y = (currentFrame / 4) * frameHeight; // Assuming 4 frames down
        android.graphics.Rect source = new android.graphics.Rect(x, y, x + frameWidth, y + frameHeight);

        // Destination rectangle: where on the screen the frame will appear
        int destLeft = screenWidth / 2 - (frameWidth * 4) / 2;
        int destTop = screenHeight / 2 - (frameHeight * 4) / 2;
        int destRight = destLeft + (frameWidth * 4);
        int destBottom = destTop + (frameHeight * 4);
        android.graphics.Rect dest = new android.graphics.Rect(destLeft, destTop, destRight, destBottom);

        // Draw the frame
        canvas.drawBitmap(spriteSheet, source, dest, null);


    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        screenWidth = w;
        screenHeight = h;

        super.onSizeChanged(w, h, oldw, oldh);

        initialize(w, h);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        _run = true;
        _thread = new Thread(this);
        _thread.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // simply copied from sample application LunarLander:
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        _run = false;
        while (retry) {
            try {
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // ON TOUCH - flip unless two are already flipped

        return true;
    }

}