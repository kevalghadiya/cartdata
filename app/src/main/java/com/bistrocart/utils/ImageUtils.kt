package com.bistrocart.utils

import android.graphics.Bitmap

import android.os.Build
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import android.provider.OpenableColumns
import com.bistrocart.BuildConfig
import com.bistrocart.R
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    fun getImageUri(inImage: Bitmap, context: Context): Uri? {
        var inImage = inImage
        inImage = scaleDown(inImage, 1200f, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/" + context.resources.getString(R.string.app_name)
                )
                val resolver = context.contentResolver
                val uriPath =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                val output = resolver.openOutputStream(uriPath!!)
                inImage.compress(Bitmap.CompressFormat.JPEG, 90, output)
                output!!.flush()
                output.close()
                return uriPath
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            try {
                val file = File(file, fileName)
                val bos = ByteArrayOutputStream()
                inImage.compress(Bitmap.CompressFormat.JPEG, 90, bos)
                val bitmapdata = bos.toByteArray()
                val fos = FileOutputStream(file)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
                return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        file
                    )
                } else {
                    Uri.fromFile(file)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun scaleDown(
        realImage: Bitmap,
        maxImageSize: Float,
        filter: Boolean
    ): Bitmap {
        val ratio = Math.min(
            maxImageSize / realImage.width,
            maxImageSize / realImage.height
        )
        val width = Math.round(ratio * realImage.width)
        val height = Math.round(ratio * realImage.height)
        return Bitmap.createScaledBitmap(realImage, width, height, filter)
    }

    fun getInputStreamByUri(cxt: Context, uri: Uri?): InputStream? {
        val inputStream: InputStream?
        return try {
            inputStream = cxt.contentResolver.openInputStream(uri!!)
            inputStream
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val file: File
        get() {
            val mainFolder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Visitors"
            )
            if (!mainFolder.exists()) {
                mainFolder.mkdir()
            }
            return mainFolder
        }

    fun getPathFromUrl(cxt: Context, uri: Uri?): String {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        val cursor = cxt.contentResolver.query(uri!!, projection, null, null, null)
        if (cursor == null || cursor.count == 0) {
            return ""
        }
        val column_index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }

    val fileName: String
        get() {
            val timeStamp = SimpleDateFormat("HH_mm_ss", Locale.getDefault()).format(Date())
            return File.separator + "IMG_" + timeStamp + System.currentTimeMillis() + ".jpg"
        }
}