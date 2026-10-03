package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GamePreferencesRepository
import com.example.data.LevelRepository
import com.example.game.HexGridMath
import com.example.game.SoundEffects
import com.example.model.AimGuide
import com.example.model.Bubble
import com.example.model.BubbleColor
import com.example.model.FallingBubble
import com.example.model.FloatingText
import com.example.model.GameStatus
import com.example.model.GameUiState
import com.example.model.GridCoord
import com.example.model.LevelData
import com.example.model.Particle
import com.example.model.PowerUpType
import com.example.model.Projectile
import com.example.model.ScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = GamePreferencesRepository(application)
    val soundEffects = SoundEffects()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var canvasWidth: Float = 1080f
    private var canvasHeight: Float = 1920f
    private var bubbleRadius: Float = 45f
    private var gridStartX: Float = 0f
    private var gridStartY: Float = 100f
    private val dangerLineRow = 11

    private val projectileSpeed = 2200f
    private val gravity = 1500f

    init {
        // Observe sound and persistence settings
        viewModelScope.launch {
            preferencesRepository.soundFxEnabled.collect { enabled ->
                soundEffects.isSfxEnabled = enabled
                _uiState.update { it.copy(soundFxEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.musicEnabled.collect { enabled ->
                soundEffects.isMusicEnabled = enabled
                _uiState.update { it.copy(musicEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.highestUnlockedLevel.collect { unlocked ->
                _uiState.update { it.copy(highestLevelUnlocked = unlocked) }
            }
        }
        startGameLoop()
    }

    fun setCanvasDimensions(width: Float, height: Float) {
        if (width <= 0 || height <= 0) return
        canvasWidth = width
        canvasHeight = height
        val baseCols = _uiState.value.currentLevel?.baseCols ?: 8
        bubbleRadius = (width / (baseCols * 2f)).coerceAtLeast(20f)
        gridStartX = 0f
        gridStartY = 120f
        updateAimGuide()
    }

    fun navigateTo(screen: ScreenState) {
        soundEffects.playClick()
        if (screen == ScreenState.GAMEPLAY && _uiState.value.currentLevel == null) {
            startLevel(1)
        } else {
            _uiState.update { it.copy(screen = screen) }
        }
    }

    fun startLevel(levelId: Int) {
        soundEffects.playClick()
        val level = LevelRepository.getLevel(levelId)

        viewModelScope.launch {
            val high = preferencesRepository.getLevelHighScore(levelId).first()

            val initialGrid = mutableMapOf<GridCoord, Bubble>()
            for ((rowIndex, rowColors) in level.initialRows.withIndex()) {
                for ((colIndex, color) in rowColors.withIndex()) {
                    if (color != null) {
                        initialGrid[GridCoord(rowIndex, colIndex)] = Bubble(
                            id = System.nanoTime() + rowIndex * 100 + colIndex,
                            color = color
                        )
                    }
                }
            }

            val curBubble = Bubble(color = BubbleColor.random(level.allowedColors))
            val nextBubble = Bubble(color = BubbleColor.random(level.allowedColors))

            _uiState.update {
                it.copy(
                    screen = ScreenState.GAMEPLAY,
                    status = GameStatus.READY,
                    currentLevel = level,
                    score = 0,
                    highScore = high,
                    stars = 0,
                    shotsFiredTotal = 0,
                    shotsUntilDrop = level.dropIntervalShots,
                    grid = initialGrid,
                    launcherAngleDegrees = -90f,
                    currentBubble = curBubble,
                    nextBubble = nextBubble,
                    activeProjectile = null,
                    fallingBubbles = emptyList(),
                    particles = emptyList(),
                    floatingTexts = emptyList(),
                    powerUpInventory = level.startingPowerUps,
                    activePowerUp = PowerUpType.NONE,
                    isAiming = false,
                    dangerLineRow = dangerLineRow
                )
            }
            updateAimGuide()
        }
    }

    fun restartCurrentLevel() {
        soundEffects.playClick()
        _uiState.value.currentLevel?.let {
            startLevel(it.id)
        }
    }

    fun nextLevel() {
        soundEffects.playClick()
        val nextId = (_uiState.value.currentLevel?.id ?: 1) + 1
        if (nextId <= LevelRepository.levels.size) {
            startLevel(nextId)
        } else {
            navigateTo(ScreenState.LEVEL_SELECT)
        }
    }

    fun pauseGame() {
        soundEffects.playClick()
        _uiState.update { it.copy(status = GameStatus.PAUSED) }
    }

    fun resumeGame() {
        soundEffects.playClick()
        _uiState.update { it.copy(status = GameStatus.READY) }
    }

    fun onLifecyclePause() {
        soundEffects.stopMusic()
        if (_uiState.value.screen == ScreenState.GAMEPLAY &&
            (_uiState.value.status == GameStatus.READY || _uiState.value.status == GameStatus.AIMING)
        ) {
            _uiState.update { it.copy(status = GameStatus.PAUSED) }
        }
    }

    fun onLifecycleResume() {
        if (_uiState.value.musicEnabled) {
            soundEffects.startMusic()
        }
    }

    fun swapBubbles() {
        val state = _uiState.value
        if (state.status != GameStatus.READY && state.status != GameStatus.AIMING) return
        soundEffects.playClick()
        val temp = state.currentBubble
        _uiState.update {
            it.copy(
                currentBubble = it.nextBubble,
                nextBubble = temp
            )
        }
        updateAimGuide()
    }

    fun activatePowerUp(type: PowerUpType) {
        val state = _uiState.value
        if (state.status != GameStatus.READY && state.status != GameStatus.AIMING) return
        val count = state.powerUpInventory[type] ?: 0
        if (count <= 0) return

        soundEffects.playClick()
        if (state.activePowerUp == type) {
            // Deactivate
            _uiState.update {
                it.copy(
                    activePowerUp = PowerUpType.NONE,
                    currentBubble = it.currentBubble.copy(powerUp = PowerUpType.NONE)
                )
            }
        } else {
            // Activate power-up
            val newInventory = state.powerUpInventory.toMutableMap()
            newInventory[type] = count - 1

            _uiState.update {
                it.copy(
                    powerUpInventory = newInventory,
                    activePowerUp = type,
                    currentBubble = it.currentBubble.copy(powerUp = type)
                )
            }
        }
        updateAimGuide()
    }

    fun onAimTouch(touchX: Float, touchY: Float, isRelease: Boolean) {
        val state = _uiState.value
        if (state.status != GameStatus.READY && state.status != GameStatus.AIMING) return

        val launcherPos = getLauncherPosition()
        val dx = touchX - launcherPos.x
        val dy = touchY - launcherPos.y

        // If user drags below the launcher, treat as cancel
        if (dy >= -20f) {
            if (isRelease) {
                _uiState.update {
                    it.copy(
                        isAiming = false,
                        status = GameStatus.READY,
                        aimGuide = AimGuide()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isAiming = false,
                        aimGuide = AimGuide()
                    )
                }
            }
            return
        }

        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        // Clamp angle between -168 and -12 degrees (upwards trajectory)
        angle = angle.coerceIn(-168f, -12f)

        if (isRelease) {
            fireBubble(angle)
        } else {
            _uiState.update {
                it.copy(
                    launcherAngleDegrees = angle,
                    isAiming = true,
                    status = GameStatus.AIMING
                )
            }
            updateAimGuide()
        }
    }

    private fun fireBubble(angle: Float) {
        val state = _uiState.value
        val launcherPos = getLauncherPosition()
        val radians = Math.toRadians(angle.toDouble()).toFloat()
        val vx = cos(radians) * projectileSpeed
        val vy = sin(radians) * projectileSpeed

        val bubbleToFire = state.currentBubble

        soundEffects.playLaunch()

        // Pick next bubble based on available colors in the remaining grid
        val remainingColors = state.grid.values.map { it.color }.distinct()
        val colorPool = if (remainingColors.isNotEmpty()) remainingColors else state.currentLevel?.allowedColors ?: listOf(BubbleColor.RED)
        val newNext = Bubble(color = BubbleColor.random(colorPool))

        _uiState.update {
            it.copy(
                status = GameStatus.FIRING,
                isAiming = false,
                launcherAngleDegrees = angle,
                activeProjectile = Projectile(
                    x = launcherPos.x,
                    y = launcherPos.y,
                    vx = vx,
                    vy = vy,
                    bubble = bubbleToFire
                ),
                currentBubble = it.nextBubble,
                nextBubble = newNext,
                activePowerUp = PowerUpType.NONE,
                shotsFiredTotal = it.shotsFiredTotal + 1,
                aimGuide = AimGuide()
            )
        }
    }

    private fun updateAimGuide() {
        val state = _uiState.value
        if (canvasWidth <= 0 || canvasHeight <= 0) return
        val launcherPos = getLauncherPosition()
        val (segments, impact) = HexGridMath.calculateAimTrajectory(
            origin = launcherPos,
            angleDegrees = state.launcherAngleDegrees,
            bubbleRadius = bubbleRadius,
            screenWidth = canvasWidth,
            gridStartX = gridStartX,
            gridStartY = gridStartY,
            maxRows = state.maxRowsVisible,
            baseCols = state.currentLevel?.baseCols ?: 8,
            grid = state.grid
        )

        val ghostCoord = impact?.let {
            HexGridMath.findSnapCoord(
                projectilePos = it,
                radius = bubbleRadius,
                gridStartX = gridStartX,
                gridStartY = gridStartY,
                maxRows = state.maxRowsVisible,
                baseCols = state.currentLevel?.baseCols ?: 8,
                occupiedGrid = state.grid
            )
        }

        _uiState.update {
            it.copy(
                aimGuide = AimGuide(
                    segments = segments,
                    targetImpact = impact,
                    ghostCoord = ghostCoord
                )
            )
        }
    }

    private fun getLauncherPosition(): Offset {
        return Offset(canvasWidth / 2f, canvasHeight - bubbleRadius * 2.8f)
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                updatePhysics(dt)
                delay(16) // ~60 FPS
            }
        }
    }

    private fun updatePhysics(dt: Float) {
        val state = _uiState.value
        if (state.status == GameStatus.PAUSED) return

        // 1. Update Projectile
        val proj = state.activeProjectile
        if (proj != null) {
            updateProjectile(proj, dt)
        }

        // 2. Update Falling Bubbles
        if (state.fallingBubbles.isNotEmpty()) {
            val updatedFalling = state.fallingBubbles.mapNotNull { fb ->
                val newVy = fb.vy + gravity * dt
                val newX = fb.x + fb.vx * dt
                val newY = fb.y + newVy * dt
                val newAlpha = fb.alpha - 0.75f * dt
                if (newY < canvasHeight + 100f && newAlpha > 0f) {
                    fb.copy(x = newX, y = newY, vy = newVy, alpha = newAlpha)
                } else {
                    null
                }
            }
            _uiState.update { it.copy(fallingBubbles = updatedFalling) }
        }

        // 3. Update Particles
        if (state.particles.isNotEmpty()) {
            val updatedParticles = state.particles.mapNotNull { p ->
                val newLife = p.life - dt * 2.2f
                if (newLife > 0f) {
                    p.copy(
                        x = p.x + p.vx * dt,
                        y = p.y + p.vy * dt,
                        life = newLife,
                        alpha = newLife
                    )
                } else null
            }
            _uiState.update { it.copy(particles = updatedParticles) }
        }

        // 4. Update Floating Score Texts
        if (state.floatingTexts.isNotEmpty()) {
            val updatedTexts = state.floatingTexts.mapNotNull { ft ->
                val newAlpha = ft.alpha - dt * 0.9f
                if (newAlpha > 0f) {
                    ft.copy(y = ft.y - 70f * dt, alpha = newAlpha)
                } else null
            }
            _uiState.update { it.copy(floatingTexts = updatedTexts) }
        }
    }

    private fun updateProjectile(proj: Projectile, dt: Float) {
        val substeps = 4
        val subDt = dt / substeps.toFloat()

        var curX = proj.x
        var curY = proj.y
        var curVx = proj.vx
        var curVy = proj.vy

        val leftBound = bubbleRadius
        val rightBound = canvasWidth - bubbleRadius
        val ceilingY = gridStartY + bubbleRadius

        for (step in 0 until substeps) {
            curX += curVx * subDt
            curY += curVy * subDt

            // Wall bounce
            if (curX <= leftBound) {
                curX = leftBound
                curVx = kotlin.math.abs(curVx)
                soundEffects.playLaunch()
            } else if (curX >= rightBound) {
                curX = rightBound
                curVx = -kotlin.math.abs(curVx)
                soundEffects.playLaunch()
            }

            val testPos = Offset(curX, curY)
            val state = _uiState.value

            // Special handling for FIREBALL: pierces through bubbles
            if (proj.bubble.powerUp == PowerUpType.FIREBALL) {
                val hitCoords = mutableListOf<GridCoord>()
                for ((coord, _) in state.grid) {
                    val center = HexGridMath.getBubbleCenter(coord.row, coord.col, bubbleRadius, gridStartX, gridStartY)
                    val dx = testPos.x - center.x
                    val dy = testPos.y - center.y
                    if (dx * dx + dy * dy <= (bubbleRadius * 2f) * (bubbleRadius * 2f)) {
                        hitCoords.add(coord)
                    }
                }

                if (hitCoords.isNotEmpty()) {
                    val updatedGrid = state.grid.toMutableMap()
                    for (coord in hitCoords) {
                        updatedGrid.remove(coord)
                        spawnBubblePopParticles(coord)
                    }
                    soundEffects.playBomb()
                    val pts = hitCoords.size * 150
                    spawnFloatingText("+$pts FIRE!", testPos.x, testPos.y)
                    _uiState.update { it.copy(grid = updatedGrid, score = it.score + pts) }
                }

                if (curY <= ceilingY) {
                    onProjectileFinished()
                    return
                }
                continue
            }

            // Standard Bubble & Bomb & Rainbow Collision check
            var collided = false
            if (curY <= ceilingY) {
                collided = true
                curY = ceilingY
            } else {
                // Check collision with any bubble in grid
                val thresholdSq = (bubbleRadius * 1.9f) * (bubbleRadius * 1.9f)
                for ((coord, _) in state.grid) {
                    val center = HexGridMath.getBubbleCenter(coord.row, coord.col, bubbleRadius, gridStartX, gridStartY)
                    val dx = curX - center.x
                    val dy = curY - center.y
                    if (dx * dx + dy * dy <= thresholdSq) {
                        collided = true
                        break
                    }
                }
            }

            if (collided) {
                handleCollision(Offset(curX, curY), proj.bubble)
                return
            }

            // Safety boundary check
            if (curY > canvasHeight + 100f) {
                onProjectileFinished()
                return
            }
        }

        _uiState.update { it.copy(activeProjectile = proj.copy(x = curX, y = curY, vx = curVx, vy = curVy)) }
    }

    private fun handleCollision(impactPos: Offset, bubble: Bubble) {
        val state = _uiState.value
        val baseCols = state.currentLevel?.baseCols ?: 8
        val snapCoord = HexGridMath.findSnapCoord(
            projectilePos = impactPos,
            radius = bubbleRadius,
            gridStartX = gridStartX,
            gridStartY = gridStartY,
            maxRows = state.maxRowsVisible,
            baseCols = baseCols,
            occupiedGrid = state.grid
        )

        if (snapCoord == null) {
            onProjectileFinished()
            return
        }

        val newGrid = state.grid.toMutableMap()

        if (bubble.powerUp == PowerUpType.BOMB) {
            // BOMB EXPLOSION
            soundEffects.playBomb()
            val bombCluster = HexGridMath.findBombCluster(snapCoord, state.grid, state.maxRowsVisible, baseCols)
            for (coord in bombCluster) {
                newGrid.remove(coord)
                spawnBubblePopParticles(coord)
            }
            val pts = (bombCluster.size * 150).coerceAtLeast(300)
            spawnFloatingText("+$pts BOOM!", impactPos.x, impactPos.y)

            // Orphan check after bomb
            val floating = HexGridMath.findFloatingBubbles(newGrid, state.maxRowsVisible, baseCols)
            detachOrphans(newGrid, floating)

            val totalAddedScore = pts + (floating.size * 250)
            val updatedScore = state.score + totalAddedScore
            checkLevelProgress(newGrid, updatedScore, wasMatch = true)
            onProjectileFinished()
            return
        }

        // Attach bubble to grid
        newGrid[snapCoord] = bubble

        // Cluster search
        val matchingCluster = HexGridMath.findMatchingCluster(snapCoord, newGrid, state.maxRowsVisible, baseCols)

        if (matchingCluster.size >= 3) {
            // Match found! Pop them!
            soundEffects.playPop()
            for (coord in matchingCluster) {
                newGrid.remove(coord)
                spawnBubblePopParticles(coord)
            }

            val matchScore = matchingCluster.size * 100
            spawnFloatingText("+$matchScore", impactPos.x, impactPos.y)

            // Orphan check
            val floating = HexGridMath.findFloatingBubbles(newGrid, state.maxRowsVisible, baseCols)
            val orphanScore = detachOrphans(newGrid, floating)

            val totalAdded = matchScore + orphanScore
            val updatedScore = state.score + totalAdded

            checkLevelProgress(newGrid, updatedScore, wasMatch = true)
        } else {
            // No match
            handleMiss(newGrid, state.score)
        }

        onProjectileFinished()
    }

    private fun handleMiss(newGrid: MutableMap<GridCoord, Bubble>, currentScore: Int) {
        val state = _uiState.value
        val shotsRemaining = state.shotsUntilDrop - 1

        if (shotsRemaining <= 0) {
            // Shift grid down!
            shiftGridDown(newGrid)
            val resetDrop = state.currentLevel?.dropIntervalShots ?: 5
            _uiState.update { it.copy(shotsUntilDrop = resetDrop) }
        } else {
            _uiState.update { it.copy(shotsUntilDrop = shotsRemaining) }
        }

        checkLevelProgress(newGrid, currentScore, wasMatch = false)
    }

    private fun shiftGridDown(grid: MutableMap<GridCoord, Bubble>) {
        val baseCols = _uiState.value.currentLevel?.baseCols ?: 8
        val shifted = mutableMapOf<GridCoord, Bubble>()

        // Move all bubbles down by 1 row
        for ((coord, bubble) in grid) {
            shifted[GridCoord(coord.row + 1, coord.col)] = bubble
        }

        // Insert new top row with allowed colors
        val allowed = _uiState.value.currentLevel?.allowedColors ?: listOf(BubbleColor.RED)
        val colCount = HexGridMath.getColCount(0, baseCols)
        for (c in 0 until colCount) {
            shifted[GridCoord(0, c)] = Bubble(
                id = System.nanoTime() + c,
                color = BubbleColor.random(allowed)
            )
        }

        grid.clear()
        grid.putAll(shifted)
        soundEffects.playOrphanDrop()
        spawnFloatingText("GRID SHIFT!", canvasWidth / 2f, gridStartY + bubbleRadius * 2)
    }

    private fun detachOrphans(
        grid: MutableMap<GridCoord, Bubble>,
        floating: Set<GridCoord>
    ): Int {
        if (floating.isEmpty()) return 0
        soundEffects.playOrphanDrop()

        val newFalling = mutableListOf<FallingBubble>()
        for (coord in floating) {
            val bubble = grid.remove(coord) ?: continue
            val center = HexGridMath.getBubbleCenter(coord.row, coord.col, bubbleRadius, gridStartX, gridStartY)
            newFalling.add(
                FallingBubble(
                    id = bubble.id,
                    x = center.x,
                    y = center.y,
                    vx = Random.nextFloat() * 160f - 80f,
                    vy = Random.nextFloat() * 100f + 50f,
                    bubble = bubble
                )
            )
        }

        _uiState.update { it.copy(fallingBubbles = it.fallingBubbles + newFalling) }

        val orphanScore = floating.size * 250
        val centerPoint = HexGridMath.getBubbleCenter(floating.first().row, floating.first().col, bubbleRadius, gridStartX, gridStartY)
        spawnFloatingText("+$orphanScore DROP!", centerPoint.x, centerPoint.y)

        return orphanScore
    }

    private fun checkLevelProgress(
        grid: Map<GridCoord, Bubble>,
        score: Int,
        wasMatch: Boolean
    ) {
        val level = _uiState.value.currentLevel ?: return
        val stars = when {
            score >= level.star3Score -> 3
            score >= level.star2Score -> 2
            score >= level.star1Score -> 1
            else -> 0
        }

        // Check Victory: all bubbles cleared
        if (grid.isEmpty()) {
            soundEffects.playVictory()
            val finalStars = stars.coerceAtLeast(1) // clearing guarantees at least 1 star
            viewModelScope.launch {
                preferencesRepository.saveLevelResult(
                    levelId = level.id,
                    score = score,
                    stars = finalStars,
                    totalLevelsCount = LevelRepository.levels.size
                )
            }
            _uiState.update {
                it.copy(
                    grid = grid,
                    score = score,
                    stars = finalStars,
                    status = GameStatus.VICTORY
                )
            }
            return
        }

        // Check Defeat: any bubble crossed danger line
        val lowestRow = grid.keys.maxOfOrNull { it.row } ?: 0
        if (lowestRow >= dangerLineRow) {
            soundEffects.playDefeat()
            _uiState.update {
                it.copy(
                    grid = grid,
                    score = score,
                    stars = stars,
                    status = GameStatus.DEFEAT
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                grid = grid,
                score = score,
                stars = stars,
                status = GameStatus.READY
            )
        }
        updateAimGuide()
    }

    private fun onProjectileFinished() {
        _uiState.update {
            it.copy(
                activeProjectile = null,
                status = if (it.status == GameStatus.VICTORY || it.status == GameStatus.DEFEAT) it.status else GameStatus.READY
            )
        }
        updateAimGuide()
    }

    private fun spawnBubblePopParticles(coord: GridCoord) {
        val bubble = _uiState.value.grid[coord] ?: return
        val center = HexGridMath.getBubbleCenter(coord.row, coord.col, bubbleRadius, gridStartX, gridStartY)
        val newParticles = mutableListOf<Particle>()

        val numParticles = 12
        for (i in 0 until numParticles) {
            val angle = (2 * Math.PI * i / numParticles) + Random.nextDouble(-0.2, 0.2)
            val speed = Random.nextFloat() * 320f + 140f
            newParticles.add(
                Particle(
                    x = center.x,
                    y = center.y,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    radius = bubbleRadius * Random.nextFloat() * 0.35f + 4f,
                    color = bubble.color.primaryColor,
                    life = 1f
                )
            )
        }
        _uiState.update { it.copy(particles = it.particles + newParticles) }
    }

    private fun spawnFloatingText(text: String, x: Float, y: Float) {
        _uiState.update {
            it.copy(
                floatingTexts = it.floatingTexts + FloatingText(
                    text = text,
                    x = x.coerceIn(80f, canvasWidth - 80f),
                    y = y.coerceIn(120f, canvasHeight - 120f)
                )
            )
        }
    }

    fun setSoundFxEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundFxEnabled(enabled)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setMusicEnabled(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            preferencesRepository.clearAllData()
            soundEffects.playClick()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEffects.stopMusic()
    }
}
