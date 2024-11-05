package com.example.arimaa

class ArimaaBoard {
    var board: List<MutableList<BoardCell>> = List(8) { MutableList(8) { BoardCell() } }

    init {
        // Initialize trap squares
        board[2][2].isTrap = true
        board[2][5].isTrap = true
        board[5][2].isTrap = true
        board[5][5].isTrap = true

        // Place initial pieces
        placeInitialPieces()
    }

    private fun placeInitialPieces() {
        // Gold pieces
        board[0][0].piece = Piece(PieceType.ELEPHANT, Player.GOLD)
        board[0][1].piece = Piece(PieceType.CAMEL, Player.GOLD)
        board[0][2].piece = Piece(PieceType.HORSE, Player.GOLD)
        board[0][3].piece = Piece(PieceType.HORSE, Player.GOLD)
        board[0][4].piece = Piece(PieceType.DOG, Player.GOLD)
        board[0][5].piece = Piece(PieceType.DOG, Player.GOLD)
        board[0][6].piece = Piece(PieceType.CAT, Player.GOLD)
        board[0][7].piece = Piece(PieceType.CAT, Player.GOLD)
        for (i in 0 until 8) {
            board[1][i].piece = Piece(PieceType.RABBIT, Player.GOLD)
        }

        // Silver pieces
        board[7][0].piece = Piece(PieceType.ELEPHANT, Player.SILVER)
        board[7][1].piece = Piece(PieceType.CAMEL, Player.SILVER)
        board[7][2].piece = Piece(PieceType.HORSE, Player.SILVER)
        board[7][3].piece = Piece(PieceType.HORSE, Player.SILVER)
        board[7][4].piece = Piece(PieceType.DOG, Player.SILVER)
        board[7][5].piece = Piece(PieceType.DOG, Player.SILVER)
        board[7][6].piece = Piece(PieceType.CAT, Player.SILVER)
        board[7][7].piece = Piece(PieceType.CAT, Player.SILVER)
        for (i in 0 until 8) {
            board[6][i].piece = Piece(PieceType.RABBIT, Player.SILVER)
        }
    }

    fun reset() {
        board = List(8) { MutableList(8) { BoardCell() } } // Reset board to empty cells
        placeInitialPieces() // Call to set up the initial state of the game again
    }

    fun movePiece(startX: Int, startY: Int, destX: Int, destY: Int, currentPlayer: Player): Boolean {
        val piece = board[startY][startX].piece ?: return false
        if (piece.player != currentPlayer) return false

        // Check if the destination cell is empty
        if (board[destY][destX].piece == null) {
            // Regular move
            board[destY][destX].piece = piece
            board[startY][startX].piece = null
            return true
        }

        // Pushing mechanic
        val targetPiece = board[destY][destX].piece
        if (targetPiece != null && targetPiece.player != currentPlayer) {
            // Check if the piece can push the target
            if (piece.type.strength > targetPiece.type.strength) {
                // Calculate opposite position for pushing
                val pushX = destX + (destX - startX)
                val pushY = destY + (destY - startY)

                // Ensure the push destination is within bounds and empty
                if (pushX in 0..7 && pushY in 0..7 && board[pushY][pushX].piece == null) {
                    // Perform the push
                    board[pushY][pushX].piece = targetPiece
                    board[destY][destX].piece = piece
                    board[startY][startX].piece = null
                    return true
                }
            }
        }
        return false
    }
}
