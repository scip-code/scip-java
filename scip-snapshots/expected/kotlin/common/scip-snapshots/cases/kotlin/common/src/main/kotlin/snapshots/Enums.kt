  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#values().
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#valueOf().
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#valueOf().(value)
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#entries.
//⌄ enclosing_range_start scip-java maven . . snapshots/Suit#getEntries().
//           ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#`<init>`().
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#`<init>`().(symbol)
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#symbol.
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#getSymbol().
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
//           ^^^^ definition scip-java maven . . snapshots/Suit#getEntries().
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
//                    ^^^^^^ definition scip-java maven . . snapshots/Suit#getSymbol().
//                           kind Method
//                           display_name symbol
//                           signature_documentation
//                           > public get(): Char
//                            ^^^^ reference scip-java maven . . kotlin/Char#
//                               ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#`<init>`().(symbol)
//                               ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#symbol.
//                               ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#getSymbol().
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
//                           ^^^^^^ reference scip-java maven . . snapshots/Suit#getSymbol().
//                                  ^^ reference scip-java maven . . kotlin/Char#equals().
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#isRed().
  
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#Companion#
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#Companion#`<init>`().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Suit#Companion.
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
//    ^^^^^^^^^ definition scip-java maven . . snapshots/Suit#Companion.
//              kind StaticField
//              display_name Companion
//              signature_documentation
//              > public final static val Companion: Suit.Companion
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
//                                              ^^^^^^^ reference scip-java maven . . snapshots/Suit#getEntries().
//                                                      ^^^^ reference scip-java maven . . kotlin/collections/Iterable#find().
//                                                           ^^^^^^^^^^^^^^^^^^^^^^^ definition local 0
//                                                                                   kind Parameter
//                                                                                   display_name it
//                                                                                   signature_documentation
//                                                                                   > it: Suit
//                                                             ^^ reference local 0
//                                                                ^^^^^^ reference scip-java maven . . snapshots/Suit#symbol.
//                                                                ^^^^^^ reference scip-java maven . . snapshots/Suit#getSymbol().
//                                                                       ^^ reference scip-java maven . . kotlin/Char#equals().
//                                                                          ^^^^^^ reference scip-java maven . . snapshots/Suit#Companion#fromSymbol().(symbol)
//                                  ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion#fromSymbol().(symbol)
//                                                                                 ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion#fromSymbol().
//                                                                                 ⌃ enclosing_range_end local 0
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion#
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion#`<init>`().
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Suit#Companion.
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#values().
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#valueOf().
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#valueOf().(value)
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#entries.
//⌃ enclosing_range_end scip-java maven . . snapshots/Suit#getEntries().
  
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
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Color#
//⌄ enclosing_range_start scip-java maven . . snapshots/Color#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/Color#values().
//⌄ enclosing_range_start scip-java maven . . snapshots/Color#valueOf().
//⌄ enclosing_range_start scip-java maven . . snapshots/Color#valueOf().(value)
//⌄ enclosing_range_start scip-java maven . . snapshots/Color#entries.
//⌄ enclosing_range_start scip-java maven . . snapshots/Color#getEntries().
//                   ⌄ enclosing_range_start scip-java maven . . snapshots/Color#RED.
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Color#GREEN.
//                               ⌄ enclosing_range_start scip-java maven . . snapshots/Color#BLUE.
  enum class Color { RED, GREEN, BLUE }
