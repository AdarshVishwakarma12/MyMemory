package com.example.mymemory.models

import com.example.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize) {
    val cards: List<MemoryCard>

    var numPairsFound = 0
    var foundMatch: Boolean = false

    private var indexOfSingleSelectedCard: Int? = null

    init {
        val chosenImages = DEFAULT_ICONS.shuffled().takeLast(boardSize.getNumPairs())
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map { MemoryCard(it, false, false) }
    }

    internal fun flipCard(position: kotlin.Int): Boolean {
        val card = cards[position]

        // Three Cases
        // 0 Cards previously flipped over => restore cards + Flip over the selected card
        // 1 Cards previously flipped over => Flip over the current card + check if the image match
        // 2 Cards previously flipped over => restore cards + Flip over the selected card
        if(indexOfSingleSelectedCard == null) {
            // 0 or 2 cards previously flipped over
            restoreCard()
            indexOfSingleSelectedCard = position
        } else {
            var foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): kotlin.Boolean {
        if(cards[position1].identifier == cards[position2].identifier) {
            cards[position1].isMatched = true
            cards[position2].isMatched = true
            numPairsFound += 1
            return true
        }
        return false
    }

    private fun restoreCard() {
        for(card in cards) {
            if(card.isMatched == false) card.isFaceUp = false
        }
    }

    internal fun hasWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    internal fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }
}
