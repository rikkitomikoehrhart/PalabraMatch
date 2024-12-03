package com.example.palabramatch;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


public class WordList {
    public static List<Map<String, String>> getWordPairs(Context context) {
        try {
            // Open the JSON file in the assets folder
            InputStream inputStream = context.getAssets().open("words.json");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            // Convert JSON to a String
            String json = new String(buffer, "UTF-8");

            // Parse JSON using Gson
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