//           ^^^^^ definition scip-java maven . . snapshots/Color#
//                 kind Class
//                 display_name Color
//                 signature_documentation
//                 > public final enum class Color : Enum<Color>
//                 relationship scip-java maven . . kotlin/Enum# implementation
//           ^^^^^ definition scip-java maven . . snapshots/Color#`<init>`().
//                 kind Constructor
//                 display_name Color
//                 signature_documentation
//                 > private constructor(): Color
//           ^^^^^ definition scip-java maven . . snapshots/Color#values().
//                 kind Method
//                 display_name values
//                 signature_documentation
//                 > public final static fun values(): Array<Color>
//           ^^^^^ definition scip-java maven . . snapshots/Color#valueOf().
//                 kind Method
//                 display_name valueOf
//                 signature_documentation
//                 > public final static fun valueOf(value: String): Color
//           ^^^^^ definition scip-java maven . . snapshots/Color#valueOf().(value)
//                 kind Parameter
//                 display_name value
//                 signature_documentation
//                 > value: String
//           ^^^^^ definition scip-java maven . . snapshots/Color#entries.
//                 kind Property
//                 display_name entries
//                 signature_documentation
//                 > public final static val entries: EnumEntries<Color>
//           ^^^^^ definition scip-java maven . . snapshots/Color#getEntries().
//                 kind Method
//                 display_name entries
//                 signature_documentation
//                 > public static get(): EnumEntries<Color>
//                   ^^^ definition scip-java maven . . snapshots/Color#RED.
//                       kind EnumMember
//                       display_name RED
//                       signature_documentation
//                       > public final static enum entry RED: Color
//                        ^^^^^ definition scip-java maven . . snapshots/Color#GREEN.
//                              kind EnumMember
//                              display_name GREEN
//                              signature_documentation
//                              > public final static enum entry GREEN: Color
//                               ^^^^ definition scip-java maven . . snapshots/Color#BLUE.
//                                    kind EnumMember
//                                    display_name BLUE
//                                    signature_documentation
//                                    > public final static enum entry BLUE: Color
//                      ⌃ enclosing_range_end scip-java maven . . snapshots/Color#RED.
//                             ⌃ enclosing_range_end scip-java maven . . snapshots/Color#GREEN.
//                                  ⌃ enclosing_range_end scip-java maven . . snapshots/Color#BLUE.
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Color#
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Color#`<init>`().
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Color#values().
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Color#valueOf().
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Color#valueOf().(value)
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Color#entries.
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Color#getEntries().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/useEnum().
  fun useEnum(): Color = Color.RED
