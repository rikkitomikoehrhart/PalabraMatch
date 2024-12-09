

package com.example.palabramatch;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.sql.Array;
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

    private final static int MAX_FPS = 60; //desired fps
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

    // Flip Delay
    private long lastFlipTime = 0;
    private boolean pendingCheck = false;

    // Game Pay Variables
    private static final int NUM_PAIRS = 4;
    private boolean isChecking = false;
    private int flippedCardCount = 0;

    // Score
    private int score;


    // Fireworks
    private Bitmap[] fireworkFrames;
    private int fireworkFrameWidth, fireworkFrameHeight;
    private List<Firework> fireworks = new ArrayList<>();
    private List<Bitmap[]> fireworkFrameSets;




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
        score = 0;

        // create paints, rectangles, init time, etc

        background.setColor(0xff200040);  // should really get this from resource file
        dark.setColor(0xffdddddd);

        // Load Fireworks Sprites
        loadFireworkSprites();

        // Set up game after the screen width and height are initialed
        setupGameCards();
    }


    protected void update() {
        long currentTime = System.currentTimeMillis();

        boolean cardsNeedRedraw = false;
        boolean fireworksNeedRedraw = false;

        if (currentTime - lastFrameTime > 100) { // 100ms per frame

            // Handle card animations
            List<Card> flippedCards = new ArrayList<>();
            for (Card card : cards) {
                if (card.getIsFlipped() && card.getCurrentFrame() < 12) {
                    card.setCurrentFrame(card.getCurrentFrame() + 1); // Animate forward
                    cardsNeedRedraw = true;
                }
                if (card.getIsFlipped() && card.getCurrentFrame() == 12) {
                    flippedCards.add(card); // Collect flipped cards
                }
                if (card.getCurrentFrame() < 12 && !card.getIsFlipped()) {
                    card.setCurrentFrame(Math.max(0, card.getCurrentFrame() - 1)); // Reverse animation
                    cardsNeedRedraw = true;
                }
            }

            // Handle pending match check
            if (pendingCheck) {
                if (currentTime - lastFlipTime > 1000) { // 1-second delay
                    checkMatch(flippedCards); // Perform match check
                    pendingCheck = false; // Reset pending flag
                }
            }

            // If two cards are flipped, initiate delay before checking match
            if (flippedCards.size() == 2 && !pendingCheck) {
                lastFlipTime = currentTime;
                pendingCheck = true; // Mark that we're waiting to check
            }

            // Handle fireworks animations
            List<Firework> finishedFireworks = new ArrayList<>();
            for (Firework firework : fireworks) {
                if (!firework.update()) { // Firework is still animating
                    fireworksNeedRedraw = true;
                } else {
                    finishedFireworks.add(firework);
                }
            }
            fireworks.removeAll(finishedFireworks);

            // Combine redraw flags
            boolean needsRedraw = cardsNeedRedraw || fireworksNeedRedraw;

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

        // Draw cards
        for (Card card : cards) {
            android.graphics.Rect dest = new android.graphics.Rect(
                    card.getX(),
                    card.getY(),
                    card.getX() + card.getWidth(),
                    card.getY() + card.getHeight()
            );

            if (card.getIsFlipped()) {
                // Animate card flip
                int frameToUse = Math.min(card.getCurrentFrame(), 12);
                int x = (card.getCurrentFrame() % 4) * frameWidth;
                int y = (card.getCurrentFrame() / 4) * frameHeight;
                android.graphics.Rect source = new android.graphics.Rect(x, y, x + frameWidth, y + frameHeight);

                canvas.drawBitmap(spriteSheet, source, dest, null);

                // Draw card text if flipped
                if (frameToUse == 12) {
                    Paint textPaint = new Paint();
                    if (card.getIsMatched()) {
                        textPaint.setColor(0xFFFFFFFF);
                    } else if (card.getSetTo().equals("english")) {
                        textPaint.setColor(0xFF4F8795);
                    } else if (card.getSetTo().equals("spanish")) {
                        textPaint.setColor(0xFFCC5500);
                    }
                    textPaint.setTextSize(card.getHeight() / 8); // Adjust text size
                    textPaint.setTextAlign(Paint.Align.CENTER);

                    float textX = card.getX() + card.getWidth() / 2.0f;
                    float textY = card.getY() + card.getHeight() / 2.0f - ((textPaint.descent() + textPaint.ascent()) / 2);

                    String wordToShow = card.getSetTo().equals("english") ? card.getEnglishWord() : card.getSpanishWord();
                    canvas.drawText(wordToShow, textX, textY, textPaint);
                }
            } else {
                // Draw card back
                int x = 0;
                int y = 0;
                android.graphics.Rect source = new android.graphics.Rect(x, y, x + frameWidth, y + frameHeight);
                canvas.drawBitmap(spriteSheet, source, dest, null);
            }
        }

        // Draw fireworks
        for (Firework firework : fireworks) {
            firework.draw(canvas); // Pass fireworkFrames
        }

        // Draw score
        drawScore(canvas);
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
        if (isChecking) { return true; }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();


            for (Card card : cards) {
                if (card.isTapped(touchX, touchY)) {
                    if (!card.getIsFlipped() && flippedCardCount < 2) { // Only allow flipping if less than 2 cards are flipped
                        card.setCurrentFrame(0);
                        card.setFlipped(!card.getIsFlipped()); // Toggle flip state
                        flippedCardCount++;
                        invalidate(); // Redraw the view
                    }
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
                    englishCard.getId(),
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
        int cardHeight = (screenHeight - 100 - (rows + 1) * padding) / rows;


        int startX = padding; // Initial horizontal padding
        int startY = 100 + padding; // Initial vertical padding

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


    private void drawScore(Canvas canvas) {
        Paint scorePaint = new Paint();
        scorePaint.setColor(0xFFFFFFFF);
        scorePaint.setTextSize(50);
        canvas.drawText("Score: " + score, 40, 75, scorePaint);

    }


    private void checkMatch(List<Card> flippedCards) {
        if (flippedCards.size() != 2) return;

        isChecking = true;

        Card card1 = flippedCards.get(0);
        Card card2 = flippedCards.get(1);

        if (card1.getEnglishWord().equals(card2.getEnglishWord()) &&
                card1.getSpanishWord().equals(card2.getSpanishWord())) {
            // Match found
            card1.setCurrentFrame(13);
            card2.setCurrentFrame(13);
            card1.setMatched(true);
            card2.setMatched(true);

            // Increment score
            score++;

            // Spawn a firework for this match
            spawnSingleFirework();

            // Check if all matches are complete
            boolean allMatched = true;
            for (Card card : cards) {
                if (!card.getIsMatched()) {
                    allMatched = false;
                    break;
                }
            }
            if (allMatched) {
                spawnMultipleFireworks();
            }
        } else {
            // No match, trigger shake
            new Thread(() -> {
                int originalX1 = card1.getX();
                int originalX2 = card2.getX();
                int shakeAmplitude = 10; // How far the cards move left and right

                for (int i = 0; i < 6; i++) { // Shake for 6 frames
                    try {
                        Thread.sleep(50); // Delay between shake movements
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Move cards left and right alternately
                    int offset = (i % 2 == 0) ? -shakeAmplitude : shakeAmplitude;
                    card1.setX(originalX1 + offset);
                    card2.setX(originalX2 + offset);

                    // Redraw the view
                    postInvalidate();
                }

                // Reset cards to original position and flip back over
                card1.setX(originalX1);
                card2.setX(originalX2);
                card1.setFlipped(false);
                card2.setFlipped(false);

                // Redraw one last time
                postInvalidate();

                flippedCardCount = 0;
                isChecking = false;
            }).start();
            return;
        }

        flippedCardCount = 0;
        isChecking = false;
    }

    private void loadFireworkSprites() {
        int spritesPerSheet = 54; // Frames in each sheet
        int spriteColumns = 54;  // Number of columns in the sheet
        int spriteRows = 1;      // Number of rows in the sheet
        int totalSheets = 7;     // Number of spritesheets

        fireworkFrameSets = new ArrayList<>();

        fireworkFrameWidth = 800;
        fireworkFrameHeight = 800;

        for (int sheetIndex = 0; sheetIndex < totalSheets; sheetIndex++) {
            int resId = getResources().getIdentifier("firework_" + sheetIndex, "drawable", _context.getPackageName());
            Bitmap spritesheet = BitmapFactory.decodeResource(getResources(), resId);
            if (spritesheet == null) {
                System.out.println("Error: Spritesheet firework_" + sheetIndex + " not found!");
                continue;
            }

            List<Bitmap> extractedFrames = new ArrayList<>();

            int sheetWidth = spritesheet.getWidth();
            int sheetHeight = spritesheet.getHeight();

            int frameWidth = sheetWidth / spriteColumns;
            int frameHeight = sheetHeight / spriteRows;

            for (int row = 0; row < spriteRows; row++) {
                for (int col = 0; col < spriteColumns; col++) {
                    if (extractedFrames.size() >= spritesPerSheet) break;

                    int x = col * frameWidth;
                    int y = row * frameHeight;

                    Bitmap frame = Bitmap.createBitmap(spritesheet, x, y, frameWidth, frameHeight);
                    extractedFrames.add(Bitmap.createScaledBitmap(frame, fireworkFrameWidth, fireworkFrameHeight, false));
                }
            }

            fireworkFrameSets.add(extractedFrames.toArray(new Bitmap[extractedFrames.size()]));
        }

        System.out.println("Total firework frame sets loaded: " + fireworkFrameSets.size());
    }

    private void spawnSingleFirework() {
        int margin = 100; // Minimum distance from edges
        int x = margin + (int) (Math.random() * (screenWidth - fireworkFrameWidth - 2 * margin));
        int y = margin + (int) (Math.random() * (screenHeight - fireworkFrameHeight - 2 * margin));

        // Randomly select one of the firework frame sets
        Bitmap[] selectedFrames = fireworkFrameSets.get((int) (Math.random() * fireworkFrameSets.size()));

        fireworks.add(new Firework(x, y, selectedFrames));
    }

    private void spawnMultipleFireworks() {
        for (int i = 0; i < 6; i++) {
            spawnSingleFirework();
        }
    }


    
}