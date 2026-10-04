package com.example.data

import com.example.model.BubbleColor
import com.example.model.LevelData
import com.example.model.PowerUpType

object LevelRepository {

    private val allColors = BubbleColor.values().toList()

    const val TOTAL_LEVELS = 1000
    const val WORLDS_COUNT = 100
    const val LEVELS_PER_WORLD = 10

    // 100 Themed Worlds (10 levels each = 1000 levels)
    val worldNames = listOf(
        // Realm 1: Sugar Meadow (1-10)
        "Sweet Meadow", "Pastry Parkway", "Cupcake Cove", "Amber Dunes", "Donut Drift",
        "Caramel Cascade", "Lollipop Lagoon", "Gummy Glade", "Macaron Maze", "Taffy Tunnels",
        // Realm 2: Cloudlands (11-20)
        "Cotton Peak", "Marshmallow Mist", "Fudge Foothills", "Ice Cream Isle", "Cookie Canyon",
        "Sundae Summit", "Bubblegum Basin", "Candy Cane Crest", "Waffle Woods", "Toffee Terrace",
        // Realm 3: Crystal Caves (21-30)
        "Sherbet Shore", "Praline Pass", "Jellybean Junction", "Butterscotch Bay", "Sorbet Spire",
        "Frosting Fortress", "Choco Citadel", "Rainbow Ridge", "Crystal Confection", "Sweet Symphony",
        // Realm 4: Neon Metropolis (31-40)
        "Neon Berry Blvd", "Blueberry Bastion", "Neon Nexus", "Raspberry Rapids", "Blackberry Bluff",
        "Electric Glaze", "Cyber Sugarfield", "Pop-Rock Plateau", "Glowdrop Garden", "Prism Park",
        // Realm 5: Tropical Tiki (41-50)
        "Mango Mirage", "Pineapple Palms", "Coconut Coast", "Papaya Passage", "Dragonfruit Dunes",
        "Citrus Cyclone", "Kiwi Kingdom", "Tiki Toffee", "Banana Breeze", "Passionfruit Peak",
        // Realm 6: Molten Caramel (51-60)
        "Caramel Crater", "Molten Truffle", "Cacao Cavern", "Toasted Marsh", "Lava Fudge",
        "Magma Meringue", "Crispy Crust", "Molten Macaroon", "S'mores Summit", "Volcano Velvet",
        // Realm 7: Sherbet Tundra (61-70)
        "Glacier Frosting", "Peppermint Polar", "Blizzard Bonbon", "Sub-Zero Sundae", "Chill Wafers",
        "Icy Eclair", "Snowcone Slopes", "Polar Praline", "Frostbite Fudge", "Arctic Angel-Cake",
        // Realm 8: Celestial Cosmos (71-80)
        "Star Sugar", "Nebula Nougat", "Starlight Sorbet", "Meteor Muffin", "Galaxy Gummy",
        "Cosmic Caramel", "Lunar Lollipop", "Astro Apple-Tart", "Supernova Sweet", "Milky Way Macaron",
        // Realm 9: Royal Citadel (81-90)
        "Crown Cupcake", "Royal Rocher", "Baron Brownie", "Duchess Doughnut", "Imperial Icing",
        "Palace Parfait", "Sovereign Souffle", "Dynasty Delight", "Monarch Meringue", "Empress Eclair",
        // Realm 10: Bubble Pantheon (91-100)
        "Ascended Sugar", "Apex Ambrosia", "Mythic Marshmallow", "Infinity Icing", "Omega Orb",
        "Eternal Eclair", "Ultimate Truffle", "Celestial Carousel", "Grand Candy Kingdom", "Bubble Master Pantheon"
    )

    private val stageSubtitles = listOf(
        "Gate", "Pathway", "Glade", "Hollow", "Drift",
        "Chamber", "Ridge", "Spire", "Sanctuary", "Apex"
    )

    val levels: List<LevelData> = generateLevels()

    fun getLevel(id: Int): LevelData {
        val index = (id - 1).coerceIn(0, levels.size - 1)
        return levels[index]
    }

