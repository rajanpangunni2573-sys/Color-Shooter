package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.model.BackgroundMode
import java.io.File

@Composable
fun BackgroundSelectorDialog(
    currentMode: BackgroundMode,
    customPhotoPath: String?,
    onModeSelected: (BackgroundMode) -> Unit,
    onPhotoSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHANGE BACKGROUND",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BackgroundSelectorControls(
                    currentMode = currentMode,
                    customPhotoPath = customPhotoPath,
                    onModeSelected = onModeSelected,
                    onPhotoSelected = onPhotoSelected
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("Done", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BackgroundSelectorControls(
    currentMode: BackgroundMode,
    customPhotoPath: String?,
    onModeSelected: (BackgroundMode) -> Unit,
    onPhotoSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Photo picker launcher (0 permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = BackgroundMediaHelper.saveUri(context, uri)
            if (savedPath != null) {
                onPhotoSelected(savedPath)
                onModeSelected(BackgroundMode.PHOTO)
            }
        }
    }

    // Camera take picture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = BackgroundMediaHelper.saveBitmap(context, bitmap)
            if (savedPath != null) {
                onPhotoSelected(savedPath)
                onModeSelected(BackgroundMode.PHOTO)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Active selection indicator
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentMode) {
                        BackgroundMode.SWEET_CANDY -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_sweet_background),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        BackgroundMode.PHOTO -> {
                            val file = if (!customPhotoPath.isNullOrBlank()) File(customPhotoPath) else null
                            if (file != null && file.exists()) {
                                AsyncImage(
                                    model = file,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF38BDF8))
                            }
                        }
                        BackgroundMode.CAMERA_LIVE -> {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF10B981))
                        }
                        BackgroundMode.NEBULA_SPACE -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF3B0764)))
                        }
                        BackgroundMode.NEON_CYBER -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF064E3B)))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current: ${currentMode.displayName}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = currentMode.description,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PHOTO & CAMERA OPTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row: Pick Photo from Gallery, Take Photo, Live Camera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pick Photo Button
            BackgroundActionCard(
                title = "Choose Photo",
                subtitle = "From Gallery",
                icon = Icons.Default.Image,
                iconTint = Color(0xFF38BDF8),
                isSelected = currentMode == BackgroundMode.PHOTO && !customPhotoPath.isNullOrBlank(),
                onClick = {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                testTag = "choose_photo_button"
            )

            // Take Camera Photo Button
            BackgroundActionCard(
                title = "Take Photo",
                subtitle = "Snap Picture",
                icon = Icons.Default.PhotoCamera,
                iconTint = Color(0xFFF59E0B),
                isSelected = false,
                onClick = {
                    cameraLauncher.launch(null)
                },
                modifier = Modifier.weight(1f),
                testTag = "take_photo_button"
            )

            // Live Camera AR Button
            BackgroundActionCard(
                title = "Live Camera",
                subtitle = "AR Background",
                icon = Icons.Default.Videocam,
                iconTint = Color(0xFF10B981),
                isSelected = currentMode == BackgroundMode.CAMERA_LIVE,
                onClick = {
                    onModeSelected(BackgroundMode.CAMERA_LIVE)
                },
                modifier = Modifier.weight(1f),
                testTag = "live_camera_button"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PRESET THEMES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Theme options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeCard(
                name = "Candyland",
                mode = BackgroundMode.SWEET_CANDY,
                isSelected = currentMode == BackgroundMode.SWEET_CANDY,
                gradient = Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))),
                onSelect = { onModeSelected(BackgroundMode.SWEET_CANDY) },
                modifier = Modifier.weight(1f),
                testTag = "theme_candyland"
            )

            ThemeCard(
                name = "Cosmic",
                mode = BackgroundMode.NEBULA_SPACE,
                isSelected = currentMode == BackgroundMode.NEBULA_SPACE,
                gradient = Brush.linearGradient(listOf(Color(0xFF312E81), Color(0xFF581C87))),
                onSelect = { onModeSelected(BackgroundMode.NEBULA_SPACE) },
                modifier = Modifier.weight(1f),
                testTag = "theme_cosmic"
            )

            ThemeCard(
                name = "Neon Cyber",
                mode = BackgroundMode.NEON_CYBER,
                isSelected = currentMode == BackgroundMode.NEON_CYBER,
                gradient = Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF0F172A))),
                onSelect = { onModeSelected(BackgroundMode.NEON_CYBER) },
                modifier = Modifier.weight(1f),
                testTag = "theme_cyber"
            )
        }
    }
}

@Composable
private fun BackgroundActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0F172A)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ThemeCard(
    name: String,
    mode: BackgroundMode,
    isSelected: Boolean,
    gradient: Brush,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else Color(0x44FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}
