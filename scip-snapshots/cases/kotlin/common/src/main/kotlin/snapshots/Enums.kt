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

enum class Color { RED, GREEN, BLUE }

fun useEnum(): Color = Color.RED

enum class Op {
    // The entry body is an anonymous subclass; its override has a local symbol.
    PLUS {
        override fun apply(a: Int, b: Int) = a + b
    };

    abstract fun apply(a: Int, b: Int): Int
}
