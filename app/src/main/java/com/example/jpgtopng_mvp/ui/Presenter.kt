package com.example.jpgtopng_mvp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.getExternalVolumeNames
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

const val REQUEST_CODE_PERMISSION = 124
const val PICK_IMAGE_REQUEST = "PICKED_IMAGE"

class Presenter(private val activity: Activity) : Contract.Presenter {


    override fun buttonClicked(imagePath: String?) {
        if (imagePath == null) {
            // Обработка ошибки, если изображение не выбрано
            return
        }

        // Конвертация изображения в отдельном потоке с использованием RxJava
        Single.fromCallable { convertImage(imagePath) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ convertedImagePath ->
                // Обработка успешной конвертации
            }, { error ->
                // Обработка ошибки конвертации
            })
    }

    private fun convertImage(imagePath: String): String {
        // Если разрешение уже предоставлено, вызываем метод для выбора изображения
        // Конвертация изображения и сохранение в файл
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val outputStreamByte = outputStream.toByteArray()
        val fileName = "image_${System.currentTimeMillis()}.png"
        val file = File(activity.applicationContext.getExternalFilesDir(null), fileName)
        file.createNewFile()
        val outputStreamToFile = FileOutputStream(file)
        outputStreamToFile.write(outputStreamByte)
        outputStream.close()
        outputStreamToFile.close()
        return file.absolutePath
    }
}