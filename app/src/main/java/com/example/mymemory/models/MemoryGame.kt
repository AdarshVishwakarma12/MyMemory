package com.example.mymemory.models

import com.example.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize) {
    val cards: List<MemoryCard>

    val numPairsFound = 0
    init {
        val chosenImages = DEFAULT_ICONS.shuffled().takeLast(boardSize.getNumPairs())
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map { MemoryCard(it, false, false) }
    }

    internal fun flipCard(position: kotlin.Int) {
        val card = cards[position]

        // Three Cases
        // 0 Cards previously flipped over => restore cards + Flip over the selected card
        // 1 Cards previously flipped over => Flip over the current card + check if the image match
        // 2 Cards previously flipped over => restore cards + Flip over the selected card
        card.isFaceUp = !card.isFaceUp
    }
}
