package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameStatus
import com.example.model.ScreenState
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.GameViewModel

@Composable
fun BubbleShooterApp(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Android Lifecycle listener: pause game automatically when backgrounded
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onLifecyclePause()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onLifecyclePause()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onLifecycleResume()
    }

    // System Back Handler
    BackHandler(enabled = true) {
        when (uiState.screen) {
            ScreenState.MAIN_MENU -> {
                // Let system handle exit
            }
            ScreenState.LEVEL_SELECT -> {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            ScreenState.SETTINGS -> {
                viewModel.navigateTo(ScreenState.MAIN_MENU)
            }
            ScreenState.GAMEPLAY -> {
                if (uiState.status == GameStatus.READY || uiState.status == GameStatus.AIMING) {
                    viewModel.pauseGame()
                } else if (uiState.status == GameStatus.PAUSED) {
                    viewModel.resumeGame()
                } else {
                    viewModel.navigateTo(ScreenState.LEVEL_SELECT)
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        when (uiState.screen) {
            ScreenState.MAIN_MENU -> {
                MainMenuScreen(
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
            ScreenState.LEVEL_SELECT -> {
                LevelSelectScreen(
                    highestUnlockedLevel = uiState.highestLevelUnlocked,
                    onLevelSelected = { levelId -> viewModel.startLevel(levelId) },
                    onBack = { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                )
            }
            ScreenState.SETTINGS -> {
                SettingsScreen(
                    soundFxEnabled = uiState.soundFxEnabled,
                    musicEnabled = uiState.musicEnabled,
                    onSoundFxToggled = { enabled -> viewModel.setSoundFxEnabled(enabled) },
                    onMusicToggled = { enabled -> viewModel.setMusicEnabled(enabled) },
                    onClearData = { viewModel.clearAllData() },
                    onBack = { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                )
            }
            ScreenState.GAMEPLAY -> {
                GameplayScreen(
                    uiState = uiState,
                    onAimTouch = { touchX, touchY, isRelease ->
                        viewModel.onAimTouch(touchX, touchY, isRelease)
                    },
                    onCanvasSizeChanged = { width, height ->
                        viewModel.setCanvasDimensions(width, height)
                    },
                    onSwapClicked = { viewModel.swapBubbles() },
                    onPowerUpClicked = { type -> viewModel.activatePowerUp(type) },
                    onPauseClicked = { viewModel.pauseGame() },
                    onResumeClicked = { viewModel.resumeGame() },
                    onRestartClicked = { viewModel.restartCurrentLevel() },
                    onNextLevelClicked = { viewModel.nextLevel() },
                    onMainMenuClicked = { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                )
            }
        }
    }
}
