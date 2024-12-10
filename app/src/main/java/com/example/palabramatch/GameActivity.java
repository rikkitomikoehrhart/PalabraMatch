package com.example.palabramatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameActivity extends Activity {

   private static final String TAG = "GameActivity";
   private static final String PREFS_NAME = "PalabraMatchPrefs";

   private PalabraMatchView gameView;

   private int isSoundEnabled;
   private int difficultyLevel;

   private List<Card> cards; // Corrected name (was allCards before)

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Log.d(TAG, "onCreate");

      // Check if we are starting a fresh game
      Intent intent = getIntent();
      boolean startNewGame = intent.getBooleanExtra("startNewGame", false);


      // Create the list of cards
      cards = new ArrayList<>();
      List<Map<String, String>> wordPairs = WordList.getWordPairs(this);
      if (wordPairs != null) {
         for (Map<String, String> pair : wordPairs) {
            String id = pair.get("id");
            String english = pair.get("english");
            String spanish = pair.get("spanish");

            Card englishCard = new Card(
                    Integer.parseInt(id),
                    english,
                    spanish,
                    false,
                    false,
                    0,
                    0,
                    100,
                    150,
                    "english"
            );
            cards.add(englishCard);
         }
      }

      Intent intentOptions = getIntent();
      isSoundEnabled = intentOptions.getIntExtra("Sound", 1);
      difficultyLevel = intentOptions.getIntExtra("Difficulty", 1);



      // Create gameView before calling loadGameState()
      SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
      gameView = new PalabraMatchView(this, cards, preferences, isSoundEnabled, difficultyLevel);
      setContentView(gameView);
      gameView.requestFocus();



      // Call loadGameState() only after gameView is initialized
      if (!startNewGame) {
         Log.d(TAG, "Attempting to load saved game state");
         loadGameState();
      } else {
         Log.d(TAG, "Starting a new game (not loading saved state)");
      }
   }

   @Override
   protected void onPause() {
      super.onPause();
      gameView.pause();
      saveGameState();
   }

   @Override
   protected void onResume() {
      super.onResume();
      loadGameState();
   }

   public void saveGameState() {
      SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();

      // Save the current score
      editor.putInt("score", gameView.getScore());

      // Save time left on timer
      editor.putInt("timeLeftInSeconds", gameView.getTimeLeftInSeconds());

      // Save card data
      for (int i = 0; i < gameView.getCurrentCardState().size(); i++) {
         Card card = gameView.getCurrentCardState().get(i);
         String cardKey = "card_" + i;

         editor.putBoolean(cardKey + "_isFlipped", card.getIsFlipped());
         editor.putBoolean(cardKey + "_isMatched", card.getIsMatched());
         editor.putInt(cardKey + "_x", card.getX());
         editor.putInt(cardKey + "_y", card.getY());
         editor.putInt(cardKey + "_width", card.getWidth());
         editor.putInt(cardKey + "_height", card.getHeight());
         editor.putInt(cardKey + "_currentFrame", card.getCurrentFrame());
         editor.putString(cardKey + "_english", card.getEnglishWord());
         editor.putString(cardKey + "_spanish", card.getSpanishWord());
         editor.putString(cardKey + "_setTo", card.getSetTo());
      }

      editor.apply();
   }

   private boolean loadGameState() {
      SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
      if (!prefs.contains("score")) return false;

      int score = prefs.getInt("score", 0);
      int savedTime = prefs.getInt("timeLeftInSeconds", 120); // Default to 2 min if not found

      gameView.setScore(score);
      gameView.setTimeLeftInSeconds(savedTime); // Restore the time from saved data

      // Reload card data
      for (int i = 0; i < gameView.getCurrentCardState().size(); i++) {
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
            gameView.addCard(card);
         }
      }

      return true;
   }


   @Override
   protected void onDestroy() {
      super.onDestroy();
      if (gameView != null) {
         gameView.pause();
         saveGameState();
      }
   }
}