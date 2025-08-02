package us.wprust.jvm.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

private val httpClient: OkHttpClient = OkHttpClient()

object NetworkUtils {

    // ---------------------------------------------------------------------------------------------------------
    private fun isURIAccessible(uri: String?): Boolean {
        val request = Request.Builder().url(uri!!).head().build()
        try {
            httpClient.newCall(request).execute().use { response ->
                return response.isSuccessful // 2xx HTTP responses
            }
        } catch (e: IOException) {
            return false // Error or unreachable URI
        }
    }

    @JvmStatic
    fun isNetworkAvailable(customServer: String): Boolean {
        return isURIAccessible(customServer)
    }

    @JvmStatic
    fun isNetworkAvailable(predefinedCommonServer: COMMON_SERVERS): Boolean {
        return isURIAccessible(predefinedCommonServer.toString())
    }

    interface COMMON_SERVERS {
        companion object {
            const val GOOGLE: String = "https://www.google.com"
            const val MICROSOFT: String = "https://www.microsoft.com"
            const val GITHUB: String = "https://github.com"
            const val GOOGLE_DNS: String = "https://dns.google"
            const val FAST_COM: String = "https://fast.com"
            const val CLOUDFLARE: String = "https://www.cloudflare.com"
            const val IPIFY: String = "https://api.ipify.org"
            const val AWS_CHECKIP: String = "https://checkip.amazonaws.com"
            const val PING_EU: String = "https://ping.eu"
        }
    }
}