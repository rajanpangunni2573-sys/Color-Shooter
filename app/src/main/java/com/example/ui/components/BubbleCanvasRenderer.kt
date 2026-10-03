package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.example.game.HexGridMath
import com.example.model.Bubble
import com.example.model.GameStatus
import com.example.model.GameUiState
import com.example.model.PowerUpType

@Composable
fun BubbleCanvasRenderer(
    uiState: GameUiState,
    onAimTouch: (touchX: Float, touchY: Float, isRelease: Boolean) -> Unit,
    onCanvasSizeChanged: (width: Float, height: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                onCanvasSizeChanged(size.width.toFloat(), size.height.toFloat())
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var lastX = down.position.x
                    var lastY = down.position.y

                    onAimTouch(lastX, lastY, false)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || !change.pressed) {
                            // Finger released! Fire bubble towards the aimed position
                            onAimTouch(lastX, lastY, true)
                            break
                        } else {
                            lastX = change.position.x
                            lastY = change.position.y
                            change.consume()
                            onAimTouch(lastX, lastY, false)
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val baseCols = uiState.currentLevel?.baseCols ?: 8
        val radius = width / (baseCols * 2f)
        val gridStartY = 120f
        val launcherPos = Offset(width / 2f, height - radius * 2.8f)

        // 1. Draw Ceiling & Danger Line
        drawCeiling(width, gridStartY, radius)
        drawDangerLine(width, gridStartY, radius, uiState.dangerLineRow, uiState.grid.keys.maxOfOrNull { it.row } ?: 0)

        // 2. Draw Aiming Guideline
        if (uiState.isAiming || uiState.status == GameStatus.AIMING) {
            drawAimGuide(uiState, radius, gridStartY, baseCols)
        }

        // 3. Draw Grid Bubbles
        for ((coord, bubble) in uiState.grid) {
            val center = HexGridMath.getBubbleCenter(coord.row, coord.col, radius, 0f, gridStartY)
            drawBubble(center, radius, bubble)
        }

        // 4. Draw Falling / Orphan Bubbles
        for (fb in uiState.fallingBubbles) {
            drawBubble(
                center = Offset(fb.x, fb.y),
                radius = radius,
                bubble = fb.bubble,
                alpha = fb.alpha
            )
        }

        // 5. Draw Active Projectile
        uiState.activeProjectile?.let { proj ->
            drawBubble(
                center = Offset(proj.x, proj.y),
                radius = radius,
                bubble = proj.bubble
            )
            // Trailing glow
            drawCircle(
                color = proj.bubble.color.lightColor.copy(alpha = 0.35f),
                radius = radius * 1.35f,
                center = Offset(proj.x, proj.y)
            )
        }

        // 6. Draw Launcher Pointer / Cannon Barrel
        drawLauncherBarrel(launcherPos, uiState.launcherAngleDegrees, radius)

        // Draw Loaded Current Bubble inside launcher
        if (uiState.status != GameStatus.FIRING) {
            drawBubble(launcherPos, radius, uiState.currentBubble)
        }

        // 7. Draw Burst Particles
        for (p in uiState.particles) {
            drawCircle(
                color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                radius = p.radius,
                center = Offset(p.x, p.y)
            )
        }

        // 8. Draw Floating Score Text
        for (ft in uiState.floatingTexts) {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (ft.alpha.coerceIn(0f, 1f) * 255).toInt(),
                        255, 220, 40
                    )
                    textSize = 42f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
                }
                drawText(ft.text, ft.x, ft.y, paint)
            }
        }
    }
}

private fun DrawScope.drawCeiling(width: Float, gridStartY: Float, radius: Float) {
    // Top steel ceiling bar
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
            startY = 0f,
            endY = gridStartY + radius
        ),
        topLeft = Offset(0f, 0f),
        size = Size(width, gridStartY + radius)
    )
    // Ceiling border rivet line
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(0f, gridStartY + radius),
        end = Offset(width, gridStartY + radius),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawDangerLine(
    width: Float,
    gridStartY: Float,
    radius: Float,
    dangerRow: Int,
    lowestRow: Int
) {
    val dangerY = gridStartY + radius + dangerRow * HexGridMath.getRowHeight(radius)
    val isNearDanger = lowestRow >= dangerRow - 2
    val lineColor = if (isNearDanger) Color(0xFFEF4444) else Color(0x66EF4444)

    drawLine(
        color = lineColor,
        start = Offset(0f, dangerY),
        end = Offset(width, dangerY),
        strokeWidth = if (isNearDanger) 3.5f else 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
    )
}

