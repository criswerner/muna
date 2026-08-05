package com.tiendamuna.stock.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier

class ImageClassifierHelper(
    private val context: Context,
    private val listener: (result: String, inferenceTime: Long) -> Unit
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupClassifier()
    }

    private fun setupClassifier() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("efficientnet_lite0.tflite")
            .build()

        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(1) // Solo queremos el resultado con mayor certeza
            .build()

        imageClassifier = ImageClassifier.createFromOptions(context, options)
    }

    fun classify(bitmap: Bitmap) {
        val startTime = SystemClock.uptimeMillis()
        val mpImage = BitmapImageBuilder(bitmap).build()

        val results = imageClassifier?.classify(mpImage)
        val inferenceTime = SystemClock.uptimeMillis() - startTime

        val topResult = results?.classificationResult()
            ?.classifications()?.firstOrNull()
            ?.categories()?.firstOrNull()

        if (topResult != null) {
            val label = topResult.categoryName()
            val score = (topResult.score() * 100).toInt()
            listener("$label ($score%)", inferenceTime)
        } else {
            listener("Sin detección", inferenceTime)
        }
    }
}