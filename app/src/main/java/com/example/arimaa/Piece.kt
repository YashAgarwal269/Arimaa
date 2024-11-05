package com.example.arimaa

enum class PieceType(val strength: Int) {
    ELEPHANT(6),
    CAMEL(5),
    HORSE(4),
    DOG(3),
    CAT(2),
    RABBIT(1)
}

enum class Player {
    GOLD, SILVER
}

class Piece(val type: PieceType, val player: Player) {
    // Define the strength of each piece
    val strength: Int
        get() = when (type) {
            PieceType.ELEPHANT -> 5
            PieceType.CAMEL -> 4
            PieceType.HORSE -> 3
            PieceType.DOG -> 2
            PieceType.CAT -> 1
            PieceType.RABBIT -> 0
        }

    override fun toString(): String {
        return "$player $type"
    }
}
