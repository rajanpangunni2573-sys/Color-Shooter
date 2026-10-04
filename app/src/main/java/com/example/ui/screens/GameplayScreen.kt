package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackgroundMode
import com.example.model.GameStatus
import com.example.model.GameUiState
import com.example.model.PowerUpType
import com.example.ui.components.BackgroundSelectorDialog
import com.example.ui.components.BubbleCanvasRenderer
import com.example.ui.components.DefeatOverlay
import com.example.ui.components.GameBackground
import com.example.ui.components.LauncherControls
import com.example.ui.components.PauseOverlay
import com.example.ui.components.StarRatingBar
import com.example.ui.components.VictoryOverlay

@Composable
fun GameplayScreen(
    uiState: GameUiState,
    onAimTouch: (touchX: Float, touchY: Float, isRelease: Boolean) -> Unit,
    onCanvasSizeChanged: (width: Float, height: Float) -> Unit,
    onSwapClicked: () -> Unit,
    onPowerUpClicked: (PowerUpType) -> Unit,
    onBackgroundModeChanged: (BackgroundMode) -> Unit,
    onCustomPhotoSelected: (String?) -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onRestartClicked: () -> Unit,
    onNextLevelClicked: () -> Unit,
    onMainMenuClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBackgroundDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Dynamic Game Background: Sweet Candy, Custom Photo from Gallery/Camera, or Live Camera AR
        GameBackground(
            backgroundMode = uiState.backgroundMode,
            customPhotoPath = uiState.customPhotoPath,
            scrimAlpha = 0.58f
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, start = 16.dp, end = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level ID & Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LEVEL ${uiState.currentLevel?.id ?: 1} / 1000",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = uiState.currentLevel?.name ?: "Arcade",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                // Score Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "SCORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${uiState.score}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Top Actions: Star Rating, Background Switcher, Pause
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StarRatingBar(
                        score = uiState.score,
                        stars = uiState.stars,
                        level = uiState.currentLevel,
                        modifier = Modifier.width(68.dp)
                    )

                    // Quick Background Switcher Button (Photo / Camera / Themes)
                    IconButton(
                        onClick = { showBackgroundDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("gameplay_background_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Background",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Pause Button
                    IconButton(
                        onClick = onPauseClicked,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Central Game Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BubbleCanvasRenderer(
                    uiState = uiState,
                    onAimTouch = onAimTouch,
                    onCanvasSizeChanged = onCanvasSizeChanged,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Controls Area (Launcher swap and powerups)
            LauncherControls(
                nextBubble = uiState.nextBubble,
                powerUpInventory = uiState.powerUpInventory,
                activePowerUp = uiState.activePowerUp,
                shotsUntilDrop = uiState.shotsUntilDrop,
                onSwapClicked = onSwapClicked,
                onPowerUpClicked = onPowerUpClicked,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Quick Background Switcher Dialog
        if (showBackgroundDialog) {
            BackgroundSelectorDialog(
                currentMode = uiState.backgroundMode,
                customPhotoPath = uiState.customPhotoPath,
                onModeSelected = onBackgroundModeChanged,
                onPhotoSelected = onCustomPhotoSelected,
                onDismiss = { showBackgroundDialog = false }
            )
        }

        // Overlays
        when (uiState.status) {
            GameStatus.PAUSED -> {
                PauseOverlay(
                    onResume = onResumeClicked,
                    onRestart = onRestartClicked,
                    onMainMenu = onMainMenuClicked
                )
            }
            GameStatus.VICTORY -> {
                VictoryOverlay(
                    score = uiState.score,
                    highScore = uiState.highScore,
                    stars = uiState.stars,
                    level = uiState.currentLevel,
                    onNextLevel = onNextLevelClicked,
                    onMainMenu = onMainMenuClicked
                )
            }
            GameStatus.DEFEAT -> {
                DefeatOverlay(
                    score = uiState.score,
                    level = uiState.currentLevel,
                    onRetry = onRestartClicked,
                    onMainMenu = onMainMenuClicked
                )
            }
            else -> {
                // Active gameplay
            }
        }
    }
}
