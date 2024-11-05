package com.dicoding.asclepius.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null
    private lateinit var classifierHelper: ImageClassifierHelper
    private val selectImageMessage = "Please select an image first."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classifierHelper = ImageClassifierHelper(this)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                currentImageUri = it
                showImage(it)
            }
        }

    private fun startGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            val bitmap = uriToBitmap(uri)
            val results = classifierHelper.classifyImage(bitmap)
            moveToResult(results)
        } ?: showToast()
    }

    private fun uriToBitmap(imageUri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = (maxWidth.toFloat() / width).coerceAtMost(maxHeight.toFloat() / height)
        val newWidth = (scale * width).toInt()
        val newHeight = (scale * height).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun moveToResult(results: List<Classifications>) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            val category = results.first().categories[0]
            putExtra("RESULTS", category.label)
            putExtra("CONFIDENCE", category.score)

            currentImageUri?.let { uri ->
                val bitmap = uriToBitmap(uri)

                bitmap.let {
                    // Optionally determine size based on screen dimensions or other logic
                    val screenWidth = resources.displayMetrics.widthPixels
                    val screenHeight = resources.displayMetrics.heightPixels

                    // Resize Bitmap based on current screen size
                    val resizedBitmap = resizeBitmap(it, screenWidth / 2, screenHeight / 2)
                    val stream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val byteArray = stream.toByteArray()
                    putExtra("IMAGE", byteArray)
                }
            }
        }
        startActivity(intent)
    }


    private fun showImage(uri: Uri) {
        binding.previewImageView.setImageURI(uri)
    }

    private fun showToast() {
        Toast.makeText(this, selectImageMessage, Toast.LENGTH_SHORT).show()
    }

}