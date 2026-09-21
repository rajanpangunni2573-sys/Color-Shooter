package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LevelData

@Composable
fun StarRatingBar(
    score: Int,
    stars: Int,
    level: LevelData?,
    modifier: Modifier = Modifier
) {
    val targetStar3 = level?.star3Score ?: 4000
    val progress = (score.toFloat() / targetStar3).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "score_progress")

    Column(
        modifier = modifier
            .background(Color(0xFF0F172A).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stars row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..3) {
                val isEarned = stars >= i
                Icon(
                    imageVector = if (isEarned) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $i",
                    tint = if (isEarned) Color(0xFFFFD700) else Color(0xFF475569),
                    modifier = Modifier
                        .size(if (i == 2) 22.dp else 18.dp)
                        .padding(horizontal = 1.dp)
                )
            }
        }

        // Progress line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700))
            )
        }
    }
}
