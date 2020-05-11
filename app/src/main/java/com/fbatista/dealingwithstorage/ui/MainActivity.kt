package com.fbatista.dealingwithstorage.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.fbatista.dealingwithstorage.BuildConfig
import com.fbatista.dealingwithstorage.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    companion object {
        const val FILE_NAME = "dealingWithDirs.png"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListeners()
    }

    private fun setListeners() {
        onlyIntDirCacheBtn.setOnClickListener {
            //cacheDir is useful to share data with other apps
            getImageAsFile(File(cacheDir, FILE_NAME))?.let {
                shareFile(it)
            }
        }

        onlyIntDirBtn.setOnClickListener {
            getImageAsFile(File(filesDir, FILE_NAME))?.let {
                shareFile(it)
            }
        }

        onlyExtDirBtn.setOnClickListener {
            getImageAsFile(File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), FILE_NAME))?.let {
                shareFile(it)
            }
        }

        onlyExtPublicDirBtn.setOnClickListener {
            //Will throw IOException when running Android Q+.
            getImageAsFile(File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME))?.let {
                shareFile(it)
            }
        }
    }

    private fun getImageAsFile(file: File): File? {
        try {
            /*
            An "IOException" will be thrown here if the app can't access the directory.
            It will happend if the device is running Android Q and the directory
            is a "external public directory".
            This happens due to new privacy policies about directories on Android Q.
             */
            file.createNewFile()
            val bitmap = logoIv.drawable.toBitmap()
            val outStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outStream)
            val bitmapData = outStream.toByteArray()

            val fileOutStream = FileOutputStream(file)
            fileOutStream.run {
                write(bitmapData)
                flush()
                close()
            }
            return file
        } catch (e: Exception) {
            e.displayAsToast(this)
            return null
        }
    }

}

fun Exception.displayAsToast(context: Context) {
    Toast.makeText(context, this.message, Toast.LENGTH_SHORT).show()
}

fun Context.shareFile(file: File) {
    /*An exception will be thrown here if the "file providers" aren't set correctly.
    Probably the file is saved in a directory that has no file provider attached.
    Check file_paths.xml to fix it.
     */
    try {
        val uri = FileProvider.getUriForFile(this,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file)
        val share = Intent()
        share.action = Intent.ACTION_SEND
        share.type = contentResolver.getType(uri)
        share.putExtra(Intent.EXTRA_STREAM, uri)
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(share)
    } catch (e: Exception) {
        e.displayAsToast(this)
    }

}