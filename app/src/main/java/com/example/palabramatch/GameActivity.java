
package com.example.palabramatch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameActivity extends Activity {
	
   private static final String TAG = "Game Activity";

   private PalabraMatchView gameView;

   private List<Card> cards;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Log.d(TAG, "onCreate");

      // Creating a list of cards
      List<Map<String, String>> wordPairs = WordList.getWordPairs(this);
      cards = new ArrayList<>();
      if (wordPairs != null) {
         int positionX = 0;
         int positionY = 0;
         int cardWidth = 100;
         int cardHeight = 150;

         for (Map<String, String> pair : wordPairs) {
            String id = pair.get("id");
            String english = pair.get("english");
            String spanish = pair.get("spanish");

            Card englishCard = new Card(Integer.parseInt(id), english, spanish, false, false, positionX, positionY, cardWidth, cardHeight, "english");

            cards.add(englishCard);

         }
      }


      gameView = new PalabraMatchView(this, cards);
      setContentView(gameView);
      gameView.requestFocus();
   }



}
