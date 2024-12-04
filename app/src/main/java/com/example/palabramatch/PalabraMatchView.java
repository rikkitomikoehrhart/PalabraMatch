

package com.example.palabramatch;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;



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

    // Card Arrays
    private List<Card> allCards;
    private List<Card> cards;

    // Game Pay Variables
    private static final int NUM_PAIRS = 4;

    





    public PalabraMatchView(Context context, List<Card> allCards) {
        super(context);
        _surfaceHolder = getHolder();
        getHolder().addCallback(this);
        this.gameActivity = (GameActivity) context;
        this.allCards = allCards;
        this.cards = new ArrayList<>();

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

        // Set up game after the screen width and height are initialed
        setupGameCards();
    }


    protected void update() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFrameTime > 100) { // 100ms per frame
            boolean needsRedraw = false;

            // Loop through the cards and animate the flip
            for (Card card : cards) {
                if (card.getIsFlipped() && card.getCurrentFrame() < 12) { // Frame 0 is the back of the card, frame 12 is the word
                    card.setCurrentFrame(card.getCurrentFrame() + 1);
                    needsRedraw = true;
                }
            }

            if (needsRedraw) {
                invalidate(); // Redraw the view
            }
            lastFrameTime = currentTime;
        }
    }



    protected void drawImage(Canvas canvas) {
        if (canvas == null) return;

        // Fill the background
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        // Look through the cards
        for (Card card : cards) {
            android.graphics.Rect dest = new android.graphics.Rect(
                    card.getX(),
                    card.getY(),
                    card.getX() + card.getWidth(),
                    card.getY() + card.getHeight()
            );

            // Check if card is flipped
            if (card.getIsFlipped()) {
                // Animate the card based on its current frame (not past frame 12, which is the word side)
                int frameToUse = Math.min(card.getCurrentFrame(), 12);
                int x = (card.getCurrentFrame() % 4) * frameWidth;
                int y = (card.getCurrentFrame() / 4) * frameHeight;
                android.graphics.Rect source = new android.graphics.Rect(x, y, x + frameWidth, y + frameHeight);

                canvas.drawBitmap(spriteSheet, source, dest, null);

                // Writes the word on the card if the card is flipped
                if (frameToUse == 12) {
                    Paint textPaint = new Paint();
                    if (card.getSetTo().equals("english")) {
                        textPaint.setColor(0xFF00008B);
                    } else if (card.getSetTo().equals("spanish")) {
                        textPaint.setColor(0xFFCC5500);
                    }
                    textPaint.setTextSize(card.getHeight() / 8); // Adjust text size relative to card height
                    textPaint.setTextAlign(Paint.Align.CENTER);

                    // Calculate the text position (center of the card)
                    float textX = card.getX() + card.getWidth() / 2.0f;
                    float textY = card.getY() + card.getHeight() / 2.0f - ((textPaint.descent() + textPaint.ascent()) / 2);

                    String wordToShow = card.getSetTo().equals("english") ? card.getEnglishWord() : card.getSpanishWord();
                    canvas.drawText(wordToShow, textX, textY, textPaint);
                }

            } else {
                // Draw the back of the card
                int x = 0;
                int y = 0;
                android.graphics.Rect source = new android.graphics.Rect(x, y, x + frameWidth, y + frameHeight);

                canvas.drawBitmap(spriteSheet, source, dest, null);
            }
        }
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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();


            for (Card card : cards) {
                if (card.isTapped(touchX, touchY)) {
                    if (!card.getIsFlipped()) {
                        card.setCurrentFrame(0);
                    }
                    card.setFlipped(!card.getIsFlipped()); // Toggle flip state
                    invalidate(); // Redraw the view
                    break;
                }
            }
        }

        return true;
    }



    private void setupGameCards() {
        cards.clear();

        Collections.shuffle(allCards);

        // For each english card, make a corresponding spanish card
        for (int i = 0; i < NUM_PAIRS; i++) {
            Card englishCard = allCards.get(i);

            Card spanishCard = new Card(
                    englishCard.getEnglishWord(),
                    englishCard.getSpanishWord(),
                    false,
                    false,
                    0,
                    0,
                    0,
                    0,
                    "spanish"
            );

            cards.add(englishCard);
            cards.add(spanishCard);
        }

        Collections.shuffle(cards);

        // Calculate card dimensions
        int columns = 2; // Number of columns
        int rows = 4;    // Number of rows
        int padding = 20;

        int cardWidth = (screenWidth - (columns + 1) * padding) / columns;
        int cardHeight = (screenHeight - (rows + 1) * padding) / rows;

        System.out.println("Screen Width: " + screenWidth);
        System.out.println("Screen Height: " + screenHeight);
        System.out.println("Card Width: " + cardWidth);
        System.out.println("Card Height: " + cardHeight);

        int startX = padding; // Initial horizontal padding
        int startY = padding; // Initial vertical padding

        for (int i = 0; i < cards.size(); i++) {
            int column = i % columns; // Column index
            int row = i / columns;    // Row index

            // Calculate positions based on column, row, and padding
            int x = startX + column * (cardWidth + padding);
            int y = startY + row * (cardHeight + padding);

            // Assign positions and dimensions to each card
            cards.get(i).setX(x);
            cards.get(i).setY(y);
            cards.get(i).setWidth(cardWidth);
            cards.get(i).setHeight(cardHeight);


        }

    }


}