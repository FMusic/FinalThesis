package fm.pathfinder.conf

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class LocalDateTimeSerializer : JsonSerializer<LocalDateTime>{
    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src!!.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }

}

class LocalDateTimeDeserializer: JsonDeserializer<LocalDateTime>{
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime {
        val instant: Instant = Instant.ofEpochMilli(json!!.asJsonPrimitive.asLong)
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    }
}
