package com.radiocore.news.util

import com.google.gson.*
import com.radiocore.news.data.entities.NewsEntity
import org.joda.time.DateTime
import timber.log.Timber
import java.lang.reflect.Type

class Serializers {
    class NewsDeserializer : JsonDeserializer<NewsEntity> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): NewsEntity {
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
            return NewsEntity(headline, content, date!!, category, imageUrl)
        }
    }

    class NewsSerializer : JsonSerializer<NewsEntity?> {
        override fun serialize(src: NewsEntity?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
            return null
        }
    }
}