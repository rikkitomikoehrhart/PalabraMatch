package com.example.palabramatch;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class WordList {
    public static List<Map<String, String>> getWordPairs(Context context) {
        List<Map<String, String>> wordPairs = new ArrayList<>();
        try {
            // words.json holds the Spanish-English Vocabulary Words
            InputStream is = context.getAssets().open("words.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Map<String, String> wordPair = new HashMap<>();
                wordPair.put("id", obj.getString("id"));
                wordPair.put("english", obj.getString("english"));
                wordPair.put("spanish", obj.getString("spanish"));
                wordPairs.add(wordPair);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wordPairs;
    }
}
