package fm.pathfinder.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

enum class API_ENDPOINTS {
    ACCELERATION_VALUES {
        override fun toString() = "/accelerationvalues"
    },
    ACCELERATION_FILTERED{
        override fun toString() = "/accelerationfiltered"
    },
    ACCELERATION_FILTERED_3D{
        override fun toString() = "/accelerationfiltered3d"
    },
    ORIENTATION_VALUES{
        override fun toString() = "/orientationvalues"
    },
    STEP_EVENTS_VALUES{
        override fun toString() = "/stepevents"
    }
}

class ApiHelper {
    private val client = OkHttpClient()
    private val apiUrl = "http://192.168.0.14:3000" // Replace with your PostgREST API base URL

    suspend fun saveData(apiData: List<Map<String, Any>>, endpoints: API_ENDPOINTS) {
        withContext(Dispatchers.IO) { // Run on a background thread
            val jsonArray = JSONArray()
            apiData.stream().map { JSONObject(it) }.forEach { jsonArray.put(it) }
            postToApi(jsonArray, endpoints)
        }
    }

    private fun postToApi(jsonData: JSONObject, endpoints: API_ENDPOINTS){
        val requestBody = jsonData.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$apiUrl$endpoints")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("ApiHelper", "Failed to post data: ${response.code}, ${response.message}")
                Log.e("ApiHelper", "Failed to post data to: $endpoints")
                Log.e("ApiHelper", "Failed to post data: ${response.body?.string()}")
                throw Exception("Failed to post data: ${response.code}, ${response.message}")
            }
            Log.i("ApiHelper", "Data successfully posted to $endpoints: ${response.body?.string()}")
        }
    }

    private fun postToApi(jsonData: JSONArray, endpoint: API_ENDPOINTS) {
        val requestBody = jsonData.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$apiUrl$endpoint")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("ApiHelper", "Failed to post data: ${response.code}, ${response.message}")
                Log.e("ApiHelper", "Failed to post data to: $endpoint")
                Log.e("ApiHelper", "Failed to post data: ${response.body?.string()}")
                throw Exception("Failed to post data: ${response.code}, ${response.message}")
            }
            Log.i("ApiHelper", "Data successfully posted to $endpoint: ${response.body?.string()}")
        }
    }
}
