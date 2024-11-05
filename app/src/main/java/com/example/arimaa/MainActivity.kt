package com.example.arimaa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.arimaa.ui.theme.ArimaaTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*

class MainActivity : ComponentActivity() {
    private lateinit var arimaaBoard: ArimaaBoard
    private var currentPlayer by mutableStateOf(Player.GOLD)
    private var movesLeft by mutableStateOf(4)
    private var selectedCell by mutableStateOf<Pair<Int, Int>?>(null)
    private var hasMoved by mutableStateOf(false)
    private var isGameActive by mutableStateOf(false) // Track if the game is active
    private var showInstructions by mutableStateOf(false) // Track if instructions should be shown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ArimaaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showInstructions) {
                        InstructionsScreen(onBack = { showInstructions = false }) // Show instructions screen
                    } else if (isGameActive) {
                        GameScreen(innerPadding = innerPadding) // Game screen
                    } else {
                        HomeScreen(onStartGame = {
                            startGame() // Start the game when button is clicked
                        }, onShowInstructions = {
                            showInstructions = true // Show instructions when button is clicked
                        })
                    }
                }
            }
        }
    }

    private fun startGame() {
        arimaaBoard = ArimaaBoard() // Initialize the board
        isGameActive = true // Set the game as active
        currentPlayer = Player.GOLD
        movesLeft = 4
        selectedCell = null
        hasMoved = false
    }

    @Composable
    fun GameScreen(innerPadding: PaddingValues) {
        var validMoves by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }

        Column(modifier = Modifier.padding(innerPadding)) {
            Text("Arimaa Game", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            // Show moves left
            Text("Moves Left: $movesLeft", style = MaterialTheme.typography.bodyMedium)

            // Highlight the current player's turn
            Text(
                text = "${currentPlayer.name}'s Turn",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = if (currentPlayer == Player.GOLD) Color.Yellow else Color.LightGray)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (y in arimaaBoard.board.indices) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (x in arimaaBoard.board[y].indices) {
                            val cell = arimaaBoard.board[y][x]
                            val isSelected = selectedCell == Pair(x, y)
                            val isValidMove = validMoves.contains(Pair(x, y))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        color = when {
                                            isValidMove -> Color.Red.copy(alpha = 0.5f)
                                            (x + y) % 2 == 0 -> Color.White
                                            else -> Color.Gray
                                        }
                                    )
                                    .clickable {
                                        val piece = arimaaBoard.board[y][x].piece

                                        if (selectedCell != null && piece != null && piece.player == currentPlayer) {
                                            // Change selection to the new piece
                                            selectedCell = Pair(x, y)
                                            validMoves = getValidMoves(x, y, piece)
                                        } else if (selectedCell == null) {
                                            if (piece != null && piece.player == currentPlayer) {
                                                selectedCell = Pair(x, y)
                                                validMoves = getValidMoves(x, y, piece)
                                            }
                                        } else {
                                            val (startX, startY) = selectedCell!!
                                            if (validMoves.contains(Pair(x, y))) {
                                                // Move the piece
                                                if (arimaaBoard.movePiece(startX, startY, x, y, currentPlayer)) {
                                                    movesLeft--
                                                    hasMoved = true // Mark that a move has been made

                                                    if (movesLeft == 0) {
                                                        movesLeft = 4
                                                        currentPlayer = if (currentPlayer == Player.GOLD) Player.SILVER else Player.GOLD
                                                    }

                                                    selectedCell = null
                                                    validMoves = emptyList()
                                                } else {
                                                    Toast.makeText(this@MainActivity, "Invalid move", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val piece = cell.piece
                                if (piece != null) {
                                    val imageResource = when (piece.type) {
                                        PieceType.ELEPHANT -> if (piece.player == Player.GOLD) R.drawable.elegold else R.drawable.elesilver
                                        PieceType.CAMEL -> if (piece.player == Player.GOLD) R.drawable.camelgold else R.drawable.camelsilver
                                        PieceType.HORSE -> if (piece.player == Player.GOLD) R.drawable.horsegold else R.drawable.horsesilver
                                        PieceType.DOG -> if (piece.player == Player.GOLD) R.drawable.doggold else R.drawable.dogsilver
                                        PieceType.CAT -> if (piece.player == Player.GOLD) R.drawable.catgold else R.drawable.catsilver
                                        PieceType.RABBIT -> if (piece.player == Player.GOLD) R.drawable.rabbitgold else R.drawable.rabbitsilver
                                    }

                                    Image(
                                        painter = painterResource(id = imageResource),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    )
                                } else {
                                    Text(
                                        text = "",
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        if (!hasMoved) {
                            // Show a message that a move must be made first
                            Toast.makeText(this@MainActivity, "You must make at least one move before ending your turn.", Toast.LENGTH_SHORT).show()
                        } else {
                            // End turn logic
                            movesLeft = 4
                            selectedCell = null
                            validMoves = emptyList()
                            hasMoved = false // Reset the move flag
                            currentPlayer = if (currentPlayer == Player.GOLD) Player.SILVER else Player.GOLD
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("End Turn")
                }

                Button(
                    onClick = {
                        resetGame() // Call the reset function
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Reset Game")
                }
            }
        }
    }

    private fun getValidMoves(x: Int, y: Int, piece: Piece): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(
            Pair(0, -1), // Up
            Pair(0, 1),  // Down
            Pair(-1, 0), // Left
            Pair(1, 0)   // Right
        )

        // Determine the maximum movement distance for each piece type
        val maxMoveDistance = when (piece.type) {
            PieceType.ELEPHANT, PieceType.CAMEL -> 2 // Elephant and Camel can move up to 2 spaces
            else -> 1 // Other pieces can move 1 space
        }

        for ((dx, dy) in directions) {
            for (distance in 1..maxMoveDistance) { // Loop through the possible distances
                val newX = x + dx * distance
                val newY = y + dy * distance

                if (newX in 0..7 && newY in 0..7) {
                    val targetCell = arimaaBoard.board[newY][newX]

                    // Ruling out backward movement for rabbits
                    if (piece.type == PieceType.RABBIT && ((piece.player == Player.GOLD && dy == -1) || (piece.player == Player.SILVER && dy == 1))) break

                    if (targetCell.piece == null || targetCell.piece?.player != piece.player) {
                        moves.add(Pair(newX, newY))
                    }

                    // If a piece is found, stop checking further in this direction
                    if (targetCell.piece != null) {
                        break
                    }
                } else {
                    break // Out of bounds
                }
            }
        }

        return moves
    }

    private fun resetGame() {
        // Reset the game state
        arimaaBoard = ArimaaBoard()
        currentPlayer = Player.GOLD
        movesLeft = 4
        selectedCell = null
        hasMoved = false
        isGameActive = false // Set game to inactive
    }

    @Composable
    fun HomeScreen(onStartGame: () -> Unit, onShowInstructions: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to Arimaa!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onStartGame) {
                Text("Start Game")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onShowInstructions) {
                Text("Instructions")
            }
        }
    }

    @Composable
    fun InstructionsScreen(onBack: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Instructions", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Game instructions
            Text(
                """
                Arimaa is a two-player strategy board game. The objective is to move your pieces into your opponent's den, located at the back row of their side of the board.

                The game consists of various pieces:
                - Elephant: Moves up to 2 spaces in any direction.
                - Camel: Moves up to 2 spaces in any direction.
                - Horse: Moves 1 space in any direction.
                - Dog: Moves 1 space in any direction.
                - Cat: Moves 1 space in any direction.
                - Rabbit: Moves 1 space in any direction, but cannot move backward.

                Players take turns to move their pieces. The game ends when a player successfully moves one of their pieces into the opponent's den.

                Remember to block your opponent while trying to advance your pieces!
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onBack) {
                Text("Back to Home")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun HomeScreenPreview() {
        ArimaaTheme {
            HomeScreen(onStartGame = {}, onShowInstructions = {})
        }
    }
}
