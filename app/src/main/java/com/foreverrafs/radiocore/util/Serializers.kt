package com.foreverrafs.radiocore.util

import com.foreverrafs.radiocore.model.News
import com.google.gson.*
import org.joda.time.DateTime
import timber.log.Timber
import java.lang.reflect.Type

class Serializers {
    class NewsDeserializer : JsonDeserializer<News> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): News {
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
            return News(headline, content, date!!, category, imageUrl)
        }
    }

    class NewsSerializer : JsonSerializer<News?> {
        override fun serialize(src: News?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
            return null
        }
    }
}