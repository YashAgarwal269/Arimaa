package com.example.arimaa

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Board(board: ArimaaBoard, onCellClick: (Int, Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        for (y in 0 until 8) {
            Row {
                for (x in 0 until 8) {
                    val cell = board.board[y][x]
                    BoardCellView(cell = cell, onClick = { onCellClick(x, y) }) // Pass cell and click handler
                }
            }
        }
    }
}

@Composable
fun BoardCellView(cell: BoardCell, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(40.dp)
            .border(1.dp, Color.Black)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val piece = cell.piece
        piece?.let {
            val textColor = if (it.player == Player.GOLD) Color.Yellow else Color.Gray
            Text(text = it.type.name, color = textColor) // Display piece type in corresponding color
        } ?: run {
            Text(text = ".") // Represent empty cell
        }
    }
}