package com.example.acremotecontrol

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import android.util.Log

data class AcState(
    val success: Boolean = false,
    val switchStatus: Int = 1,
    val runMode: Int = 2,
    val windSpeed: Int = 2,
    val tempSet: Int = 24
)

object AcApiService {
    private const val BASE_URL = "https://gxkt.juhaolian.cn/api/device/direct/state"
    private const val COMMAND_URL = "https://gxkt.juhaolian.cn/api/device/direct/command"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private fun commonHeaders() = mapOf(
        "Host" to "gxkt.juhaolian.cn",
        "Connection" to "keep-alive",
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 MicroMessenger/7.0.20.1781(0x6700143B) NetType/WIFI MiniProgramEnv/Windows WindowsWechat/WMPF WindowsWechat(0x63090a13) UnifiedPCWindowsWechat(0xf254173b) XWEB/19027",
        "xweb_xhr" to "1",
        "Content-Type" to "application/json",
        "Accept" to "*/*",
        "Sec-Fetch-Site" to "cross-site",
        "Sec-Fetch-Mode" to "cors",
        "Sec-Fetch-Dest" to "empty",
        "Referer" to "https://servicewechat.com/wx09edd3fe0e526f26/16/page-frame.html",
        "Accept-Language" to "zh-CN,zh;q=0.9"
    )

    suspend fun fetchState(imei: String): AcState {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = "$BASE_URL?imei=$imei"
                val request = Request.Builder()
                    .url(url)
                    .apply {
                        commonHeaders().forEach { (k, v) -> header(k, v) }
                    }
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val result = json.optJSONObject("result")
                    if (result != null) {
                        AcState(
                            success = true,
                            switchStatus = result.optInt("switchStatus", 1),
                            runMode = result.optInt("runMode", 2),
                            windSpeed = result.optInt("windSpeed", 2),
                            tempSet = result.optInt("tempSet", 24)
                        )
                    } else {
                        Log.d("null","notok")
                        AcState()
                    }
                } else {
                    AcState()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AcState()
            }
        }
    }

    suspend fun sendCommand(imei: String, params: Map<String, Any>): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val jsonParams = JSONObject(params).apply { put("imei", imei) }
                val body = jsonParams.toString().toRequestBody(JSON_MEDIA_TYPE)

                val request = Request.Builder()
                    .url(COMMAND_URL)
                    .post(body)
                    .apply {
                        commonHeaders().forEach { (k, v) -> header(k, v) }
                    }
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    json.optBoolean("success", false)
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
