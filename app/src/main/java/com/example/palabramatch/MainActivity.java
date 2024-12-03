package com.example.palabramatch;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener {
   private static final String TAG = "APong";
   
   /** Called when the activity is first created. */
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
   }

   // ...
   public void onClick(View v) {
      int id = v.getId();
      if (id == R.id.about_button) {
         Intent i = new Intent(this, About.class);
         startActivity(i);
         // More buttons go here (if any) ...
      } else if (id == R.id.new_button) {
         Intent g = new Intent(this, GameActivity.class);
         startActivity(g);
      } else if (id == R.id.exit_button) {
         finish();
      }
   }
   
   public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    Log.d(TAG, "onConfigurationChanged " + newConfig.orientation);
	}

 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
