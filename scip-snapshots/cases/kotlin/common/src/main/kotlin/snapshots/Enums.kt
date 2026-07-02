package snapshots

enum class Suit(val symbol: Char) {
    HEARTS('h'),
    SPADES('s');

    fun isRed(): Boolean = symbol == 'h'

    companion object {
        fun fromSymbol(symbol: Char): Suit? = entries.find { it.symbol == symbol }
    }
}

fun describe(suit: Suit): String =
    when (suit) {
        Suit.HEARTS -> "red"
        Suit.SPADES -> "black"
    }
