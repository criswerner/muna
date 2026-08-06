package com.tiendamuna.stock.presentation.ia.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tiendamuna.stock.utils.CaptureImageClassifierHelper

@Composable
fun CameraCaptureAndInferenceScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado para guardar el clasificador (reutilizado)
    val classifierHelper = remember { CaptureImageClassifierHelper(context) }

    // Estados para la UI
    var classifierResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Componentes de CameraX
    val previewView = remember { PreviewView(context) }
    // IMPORTANTE: Caso de uso para TOMAR FOTOS
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    // Inicializar cámara al inicio
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                // Vinculamos Preview e ImageCapture
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Vista previa de la cámara en el fondo
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay con controles y resultados
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            if (classifierResult != null) {
                // Mostrar resultado si ya se tomó la foto
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Detectado:", style = MaterialTheme.typography.titleMedium)
                        Text(text = classifierResult!!, style = MaterialTheme.typography.bodyLarge)
                        Button(
                            onClick = { classifierResult = null },
                            modifier = Modifier.padding(top = 8.dp).align(Alignment.End)
                        ) {
                            Text("Tomar otra")
                        }
                    }
                }
            } else {
                // Botón de Obturador (Círculo)
                FloatingActionButton(
                    onClick = {
                        if (!isAnalyzing) {
                            takePhotoAndAnalyze(
                                context,
                                imageCapture,
                                classifierHelper,
                                onStart = { isAnalyzing = true },
                                onResult = {
                                    classifierResult = it
                                    isAnalyzing = false
                                }
                            )
                        }
                    },
                    containerColor = if (isAnalyzing) Color.Gray else Color.White,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Center)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
            }
        }
    }
}

// Función auxiliar para gestionar la captura e inferencia
private fun takePhotoAndAnalyze(
    context: Context,
    imageCapture: ImageCapture,
    classifierHelper: CaptureImageClassifierHelper,
    onStart: () -> Unit,
    onResult: (String) -> Unit
) {
    onStart() // Iniciar loading

    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                // 1. Convertir ImageProxy a Bitmap
                val bitmap = imageProxyToBitmap(image)
                image.close() // IMPORTANTE cerrar

                if (bitmap != null) {
                    // 2. Ejecutar Inferencia (100% local)
                    val result = classifierHelper.classify(bitmap)
                    onResult(result) // Enviar resultado a la UI
                } else {
                    onResult("Error al procesar imagen")
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onResult("Error de captura: ${exception.message}")
            }
        }
    )
}

// Función auxiliar (boilerplate) para convertir ImageProxy a Bitmap
private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
