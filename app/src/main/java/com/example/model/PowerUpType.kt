package com.example.model

import androidx.compose.ui.graphics.Color

enum class PowerUpType(
    val title: String,
    val description: String,
    val primaryColor: Color,
    val accentColor: Color
) {
    NONE(
        title = "Normal",
        description = "Standard colored bubble",
        primaryColor = Color.Transparent,
        accentColor = Color.Transparent
    ),
    BOMB(
        title = "Bomb",
        description = "Explodes nearby bubbles within 2 grid rings",
        primaryColor = Color(0xFF1E293B),
        accentColor = Color(0xFFF97316)
    ),
    FIREBALL(
        title = "Fireball",
        description = "Blasts straight through all bubbles in its line of flight",
        primaryColor = Color(0xFFEA580C),
        accentColor = Color(0xFFFDE047)
    ),
    RAINBOW(
        title = "Rainbow",
        description = "Matches any bubble color it touches to form clusters",
        primaryColor = Color(0xFFEC4899),
        accentColor = Color(0xFF06B6D4)
    )
}
