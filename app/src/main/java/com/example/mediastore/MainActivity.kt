package com.example.mediastore

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.mediastore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageCapture: ImageCapture

    private var checkedItem = 0

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123456

        private const val MEDIA_IMAGE_PERMISSION_REQUEST_CODE = 123457

        private const val MEDIA_IMAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val MEDIA_IMAGE_PERMISSION_TIRAMISU = android.Manifest.permission.READ_MEDIA_IMAGES
    }

    private val mediaImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        MEDIA_IMAGE_PERMISSION_TIRAMISU
    } else {
        MEDIA_IMAGE_PERMISSION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            setupCamera()
        }
    }

    private fun isMediaImagePermissionGranted(): Boolean {
        return checkSelfPermission(mediaImagePermission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkMediaImagePermission() {
        if (isMediaImagePermissionGranted()) {
            listImages()
        } else {
            requestPermissions(arrayOf(mediaImagePermission), MEDIA_IMAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupCamera() {
        val provider = ProcessCameraProvider.getInstance(this)
        provider.addListener({
            val cameraProvider = provider.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.preview.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(this))

        binding.capture.setOnClickListener {
            capture()
        }
        binding.showImages.setOnClickListener {
            if (isMediaImagePermissionGranted()) {
                listImages()
            } else {
                confirmShowImages()
            }
        }
    }

    private fun capture() {
        if (!::imageCapture.isInitialized) {
            return
        }

        val fileName = "${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(this@MainActivity, "Image saved", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                Toast.makeText(this@MainActivity, "Error saving image", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmShowImages() {
        AlertDialog.Builder(this)
            .setTitle("画像一覧を表示")
            .setSingleChoiceItems(arrayOf("このアプリで撮影した画像", "ライブラリから選択"), checkedItem) { _, which ->
                checkedItem = which
            }
            .setPositiveButton("OK") { _, _ ->
                when (checkedItem) {
                    0 -> listImages()
                    1 -> checkMediaImagePermission()
                }
                checkedItem = 0
            }
            .setNegativeButton("キャンセル") { _, _ ->
                checkedItem = 0
            }
            .show()
    }

    private fun listImages() {
        startActivity(Intent(this, ImageListActivity::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCamera()
                }
            }
            MEDIA_IMAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listImages()
                }
            }
        }
    }
}