//    ^^^^^^^ definition scip-java maven . . snapshots/useEnum().
//            kind Method
//            display_name useEnum
//            signature_documentation
//            > public final fun useEnum(): Color
//               ^^^^^ reference scip-java maven . . snapshots/Color#
//                       ^^^^^ reference scip-java maven . . snapshots/Color#
//                             ^^^ reference scip-java maven . . snapshots/Color#RED.
//                               ⌃ enclosing_range_end scip-java maven . . snapshots/useEnum().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Op#
//⌄ enclosing_range_start scip-java maven . . snapshots/Op#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/Op#values().
//⌄ enclosing_range_start scip-java maven . . snapshots/Op#valueOf().
//⌄ enclosing_range_start scip-java maven . . snapshots/Op#valueOf().(value)
//⌄ enclosing_range_start scip-java maven . . snapshots/Op#entries.
//⌄ enclosing_range_start scip-java maven . . snapshots/Op#getEntries().
  enum class Op {
//           ^^ definition scip-java maven . . snapshots/Op#
//              kind Class
//              display_name Op
//              signature_documentation
//              > public final enum class Op : Enum<Op>
//              relationship scip-java maven . . kotlin/Enum# implementation
//           ^^ definition scip-java maven . . snapshots/Op#`<init>`().
//              kind Constructor
//              display_name Op
//              signature_documentation
//              > private constructor(): Op
//           ^^ definition scip-java maven . . snapshots/Op#values().
//              kind Method
//              display_name values
//              signature_documentation
//              > public final static fun values(): Array<Op>
//           ^^ definition scip-java maven . . snapshots/Op#valueOf().
//              kind Method
//              display_name valueOf
//              signature_documentation
//              > public final static fun valueOf(value: String): Op
//           ^^ definition scip-java maven . . snapshots/Op#valueOf().(value)
//              kind Parameter
//              display_name value
//              signature_documentation
//              > value: String
//           ^^ definition scip-java maven . . snapshots/Op#entries.
//              kind Property
//              display_name entries
//              signature_documentation
//              > public final static val entries: EnumEntries<Op>
//           ^^ definition scip-java maven . . snapshots/Op#getEntries().
//              kind Method
//              display_name entries
//              signature_documentation
//              > public static get(): EnumEntries<Op>
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Op#PLUS.
//    ⌄ enclosing_range_start local 2
//    ⌄ enclosing_range_start local 3
      // The entry body is an anonymous subclass; its override has a local symbol.
      PLUS {
//    ^^^^ definition scip-java maven . . snapshots/Op#PLUS.
//         kind EnumMember
//         display_name PLUS
//         signature_documentation
//         > public final static enum entry PLUS: Op
//    ^^^^ definition local 2
//         kind Class
//         display_name <anonymous>
//         signature_documentation
//         > object : Op
//         relationship scip-java maven . . snapshots/Op# implementation
//    ^^^^ definition local 3
//         kind Constructor
//         display_name PLUS
//         signature_documentation
//         > private constructor(): <anonymous>
//        ⌄ enclosing_range_start local 4
//                           ⌄ enclosing_range_start local 5
//                                   ⌄ enclosing_range_start local 6
          override fun apply(a: Int, b: Int) = a + b
//                     ^^^^^ definition local 4
//                           kind Method
//                           display_name apply
//                           signature_documentation
//                           > public open override fun apply(a: Int, b: Int): Int
//                           relationship scip-java maven . . snapshots/Op#apply(). implementation
//                           ^ definition local 5
//                             kind Parameter
//                             display_name a
//                             signature_documentation
//                             > a: Int
//                              ^^^ reference scip-java maven . . kotlin/Int#
//                                   ^ definition local 6
//                                     kind Parameter
//                                     display_name b
//                                     signature_documentation
//                                     > b: Int
//                                      ^^^ reference scip-java maven . . kotlin/Int#
//                                             ^ reference local 5
//                                               ^ reference scip-java maven . . kotlin/Int#plus(+2).
//                                                 ^ reference local 6
//                                ⌃ enclosing_range_end local 5
//                                        ⌃ enclosing_range_end local 6
//                                                 ⌃ enclosing_range_end local 4
      };
//     ⌃ enclosing_range_end scip-java maven . . snapshots/Op#PLUS.
//     ⌃ enclosing_range_end local 2
//     ⌃ enclosing_range_end local 3
  
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Op#apply().
//                       ⌄ enclosing_range_start scip-java maven . . snapshots/Op#apply().(a)
//                               ⌄ enclosing_range_start scip-java maven . . snapshots/Op#apply().(b)
      abstract fun apply(a: Int, b: Int): Int
//                 ^^^^^ definition scip-java maven . . snapshots/Op#apply().
//                       kind Method
//                       display_name apply
//                       signature_documentation
//                       > public abstract fun apply(a: Int, b: Int): Int
//                       > 
//                       ^ definition scip-java maven . . snapshots/Op#apply().(a)
//                         kind Parameter
//                         display_name a
//                         signature_documentation
//                         > a: Int
//                          ^^^ reference scip-java maven . . kotlin/Int#
//                               ^ definition scip-java maven . . snapshots/Op#apply().(b)
//                                 kind Parameter
//                                 display_name b
//                                 signature_documentation
//                                 > b: Int
//                                  ^^^ reference scip-java maven . . kotlin/Int#
//                                        ^^^ reference scip-java maven . . kotlin/Int#
//                            ⌃ enclosing_range_end scip-java maven . . snapshots/Op#apply().(a)
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Op#apply().(b)
//                                          ⌃ enclosing_range_end scip-java maven . . snapshots/Op#apply().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Op#
//⌃ enclosing_range_end scip-java maven . . snapshots/Op#`<init>`().
//⌃ enclosing_range_end scip-java maven . . snapshots/Op#values().
//⌃ enclosing_range_end scip-java maven . . snapshots/Op#valueOf().
//⌃ enclosing_range_end scip-java maven . . snapshots/Op#valueOf().(value)
//⌃ enclosing_range_end scip-java maven . . snapshots/Op#entries.
//⌃ enclosing_range_end scip-java maven . . snapshots/Op#getEntries().
  
