package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Bubble
import com.example.model.PowerUpType

@Composable
fun LauncherControls(
    nextBubble: Bubble,
    powerUpInventory: Map<PowerUpType, Int>,
    activePowerUp: PowerUpType,
    shotsUntilDrop: Int,
    onSwapClicked: () -> Unit,
    onPowerUpClicked: (PowerUpType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Next Bubble & Swap Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NEXT",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.dp, Color(0xFF475569), CircleShape)
                    .clickable { onSwapClicked() }
                    .testTag("swap_bubble_button"),
                contentAlignment = Alignment.Center
            ) {
                // Mini canvas for next bubble
                Canvas(modifier = Modifier.size(36.dp)) {
                    val r = size.minDimension / 2f
                    drawBubble(Offset(r, r), r * 0.9f, nextBubble)
                }

                // Swap Icon badge on top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Drop Countdown Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GRID DROP IN",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (shotsUntilDrop <= 2) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF334155).copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        if (shotsUntilDrop <= 2) Color(0xFFEF4444) else Color(0xFF64748B),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$shotsUntilDrop SHOTS",
                    color = if (shotsUntilDrop <= 2) Color(0xFFFCA5A5) else Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Power-Up Trays (Bomb, Fireball, Rainbow)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PowerUpButton(
                type = PowerUpType.BOMB,
                count = powerUpInventory[PowerUpType.BOMB] ?: 0,
                isActive = activePowerUp == PowerUpType.BOMB,
                onClick = { onPowerUpClicked(PowerUpType.BOMB) }
            )
            PowerUpButton(
                type = PowerUpType.FIREBALL,
                count = powerUpInventory[PowerUpType.FIREBALL] ?: 0,
                isActive = activePowerUp == PowerUpType.FIREBALL,
                onClick = { onPowerUpClicked(PowerUpType.FIREBALL) }
            )
            PowerUpButton(
                type = PowerUpType.RAINBOW,
                count = powerUpInventory[PowerUpType.RAINBOW] ?: 0,
                isActive = activePowerUp == PowerUpType.RAINBOW,
                onClick = { onPowerUpClicked(PowerUpType.RAINBOW) }
            )
        }
    }
}

@Composable
private fun PowerUpButton(
    type: PowerUpType,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val (icon, bgBrush, borderCol) = when (type) {
        PowerUpType.BOMB -> Triple(
            Icons.Default.Power,
            Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF0F172A))),
            if (isActive) Color(0xFFF97316) else Color(0xFF475569)
        )
        PowerUpType.FIREBALL -> Triple(
            Icons.Default.LocalFireDepartment,
            Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFF7C2D12))),
            if (isActive) Color(0xFFFDE047) else Color(0xFFEA580C)
        )
        PowerUpType.RAINBOW -> Triple(
            Icons.Default.Waves,
            Brush.sweepGradient(listOf(Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF8B5CF6))),
            if (isActive) Color.White else Color(0xFFC084FC)
        )
        PowerUpType.NONE -> Triple(Icons.Default.SwapHoriz, Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)), Color.Transparent)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush)
            .border(if (isActive) 2.5.dp else 1.dp, borderCol, RoundedCornerShape(12.dp))
            .clickable(enabled = count > 0) { onClick() }
            .testTag("power_up_${type.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.title,
            tint = if (count > 0) Color.White else Color.Gray,
            modifier = Modifier.size(22.dp)
        )

        // Count badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(if (count > 0) Color(0xFF38BDF8) else Color(0xFF475569)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
