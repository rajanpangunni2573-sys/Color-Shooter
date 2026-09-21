package com.example.model

data class GridCoord(val row: Int, val col: Int) {
    override fun toString(): String = "($row,$col)"
}
