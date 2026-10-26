  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#values().
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#valueOf().
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#valueOf().(value)
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#entries.
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#entries.get().
//           ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#`<init>`().
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#`<init>`().(symbol)
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#symbol.
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#symbol.get().
  enum class Suit(val symbol: Char) {
//           ^^^^ definition scip-java maven . . snapshots/Suit#
//                kind Class
//                display_name Suit
//                signature_documentation
//                > public final enum class Suit : Enum<Suit>
//                relationship scip-java maven . . kotlin/Enum# implementation
//           ^^^^ definition scip-java maven . . snapshots/Suit#`<init>`().
//                kind Constructor
//                display_name Suit
//                signature_documentation
//                > private constructor(symbol: Char): Suit
//           ^^^^ definition scip-java maven . . snapshots/Suit#values().
//                kind Method
//                display_name values
//                signature_documentation
//                > public final static fun values(): Array<Suit>
//           ^^^^ definition scip-java maven . . snapshots/Suit#valueOf().
//                kind Method
//                display_name valueOf
//                signature_documentation
//                > public final static fun valueOf(value: String): Suit
//           ^^^^ definition scip-java maven . . snapshots/Suit#valueOf().(value)
//                kind Parameter
//                display_name value
//                signature_documentation
//                > value: String
//           ^^^^ definition scip-java maven . . snapshots/Suit#entries.
//                kind Property
//                display_name entries
//                signature_documentation
//                > public final static val entries: EnumEntries<Suit>
//           ^^^^ definition scip-java maven . . snapshots/Suit#entries.get().
//                kind Method
//                display_name entries
//                signature_documentation
//                > public static get(): EnumEntries<Suit>
//                    ^^^^^^ definition scip-java maven . . snapshots/Suit#`<init>`().(symbol)
//                           kind Parameter
//                           display_name symbol
//                           signature_documentation
//                           > symbol: Char
//                    ^^^^^^ definition scip-java maven . . snapshots/Suit#symbol.
//                           kind Property
//                           display_name symbol
//                           signature_documentation
//                           > public final val symbol: Char
//                    ^^^^^^ reference scip-java maven . . snapshots/Suit#`<init>`().(symbol)
//                    ^^^^^^ definition scip-java maven . . snapshots/Suit#symbol.get().
//                           kind Method
//                           display_name symbol
//                           signature_documentation
//                           > public get(): Char
//                            ^^^^ reference scip-java maven . . kotlin/Char#
//                               ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#`<init>`().(symbol)
//                               ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#symbol.
//                               ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#symbol.get().
//                                ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#`<init>`().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#HEARTS.
      HEARTS('h'),
//    ^^^^^^ definition scip-java maven . . snapshots/Suit#HEARTS.
//           kind EnumMember
//           display_name HEARTS
//           signature_documentation
//           > public final static enum entry HEARTS: Suit
//               ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#HEARTS.
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#SPADES.
      SPADES('s');
//    ^^^^^^ definition scip-java maven . . snapshots/Suit#SPADES.
//           kind EnumMember
//           display_name SPADES
//           signature_documentation
//           > public final static enum entry SPADES: Suit
//               ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#SPADES.
  
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#isRed().
      fun isRed(): Boolean = symbol == 'h'
//        ^^^^^ definition scip-java maven . . snapshots/Suit#isRed().
//              kind Method
//              display_name isRed
//              signature_documentation
//              > public final fun isRed(): Boolean
//                 ^^^^^^^ reference scip-java maven . . kotlin/Boolean#
//                           ^^^^^^ reference scip-java maven . . snapshots/Suit#symbol.
//                           ^^^^^^ reference scip-java maven . . snapshots/Suit#symbol.get().
//                                  ^^ reference scip-java maven . . kotlin/Char#equals().
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#isRed().
  
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#Companion#
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#Companion#`<init>`().
      companion object {
//    ^^^^^^^^^ definition scip-java maven . . snapshots/Suit#Companion#
//              kind Class
//              display_name Companion
//              signature_documentation
//              > public final companion object Companion : Any
//    ^^^^^^^^^ definition scip-java maven . . snapshots/Suit#Companion#`<init>`().
//              kind Constructor
//              display_name Companion
//              signature_documentation
//              > private constructor(): Suit.Companion
//        ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#Companion#fromSymbol().
//                       ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#Companion#fromSymbol().(symbol)
//                                                           ⌄ enclosing_range_start local 0
          fun fromSymbol(symbol: Char): Suit? = entries.find { it.symbol == symbol }
//            ^^^^^^^^^^ definition scip-java maven . . snapshots/Suit#Companion#fromSymbol().
//                       kind Method
//                       display_name fromSymbol
//                       signature_documentation
//                       > public final fun fromSymbol(symbol: Char): Suit?
//                       ^^^^^^ definition scip-java maven . . snapshots/Suit#Companion#fromSymbol().(symbol)
//                              kind Parameter
//                              display_name symbol
//                              signature_documentation
//                              > symbol: Char
//                               ^^^^ reference scip-java maven . . kotlin/Char#
//                                      ^^^^ reference scip-java maven . . snapshots/Suit#
//                                              ^^^^^^^ reference scip-java maven . . snapshots/Suit#entries.
//                                              ^^^^^^^ reference scip-java maven . . snapshots/Suit#entries.get().
//                                                      ^^^^ reference scip-java maven . . kotlin/collections/Iterable#find().
//                                                           ^^^^^^^^^^^^^^^^^^^^^^^ definition local 0
//                                                                                   kind Parameter
//                                                                                   display_name it
//                                                                                   signature_documentation
//                                                                                   > it: Suit
//                                                             ^^ reference local 0
//                                                                ^^^^^^ reference scip-java maven . . snapshots/Suit#symbol.
//                                                                ^^^^^^ reference scip-java maven . . snapshots/Suit#symbol.get().
//                                                                       ^^ reference scip-java maven . . kotlin/Char#equals().
//                                                                          ^^^^^^ reference scip-java maven . . snapshots/Suit#Companion#fromSymbol().(symbol)
//                                  ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion#fromSymbol().(symbol)
//                                                                                 ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion#fromSymbol().
//                                                                                 ⌃ enclosing_range_end local 0
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion#
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion#`<init>`().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#values().
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#valueOf().
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#valueOf().(value)
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#entries.
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#entries.get().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/describe().
//             ⌄ enclosing_range_start scip-java maven . . snapshots/describe().(suit)
  fun describe(suit: Suit): String =
//    ^^^^^^^^ definition scip-java maven . . snapshots/describe().
//             kind Method
//             display_name describe
//             signature_documentation
//             > public final fun describe(suit: Suit): String
//             ^^^^ definition scip-java maven . . snapshots/describe().(suit)
//                  kind Parameter
//                  display_name suit
//                  signature_documentation
//                  > suit: Suit
//                   ^^^^ reference scip-java maven . . snapshots/Suit#
//                          ^^^^^^ reference scip-java maven . . kotlin/String#
//                      ⌃ enclosing_range_end scip-java maven . . snapshots/describe().(suit)
//          ⌄ enclosing_range_start local 1
      when (suit) {
//          ^^^^ definition local 1
//               kind Property
//               display_name <when-subject>
//               signature_documentation
//               > local val <when-subject>: Suit
//          ^^^^ reference scip-java maven . . snapshots/describe().(suit)
//             ⌃ enclosing_range_end local 1
          Suit.HEARTS -> "red"
//        ^^^^ reference scip-java maven . . snapshots/Suit#
//             ^^^^^^ reference scip-java maven . . snapshots/Suit#HEARTS.
          Suit.SPADES -> "black"
//        ^^^^ reference scip-java maven . . snapshots/Suit#
//             ^^^^^^ reference scip-java maven . . snapshots/Suit#SPADES.
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/describe().
  
