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
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.viewmodel.HomeViewModel
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    companion object {
        private const val MAX_WIDTH = 300
        private const val MAX_HEIGHT = 300
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }
    private lateinit var classifierHelper: ImageClassifierHelper
    private val selectImageMessage = "Please select an image first."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classifierHelper = ImageClassifierHelper(this)


        viewModel.currentImageUri?.let { uri -> showImage(uri) }
        viewModel.resizedBitmap?.let { binding.previewImageView.setImageBitmap(it) }


        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                viewModel.currentImageUri = it
                val bitmap = uriToBitmap(it)
                val resizedBitmap = resizeBitmap(bitmap)
                viewModel.resizedBitmap = resizedBitmap
                binding.previewImageView.setImageBitmap(resizedBitmap)
            }
        }

    private fun startGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun analyzeImage() {
        viewModel.resizedBitmap?.let { bitmap ->
            val results = classifierHelper.classifyImage(bitmap)
            if (results != null) moveToResult(results) else showToast("Classification failed.")
        } ?: showToast(selectImageMessage)
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

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scale = (MAX_WIDTH.toFloat() / width).coerceAtMost(MAX_HEIGHT.toFloat() / height)
        val newWidth = (scale * width).toInt()
        val newHeight = (scale * height).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun moveToResult(results: List<Classifications>) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            val category = results.first().categories[0]
            putExtra("RESULTS", category.label)
            putExtra("CONFIDENCE", category.score)

            viewModel.resizedBitmap?.let { bitmap ->
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                val byteArray = stream.toByteArray()
                putExtra("IMAGE", byteArray)
            }
        }
        startActivity(intent)
    }

    private fun showImage(uri: Uri) {
        binding.previewImageView.setImageURI(uri)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