private fun DrawScope.drawAimGuide(
    uiState: GameUiState,
    radius: Float,
    gridStartY: Float,
    baseCols: Int
) {
    val aimGuide = uiState.aimGuide
    val aimColor = Color.White.copy(alpha = 0.75f)

    for ((start, end) in aimGuide.segments) {
        drawLine(
            color = aimColor,
            start = start,
            end = end,
            strokeWidth = 3.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 14f), 0f),
            cap = StrokeCap.Round
        )
    }

    // Draw ghost bubble at projected snap coordinate
    aimGuide.ghostCoord?.let { ghost ->
        val ghostCenter = HexGridMath.getBubbleCenter(ghost.row, ghost.col, radius, 0f, gridStartY)
        drawCircle(
            color = uiState.currentBubble.color.primaryColor.copy(alpha = 0.35f),
            radius = radius,
            center = ghostCenter
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = radius,
            center = ghostCenter,
            style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )
    }
}

private fun DrawScope.drawLauncherBarrel(launcherPos: Offset, angleDegrees: Float, radius: Float) {
    val radians = Math.toRadians(angleDegrees.toDouble()).toFloat()
    val barrelLength = radius * 2.2f
    val barrelEnd = Offset(
        launcherPos.x + kotlin.math.cos(radians) * barrelLength,
        launcherPos.y + kotlin.math.sin(radians) * barrelLength
    )

    // Base pedestal
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF475569), Color(0xFF0F172A)),
            center = launcherPos,
            radius = radius * 1.5f
        ),
        radius = radius * 1.35f,
        center = launcherPos
    )
    drawCircle(
        color = Color(0xFF94A3B8),
        radius = radius * 1.35f,
        center = launcherPos,
        style = Stroke(width = 2.5f)
    )

    // Directional aimer barrel
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF94A3B8), Color(0xFF38BDF8)),
            start = launcherPos,
            end = barrelEnd
        ),
        start = launcherPos,
        end = barrelEnd,
        strokeWidth = radius * 0.45f,
        cap = StrokeCap.Round
    )
}

fun DrawScope.drawBubble(
    center: Offset,
    radius: Float,
    bubble: Bubble,
    alpha: Float = 1f
) {
    if (alpha <= 0f) return

    val primary = bubble.color.primaryColor.copy(alpha = alpha)
    val light = bubble.color.lightColor.copy(alpha = alpha)
    val dark = bubble.color.darkColor.copy(alpha = alpha)

    when (bubble.powerUp) {
        PowerUpType.BOMB -> {
            // Metallic dark bomb sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF64748B).copy(alpha = alpha), Color(0xFF0F172A).copy(alpha = alpha)),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                    radius = radius * 1.2f
                ),
                radius = radius,
                center = center
            )
            // Orange warning ring
            drawCircle(
                color = Color(0xFFF97316).copy(alpha = alpha),
                radius = radius * 0.55f,
                center = center,
                style = Stroke(width = 3.5f)
            )
            // Central core
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = alpha),
                radius = radius * 0.28f,
                center = center
            )
        }
        PowerUpType.FIREBALL -> {
            // Blazing fireball
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFEF08A).copy(alpha = alpha), Color(0xFFEA580C).copy(alpha = alpha)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color(0xFFFDE047).copy(alpha = alpha),
                radius = radius * 0.45f,
                center = center
            )
        }
        PowerUpType.RAINBOW -> {
            // Rainbow sphere with multi-color radial sweep
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFEF4444).copy(alpha = alpha),
                        Color(0xFFF59E0B).copy(alpha = alpha),
                        Color(0xFF10B981).copy(alpha = alpha),
                        Color(0xFF06B6D4).copy(alpha = alpha),
                        Color(0xFF8B5CF6).copy(alpha = alpha),
                        Color(0xFFEF4444).copy(alpha = alpha)
                    ),
                    center = center
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f * alpha),
                radius = radius * 0.35f,
                center = center
            )
        }
        PowerUpType.NONE -> {
            // 3D Sphere appearance
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(light, primary, dark),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.35f),
                    radius = radius * 1.3f
                ),
                radius = radius * 0.96f,
                center = center
            )

            // Glossy highlight reflection arc/dot
            drawCircle(
                color = Color.White.copy(alpha = 0.65f * alpha),
                radius = radius * 0.22f,
                center = Offset(center.x - radius * 0.32f, center.y - radius * 0.35f)
            )

            // Subtle outer border
            drawCircle(
                color = dark.copy(alpha = 0.4f * alpha),
                radius = radius * 0.96f,
                center = center,
                style = Stroke(width = 1.2f)
            )
        }
    }
}
