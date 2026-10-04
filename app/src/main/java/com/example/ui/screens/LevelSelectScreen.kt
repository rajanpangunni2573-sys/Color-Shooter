package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LevelRepository
import com.example.model.BackgroundMode
import com.example.model.LevelData
import com.example.ui.components.GameBackground
import kotlinx.coroutines.launch

@Composable
fun LevelSelectScreen(
    highestUnlockedLevel: Int,
    backgroundMode: BackgroundMode = BackgroundMode.SWEET_CANDY,
    customPhotoPath: String? = null,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialWorldIndex = LevelRepository.getWorldIndexForLevel(highestUnlockedLevel)
    var selectedWorld by remember { mutableIntStateOf(initialWorldIndex) }
    var showJumpDialog by remember { mutableStateOf(false) }

    val activeRealmIndex = selectedWorld / 10
    var selectedRealm by remember { mutableIntStateOf(activeRealmIndex) }

    val gridState = rememberLazyGridState()
    val worldListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to selected world chip in LazyRow
    LaunchedEffect(selectedWorld) {
        val targetIndex = (selectedWorld % 10).coerceAtLeast(0)
        worldListState.animateScrollToItem(targetIndex)
        gridState.scrollToItem(0)
        selectedRealm = selectedWorld / 10
    }

    val realmNames = listOf(
        "Meadows (1-100)",
        "Clouds (101-200)",
        "Crystals (201-300)",
        "Metropolis (301-400)",
        "Tiki Isles (401-500)",
        "Volcanoes (501-600)",
        "Tundra (601-700)",
        "Cosmos (701-800)",
        "Citadel (801-900)",
        "Pantheon (901-1000)"
    )

    // Current displayed levels for selected world (10 levels per world)
    val displayedLevels = remember(selectedWorld) {
        val startId = selectedWorld * LevelRepository.LEVELS_PER_WORLD + 1
        val endId = (selectedWorld + 1) * LevelRepository.LEVELS_PER_WORLD
        (startId..endId).map { LevelRepository.getLevel(it) }
    }

    val currentWorldName = LevelRepository.getWorldName(selectedWorld)
    val worldStartId = selectedWorld * LevelRepository.LEVELS_PER_WORLD + 1
    val worldEndId = (selectedWorld + 1) * LevelRepository.LEVELS_PER_WORLD
    val isWorldUnlocked = worldStartId <= highestUnlockedLevel

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Dynamic Game Background
        GameBackground(
            backgroundMode = backgroundMode,
            customPhotoPath = customPhotoPath,
            scrimAlpha = 0.72f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(46.dp)
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

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "1,000 LEVELS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Unlocked: $highestUnlockedLevel / 1,000 Stages",
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quick Jump Button
                IconButton(
                    onClick = { showJumpDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.35f))
                        .border(1.dp, Color(0xFF38BDF8), CircleShape)
                        .testTag("quick_jump_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Jump to Level",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Realm Selector (10 Chapters)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(realmNames) { index, realmLabel ->
                    val isSelected = selectedRealm == index
                    val realmStartLevel = index * 100 + 1
                    val isRealmUnlocked = realmStartLevel <= highestUnlockedLevel

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedRealm = index
                            selectedWorld = index * 10
                        },
                        label = {
                            Text(
                                text = "R${index + 1}: $realmLabel",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.8f),
                            labelColor = if (isRealmUnlocked) Color(0xFFCBD5E1) else Color(0xFF64748B)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                            selectedBorderColor = Color(0xFF38BDF8),
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Worlds within active Realm (10 worlds)
            LazyRow(
                state = worldListState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val realmStartWorld = selectedRealm * 10
                val realmEndWorld = realmStartWorld + 9

                items((realmStartWorld..realmEndWorld).toList()) { wIndex ->
                    val isSelected = selectedWorld == wIndex
                    val startLevel = wIndex * LevelRepository.LEVELS_PER_WORLD + 1
                    val endLevel = (wIndex + 1) * LevelRepository.LEVELS_PER_WORLD
                    val unlocked = startLevel <= highestUnlockedLevel

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedWorld = wIndex
                        },
                        label = {
                            Text(
                                text = "World ${wIndex + 1} ($startLevel-$endLevel)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (!unlocked) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0EA5E9),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.85f),
                            labelColor = if (unlocked) Color(0xFFCBD5E1) else Color(0xFF64748B)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                            selectedBorderColor = Color(0xFF38BDF8),
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("world_chip_$wIndex")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // World Banner with Previous / Next Arrows
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedWorld > 0) {
                                selectedWorld -= 1
                            }
                        },
                        enabled = selectedWorld > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous World",
                            tint = if (selectedWorld > 0) Color.White else Color(0xFF475569)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "WORLD ${selectedWorld + 1}: $currentWorldName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Levels $worldStartId – $worldEndId of 1,000",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (selectedWorld < LevelRepository.WORLDS_COUNT - 1) {
                                selectedWorld += 1
                            }
                        },
                        enabled = selectedWorld < LevelRepository.WORLDS_COUNT - 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next World",
                            tint = if (selectedWorld < LevelRepository.WORLDS_COUNT - 1) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 10 Levels Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("levels_grid")
            ) {
                items(displayedLevels) { level ->
                    val isUnlocked = level.id <= highestUnlockedLevel
                    LevelItemCard(
                        level = level,
                        isUnlocked = isUnlocked,
                        onClick = {
                            if (isUnlocked) {
                                onLevelSelected(level.id)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Continue Button
            Button(
                onClick = { onLevelSelected(highestUnlockedLevel) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("continue_adventure_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CONTINUE LEVEL $highestUnlockedLevel / 1,000",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp
                )
            }
        }
    }

    // Quick Jump Dialog
    if (showJumpDialog) {
        var inputLevelText by remember { mutableStateOf("$highestUnlockedLevel") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = {
                Text("Jump to Level", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    Text(
                        "Enter any level from 1 to 1000:",
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputLevelText,
                        onValueChange = {
                            inputLevelText = it.filter { char -> char.isDigit() }
                            errorMessage = null
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputLevelText.toIntOrNull()
                        if (parsed != null && parsed in 1..LevelRepository.TOTAL_LEVELS) {
                            if (parsed <= highestUnlockedLevel) {
                                showJumpDialog = false
                                onLevelSelected(parsed)
                            } else {
                                // Jump view to that world
                                selectedWorld = LevelRepository.getWorldIndexForLevel(parsed)
                                showJumpDialog = false
                            }
                        } else {
                            errorMessage = "Please enter 1 - 1000"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("Go", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun LevelItemCard(
    level: LevelData,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF1E293B).copy(alpha = 0.95f) else Color(0xFF0F172A).copy(alpha = 0.85f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isUnlocked) 1.5.dp else 1.dp,
                color = if (isUnlocked) Color(0xFF38BDF8).copy(alpha = 0.7f) else Color(0xFF334155),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
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
                    .size(46.dp)
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
                        fontSize = if (level.id >= 1000) 14.sp else if (level.id >= 100) 16.sp else 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = level.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.White else Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${level.allowedColors.size} Colors • ${level.initialRows.size} Rows",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Stars row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (isUnlocked) Color(0xFFFFD700) else Color(0xFF475569),
                        modifier = Modifier.size(13.dp)
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
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PLAY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38BDF8)
                    )
                }
            } else {
                Text(
                    text = "LOCKED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
