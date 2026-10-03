package com.example.data

import com.example.model.BubbleColor
import com.example.model.LevelData
import com.example.model.PowerUpType

object LevelRepository {

    private val allColors = BubbleColor.values().toList()

    val levels: List<LevelData> = generateLevels()

    fun getLevel(id: Int): LevelData {
        return levels.find { it.id == id } ?: levels.first()
    }

    private fun generateLevels(): List<LevelData> {
        val list = mutableListOf<LevelData>()

        // 10 Distinct Worlds / Zones (10 levels each = 100 total levels)
        val worldThemes = listOf(
            // World 1: 1-10 Sweet Meadow & Pastry Parks
            listOf("Morning Meadow", "Pastry Parkway", "Cupcake Cove", "Amber Dunes", "Donut Drift",
                   "Caramel Cascade", "Lollipop Lagoon", "Gummy Glade", "Macaron Maze", "Taffy Tunnels"),
            // World 2: 11-20 Cotton Confection Cloudlands
            listOf("Cotton Candy Peak", "Marshmallow Mist", "Fudge Foothills", "Ice Cream Isle", "Cookie Canyon",
                   "Sundae Summit", "Bubblegum Basin", "Candy Cane Crest", "Waffle Woods", "Toffee Terrace"),
            // World 3: 21-30 Crystal Sugar Caves
            listOf("Sherbet Shore", "Praline Pass", "Jellybean Junction", "Butterscotch Bay", "Sorbet Spire",
                   "Frosting Fortress", "Choco Citadel", "Rainbow Ridge", "Crystal Confection", "Sweet Symphony"),
            // World 4: 31-40 Neon Berry Metropolis
            listOf("Neon Berry Blvd", "Blueberry Bastion", "Neon Nexus", "Raspberry Rapids", "Blackberry Bluff",
                   "Electric Glaze", "Cyber Sugarfield", "Pop-Rock Plateau", "Glowdrop Garden", "Prism Park"),
            // World 5: 41-50 Tropical Tiki Treats
            listOf("Mango Mirage", "Pineapple Palms", "Coconut Coast", "Papaya Passage", "Dragonfruit Dunes",
                   "Citrus Cyclone", "Kiwi Kingdom", "Tiki Toffee", "Banana Breeze", "Passionfruit Peak"),
            // World 6: 51-60 Molten Caramel Volcanoes
            listOf("Caramel Crater", "Molten Truffle", "Cacao Cavern", "Toasted Marsh", "Lava Fudge",
                   "Magma Meringue", "Crispy Crust", "Molten Macaroon", "S'mores Summit", "Volcano Velvet"),
            // World 7: 61-70 Frozen Sherbet Tundra
            listOf("Glacier Frosting", "Peppermint Polar", "Blizzard Bonbon", "Sub-Zero Sundae", "Chill Wafers",
                   "Icy Eclair", "Snowcone Slopes", "Polar Praline", "Frostbite Fudge", "Arctic Angel-Cake"),
            // World 8: 71-80 Celestial Candy Cosmos
            listOf("Star Sugar", "Nebula Nougat", "Starlight Sorbet", "Meteor Muffin", "Galaxy Gummy",
                   "Cosmic Caramel", "Lunar Lollipop", "Astro Apple-Tart", "Supernova Sweet", "Milky Way Macaron"),
            // World 9: 81-90 Royal Sugar Sanctuary
            listOf("Crown Cupcake", "Royal Rocher", "Baron Brownie", "Duchess Doughnut", "Imperial Icing",
                   "Palace Parfait", "Sovereign Souffle", "Dynasty Delight", "Monarch Meringue", "Empress Eclair"),
            // World 10: 91-100 Grand Master Sweet Kingdom
            listOf("Ascended Sugar", "Apex Ambrosia", "Mythic Marshmallow", "Infinity Icing", "Omega Orb",
                   "Eternal Eclair", "Ultimate Truffle", "Celestial Carousel", "Grand Candy Kingdom", "Bubble Master Pantheon")
        )

        for (i in 1..100) {
            val worldIndex = ((i - 1) / 10).coerceIn(0, worldThemes.size - 1)
            val levelIndexInWorld = (i - 1) % 10
            val name = worldThemes[worldIndex].getOrElse(levelIndexInWorld) { "Level $i" }

            // Gradual color variety progression
            val colorCount = when {
                i <= 4 -> 3
                i <= 14 -> 4
                i <= 35 -> 5
                else -> 6
            }

            // Cycle colors deterministically so levels have distinct, vibrant color palettes
            val colors = (0 until colorCount).map { allColors[(it + (i * 3)) % allColors.size] }.distinct()

            // Number of starting rows scales gradually from 4 up to 8
            val numRows = when {
                i <= 8 -> 4
                i <= 20 -> 5
                i <= 45 -> 6
                i <= 75 -> 7
                else -> 8
            }

            // Rich variety of level patterns (Stripes, Checkers, Diamonds, Cascades, Clusters, Targets)
            val initialRows = mutableListOf<List<BubbleColor?>>()
            for (row in 0 until numRows) {
                val rowCols = if (row % 2 == 0) 8 else 7
                val rowBubbles = mutableListOf<BubbleColor?>()

                for (col in 0 until rowCols) {
                    val patternType = i % 8
                    val colorIndex = when (patternType) {
                        0 -> (col / 2 + row) % colors.size                // Paired color blocks
                        1 -> (col + row) % colors.size                    // Diagonal checkerboard
                        2 -> (row) % colors.size                          // Horizontal color bands
                        3 -> if (col % 2 == 0) (row % colors.size) else ((row + 1) % colors.size) // Interlocking
                        4 -> ((col * 2 + row * 3 + i) % colors.size)      // Gem mosaic swirl
                        5 -> ((col - row + 100) % colors.size)            // Counter-diagonal waves
                        6 -> (if (row == 0 || col == 0 || col == rowCols - 1) (i % colors.size) else ((col + row) % colors.size)) // Framed cluster
                        else -> ((col * col + row * 2 + i) % colors.size) // Concentric pulse
                    }
                    rowBubbles.add(colors[colorIndex])
                }
                initialRows.add(rowBubbles)
            }

            // Drop interval gets tighter as player advances (gives urgency but stays fair)
            val dropInterval = when {
                i <= 10 -> 6
                i <= 40 -> 5
                i <= 70 -> 4
                else -> 4
            }

            // Dynamic scoring curve for 3 stars
            val baseStar = 800 + (i * 180)
            val s1 = baseStar
            val s2 = (baseStar * 2.1f).toInt()
            val s3 = (baseStar * 3.6f).toInt()

            // Generous power-ups unlocked progressively for later difficult levels
            val bombCount = (2 + (i / 15)).coerceAtMost(6)
            val fireballCount = (1 + (i / 12)).coerceAtMost(5)
            val rainbowCount = (2 + (i / 14)).coerceAtMost(6)

            list.add(
                LevelData(
                    id = i,
                    name = name,
                    baseCols = 8,
                    allowedColors = colors,
                    initialRows = initialRows,
                    dropIntervalShots = dropInterval,
                    star1Score = s1,
                    star2Score = s2,
                    star3Score = s3,
                    startingPowerUps = mapOf(
                        PowerUpType.BOMB to bombCount,
                        PowerUpType.FIREBALL to fireballCount,
                        PowerUpType.RAINBOW to rainbowCount
                    )
                )
            )
        }

        return list
    }
}
