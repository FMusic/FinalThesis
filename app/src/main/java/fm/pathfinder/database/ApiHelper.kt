package fm.pathfinder.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class ApiDataSingle(val x: Float, val y: Float, val z: Float, val timestamp: Long)
data class ApiData(val data: MutableList<ApiDataSingle>)

class ApiHelper(
    private val endpoint: String
) {
    private val client = OkHttpClient()
    private val apiUrl = "http://192.168.0.14:3000" // Replace with your PostgREST API base URL

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
            postToApi(jsonArray)
        }
    }


    private fun postToApi(jsonData: JSONArray) {
        val requestBody = jsonData.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

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
