package com.example.data

import com.example.model.BubbleColor
import com.example.model.LevelData
import com.example.model.PowerUpType

object LevelRepository {

    val levels: List<LevelData> = listOf(
        // Level 1: Beginner / Tutorial level
        LevelData(
            id = 1,
            name = "Morning Meadow",
            baseCols = 8,
            allowedColors = listOf(BubbleColor.RED, BubbleColor.BLUE, BubbleColor.GREEN),
            initialRows = listOf(
                listOf(BubbleColor.RED, BubbleColor.RED, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.RED, BubbleColor.RED),
                listOf(BubbleColor.RED, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.RED, BubbleColor.RED),
                listOf(BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.RED, BubbleColor.RED, BubbleColor.BLUE, BubbleColor.BLUE),
                listOf(BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.RED, BubbleColor.RED, BubbleColor.BLUE, BubbleColor.BLUE)
            ),
            dropIntervalShots = 6,
            star1Score = 800,
            star2Score = 1800,
            star3Score = 3200,
            startingPowerUps = mapOf(
                PowerUpType.BOMB to 2,
                PowerUpType.FIREBALL to 1,
                PowerUpType.RAINBOW to 2
            )
        ),

        // Level 2: Checkerboard
        LevelData(
            id = 2,
            name = "Amber Dunes",
            baseCols = 8,
            allowedColors = listOf(BubbleColor.RED, BubbleColor.BLUE, BubbleColor.YELLOW, BubbleColor.GREEN),
            initialRows = listOf(
                listOf(BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED),
                listOf(BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.BLUE),
                listOf(BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED),
                listOf(BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.BLUE),
                listOf(BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.RED, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.GREEN)
            ),
            dropIntervalShots = 5,
            star1Score = 1200,
            star2Score = 2600,
            star3Score = 4400,
            startingPowerUps = mapOf(
                PowerUpType.BOMB to 2,
                PowerUpType.FIREBALL to 2,
                PowerUpType.RAINBOW to 2
            )
        ),

        // Level 3: Crystal formations
        LevelData(
            id = 3,
            name = "Amethyst Cavern",
            baseCols = 8,
            allowedColors = listOf(BubbleColor.PURPLE, BubbleColor.BLUE, BubbleColor.CYAN, BubbleColor.YELLOW),
            initialRows = listOf(
                listOf(BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.PURPLE),
                listOf(BubbleColor.BLUE, BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.CYAN, BubbleColor.PURPLE, BubbleColor.BLUE),
                listOf(BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.BLUE),
                listOf(BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.PURPLE, BubbleColor.YELLOW, BubbleColor.CYAN, BubbleColor.PURPLE),
                listOf(BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.CYAN, BubbleColor.CYAN),
                listOf(BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.YELLOW, BubbleColor.YELLOW)
            ),
            dropIntervalShots = 5,
            star1Score = 1500,
            star2Score = 3200,
            star3Score = 5500,
            startingPowerUps = mapOf(
                PowerUpType.BOMB to 3,
                PowerUpType.FIREBALL to 2,
                PowerUpType.RAINBOW to 3
            )
        ),

        // Level 4: Waves
        LevelData(
            id = 4,
            name = "Coral Reef",
            baseCols = 8,
            allowedColors = listOf(BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.GREEN),
            initialRows = listOf(
                listOf(BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.CYAN, BubbleColor.BLUE),
                listOf(BubbleColor.GREEN, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.GREEN, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.GREEN),
                listOf(BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.RED, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.YELLOW),
                listOf(BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.RED),
                listOf(BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.BLUE),
                listOf(BubbleColor.RED, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.CYAN)
            ),
            dropIntervalShots = 4,
            star1Score = 2000,
            star2Score = 4000,
            star3Score = 6500,
            startingPowerUps = mapOf(
                PowerUpType.BOMB to 3,
                PowerUpType.FIREBALL to 2,
                PowerUpType.RAINBOW to 2
            )
        ),

        // Level 5: High density
        LevelData(
            id = 5,
            name = "Neon Metropolis",
            baseCols = 8,
            allowedColors = listOf(BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.GREEN),
            initialRows = listOf(
                listOf(BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.RED),
                listOf(BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.GREEN, BubbleColor.CYAN, BubbleColor.PURPLE),
                listOf(BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.YELLOW),
                listOf(BubbleColor.RED, BubbleColor.RED, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.CYAN),
                listOf(BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.RED, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.PURPLE, BubbleColor.PURPLE),
                listOf(BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.RED, BubbleColor.GREEN),
                listOf(BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.RED)
            ),
            dropIntervalShots = 4,
            star1Score = 2500,
            star2Score = 5200,
            star3Score = 8000,
            startingPowerUps = mapOf(
                PowerUpType.BOMB to 3,
                PowerUpType.FIREBALL to 3,
                PowerUpType.RAINBOW to 3
            )
        ),

        // Level 6: Master challenge
        LevelData(
            id = 6,
            name = "Solar Flare",
            baseCols = 8,
            allowedColors = BubbleColor.values().toList(),
            initialRows = listOf(
                listOf(BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.RED, BubbleColor.YELLOW),
                listOf(BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.PURPLE, BubbleColor.CYAN, BubbleColor.BLUE),
                listOf(BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.RED, BubbleColor.RED),
                listOf(BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.CYAN),
                listOf(BubbleColor.RED, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.PURPLE, BubbleColor.PURPLE, BubbleColor.BLUE, BubbleColor.BLUE),
                listOf(BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.RED, BubbleColor.RED, BubbleColor.YELLOW, BubbleColor.YELLOW, BubbleColor.PURPLE),
                listOf(BubbleColor.CYAN, BubbleColor.CYAN, BubbleColor.BLUE, BubbleColor.BLUE, BubbleColor.GREEN, BubbleColor.GREEN, BubbleColor.RED, BubbleColor.RED)
            ),
            dropIntervalShots = 3,
            star1Score = 3000,
            star2Score = 6500,
            star3Score = 10000,
            startingPowerUps = mapOf(
                PowerUpType.BOMB to 4,
                PowerUpType.FIREBALL to 3,
                PowerUpType.RAINBOW to 4
            )
        )
    )

    fun getLevel(id: Int): LevelData {
        return levels.find { it.id == id } ?: levels.first()
    }
}
