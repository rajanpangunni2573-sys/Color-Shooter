package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LevelRepository
import com.example.model.LevelData
import kotlinx.coroutines.launch

@Composable
fun LevelSelectScreen(
    highestUnlockedLevel: Int,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 10 Worlds (10 levels each)
    val worldNames = listOf(
        "World 1 (1-10)",
        "World 2 (11-20)",
        "World 3 (21-30)",
        "World 4 (31-40)",
        "World 5 (41-50)",
        "World 6 (51-60)",
        "World 7 (61-70)",
        "World 8 (71-80)",
        "World 9 (81-90)",
        "World 10 (91-100)"
    )

    val currentWorldIndex = ((highestUnlockedLevel - 1) / 10).coerceIn(0, 9)
    var selectedWorld by remember { mutableIntStateOf(currentWorldIndex) }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll smoothly to top of grid when world changes
    LaunchedEffect(selectedWorld) {
        gridState.scrollToItem(0)
    }

    val displayedLevels = remember(selectedWorld) {
        val startId = selectedWorld * 10 + 1
        val endId = (selectedWorld + 1) * 10
        LevelRepository.levels.filter { it.id in startId..endId }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Candy / Sweet Path Background Image
        Image(
            painter = painterResource(id = R.drawable.img_sweet_background),
            contentDescription = "Candy Kingdom Map",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Readability Scrim Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE0B1020),
                            Color(0xD910162F),
                            Color(0xFA090D1A)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                        .testTag("level_select_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "SELECT LEVEL",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "100 Sweet World Challenges • Unlocked: $highestUnlockedLevel/100",
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // World Selector Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(worldNames) { index, worldName ->
                    val isSelected = selectedWorld == index
                    val isWorldUnlocked = (index * 10 + 1) <= highestUnlockedLevel
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedWorld = index
                            coroutineScope.launch {
                                gridState.animateScrollToItem(0)
                            }
                        },
                        label = {
                            Text(
                                text = worldName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (!isWorldUnlocked) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.85f),
                            labelColor = if (isWorldUnlocked) Color(0xFFCBD5E1) else Color(0xFF64748B)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                            selectedBorderColor = Color(0xFF38BDF8),
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("world_chip_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level Grid for current selected World (10 levels per world)
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedLevels) { level ->
                    val isUnlocked = level.id <= highestUnlockedLevel
                    LevelCard(
                        level = level,
                        isUnlocked = isUnlocked,
                        onClick = {
                            if (isUnlocked) onLevelSelected(level.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelData,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xDD1E293B) else Color(0x990F172A)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isUnlocked) 1.5.dp else 1.dp,
                color = if (isUnlocked) Color(0xFF38BDF8) else Color(0xFF334155),
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("level_card_${level.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level Badge Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Brush.radialGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        ) else Brush.radialGradient(
                            listOf(Color(0xFF334155), Color(0xFF1E293B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = "${level.id}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = level.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.White else Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${level.allowedColors.size} Colors • ${level.initialRows.size} Rows",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Stars placeholder (3 stars)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (isUnlocked) Color(0xFFFFD700) else Color(0xFF475569),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isUnlocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PLAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8)
                    )
                }
            } else {
                Text(
                    text = "LOCKED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
