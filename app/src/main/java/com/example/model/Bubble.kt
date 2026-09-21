package com.example.model

data class Bubble(
    val id: Long = System.nanoTime(),
    val color: BubbleColor = BubbleColor.RED,
    val powerUp: PowerUpType = PowerUpType.NONE,
    val isPopping: Boolean = false,
    val popProgress: Float = 0f
)
