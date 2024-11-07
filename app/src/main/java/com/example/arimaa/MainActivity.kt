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
import androidx. compose. material3.CheckboxDefaults. colors

class MainActivity : ComponentActivity() {
    private lateinit var arimaaBoard: ArimaaBoard
    private var currentPlayer by mutableStateOf(Player.GOLD)
    private var movesLeft by mutableStateOf(4)
    private var selectedCell by mutableStateOf<Pair<Int, Int>?>(null)
    private var hasMoved by mutableStateOf(false)
    private var isGameActive by mutableStateOf(false)
    private var showInstructions by mutableStateOf(false)
    val brownColor = Color(0x8B4513)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ArimaaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showInstructions) {
                        InstructionsScreen(onBack = { showInstructions = false })
                    } else if (isGameActive) {
                        GameScreen(innerPadding = innerPadding)
                    } else {
                        HomeScreen(
                            onStartGame = { startGame() },
                            onShowInstructions = { showInstructions = true }
                        )
                    }
                }
            }
        }
    }

    private fun startGame() {
        arimaaBoard = ArimaaBoard()
        isGameActive = true
        currentPlayer = Player.GOLD
        movesLeft = 4
        selectedCell = null
        hasMoved = false
    }

    private fun checkWinCondition() {
        // Check if any of the current player's rabbits reached the opponent's home row
        for (y in arimaaBoard.board.indices) {
            for (x in arimaaBoard.board[y].indices) {
                val piece = arimaaBoard.board[y][x].piece
                if (piece != null && piece.type == PieceType.RABBIT) {
                    if ((piece.player == Player.GOLD && y == 0) || (piece.player == Player.SILVER && y == 7)) {
                        // A rabbit has reached the opponent's home row
                        showWinMessage(piece.player)
                        return
                    }
                }
            }
        }
    }

    private fun showWinMessage(winningPlayer: Player) {
        // Show the winner message
        Toast.makeText(this, "${winningPlayer.name} wins!", Toast.LENGTH_LONG).show()
        isGameActive = false
    }

    @Composable
    fun GameScreen(innerPadding: PaddingValues) {
        var validMoves by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
        var showCaptureDialog by remember { mutableStateOf(false) }
        var trapCellPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var strongerPieceCanCapture by remember { mutableStateOf(false) }

        val gameScreenBackgroundColor = Color(0xFFBFA38E) // Light brown color for the game screen

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(gameScreenBackgroundColor) // Set the background color of the screen
        ) {
            Text("Arimaa Game", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Moves Left: $movesLeft", style = MaterialTheme.typography.bodyMedium)

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
                                            arimaaBoard.isTrapCell(x, y) -> Color.Black.copy(alpha = 0.7f)
                                            isValidMove -> Color.Red.copy(alpha = .5f)
                                            (x + y) % 2 == 0 -> Color.White
                                            else -> Color(0xFF8B4513)
                                        }
                                    )
                                    .clickable {

                                        val piece = arimaaBoard.board[y][x].piece

                                        if (selectedCell != null && piece != null && piece.player == currentPlayer) {
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
                                                val isTrapCell = arimaaBoard.isTrapCell(x, y)
                                                val targetCell = arimaaBoard.board[y][x]
                                                val targetPiece = targetCell.piece

                                                if (isTrapCell && targetPiece != null && targetPiece.player != currentPlayer) {
                                                    // Check if the moving piece is stronger than the piece in the trap cell
                                                    val movingPiece = arimaaBoard.board[startY][startX].piece
                                                    if (movingPiece != null && movingPiece.strength > targetPiece.strength) {
                                                        // Set trap position and allow capture
                                                        trapCellPosition = Pair(x, y)
                                                        showCaptureDialog = true
                                                        strongerPieceCanCapture = true
                                                    } else {
                                                        // Do not allow capture if the moving piece is weaker
                                                        strongerPieceCanCapture = false
                                                    }
                                                } else if (arimaaBoard.movePiece(startX, startY, x, y, currentPlayer)) {
                                                    movesLeft--
                                                    hasMoved = true

                                                    checkWinCondition()  // Check for win after each move

                                                    if (movesLeft == 0) {
                                                        endTurn()
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
                            Toast.makeText(this@MainActivity, "You must make at least one move before ending your turn.", Toast.LENGTH_SHORT).show()
                        } else {
                            movesLeft = 4
                            selectedCell = null
                            validMoves = emptyList()
                            hasMoved = false
                            currentPlayer = if (currentPlayer == Player.GOLD) Player.SILVER else Player.GOLD
                        }
                    },colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B4513), // Dark brown color for the button background
                        contentColor = Color.White // White text color
                    ),

                    modifier = Modifier.weight(1f)
                ) {
                    Text("End Turn")
                }

                Button(
                    onClick = {
                        // Quit the game and return to home screen
                        isGameActive = false
                        Toast.makeText(this@MainActivity, "Game has been quit.", Toast.LENGTH_SHORT).show()
                    },colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B4513), // Dark brown color for the button background
                        contentColor = Color.White // White text color
                    ),

                    modifier = Modifier.weight(1f)
                ) {
                    Text("Quit Game")
                }
            }
        }
    }


    @Composable
    fun GameOverDialog(winner: Player) {
        AlertDialog(
            onDismissRequest = { /* Handle dismiss */ },
            title = { Text("Game Over!") },
            text = { Text("${winner.name} wins!") },
            confirmButton = {
                Button(onClick = { /* Handle restart or exit */ }) {
                    Text("Ok")
                }
            }
        )
    }



    private fun getValidMoves(x: Int, y: Int, piece: Piece): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(Pair(0, -1), Pair(0, 1), Pair(-1, 0), Pair(1, 0))

        val maxMoveDistance = when (piece.type) {
            PieceType.ELEPHANT, PieceType.CAMEL -> 2
            else -> 1
        }

        for ((dx, dy) in directions) {
            for (distance in 1..maxMoveDistance) {
                val newX = x + dx * distance
                val newY = y + dy * distance

                if (newX in 0..7 && newY in 0..7) {
                    val targetCell = arimaaBoard.board[newY][newX]

                    if (piece.type == PieceType.RABBIT && ((piece.player == Player.GOLD && dy == -1) || (piece.player == Player.SILVER && dy == 1))) break

                    if (targetCell.piece == null || targetCell.piece?.player != piece.player) {
                        moves.add(Pair(newX, newY))
                    }

                    if (targetCell.piece != null) {
                        break
                    }
                } else {
                    break
                }
            }
        }
        return moves
    }

    private fun endTurn() {
        if (hasMoved) {
            movesLeft = 4
            selectedCell = null
            hasMoved = false
            currentPlayer = if (currentPlayer == Player.GOLD) Player.SILVER else Player.GOLD
        }
    }

    private fun resetGame() {
        arimaaBoard = ArimaaBoard()
        currentPlayer = Player.GOLD
        movesLeft = 4
        selectedCell = null
        hasMoved = false
        isGameActive = false
    }

    @Composable
    fun HomeScreen(onStartGame: () -> Unit, onShowInstructions: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize() // Ensures the Box takes up the entire screen
                .background(Color(0xFFD2B48C)) // Light Brown color background (Hex code for light brown)
        ) {
            Image(
                painter = painterResource(id = R.drawable.front_s), // Your background image
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize() // Make the image fill the entire screen
                    .align(Alignment.Center) // Align the image at the center (optional)
            )
            // UI elements on top of the light brown background
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to Arimaa!",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Black // Make sure the text is visible against the light brown background
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B4513), // Dark brown color for the button background
                        contentColor = Color.White // White text color
                    ),
                    modifier = Modifier
                        .fillMaxWidth() // Ensures button takes up full width
                        .padding(horizontal = 16.dp) // Optional: Add some horizontal padding for better appearance
                ) {
                    Text(
                        text = "Start Game",
                        style = MaterialTheme.typography.bodyLarge, // Optional: Customize text style
                        textAlign = TextAlign.Center // Ensure text is centered in the button
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onShowInstructions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B4513), // Dark brown color for the button background
                        contentColor = Color.White // White text color
                    ),
                    modifier = Modifier
                        .fillMaxWidth() // Ensures button takes up full width
                        .padding(horizontal = 16.dp) // Optional: Add some horizontal padding for better appearance
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.bodyLarge, // Optional: Customize text style
                        textAlign = TextAlign.Center // Ensure text is centered in the button
                    )
                }
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