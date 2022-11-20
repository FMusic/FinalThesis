package fm.pathfinder.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import fm.pathfinder.conf.LocalDateTimeDeserializer
import fm.pathfinder.conf.LocalDateTimeSerializer
import fm.pathfinder.model.Room
import java.io.File
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

class FileManager(
    private val ctx: Context
) {
    private var gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        .setPrettyPrinting()
        .create()

    fun storeBuildingToFile(buildingData: Any): Boolean {
        val jsonBuilding = gson.toJson(buildingData)
        return storeDataToFile(jsonBuilding)
    }

    private fun storeDataToFile(stringToSave: String): Boolean {
        val pattern: Pattern = Pattern.compile("(\\d+)")
        val maxPathFinder = ctx.fileList().toList().stream()
            .map(pattern::matcher)
            .filter(Matcher::find)
            .map { m -> Integer.valueOf(m.group(1)) }
            .mapToInt { it -> it }
            .max()
        val nextNum = if (!maxPathFinder.isPresent) 1 else maxPathFinder.asInt + 1
        return try {
            val file = File(ctx.filesDir, "pathfinder$nextNum.json")
            file.writeText(stringToSave)
            true
        } catch (ex: java.lang.NullPointerException) {
            false
        }
    }

    fun loadAllDataFiles(): List<String> = ctx
        .fileList().toList()
        .stream()
        .filter { it.startsWith("path") }
        .collect(Collectors.toList())

    fun loadDataFromFile(fileName: String): Collection<Room> {
        var fileString = StringBuilder()
        ctx.openFileInput(fileName).bufferedReader().useLines {
            it.toList().stream().forEach { xo ->
                fileString.append(xo + "\n")
            }
        }
//        val collectionType: Type = object : TypeToken<Collection<Room?>?>() {}.type
        return gson.fromJson(fileString.toString())
    }

    fun cleanArchive(){
        loadAllDataFiles().forEach{
            ctx.deleteFile(it)
        }
    }

    private inline fun <reified T> Gson.fromJson(json: String): T =
        fromJson(json, object : TypeToken<T>() {}.type)
}
