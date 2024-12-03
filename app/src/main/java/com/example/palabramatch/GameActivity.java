
package com.example.palabramatch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class GameActivity extends Activity {
	
   private static final String TAG = "Game Activity";

   private PalabraMatchView gameView;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Log.d(TAG, "onCreate");


      gameView = new PalabraMatchView(this);
      setContentView(gameView);
      gameView.requestFocus();
   }




}
