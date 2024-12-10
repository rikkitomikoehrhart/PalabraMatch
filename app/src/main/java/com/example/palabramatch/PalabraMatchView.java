package com.example.palabramatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import android.content.SharedPreferences;
import android.util.Log;



public class PalabraMatchView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private final GameActivity gameActivity;
    private Thread _thread;
    private int screenWidth;    // screen width
    private int screenHeight;   // screen height
    Context _context;
    private SurfaceHolder _surfaceHolder;
    private boolean _run = false;



    //*********** FOR DEBUGGING ************//
    private static final String TAG = "PalabraMatchView";


    //  All variables go in here

    // FRAME RATES
    private final static int MAX_FPS = 60; //desired fps
    private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period

    // COLORS
    private Paint background = new Paint();
    private Paint dark = new Paint();
    private Paint timerPaint;

    // DIMENSIONS
    private int frameWidth;
    private int frameHeight;

    // ANIMATIONS
    private Bitmap spriteSheet;
    private int currentFrame = 0;
    private long lastFrameTime = System.currentTimeMillis();
    private long lastFlipTime = 0;
    private boolean pendingCheck = false;
    private Bitmap[] fireworkFrames;
    private int fireworkFrameWidth, fireworkFrameHeight;
    private List<Firework> fireworks = new ArrayList<>();
    private List<Bitmap[]> fireworkFrameSets;


    // CARD ARRAYS
    private List<Card> allCards;
    private List<Card> cards;

    // GAME PLAY VARIABLES
    private boolean isChecking = false;
    private int flippedCardCount = 0;
    private int score;


    // SAVED STATE VARIABLES
    private SharedPreferences preferences;


    // TIMER VARIABLES
    private int timeLeftInSeconds;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private int EASY_TIME = 60;
    private int MEDIUM_TIME = 90;
    private int HARD_TIME = 120;


    // DIFFICULTY VARIABLES
    private int totalCards;
    private int columns;
    private int rows;
    private int fontSize;
    private int EASY_CARDS = 4;
    private int EASY_COLS = 2;
    private int EASY_ROWS = 4;
    private int EASY_FONT = 50;
    private int MEDIUM_CARDS = 6;
    private int MEDIUM_COLS = 3;
    private int MEDIUM_ROWS = 4;
    private int MEDIUM_FONT = 40;
    private int HARD_CARDS = 9;
    private int HARD_COLS = 3;
    private int HARD_ROWS = 6;
    private int HARD_FONT = 40;




    // GAME OPTIONS
    int sound = 1;
    int difficulty = 1;





    public PalabraMatchView(Context context, List<Card> allCards, SharedPreferences preferences, int sound, int difficulty) {
        super(context);
        _surfaceHolder = getHolder();
        getHolder().addCallback(this);
        this.gameActivity = (GameActivity) context;
        this.allCards = allCards;
        this.cards = new ArrayList<>();
        this.preferences = preferences;
        this.sound = sound;
        this.difficulty = difficulty;

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


        // Initialize Difficulty
        initializeDifficulty();


        // Initialize times
        timerPaint = new Paint();
        timerPaint.setColor(0xFFFFFFFF); // White color
        timerPaint.setTextSize(50);
        timerPaint.setTextAlign(Paint.Align.RIGHT);
        timerHandler = new Handler(Looper.getMainLooper());

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
        if (_thread != null) { // Check if _thread is null before calling join()
            boolean retry = true;
            while (retry) {
                try {
                    _thread.join(); // Call join() only if _thread is not null
                    retry = false;
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error joining thread: " + e.getMessage());
                }
            }
        }
        timerHandler.removeCallbacks(timerRunnable);
    }

    public void initialize(int w, int h) {
        screenWidth = w;
        screenHeight = h;
        score = 0;
        startTimer(timeLeftInSeconds);

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

        if (currentTime - lastFrameTime > 50) { // 100ms per frame

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
                if (currentTime - lastFlipTime > 500) { // 1/2-second delay
                    checkMatch(flippedCards); // Perform match check
                    pendingCheck = false; // Reset pending flag
                }
            }

            // If two cards are flipped, initiate delay before checking match
            if (flippedCards.size() == 2 && !pendingCheck) {
                lastFlipTime = currentTime;
                pendingCheck = true; // Indicate that we're waiting to check
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


            boolean needsRedraw = cardsNeedRedraw || fireworksNeedRedraw;
            if (needsRedraw) {
                invalidate();
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
                    textPaint.setTextSize(fontSize); // Adjust text size
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

        // Draw timer
        drawTimer(canvas);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        screenWidth = w;
        screenHeight = h;

        super.onSizeChanged(w, h, oldw, oldh);

        if (!loadSavedState()) {
            initialize(w, h);
        }

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (_thread == null) { // Create a new thread if one is not running
            _run = true;
            _thread = new Thread(this);
            _thread.start(); // Start thread for the game loop

            if (timeLeftInSeconds <= 0) {
                initializeDifficulty();
            }
            startTimer(timeLeftInSeconds);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        _run = false; // Stop loop in the thread
        if (_thread != null) {
            while (retry) {
                try {
                    _thread.join(); // Wait for thread to finish
                    retry = false; // Exit the loop
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error in surfaceDestroyed(): " + e.getMessage(), e);
                }
            }
            _thread = null;
        }

        timerHandler.removeCallbacks(timerRunnable);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isChecking) return true; // Do not allow tapping more cards while checking for matches

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            for (Card card : cards) {
                if (card.isTapped(touchX, touchY)) {
                    if (!card.getIsFlipped() && flippedCardCount < 2) {
                        card.setCurrentFrame(0);
                        card.setIsFlipped(true); // Flip the card
                        flippedCardCount++;
                        postInvalidate(); // Redraw the view
                    }
                    break; // Stop checking other cards
                }
            }
        }

        return true; // Consume touch event
    }



    private void setupGameCards() {
        // Clear the cards array
        cards.clear();

        // Create variables for the length of cards (100) per difficulty and
        // a temporary array to hold the cards
        int length = 100;
        List<Card> tempCards = new ArrayList<>();

        // Determine the index of cards based on difficulty
        if (difficulty == 3) {
            // Cards with ids 200-299 are B1 Vocabulary Words and are for HARD Mode
            for (int i = 200; i <= 299; i++) {
                tempCards.add(allCards.get(i));
            }
        } else if (difficulty == 2) {
            // Cards with ids 100-199 are A2 Vocabulary Words and are for MEDIUM Mode
            for (int i = 100; i <= 199; i++) {
                tempCards.add(allCards.get(i));
            }
        } else {
            // Cards with ids 0-99 are A1 Vocabulary Words and are for EASY Mode
            // This is also the default
            for (int i = 0; i <= 99; i++) {
                tempCards.add(allCards.get(i));
            }
        }


        // Shuffle the tempCards deck so that the list is not in order
        Collections.shuffle(tempCards);

        // For each english card, make a corresponding spanish card
        for (int i = 0; i < totalCards; i++) {
            Card englishCard = tempCards.get(i);


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

        // Shuffle cards again so that english and spanish cooresponding
        // cards aren't right next to each other
        Collections.shuffle(cards);

        // Calculate card dimensions
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

    private void drawTimer(Canvas canvas) {
        Paint timerPaint = new Paint();
        timerPaint.setColor(0xFFFFFFFF); // White color
        timerPaint.setTextSize(50); // Set font size for timer
        timerPaint.setTextAlign(Paint.Align.RIGHT); // Right-align the timer text

        String timeText = String.format("%02d:%02d", timeLeftInSeconds / 60, timeLeftInSeconds % 60);
        canvas.drawText(timeText, screenWidth - 40, 75, timerPaint); // Adjust position as needed
    }

    private void checkMatch(List<Card> flippedCards) {
        if (flippedCards.size() != 2) return;

        isChecking = true; // Do not allow tapping more cards while checking for matches

        Card card1 = flippedCards.get(0);
        Card card2 = flippedCards.get(1);

        // If the two cards match
        if (card1.getEnglishWord().equals(card2.getEnglishWord()) &&
                card1.getSpanishWord().equals(card2.getSpanishWord())) {

            // Successful match
            card1.setIsMatched(true);
            card2.setIsMatched(true);
            card1.setCurrentFrame(13);
            card2.setCurrentFrame(13);

            score++; // Score + 1


            // Save game
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                gameActivity.saveGameState(); // Save game after match
                spawnSingleFirework(); // Firework animation
                // Check if all cards are matched
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
                // Reset checking lock
                flippedCardCount = 0;
                isChecking = false; // Done checking, allow player to continue
            });

        } else {
            // Doesn't Match
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                shakeCards(card1, card2, () -> {
                    // Reset cards after the shake animation
                    card1.setIsFlipped(false);
                    card2.setIsFlipped(false);
                    card1.setX(card1.getX()); // Reset position
                    card2.setX(card2.getX()); // Reset position
                    postInvalidate(); // Update view
                    flippedCardCount = 0; // Reset flipped card count
                    isChecking = false; // No longer checking
                });
            });
        }
    }

    private void shakeCards(Card card1, Card card2, Runnable onComplete) {
        int originalX1 = card1.getX();
        int originalX2 = card2.getX();
        int shakeAmplitude = 10; // How far the cards move left and right

        Handler mainHandler = new Handler(Looper.getMainLooper());

        // Recursive animation method
        Runnable shakeAnimation = new Runnable() {
            int frame = 0;

            @Override
            public void run() {
                if (frame >= 6) {
                    // End of shake
                    card1.setX(originalX1);
                    card2.setX(originalX2);
                    postInvalidate();
                    if (onComplete != null) onComplete.run();
                    return;
                }

                // Shake logic (alternates -10, +10, -10, +10, etc.)
                int offset = (frame % 2 == 0) ? -shakeAmplitude : shakeAmplitude;
                card1.setX(originalX1 + offset);
                card2.setX(originalX2 + offset);
                postInvalidate();

                frame++;
                mainHandler.postDelayed(this, 20); // Delay 20ms for each frame
            }
        };

        // Start the shake animation
        shakeAnimation.run();
    }

    public void loadFireworkSprites() {
        int spritesPerSheet = 54; // Frames in each sheet
        int spriteColumns = 54;  // Number of columns in the sheet
        int spriteRows = 1;      // Number of rows in the sheet
        int totalSheets = 7;     // Number of spritesheets

        fireworkFrameSets = new ArrayList<>();

        fireworkFrameWidth = 600;
        fireworkFrameHeight = 600;

        for (int sheetIndex = 0; sheetIndex < totalSheets; sheetIndex++) {
            int resId = getResources().getIdentifier("firework_" + sheetIndex, "drawable", _context.getPackageName());
            Bitmap spritesheet = BitmapFactory.decodeResource(getResources(), resId);
            if (spritesheet == null) {
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

    }

    private void spawnSingleFirework() {
        int margin = 20; // Minimum distance from edges
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

        // Save game after fireworks
        gameActivity.saveGameState();
    }



    public List<Card> getCurrentCardState() {
        return cards;
    }

    public void addCard(Card card) {
        if (cards != null) {
            cards.add(card);
        } else {
            Log.e(TAG, "Card list is null, cannot add card");
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }




    public boolean loadSavedState() {
        SharedPreferences prefs = preferences;
        if (!prefs.contains("score")) {
            // No save data found, return false to indicate we should create a new game
            return false;
        }

        // Restore score
        score = prefs.getInt("score", 0);

        // Clear and reload cards
        cards.clear(); // Clear cards list before loading new cards
        for (int i = 0; i < allCards.size(); i++) {
            String cardKey = "card_" + i;

            if (prefs.contains(cardKey + "_english")) {
                Card card = new Card(
                        prefs.getInt(cardKey + "_id", 0),
                        prefs.getString(cardKey + "_english", ""),
                        prefs.getString(cardKey + "_spanish", ""),
                        prefs.getBoolean(cardKey + "_isFlipped", false),
                        prefs.getBoolean(cardKey + "_isMatched", false),
                        prefs.getInt(cardKey + "_x", 0),
                        prefs.getInt(cardKey + "_y", 0),
                        prefs.getInt(cardKey + "_width", 100),
                        prefs.getInt(cardKey + "_height", 150),
                        prefs.getString(cardKey + "_setTo", "english")
                );
                card.setCurrentFrame(prefs.getInt(cardKey + "_currentFrame", 0));
                cards.add(card);
            }
        }
        return true; // Successfully loaded saved data
    }



    private void startTimer(int totalTimeInSeconds) {
        timerHandler.removeCallbacks(timerRunnable);
        timeLeftInSeconds = totalTimeInSeconds; // Start the timer with total time
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (timeLeftInSeconds > 0) {
                timeLeftInSeconds--;
                postInvalidate(); // Redraw the screen to update the timer
                timerHandler.postDelayed(this, 1000); // Call this function every 1 second
            } else {
                // Timer has run out, show "Game Over" message
                displayGameOverMessage();
            }
        }
    };

    private void displayGameOverMessage() {
        Paint gameOverPaint = new Paint();
        gameOverPaint.setColor(0xFFFF0000); // Red color for "Game Over"
        gameOverPaint.setTextSize(100); // Large font
        gameOverPaint.setTextAlign(Paint.Align.CENTER);

        Canvas canvas = _surfaceHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), background); // Clear the screen
            canvas.drawText("Game Over", screenWidth / 2, screenHeight / 2, gameOverPaint);
            _surfaceHolder.unlockCanvasAndPost(canvas);
        }

        // Stop the game loop
        _run = false;
    }

    private void triggerGameOver() {
        // Stop game updates
        _run = false;

        // Clear screen except for score and timer
        cards.clear();
        fireworks.clear();

        // Draw "Game Over" message
        Canvas canvas = _surfaceHolder.lockCanvas();
        if (canvas != null) {
            drawImage(canvas); // Draw the current screen
            Paint gameOverPaint = new Paint();
            gameOverPaint.setColor(0xFFFF0000); // Red color
            gameOverPaint.setTextSize(100);
            gameOverPaint.setTextAlign(Paint.Align.CENTER);
            float centerX = screenWidth / 2f;
            float centerY = screenHeight / 2f;
            canvas.drawText("Game Over", centerX, centerY, gameOverPaint);
            _surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    public int getTimeLeftInSeconds() {
        return timeLeftInSeconds;
    }

    public void setTimeLeftInSeconds(int seconds) {
        timeLeftInSeconds = seconds;
        startTimer(timeLeftInSeconds); // Restart the timer when it is restored
    }

    public void initializeDifficulty() {
        if (difficulty == 1) {
            timeLeftInSeconds = EASY_TIME;
            totalCards = EASY_CARDS;
            columns = EASY_COLS;
            rows = EASY_ROWS;
            fontSize = EASY_FONT;
        } else if (difficulty == 2) {
            timeLeftInSeconds = MEDIUM_TIME;
            totalCards = MEDIUM_CARDS;
            columns = MEDIUM_COLS;
            rows = MEDIUM_ROWS;
            fontSize = MEDIUM_FONT;
        } else if (difficulty == 3) {
            timeLeftInSeconds = HARD_TIME;
            totalCards = HARD_CARDS;
            columns = HARD_COLS;
            rows = HARD_ROWS;
            fontSize = HARD_FONT;
        } else {
            timeLeftInSeconds = EASY_TIME;
            totalCards = EASY_CARDS;
            columns = EASY_COLS;
            rows = EASY_ROWS;
            fontSize = EASY_FONT;
        }
    }
}