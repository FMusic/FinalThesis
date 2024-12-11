package fm.pathfinder.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

data class ApiDataSingle(val x: Float, val y: Float, val z: Float, val timestamp: Instant)
data class ApiData(val data: MutableList<ApiDataSingle>, val endpoint: String)

class ApiHelper {

    private val client = OkHttpClient()
    private val apiUrl = "http://192.168.0.14:3000" // Replace with your PostgREST API base URL

    private fun initializeConnection() {
        // Optionally initialize any API-related resources or log the connection details.
    }

    suspend fun saveData(apiData: ApiData) {
        withContext(Dispatchers.IO) { // Run on a background thread
            val jsonArray = JSONArray()
            apiData.data.toList().forEach {
                val jsonObject = JSONObject()
                jsonObject.put("x", it.x.toDouble()) // Convert Float to Double
                jsonObject.put("y", it.y.toDouble()) // Convert Float to Double
                jsonObject.put("z", it.z.toDouble()) // Convert Float to Double
                jsonObject.put("timestamp", it.timestamp.toString())
                jsonArray.put(jsonObject)
            }
            postToApi(apiData.endpoint, jsonArray)
        }
    }


    private fun postToApi(endpoint: String, jsonData: JSONArray) {
        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonData.toString()
        )

        val request = Request.Builder()
            .url("$apiUrl$endpoint")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to post data: ${response.code}, ${response.message}")
            }
            println("Data successfully posted to $endpoint: ${response.body?.string()}")
        }
    }
}
