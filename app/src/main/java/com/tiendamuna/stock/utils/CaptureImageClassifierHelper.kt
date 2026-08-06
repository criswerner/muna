package com.tiendamuna.stock.utils

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier

class CaptureImageClassifierHelper(
    private val context: Context,
) {

    private var imageClassifier: ImageClassifier? = null

    init {
        setupClassifier()
    }

    private fun setupClassifier() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_LITE_PATH)
            .build()

        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(1)
            .build()

        imageClassifier = ImageClassifier.createFromOptions(context, options)
    }

    // AHORA: La función devuelve el resultado directamente
    fun classify(bitmap: Bitmap): String {
        val mpImage = BitmapImageBuilder(bitmap).build()

        val results = imageClassifier?.classify(mpImage)

        val topResult = results?.classificationResult()
            ?.classifications()?.firstOrNull()
            ?.categories()?.firstOrNull()

        return if (topResult != null) {
            val label = topResult.categoryName()
            val score = (topResult.score() * 100).toInt()
            "$label ($score%)"
        } else {
            "No se pudo clasificar"
        }
    }

    companion object {
        private const val MODEL_LITE_PATH = "efficientnet_lite0.tflite"
    }
}