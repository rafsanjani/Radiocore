package com.radiocore.news.util

import com.google.gson.*
import com.radiocore.news.model.News
import com.radiocore.news.util.Serializers.NewsDeserializer
import com.radiocore.news.util.Serializers.NewsSerializer
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.lang.reflect.Type

object GsonConverters {
    private var gson: Gson? = null
    val instance: Gson?
        get() {
            if (gson != null) return gson
            gson = GsonBuilder()
                    .registerTypeAdapter(DateTime::class.java, JsonSerializer { json: DateTime?, _: Type?, _: JsonSerializationContext? ->
                        JsonPrimitive(ISODateTimeFormat.dateTime().print(json))
                    })
                    .registerTypeAdapter(DateTime::class.java, JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                        ISODateTimeFormat.dateTime().parseDateTime(json.asString)
                    } as JsonDeserializer<DateTime>)
                    .registerTypeAdapter(News::class.java, NewsSerializer())
                    .registerTypeAdapter(News::class.java, NewsDeserializer())
                    .create()
            return gson
        }
}