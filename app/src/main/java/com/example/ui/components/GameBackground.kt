package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.R
import com.example.model.BackgroundMode
import java.io.File
import java.io.FileOutputStream

object BackgroundMediaHelper {
    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val file = File(context.filesDir, "custom_game_bg.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveUri(context: Context, uri: Uri): String? {
        return try {
            val file = File(context.filesDir, "custom_game_bg.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun GameBackground(
    backgroundMode: BackgroundMode,
    customPhotoPath: String?,
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0.55f
) {
    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(targetState = backgroundMode, label = "bg_crossfade") { mode ->
            when (mode) {
                BackgroundMode.SWEET_CANDY -> {
                    SweetCandyBackground()
                }
                BackgroundMode.PHOTO -> {
                    PhotoBackground(photoPath = customPhotoPath)
                }
                BackgroundMode.CAMERA_LIVE -> {
                    LiveCameraBackground()
                }
                BackgroundMode.NEBULA_SPACE -> {
                    NebulaSpaceBackground()
                }
                BackgroundMode.NEON_CYBER -> {
                    NeonCyberBackground()
                }
            }
        }

        // Contrast scrim overlay to ensure optimal bubble & UI contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090D1A).copy(alpha = (scrimAlpha * 1.1f).coerceAtMost(0.95f)),
                            Color(0xFF10162F).copy(alpha = scrimAlpha),
                            Color(0xFF060913).copy(alpha = (scrimAlpha * 1.25f).coerceAtMost(0.98f))
                        )
                    )
                )
        )
    }
}

@Composable
private fun SweetCandyBackground() {
    Image(
        painter = painterResource(id = R.drawable.img_sweet_background),
        contentDescription = "Candy Background",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun PhotoBackground(photoPath: String?) {
    val file = remember(photoPath) {
        if (!photoPath.isNullOrBlank()) File(photoPath) else null
    }

    if (file != null && file.exists()) {
        AsyncImage(
            model = file,
            contentDescription = "Custom Photo Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Fallback if photo not yet selected
        SweetCandyBackground()
    }
}

@Composable
fun LiveCameraBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    if (hasCameraPermission) {
        CameraPreview(lifecycleOwner = lifecycleOwner, modifier = modifier)
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0B1020)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant camera permission to see the live AR camera feed behind your bubbles!",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Camera Access", color = Color(0xFF0B1020), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                // Ignore cleanup error
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun NebulaSpaceBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2E1065),
                        Color(0xFF1E1B4B),
                        Color(0xFF090A1A)
                    ),
                    radius = 1200f
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val starCount = 60
            for (i in 0 until starCount) {
                val seed = i * 7919
                val x = (seed % size.width.toInt()).toFloat()
                val y = ((seed * 31) % size.height.toInt()).toFloat()
                val radius = (1.5f + (seed % 4) * 0.8f)
                val alpha = 0.3f + ((seed % 7) / 10f)
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun NeonCyberBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF022C22),
                        Color(0xFF064E3B),
                        Color(0xFF021B15)
                    )
                )
            )
    )
}
