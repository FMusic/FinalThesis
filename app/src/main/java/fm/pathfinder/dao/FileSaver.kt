//package fm.pathfinder.dao
//
//import android.content.Context
//import android.os.Environment
//import fm.pathfinder.model.Building
//import fm.pathfinder.processor.BuildingJsonProcessor
//import java.io.File
//import java.io.FileWriter
//import java.lang.Exception
//import java.nio.file.Path
//import kotlin.io.path.Path
//
//object FileSaver {
//    fun save(jsonText: String, context: Context) {
//        val filename = "PathFinder.json"
//        val dir = File(context.filesDir, "PathFinder")
//        if (!dir.exists()) {
//            dir.mkdir()
//        }
//        val file = File(dir, filename)
//        val writer = FileWriter(file)
//        writer.append(jsonText)
//        writer.flush()
//        writer.close()
//    }
//
//}