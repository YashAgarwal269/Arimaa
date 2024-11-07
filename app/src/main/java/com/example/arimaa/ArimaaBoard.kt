package com.example.arimaa

class ArimaaBoard {
    var board: List<MutableList<BoardCell>> = List(8) { MutableList(8) { BoardCell() } }
    private val trapCells = setOf(Pair(2, 2), Pair(2, 5), Pair(5, 2), Pair(5, 5))

    init {
        // Initialize trap squares
        trapCells.forEach { (x, y) ->
            board[y][x].isTrap = true
        }
        placeInitialPieces()
    }

    fun capturePiece(x: Int, y: Int) {
        board[y][x].piece = null
    }

    private fun placeInitialPieces() {
        // Initialize Gold and Silver pieces on the board (as specified)
        // Place Gold pieces
        listOf(
            Pair(0, Piece(PieceType.ELEPHANT, Player.GOLD)),
            Pair(1, Piece(PieceType.CAMEL, Player.GOLD)),
            Pair(2, Piece(PieceType.HORSE, Player.GOLD)),
            Pair(3, Piece(PieceType.HORSE, Player.GOLD)),
            Pair(4, Piece(PieceType.DOG, Player.GOLD)),
            Pair(5, Piece(PieceType.DOG, Player.GOLD)),
            Pair(6, Piece(PieceType.CAT, Player.GOLD)),
            Pair(7, Piece(PieceType.CAT, Player.GOLD))
        ).forEach { (x, piece) -> board[0][x].piece = piece }

        for (i in 0 until 8) {
            board[1][i].piece = Piece(PieceType.RABBIT, Player.GOLD)
        }

        // Place Silver pieces
        listOf(
            Pair(0, Piece(PieceType.ELEPHANT, Player.SILVER)),
            Pair(1, Piece(PieceType.CAMEL, Player.SILVER)),
            Pair(2, Piece(PieceType.HORSE, Player.SILVER)),
            Pair(3, Piece(PieceType.HORSE, Player.SILVER)),
            Pair(4, Piece(PieceType.DOG, Player.SILVER)),
            Pair(5, Piece(PieceType.DOG, Player.SILVER)),
            Pair(6, Piece(PieceType.CAT, Player.SILVER)),
            Pair(7, Piece(PieceType.CAT, Player.SILVER))
        ).forEach { (x, piece) -> board[7][x].piece = piece }

        for (i in 0 until 8) {
            board[6][i].piece = Piece(PieceType.RABBIT, Player.SILVER)
        }
    }

    fun reset() {
        board = List(8) { MutableList(8) { BoardCell() } }
        placeInitialPieces()
    }

    private fun removeIfOnTrap(x: Int, y: Int) {
        val cell = board[y][x]
        val piece = cell.piece
        if (cell.isTrap && piece != null) {
            // Check for any adjacent friendly piece
            val adjacentFriendly = listOf(
                Pair(x - 1, y), Pair(x + 1, y),
                Pair(x, y - 1), Pair(x, y + 1)
            ).any { (adjX, adjY) ->
                adjX in 0..7 && adjY in 0..7 && board[adjY][adjX].piece?.player == piece.player
            }
            // Remove the piece if no adjacent friendly piece is present
            if (!adjacentFriendly) {
                cell.piece = null
            }
        }
    }


    fun movePiece(startX: Int, startY: Int, destX: Int, destY: Int, currentPlayer: Player): Boolean {
        val piece = board[startY][startX].piece ?: return false
        if (piece.player != currentPlayer) return false

        if (piece.frozen) {
            val adjacentFriendly = listOf(
                Pair(startX - 1, startY), Pair(startX + 1, startY),
                Pair(startX, startY - 1), Pair(startX, startY + 1)
            ).any { (adjX, adjY) ->
                adjX in 0..7 && adjY in 0..7 && board[adjY][adjX].piece?.player == currentPlayer
            }
            if (!adjacentFriendly) return false
        }

        // Ensure that the rabbit can move to the last row (final rank)
        if (piece.type == PieceType.RABBIT) {
            // Gold player cannot move backward (to higher rows)
            if (piece.player == Player.GOLD && destY < startY) return false
            // Silver player cannot move backward (to lower rows)
            if (piece.player == Player.SILVER && destY > startY) return false
        }

        if (board[destY][destX].piece == null) {
            // Move the piece to the destination
            board[destY][destX].piece = piece
            board[startY][startX].piece = null
            removeIfOnTrap(destX, destY)
            checkForFreezing(destX, destY)

            // Debug: Log the movement of the rabbit
            if (piece.type == PieceType.RABBIT) {
                println("Rabbit moved to ($destX, $destY)")

                if ((piece.player == Player.GOLD && destY == 7) || (piece.player == Player.SILVER && destY == 0)) {
                    println("${piece.player.name} rabbit reached the last rank!")
                    showWinMessage(piece.player)  // Trigger the win screen
                }
            }

            return true
        }

        val targetPiece = board[destY][destX].piece
        if (targetPiece != null && targetPiece.player != currentPlayer) {
            if (piece.type.strength <= targetPiece.type.strength) return false

            val pushX = destX + (destX - startX)
            val pushY = destY + (destY - startY)

            if (pushX in 0..7 && pushY in 0..7 && board[pushY][pushX].piece == null) {
                board[pushY][pushX].piece = targetPiece
                board[destY][destX].piece = piece
                board[startY][startX].piece = null

                // Remove pieces on traps and check for freezing
                removeIfOnTrap(destX, destY)
                removeIfOnTrap(pushX, pushY)
                checkForFreezing(pushX, pushY)
                return true
            }
        }
        return false
    }

