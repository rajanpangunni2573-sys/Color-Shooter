package com.example.game

import androidx.compose.ui.geometry.Offset
import com.example.model.Bubble
import com.example.model.BubbleColor
import com.example.model.GridCoord
import com.example.model.PowerUpType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object HexGridMath {

    // Height step between hexagonal rows: sqrt(3) * radius
    private const val ROW_HEIGHT_FACTOR = 1.7320508f

    fun getRowHeight(radius: Float): Float = radius * ROW_HEIGHT_FACTOR

    fun getColCount(row: Int, baseCols: Int): Int {
        return if (row % 2 == 0) baseCols else baseCols - 1
    }

    /**
     * Calculates center coordinate (x, y) of a bubble in the grid.
     * Even rows (0, 2, 4...) have baseCols bubbles, starting with left margin = radius.
     * Odd rows (1, 3, 5...) have baseCols - 1 bubbles, offset horizontally by an extra radius.
     */
    fun getBubbleCenter(
        row: Int,
        col: Int,
        radius: Float,
        gridStartX: Float,
        gridStartY: Float
    ): Offset {
        val diameter = radius * 2f
        val x = if (row % 2 == 0) {
            gridStartX + radius + col * diameter
        } else {
            gridStartX + (radius * 2f) + col * diameter
        }
        val y = gridStartY + radius + row * getRowHeight(radius)
        return Offset(x, y)
    }

    /**
     * Finds adjacent neighbors of a hexagonal offset grid cell.
     */
    fun getNeighbors(coord: GridCoord, maxRows: Int, baseCols: Int): List<GridCoord> {
        val (r, c) = coord
        val neighbors = mutableListOf<GridCoord>()
        val isEvenRow = (r % 2 == 0)

        // Left and Right
        neighbors.add(GridCoord(r, c - 1))
        neighbors.add(GridCoord(r, c + 1))

        if (isEvenRow) {
            // Row above (r - 1)
            neighbors.add(GridCoord(r - 1, c - 1))
            neighbors.add(GridCoord(r - 1, c))
            // Row below (r + 1)
            neighbors.add(GridCoord(r + 1, c - 1))
            neighbors.add(GridCoord(r + 1, c))
        } else {
            // Row above (r - 1)
            neighbors.add(GridCoord(r - 1, c))
            neighbors.add(GridCoord(r - 1, c + 1))
            // Row below (r + 1)
            neighbors.add(GridCoord(r + 1, c))
            neighbors.add(GridCoord(r + 1, c + 1))
        }

        // Filter valid in-bound slots
        return neighbors.filter { (nr, nc) ->
            nr in 0 until maxRows && nc in 0 until getColCount(nr, baseCols)
        }
    }

    /**
     * Finds closest vacant grid cell when a projectile collides with an existing bubble or reaches ceiling.
     */
    fun findSnapCoord(
        projectilePos: Offset,
        radius: Float,
        gridStartX: Float,
        gridStartY: Float,
        maxRows: Int,
        baseCols: Int,
        occupiedGrid: Map<GridCoord, Bubble>
    ): GridCoord? {
        // Collect candidate slots: either any slot in row 0 if near ceiling,
        // or any vacant neighbor of an existing occupied bubble.
        val candidateSlots = mutableSetOf<GridCoord>()

        // Row 0 slots (ceiling attachment)
        val colsInRow0 = getColCount(0, baseCols)
        for (c in 0 until colsInRow0) {
            val coord = GridCoord(0, c)
            if (!occupiedGrid.containsKey(coord)) {
                candidateSlots.add(coord)
            }
        }

        // Vacant neighbors of all existing bubbles
        for (occupiedCoord in occupiedGrid.keys) {
            val neighbors = getNeighbors(occupiedCoord, maxRows, baseCols)
            for (neighbor in neighbors) {
                if (!occupiedGrid.containsKey(neighbor)) {
                    candidateSlots.add(neighbor)
                }
            }
        }

        if (candidateSlots.isEmpty()) return null

        var bestCoord: GridCoord? = null
        var minDistanceSq = Float.MAX_VALUE

        for (slot in candidateSlots) {
            val center = getBubbleCenter(slot.row, slot.col, radius, gridStartX, gridStartY)
            val dx = projectilePos.x - center.x
            val dy = projectilePos.y - center.y
            val distSq = dx * dx + dy * dy
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq
                bestCoord = slot
            }
        }

        return bestCoord
    }

    /**
     * BFS cluster search for matching bubble colors or Rainbow wildcard.
     * Returns set of GridCoords to pop if cluster size is >= 3, or if special power-up triggered.
     */
    fun findMatchingCluster(
        startCoord: GridCoord,
        grid: Map<GridCoord, Bubble>,
        maxRows: Int,
        baseCols: Int
    ): Set<GridCoord> {
        val rootBubble = grid[startCoord] ?: return emptySet()

        // Handle Rainbow wildcard: matches color of adjacent clusters
        val targetColor = if (rootBubble.powerUp == PowerUpType.RAINBOW) {
            // Pick color from the first non-rainbow neighbor if available
            val neighbors = getNeighbors(startCoord, maxRows, baseCols)
            neighbors.mapNotNull { grid[it]?.color }.firstOrNull() ?: rootBubble.color
        } else {
            rootBubble.color
        }

        val visited = mutableSetOf<GridCoord>()
        val queue = ArrayDeque<GridCoord>()
        val cluster = mutableSetOf<GridCoord>()

        queue.add(startCoord)
        visited.add(startCoord)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val bubble = grid[current] ?: continue

            val isMatch = bubble.powerUp == PowerUpType.RAINBOW ||
                    rootBubble.powerUp == PowerUpType.RAINBOW ||
                    bubble.color == targetColor

            if (isMatch) {
                cluster.add(current)
                for (neighbor in getNeighbors(current, maxRows, baseCols)) {
                    if (!visited.contains(neighbor) && grid.containsKey(neighbor)) {
                        visited.add(neighbor)
                        queue.add(neighbor)
                    }
                }
            }
        }

        // Return cluster only if >= 3 bubbles, or if Rainbow wildcard formed at least 3
        return if (cluster.size >= 3) cluster else emptySet()
    }

    /**
     * Bomb explosive search: gathers all bubbles within a 2-ring hexagonal neighborhood.
     */
    fun findBombCluster(
        centerCoord: GridCoord,
        grid: Map<GridCoord, Bubble>,
        maxRows: Int,
        baseCols: Int
    ): Set<GridCoord> {
        val bombCluster = mutableSetOf<GridCoord>()
        if (grid.containsKey(centerCoord)) {
            bombCluster.add(centerCoord)
        }
        val ring1 = getNeighbors(centerCoord, maxRows, baseCols)
        bombCluster.addAll(ring1.filter { grid.containsKey(it) })

        for (r1 in ring1) {
            val ring2 = getNeighbors(r1, maxRows, baseCols)
            bombCluster.addAll(ring2.filter { grid.containsKey(it) })
        }
        return bombCluster
    }

    /**
     * BFS search from the top row / ceiling to detect floating (orphan) bubbles.
     * Any bubble in the grid that is NOT visited during ceiling BFS is floating.
     */
    fun findFloatingBubbles(
        grid: Map<GridCoord, Bubble>,
        maxRows: Int,
        baseCols: Int
    ): Set<GridCoord> {
        if (grid.isEmpty()) return emptySet()

        val visited = mutableSetOf<GridCoord>()
        val queue = ArrayDeque<GridCoord>()

        // Add all bubbles currently attached to row 0 (the ceiling)
        val colsInRow0 = getColCount(0, baseCols)
        for (c in 0 until colsInRow0) {
            val coord = GridCoord(0, c)
            if (grid.containsKey(coord)) {
                queue.add(coord)
                visited.add(coord)
            }
        }

        // Traverse all connected neighbors
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val neighbors = getNeighbors(current, maxRows, baseCols)
            for (neighbor in neighbors) {
                if (grid.containsKey(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }

        // Any bubble not visited is an orphan
        val floating = mutableSetOf<GridCoord>()
        for (coord in grid.keys) {
            if (!visited.contains(coord)) {
                floating.add(coord)
            }
        }

        return floating
    }

    /**
     * Predicts aiming ray trajectory with wall bounces and detects impact with grid bubbles or ceiling.
     */
    fun calculateAimTrajectory(
        origin: Offset,
        angleDegrees: Float,
        bubbleRadius: Float,
        screenWidth: Float,
        gridStartX: Float,
        gridStartY: Float,
        maxRows: Int,
        baseCols: Int,
        grid: Map<GridCoord, Bubble>
    ): Pair<List<Pair<Offset, Offset>>, Offset?> {
        val segments = mutableListOf<Pair<Offset, Offset>>()
        val radians = Math.toRadians(angleDegrees.toDouble()).toFloat()

        var currentPos = origin
        var dirX = cos(radians)
        var dirY = sin(radians)

        val leftBound = bubbleRadius
        val rightBound = screenWidth - bubbleRadius
        val ceilingY = gridStartY + bubbleRadius

        val maxBounces = 4
        var remainingDistance = 2500f
        val step = bubbleRadius * 0.4f
        var impactPoint: Offset? = null

        for (bounce in 0..maxBounces) {
            var segmentStart = currentPos
            var currentSegmentDist = 0f
            var hit = false

            while (currentSegmentDist < remainingDistance) {
                val nextX = currentPos.x + dirX * step
                val nextY = currentPos.y + dirY * step
                val testPos = Offset(nextX, nextY)
                currentSegmentDist += step

                // Wall collision: Left wall
                if (nextX <= leftBound) {
                    currentPos = Offset(leftBound, nextY)
                    dirX = -dirX // bounce
                    segments.add(segmentStart to currentPos)
                    hit = false
                    break
                }
                // Wall collision: Right wall
                if (nextX >= rightBound) {
                    currentPos = Offset(rightBound, nextY)
                    dirX = -dirX // bounce
                    segments.add(segmentStart to currentPos)
                    hit = false
                    break
                }
                // Ceiling collision
                if (nextY <= ceilingY) {
                    currentPos = Offset(nextX, ceilingY)
                    impactPoint = currentPos
                    segments.add(segmentStart to currentPos)
                    hit = true
                    break
                }

                // Bubble collision check
                val hitBubble = checkBubbleCollision(
                    testPos,
                    bubbleRadius,
                    gridStartX,
                    gridStartY,
                    grid
                )
                if (hitBubble) {
                    impactPoint = testPos
                    segments.add(segmentStart to testPos)
                    hit = true
                    break
                }

                currentPos = testPos
            }

            if (hit || currentSegmentDist >= remainingDistance) {
                if (!hit && segmentStart != currentPos) {
                    segments.add(segmentStart to currentPos)
                }
                break
            }
        }

        return segments to impactPoint
    }

    private fun checkBubbleCollision(
        pos: Offset,
        radius: Float,
        gridStartX: Float,
        gridStartY: Float,
        grid: Map<GridCoord, Bubble>
    ): Boolean {
        // Bubble collision threshold: 2 * radius * 0.95f (slight buffer for realistic snapping)
        val collisionThresholdSq = (radius * 1.9f) * (radius * 1.9f)

        for ((coord, _) in grid) {
            val center = getBubbleCenter(coord.row, coord.col, radius, gridStartX, gridStartY)
            val dx = pos.x - center.x
            val dy = pos.y - center.y
            if ((dx * dx + dy * dy) <= collisionThresholdSq) {
                return true
            }
        }
        return false
    }
}
