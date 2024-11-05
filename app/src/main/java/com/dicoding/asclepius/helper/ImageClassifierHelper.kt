package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.File

class ImageClassifierHelper(context: Context) {

    private var imageClassifier: ImageClassifier

    init {
        // Load the model from the assets directory
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

    fun classifyImage(bitmap: Bitmap): List<Classifications> {
        val convertedBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap
        }

        val tensorImage = TensorImage.fromBitmap(convertedBitmap)
        return imageClassifier.classify(tensorImage)
    }
}