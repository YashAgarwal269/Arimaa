package com.example.arimaa

data class BoardCell(
    var piece: Piece? = null,
    var isTrap: Boolean = false  // Change to var
)
