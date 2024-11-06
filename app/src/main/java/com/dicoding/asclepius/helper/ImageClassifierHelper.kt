package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.File

class ImageClassifierHelper(private val context: Context) {

    private var imageClassifier: ImageClassifier

    init {
        val modelFile = File(context.filesDir, "cancer_classification.tflite")
        if (!modelFile.exists()) {
            context.assets.open("cancer_classification.tflite").use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        imageClassifier = ImageClassifier.createFromFile(modelFile)
    }

    fun classifyImage(bitmap: Bitmap): List<Classifications>? {
        return try {
            val convertedBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
                bitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                bitmap
            }

            val tensorImage = TensorImage.fromBitmap(convertedBitmap)
            imageClassifier.classify(tensorImage)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error during image classification: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            null
        }
    }
}
