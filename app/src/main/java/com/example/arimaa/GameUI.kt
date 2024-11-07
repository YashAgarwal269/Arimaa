package com.example.arimaa
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border

// //This composable will be used to display the decision dialog
//@Composable
//fun CaptureDecisionDialog(
//    showDialog: Boolean,
//    onCapture: () -> Unit,
//    onPush: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    if (showDialog) {
//        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
//            Card(
//                modifier = Modifier.fillMaxWidth(0.8f).padding(16.dp),
//                elevation = 8.dp
//            ) {
//                Column(
//                    modifier = Modifier.fillMaxSize().padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Text(text = "Do you want to capture or push the piece?", color = Color.Black)
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Button(onClick = {
//                            onCapture()
//                            onDismiss()  // Close the dialog
//                        }) {
//                            Text(text = "Capture")
//                        }
//                        Button(onClick = {
//                            onPush()
//                            onDismiss()  // Close the dialog
//                        }) {
//                            Text(text = "Push")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}




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
    // Set the background color based on whether the cell is a trap
    val backgroundColor = if (cell.isTrap) Color.Red else Color.Transparent

    Box(
        modifier = modifier
            .size(40.dp)
            .background(color = backgroundColor) // Apply the trap color here
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
