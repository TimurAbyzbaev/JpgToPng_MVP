package com.example.jpgtopng_mvp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import com.example.jpgtopng_mvp.databinding.ActivityMainBinding
import com.example.jpgtopng_mvp.ui.Contract
import com.example.jpgtopng_mvp.ui.Presenter
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.PublishSubject

const val REQUEST_CODE_PERMISSION = 123
private const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE


class MainActivity : AppCompatActivity(), Contract.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: Contract.Presenter
    private var imagePath: String? = null
    val PICK_IMAGE_REQUEST = 1


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = Presenter(this)



        binding.button.setOnClickListener {

            // Проверка наличия разрешения на чтение изображений из галереи
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Если разрешение не предоставлено, запрашиваем его
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE_PERMISSION
                )
            } else {
                // Если разрешение уже предоставлено, вызываем метод для выбора изображения
                pickImage()
            }

        }

        (presenter as Presenter).processingSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe() { inProgress ->
                showProcess(inProgress)
            }

        (presenter as Presenter).messageSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe() { messageSubject ->
                showResult(messageSubject)
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Разрешение было предоставлено, вызываем метод для выбора изображения
                    pickImage()
                } else {
                    // Разрешение не было предоставлено, выводим сообщение об ошибке
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun showProcess(inProcess: Boolean) {
        binding.progressBar.visibility = if (inProcess) VISIBLE else GONE
    }

    override fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data?.data
            if (imageUri != null) {
                imagePath = getRealPathFromURI(imageUri)
            }

            // Здесь можно вызвать метод Presenter, например:
            presenter.buttonClicked(imagePath)
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return filePath
    }
}