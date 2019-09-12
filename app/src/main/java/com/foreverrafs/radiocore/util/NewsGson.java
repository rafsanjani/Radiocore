package com.foreverrafs.radiocore.util;

import com.foreverrafs.radiocore.model.News;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class NewsGson {

    private static Gson gson = null;

    public static Gson getInstance() {

        if (gson != null)
            return gson;

        gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (json, typeOfSrc, context) ->
                        new JsonPrimitive(ISODateTimeFormat.dateTime().print(json)))
                .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) ->
                        ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()))
                .registerTypeAdapter(News.class, new Serializers.NewsSerializer())
                .registerTypeAdapter(News.class, new Serializers.NewsDeserializer())
                .create();
        return gson;
    }

}