    fun getWorldIndexForLevel(levelId: Int): Int {
        return ((levelId - 1) / LEVELS_PER_WORLD).coerceIn(0, WORLDS_COUNT - 1)
    }

    fun getWorldName(worldIndex: Int): String {
        return worldNames.getOrElse(worldIndex) { "World ${worldIndex + 1}" }
    }

    private fun generateLevels(): List<LevelData> {
        val list = ArrayList<LevelData>(TOTAL_LEVELS)

        for (i in 1..TOTAL_LEVELS) {
            val worldIndex = (i - 1) / LEVELS_PER_WORLD
            val stageIndex = (i - 1) % LEVELS_PER_WORLD
            val worldName = worldNames.getOrElse(worldIndex) { "World ${worldIndex + 1}" }
            val subtitle = stageSubtitles.getOrElse(stageIndex) { "Stage ${stageIndex + 1}" }
            val name = "$worldName: $subtitle"

            // Gradual color variety progression
            val colorCount = when {
                i <= 4 -> 3
                i <= 18 -> 4
                i <= 60 -> 5
                else -> 6
            }

            // Colors shift deterministically by world and level
            val colorShift = (worldIndex * 2 + (i % 5)) % allColors.size
            val colors = (0 until colorCount).map { allColors[(it + colorShift) % allColors.size] }.distinct()

            // Number of starting rows scales gradually from 4 up to 8
            val numRows = when {
                i <= 8 -> 4
                i <= 25 -> 5
                i <= 80 -> 6
                i <= 200 -> 7
                else -> 8
            }

            // 12 distinct pattern topologies (Stripes, Checkers, Diamonds, Cascades, Clusters, Targets, Spirals)
            val initialRows = ArrayList<List<BubbleColor?>>(numRows)
            for (row in 0 until numRows) {
                val rowCols = if (row % 2 == 0) 8 else 7
                val rowBubbles = ArrayList<BubbleColor?>(rowCols)

                val patternType = (i + row) % 12
                for (col in 0 until rowCols) {
                    val colorIndex = when (patternType) {
                        0 -> (col / 2 + row) % colors.size                // Paired color blocks
                        1 -> (col + row) % colors.size                    // Diagonal checkerboard
                        2 -> (row) % colors.size                          // Horizontal wave bands
                        3 -> if (col % 2 == 0) (row % colors.size) else ((row + 1) % colors.size) // Interlocking
                        4 -> ((col * 2 + row * 3 + i) % colors.size)      // Gem mosaic swirl
                        5 -> ((col - row + 100) % colors.size)            // Counter-diagonal waves
                        6 -> (if (row == 0 || col == 0 || col == rowCols - 1) (i % colors.size) else ((col + row) % colors.size)) // Framed cluster
                        7 -> ((col * col + row * 2 + i) % colors.size)    // Concentric pulse
                        8 -> (col % colors.size)                          // Vertical pillars
                        9 -> if (col == rowCols / 2 || row == 0) (i % colors.size) else ((row + col) % colors.size) // Diamond fortress
                        10 -> ((col * 3 + row + i / 10) % colors.size)    // Honeycomb cluster
                        else -> ((col + row * 2) % colors.size)           // Stepped gradient
                    }
                    rowBubbles.add(colors[colorIndex])
                }
                initialRows.add(rowBubbles)
            }

            // Drop interval variation: creates rhythmic tactical variety
            val dropInterval = when {
                i % 7 == 0 -> 4 // Urgent rush levels
                i % 4 == 0 -> 6 // Strategic thinking levels
                else -> 5
            }

            // Dynamic scoring curve for 3 stars
            val baseStar = 750 + (i * 35) + (numRows * 120)
            val s1 = baseStar
            val s2 = (baseStar * 2.1f).toInt()
            val s3 = (baseStar * 3.6f).toInt()

            // Generous power-ups unlocked progressively for later stages
            val bombCount = (2 + (i / 120)).coerceAtMost(6)
            val fireballCount = (1 + (i / 100)).coerceAtMost(5)
            val rainbowCount = (2 + (i / 110)).coerceAtMost(6)

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
