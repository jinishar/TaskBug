package com.example.taskbug.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

private const val TAG = "CloudinaryUploader"

/**
 * Uploads images to Cloudinary using an unsigned upload preset (free tier).
 * Uses base64 data URL encoding — the most reliable method from Android.
 *
 * Your credentials:
 *  Cloud Name  : dkthpokztb
 *  Upload Preset: check Settings → Upload → preset name field exactly
 */
object CloudinaryUploader {

    private const val CLOUD_NAME    = "dkhpxkzib"
    /**
     * !! IMPORTANT !!
     * Open Cloudinary Console → Settings → Upload → click your preset to edit it.
     * Copy the exact value from the "Preset name" text field and paste it below.
     */
    private const val UPLOAD_PRESET = "my_unsigned_upload"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Read image bytes
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext Result.failure(Exception("Cannot read image file"))
                val imageBytes = inputStream.readBytes()
                inputStream.close()

                // 2. Encode as base64 data URL (Cloudinary accepts this directly)
                val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                val dataUrl = "data:image/jpeg;base64,$base64"

                val body = FormBody.Builder()
                    .add("file", dataUrl)
                    .add("upload_preset", UPLOAD_PRESET)
                    .build()

                val uploadUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
                Log.d(TAG, "=== CLOUDINARY UPLOAD DEBUG ===")
                Log.d(TAG, "URL: $uploadUrl")
                Log.d(TAG, "Preset: '$UPLOAD_PRESET'")
                Log.d(TAG, "Image size: ${imageBytes.size} bytes")

                val request = Request.Builder()
                    .url(uploadUrl)
                    .post(body)
                    .build()

                Log.d(TAG, "Uploading to Cloudinary cloud=$CLOUD_NAME preset=$UPLOAD_PRESET size=${imageBytes.size} bytes")

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d(TAG, "Cloudinary response ${response.code}: $responseBody")

                if (!response.isSuccessful) {
                    val errMsg = try {
                        JSONObject(responseBody)
                            .optJSONObject("error")
                            ?.optString("message") ?: responseBody
                    } catch (e: Exception) { responseBody }
                    return@withContext Result.failure(Exception(errMsg))
                }

                val secureUrl = JSONObject(responseBody).getString("secure_url")
                Log.d(TAG, "Upload success: $secureUrl")
                Result.success(secureUrl)

            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: Exception) {
                Log.e(TAG, "Upload error: ${e.message}", e)
                Result.failure(e)
            }
        }
}
