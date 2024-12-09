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

      // Create gameView before calling loadGameState()
      SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
      gameView = new PalabraMatchView(this, cards, preferences);
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
   }

   @Override
   protected void onResume() {
      super.onResume();
      loadGameState();
   }

   public void saveGameState() {
      SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();

      // Save the score
      editor.putInt("score", gameView.getScore());

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

   private void loadGameState() {
      if (gameView != null) {
         SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

         // Restore score
         int score = prefs.getInt("score", 0);
         gameView.setScore(score);

         // Restore cards
         gameView.loadSavedState();

         // Load Fireworks
         gameView.loadFireworkSprites();
      }
   }


   @Override
   protected void onDestroy() {
      super.onDestroy();
      if (gameView != null) {
         gameView.pause();
      }
   }
}