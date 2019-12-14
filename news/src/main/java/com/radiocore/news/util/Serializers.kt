package com.radiocore.news.util

import com.google.gson.*
import org.joda.time.DateTime
import timber.log.Timber
import java.lang.reflect.Type

class Serializers {
    class NewsDeserializer : JsonDeserializer<com.radiocore.news.model.News> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): com.radiocore.news.model.News {
            val headline = (json as JsonObject)["headline"].asString
            val content = json["content"].asString
            val category = json["category"].asString
            val imageUrl = json["imageUrl"].asString
            val dateStr = json["date"].asString
            var date = DateTime.now()
            try {
                date = DateTime.parse(dateStr)
            } catch (exception: Exception) {
                Timber.i(exception)
            }
            return com.radiocore.news.model.News(headline, content, date!!, category, imageUrl)
        }
    }

    class NewsSerializer : JsonSerializer<com.radiocore.news.model.News?> {
        override fun serialize(src: com.radiocore.news.model.News?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
            return null
        }
    }
}