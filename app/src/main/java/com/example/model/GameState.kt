package com.example.model

import androidx.compose.ui.geometry.Offset

enum class ScreenState {
    MAIN_MENU,
    LEVEL_SELECT,
    GAMEPLAY,
    SETTINGS
}

enum class GameStatus {
    READY,
    AIMING,
    FIRING,
    RESOLVING,
    PAUSED,
    VICTORY,
    DEFEAT
}

data class Projectile(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val bubble: Bubble
)

data class FallingBubble(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val bubble: Bubble,
    val alpha: Float = 1f
)

data class FloatingText(
    val id: Long = System.nanoTime(),
    val text: String,
    val x: Float,
    val y: Float,
    val alpha: Float = 1f,
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFFFFD700)
)

data class Particle(
    val id: Long = System.nanoTime(),
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: androidx.compose.ui.graphics.Color,
    val alpha: Float = 1f,
    val life: Float = 1f
)

data class AimGuide(
    val segments: List<Pair<Offset, Offset>> = emptyList(),
    val targetImpact: Offset? = null,
    val ghostCoord: GridCoord? = null
)

data class GameUiState(
    val screen: ScreenState = ScreenState.MAIN_MENU,
    val status: GameStatus = GameStatus.READY,
    val currentLevel: LevelData? = null,
    val score: Int = 0,
    val highScore: Int = 0,
    val stars: Int = 0,
    val shotsFiredTotal: Int = 0,
    val shotsUntilDrop: Int = 5,
    val grid: Map<GridCoord, Bubble> = emptyMap(),
    val maxRowsVisible: Int = 13,
    val launcherAngleDegrees: Float = -90f,
    val currentBubble: Bubble = Bubble(),
    val nextBubble: Bubble = Bubble(),
    val activeProjectile: Projectile? = null,
    val fallingBubbles: List<FallingBubble> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val floatingTexts: List<FloatingText> = emptyList(),
    val aimGuide: AimGuide = AimGuide(),
    val powerUpInventory: Map<PowerUpType, Int> = emptyMap(),
    val activePowerUp: PowerUpType = PowerUpType.NONE,
    val isAiming: Boolean = false,
    val highestLevelUnlocked: Int = 1,
    val soundFxEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val dangerLineRow: Int = 11,
    val backgroundMode: BackgroundMode = BackgroundMode.SWEET_CANDY,
    val customPhotoPath: String? = null
)
