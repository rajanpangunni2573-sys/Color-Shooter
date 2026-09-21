package com.example.model

import androidx.compose.ui.graphics.Color

enum class BubbleColor(
    val displayName: String,
    val primaryColor: Color,
    val lightColor: Color,
    val darkColor: Color
) {
    RED(
        displayName = "Red",
        primaryColor = Color(0xFFEF4444),
        lightColor = Color(0xFFFCA5A5),
        darkColor = Color(0xFF991B1B)
    ),
    BLUE(
        displayName = "Blue",
        primaryColor = Color(0xFF3B82F6),
        lightColor = Color(0xFF93C5FD),
        darkColor = Color(0xFF1E40AF)
    ),
    GREEN(
        displayName = "Green",
        primaryColor = Color(0xFF10B981),
        lightColor = Color(0xFF6EE7B7),
        darkColor = Color(0xFF065F46)
    ),
    YELLOW(
        displayName = "Yellow",
        primaryColor = Color(0xFFF59E0B),
        lightColor = Color(0xFFFDE68A),
        darkColor = Color(0xFF92400E)
    ),
    PURPLE(
        displayName = "Purple",
        primaryColor = Color(0xFF8B5CF6),
        lightColor = Color(0xFFC4B5FD),
        darkColor = Color(0xFF5B21B6)
    ),
    CYAN(
        displayName = "Cyan",
        primaryColor = Color(0xFF06B6D4),
        lightColor = Color(0xFF67E8F9),
        darkColor = Color(0xFF155E75)
    );

    companion object {
        fun random(allowed: List<BubbleColor>): BubbleColor {
            return if (allowed.isNotEmpty()) allowed.random() else RED
        }
    }
}
