package com.tiendamuna.stock.presentation.ia.image

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import android.content.Context
import com.tiendamuna.stock.utils.ImageClassifierHelper

@Composable
fun CameraInferenceScreen(context: Context) {
    var resultText by remember { mutableStateOf("Analizando...") }
    var inferenceTime by remember { mutableStateOf(0L) }

    val classifierHelper = remember {
        ImageClassifierHelper(
            context = context,
            listener = { result, time ->
                resultText = result
                inferenceTime = time
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Vista previa de la cámara (CameraX AndroidView)
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)
                val executor = ContextCompat.getMainExecutor(context)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // Analizador de frames
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        val bitmap = imageProxy.toBitmap() // Convertir frame a Bitmap
                        classifierHelper.classify(bitmap)
                        imageProxy.close()
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        context as LifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay con los resultados en tiempo real
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Objeto: $resultText", style = MaterialTheme.typography.titleMedium)
                Text(text = "Tiempo de Inferencia: ${inferenceTime}ms", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}