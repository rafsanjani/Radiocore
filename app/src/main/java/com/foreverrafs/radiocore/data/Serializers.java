package com.foreverrafs.radiocore.data;

import android.util.Log;

import com.foreverrafs.radiocore.model.News;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class Serializers {

    static class NewsDeserializer implements JsonDeserializer<News> {
        @Override
        public News deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String headline = ((JsonObject) json).get("headline").getAsString();
            String content = ((JsonObject) json).get("content").getAsString();
            String category = ((JsonObject) json).get("category").getAsString();
            String imageUrl = ((JsonObject) json).get("imageUrl").getAsString();
            String dateStr = ((JsonObject) json).get("date").getAsString();
            DateTime date = DateTime.now();

            try {
                date = DateTime.parse(dateStr);
            } catch (Exception exception) {
                Log.i("Serializers", exception.getMessage());
            }

            return new News(headline, date, imageUrl, content, category);
        }
    }

    static class NewsSerializer implements JsonSerializer<News> {

        @Override
        public JsonElement serialize(News src, Type typeOfSrc, JsonSerializationContext context) {
            return null;
        }
    }
}
