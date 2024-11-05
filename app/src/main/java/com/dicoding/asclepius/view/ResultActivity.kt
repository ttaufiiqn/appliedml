package com.dicoding.asclepius.view

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val result = intent.getStringExtra("RESULTS")
        val confidence = intent.getFloatExtra("CONFIDENCE", 0.0f)

        binding.resultText.text = getString(R.string.prediction_text, result)
        binding.resultText.append("\n" + getString(R.string.confidence_text, confidence * 100))

        // Retrieve the byte array and convert it back to a Bitmap
        val byteArray = intent.getByteArrayExtra("IMAGE")
        byteArray?.let {
            try {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                if (bitmap != null) {
                    binding.resultImage.setImageBitmap(bitmap)
                } else {
                    Log.e("ResultActivity", "Bitmap is null after decoding")
                    // Set a placeholder image if decoding fails
                    binding.resultImage.setImageResource(R.drawable.ic_place_holder)
                }
            } catch (e: Exception) {
                Log.e("ResultActivity", "Error decoding image: ${e.message}")
                // Set a placeholder image in case of error
                binding.resultImage.setImageResource(R.drawable.ic_place_holder)
            }
        } ?: run {
            Log.e("ResultActivity", "No image data received")
            // Set a placeholder image if no data
            binding.resultImage.setImageResource(R.drawable.ic_place_holder)
        }
    }
}