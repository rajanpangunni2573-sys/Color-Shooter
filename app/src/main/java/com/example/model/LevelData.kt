package com.example.model

data class LevelData(
    val id: Int,
    val name: String,
    val baseCols: Int = 8,
    val allowedColors: List<BubbleColor>,
    val initialRows: List<List<BubbleColor?>>,
    val dropIntervalShots: Int = 5,
    val star1Score: Int = 1000,
    val star2Score: Int = 2500,
    val star3Score: Int = 4500,
    val startingPowerUps: Map<PowerUpType, Int> = mapOf(
        PowerUpType.BOMB to 2,
        PowerUpType.FIREBALL to 1,
        PowerUpType.RAINBOW to 2
    )
)
