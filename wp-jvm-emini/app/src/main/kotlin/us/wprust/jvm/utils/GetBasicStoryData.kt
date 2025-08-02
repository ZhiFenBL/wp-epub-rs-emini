import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import us.wprust.jvm.utils.Story
import us.wprust.jvm.utils.User

// Client and Parser are the same

private val client = OkHttpClient()
private val jsonParser = Json { ignoreUnknownKeys = true }

// Helper class that matches the API's JSON response
@Serializable
private data class StoryApiResponse(
    val title: String,
    val user: User,
    val cover: String,
    val mature: Boolean
)

/**
 * Fetches story data and its cover image asynchronously.
 * Returns a complete Story object on success, or null on failure.
 */
suspend fun getStoryDataAsync(storyId: String): Story? {
    val apiUrl = "https://www.wattpad.com/api/v3/stories/$storyId?fields=title,user(name),cover,mature"
    val apiRequest = Request.Builder()
        .url(apiUrl)
        .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
        )
        .build()

    return withContext(Dispatchers.IO) {
        try {
            // Step 1: Get the story metadata
            val apiResponse = client.newCall(apiRequest).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val responseBody = response.body.string()
                jsonParser.decodeFromString<StoryApiResponse>(responseBody)
            }

            // Step 2: Download the cover image
            val imageRequest = Request.Builder().url(apiResponse.cover).build()
            val imageBytes = client.newCall(imageRequest).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                response.body.bytes()
            }

            // Step 3: Construct the final Story object
            Story(
                title = apiResponse.title,
                user = apiResponse.user,
                mature = apiResponse.mature,
                coverData = imageBytes
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}