    fun showWinMessage(winner: Player) {
        // Debugging: Check if this method is being called
        println("Game Over! ${winner.name} wins by getting their rabbit to the last row!")

    }

    fun getPullablePiece(x: Int, y: Int, player: Player): List<Pair<Int, Int>> {
        val pullablePositions = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(Pair(0, -1), Pair(0, 1), Pair(-1, 0), Pair(1, 0)) // Up, Down, Left, Right

        val pullingPiece = board[y][x].piece ?: return emptyList()

        for ((dx, dy) in directions) {
            val adjacentX = x + dx
            val adjacentY = y + dy

            // Check if adjacent position is within bounds
            if (adjacentX in 0..7 && adjacentY in 0..7) {
                val adjacentCell = board[adjacentY][adjacentX]
                val adjacentPiece = adjacentCell.piece

                // Check if the adjacent piece belongs to the opponent and is weaker
                if (adjacentPiece != null && adjacentPiece.player != player && pullingPiece.type.strength > adjacentPiece.type.strength) {
                    // Now check if there is an open space to pull the piece into
                    val pullTargetX = x - dx
                    val pullTargetY = y - dy
                    if (pullTargetX in 0..7 && pullTargetY in 0..7 && board[pullTargetY][pullTargetX].piece == null) {
                        pullablePositions.add(Pair(adjacentX, adjacentY))
                    }
                }
            }
        }
        return pullablePositions
    }

    fun pullPiece(startX: Int, startY: Int, destX: Int, destY: Int, currentPlayer: Player): Boolean {
        val pullingPiece = board[startY][startX].piece ?: return false
        if (pullingPiece.player != currentPlayer) return false

        // Get the piece to be pulled from the adjacent cell
        val targetPiece = board[destY][destX].piece ?: return false
        if (targetPiece.player == currentPlayer || pullingPiece.type.strength <= targetPiece.type.strength) return false

        // Calculate the target position where the pulled piece will move
        val pullX = startX + (startX - destX)
        val pullY = startY + (startY - destY)

        // Check if the destination to pull the piece is valid (within bounds and empty)
        if (pullX !in 0..7 || pullY !in 0..7 || board[pullY][pullX].piece != null) return false

        // Perform the pull
        // Move the pulling piece into the position of the target piece
        board[startY][startX].piece = targetPiece
        board[destY][destX].piece = null

        // Move the pulled piece to the calculated position
        board[pullY][pullX].piece = pullingPiece
        board[startY][startX].piece = null

        // Check for trap conditions after the move
        removeIfOnTrap(pullX, pullY)
        removeIfOnTrap(startX, startY)

        // Check for freezing after the move
        checkForFreezing(pullX, pullY)
        checkForFreezing(startX, startY)

        return true
    }

    // Method to get pullable positions for a piec

    private fun checkForFreezing(x: Int, y: Int) {
        val piece = board[y][x].piece ?: return
        val directions = listOf(
            Pair(x - 1, y), Pair(x + 1, y),
            Pair(x, y - 1), Pair(x, y + 1)
        )

        for ((adjX, adjY) in directions) {
            if (adjX in 0..7 && adjY in 0..7) {
                val adjacentPiece = board[adjY][adjX].piece
                if (adjacentPiece != null && adjacentPiece.player != piece.player) {
                    if (adjacentPiece.type.strength > piece.type.strength) {
                        piece.frozen = true
                    }
                }
            }
        }
    }
    fun isTrapCell(x: Int, y: Int): Boolean {
        return trapCells.contains(Pair(x, y))
    }
}