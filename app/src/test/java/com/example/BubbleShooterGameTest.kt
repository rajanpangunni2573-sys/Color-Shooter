package com.example

import com.example.data.LevelRepository
import com.example.game.HexGridMath
import com.example.model.Bubble
import com.example.model.BubbleColor
import com.example.model.GridCoord
import com.example.model.PowerUpType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BubbleShooterGameTest {

    @Test
    fun testHexagonalNeighborsEvenRow() {
        val neighbors = HexGridMath.getNeighbors(
            coord = GridCoord(0, 1),
            maxRows = 10,
            baseCols = 8
        )
        // Row 0 has col neighbors (0, 0), (0, 2) and row 1 neighbors (1, 0), (1, 1)
        assertTrue(neighbors.contains(GridCoord(0, 0)))
        assertTrue(neighbors.contains(GridCoord(0, 2)))
        assertTrue(neighbors.contains(GridCoord(1, 0)))
        assertTrue(neighbors.contains(GridCoord(1, 1)))
    }

    @Test
    fun testClusterMatchingBFS() {
        val grid = mutableMapOf<GridCoord, Bubble>()
        grid[GridCoord(0, 0)] = Bubble(color = BubbleColor.RED)
        grid[GridCoord(0, 1)] = Bubble(color = BubbleColor.RED)
        grid[GridCoord(1, 0)] = Bubble(color = BubbleColor.RED)
        grid[GridCoord(0, 2)] = Bubble(color = BubbleColor.BLUE)

        val cluster = HexGridMath.findMatchingCluster(
            startCoord = GridCoord(0, 0),
            grid = grid,
            maxRows = 10,
            baseCols = 8
        )

        // All 3 adjacent RED bubbles must match
        assertEquals(3, cluster.size)
        assertTrue(cluster.contains(GridCoord(0, 0)))
        assertTrue(cluster.contains(GridCoord(0, 1)))
        assertTrue(cluster.contains(GridCoord(1, 0)))
        assertTrue(!cluster.contains(GridCoord(0, 2)))
    }

    @Test
    fun testOrphanDetection() {
        val grid = mutableMapOf<GridCoord, Bubble>()
        // Attached to ceiling
        grid[GridCoord(0, 0)] = Bubble(color = BubbleColor.RED)
        grid[GridCoord(1, 0)] = Bubble(color = BubbleColor.BLUE)

        // Detached bubble hanging in row 4 with no connection to row 0
        grid[GridCoord(4, 2)] = Bubble(color = BubbleColor.GREEN)

        val orphans = HexGridMath.findFloatingBubbles(
            grid = grid,
            maxRows = 10,
            baseCols = 8
        )

        assertEquals(1, orphans.size)
        assertTrue(orphans.contains(GridCoord(4, 2)))
    }

    @Test
    fun testLevelsDataIntegrity() {
        val levels = LevelRepository.levels
        assertTrue(levels.size >= 3)
        for (lvl in levels) {
            assertTrue(lvl.allowedColors.isNotEmpty())
            assertTrue(lvl.initialRows.isNotEmpty())
            assertTrue(lvl.star1Score < lvl.star2Score)
            assertTrue(lvl.star2Score < lvl.star3Score)
        }
    }
}
