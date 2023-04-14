package com.example.jpgtopng_mvp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.getExternalVolumeNames
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Thread.sleep

class Presenter(private val activity: AppCompatActivity) : Contract.Presenter {
    val processingSubject = PublishSubject.create<Boolean>()
    val messageSubject = PublishSubject.create<String>()

    @SuppressLint("CheckResult")
    override fun buttonClicked(imagePath: String?) {
        if (imagePath == null) {
            Toast.makeText(
                activity.applicationContext,
                "Пожалуйста выберите изображение",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val inputBitmap = BitmapFactory.decodeFile(imagePath)
        if (inputBitmap == null) {
            Toast.makeText(
                activity.applicationContext,
                "Невозможно декодировать это изображение",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        processingSubject.onNext(true)

        convertImage(inputBitmap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { outputBitmap ->
                    val outputImagePath = saveImage(outputBitmap, activity)
                    processingSubject.onNext(false)
                    messageSubject.onNext("Сохранено в  $outputImagePath")
                },
                { error ->
                    processingSubject.onNext(false)
                }
            )
    }


    private fun convertImage(inputBitmap: Bitmap): Single<Bitmap> {
        return Single.create { emitter ->
            try {
                //sleep(1000) // simulate conversion process
                val outputStream = ByteArrayOutputStream()
                inputBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val outputBytes = outputStream.toByteArray()
                val outputBitmap = BitmapFactory.decodeByteArray(outputBytes, 0, outputBytes.size)
                emitter.onSuccess(outputBitmap)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun saveImage(bitmap: Bitmap, context: Context): String {
        val filePath = context.getExternalFilesDir(null)?.absolutePath + "/output.png"
        val file = File(filePath)
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        MediaStore.Images.Media.insertImage(
            context.contentResolver,
            file.absolutePath,
            file.name,
            file.name
        )
        return file.absolutePath
    }
}