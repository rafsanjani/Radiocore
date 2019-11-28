package com.foreverrafs.radiocore.util

import com.foreverrafs.radiocore.model.News
import com.foreverrafs.radiocore.util.Serializers.NewsDeserializer
import com.foreverrafs.radiocore.util.Serializers.NewsSerializer
import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.lang.reflect.Type

object NewsGson {
    private var gson: Gson? = null
    val instance: Gson?
        get() {
            if (gson != null) return gson
            gson = GsonBuilder()
                    .registerTypeAdapter(DateTime::class.java, JsonSerializer { json: DateTime?, typeOfSrc: Type?, context: JsonSerializationContext? -> JsonPrimitive(ISODateTimeFormat.dateTime().print(json)) })
                    .registerTypeAdapter(DateTime::class.java, JsonDeserializer { json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext? -> ISODateTimeFormat.dateTime().parseDateTime(json.asString) } as JsonDeserializer<DateTime>)
                    .registerTypeAdapter(News::class.java, NewsSerializer())
                    .registerTypeAdapter(News::class.java, NewsDeserializer())
                    .create()
            return gson
        }
}