package com.example.palabramatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener {
   private static final String TAG = "MainActivity";
   private static final String PREFS_NAME = "PalabraMatchPrefs";


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      // Set up click listeners for all the buttons
      View continueButton = findViewById(R.id.continue_button);
      continueButton.setOnClickListener(this);
      View newButton = findViewById(R.id.new_button);
      newButton.setOnClickListener(this);
      View aboutButton = findViewById(R.id.about_button);
      aboutButton.setOnClickListener(this);
      View exitButton = findViewById(R.id.exit_button);
      exitButton.setOnClickListener(this);

      // Check if saved state exists (look for 'score' instead of 'card_0_id')
      SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
      boolean hasSavedState = prefs.contains("score"); // Better than card_0_id
      continueButton.setEnabled(hasSavedState);
   }



   public void onClick(View v) {
      int id = v.getId();
      if (id == R.id.about_button) {
         Intent i = new Intent(this, About.class);
         startActivity(i);
      }
      else if (id == R.id.new_button) {
         clearSavedGameState(); // Clear any previous game data
         Intent g = new Intent(this, GameActivity.class);
         g.putExtra("startNewGame", true); // Indicate to GameActivity that we want a fresh start
         startActivity(g);
      }
      else if (id == R.id.continue_button) {
         Intent g = new Intent(this, GameActivity.class);
         g.putExtra("startNewGame", false); // Tell GameActivity to load the saved state
         startActivity(g);
      }
      else if (id == R.id.exit_button) {
         finish();
      }
   }


   public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
   }



   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.activity_main, menu);
      return true;
   }


   private void clearSavedGameState() {
      SharedPreferences prefs = getSharedPreferences("PalabraMatchPrefs", MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.clear(); // Clear all saved data
      editor.apply(); // Apply changes
   }
}
