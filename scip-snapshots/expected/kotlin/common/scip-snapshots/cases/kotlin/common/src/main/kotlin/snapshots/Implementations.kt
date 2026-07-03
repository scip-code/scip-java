  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Overrides#
//⌄ enclosing_range_start scip-java maven . . snapshots/Overrides#`<init>`().
  class Overrides : AutoCloseable {
//      ^^^^^^^^^ definition scip-java maven . . snapshots/Overrides#
//                kind Class
//                display_name Overrides
//                signature_documentation
//                > public final class Overrides : {kotlin/AutoCloseable=} AutoCloseable
//                relationship scip-java maven jdk 17 java/lang/AutoCloseable# implementation
//      ^^^^^^^^^ definition scip-java maven . . snapshots/Overrides#`<init>`().
//                kind Constructor
//                display_name Overrides
//                signature_documentation
//                > public constructor(): Overrides
//                  ^^^^^^^^^^^^^ reference scip-java maven jdk 17 java/lang/AutoCloseable#
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Overrides#close().
      override fun close() {
//                 ^^^^^ definition scip-java maven . . snapshots/Overrides#close().
//                       kind Method
//                       display_name close
//                       signature_documentation
//                       > public open override fun close(): Unit
//                       relationship scip-java maven jdk 17 java/lang/AutoCloseable#close(). implementation
          TODO("Not yet implemented")
//        ^^^^ reference scip-java maven . . kotlin/TODO(+1).
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Overrides#close().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Overrides#
//⌃ enclosing_range_end scip-java maven . . snapshots/Overrides#`<init>`().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Animal#
  interface Animal {
//          ^^^^^^ definition scip-java maven . . snapshots/Animal#
//                 kind Interface
//                 display_name Animal
//                 signature_documentation
//                 > public abstract interface Animal : Any
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Animal#favoriteNumber.
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Animal#favoriteNumber.get().
      val favoriteNumber: Int
//        ^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Animal#favoriteNumber.
//                       kind Property
//                       display_name favoriteNumber
//                       signature_documentation
//                       > public abstract val favoriteNumber: Int
//        ^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Animal#favoriteNumber.get().
//                       kind Method
//                       display_name favoriteNumber
//                       signature_documentation
//                       > public get(): Int
//                        ^^^ reference scip-java maven . . kotlin/Int#
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Animal#favoriteNumber.
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Animal#favoriteNumber.get().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Animal#sound().
      fun sound(): String
//        ^^^^^ definition scip-java maven . . snapshots/Animal#sound().
//              kind Method
//              display_name sound
//              signature_documentation
//              > public abstract fun sound(): String
//              > 
//                 ^^^^^^ reference scip-java maven . . kotlin/String#
//                      ⌃ enclosing_range_end scip-java maven . . snapshots/Animal#sound().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Animal#
//⌄ enclosing_range_start scip-java maven . . snapshots/Bird#
//⌄ enclosing_range_start scip-java maven . . snapshots/Bird#`<init>`().
  open class Bird : Animal {
//           ^^^^ definition scip-java maven . . snapshots/Bird#
//                kind Class
//                display_name Bird
//                signature_documentation
//                > public open class Bird : Animal
//                relationship scip-java maven . . snapshots/Animal# implementation
//           ^^^^ definition scip-java maven . . snapshots/Bird#`<init>`().
//                kind Constructor
//                display_name Bird
//                signature_documentation
//                > public constructor(): Bird
//                  ^^^^^^ reference scip-java maven . . snapshots/Animal#
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Bird#favoriteNumber.
      override val favoriteNumber: Int
//                 ^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Bird#favoriteNumber.
//                                kind Property
//                                display_name favoriteNumber
//                                signature_documentation
//                                > public open override val favoriteNumber: Int
//                                 ^^^ reference scip-java maven . . kotlin/Int#
//        ⌄ enclosing_range_start scip-java maven . . snapshots/Bird#favoriteNumber.get().
          get() = 42
//        ^^^ definition scip-java maven . . snapshots/Bird#favoriteNumber.get().
//            kind Method
//            display_name favoriteNumber
//            signature_documentation
//            > public get(): Int
//                 ⌃ enclosing_range_end scip-java maven . . snapshots/Bird#favoriteNumber.
//                 ⌃ enclosing_range_end scip-java maven . . snapshots/Bird#favoriteNumber.get().
  
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Bird#sound().
      override fun sound(): String {
//                 ^^^^^ definition scip-java maven . . snapshots/Bird#sound().
//                       kind Method
//                       display_name sound
//                       signature_documentation
//                       > public open override fun sound(): String
//                       relationship scip-java maven . . snapshots/Animal#sound(). implementation
//                          ^^^^^^ reference scip-java maven . . kotlin/String#
          return "tweet"
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Bird#sound().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Bird#
//⌃ enclosing_range_end scip-java maven . . snapshots/Bird#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/Seagull#
//⌄ enclosing_range_start scip-java maven . . snapshots/Seagull#`<init>`().
  class Seagull : Bird() {
//      ^^^^^^^ definition scip-java maven . . snapshots/Seagull#
//              kind Class
//              display_name Seagull
//              signature_documentation
//              > public final class Seagull : Bird
//              relationship scip-java maven . . snapshots/Bird# implementation
//      ^^^^^^^ definition scip-java maven . . snapshots/Seagull#`<init>`().
//              kind Constructor
//              display_name Seagull
//              signature_documentation
//              > public constructor(): Seagull
//                ^^^^ reference scip-java maven . . snapshots/Bird#
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Seagull#favoriteNumber.
      override val favoriteNumber: Int
//                 ^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Seagull#favoriteNumber.
//                                kind Property
//                                display_name favoriteNumber
//                                signature_documentation
//                                > public open override val favoriteNumber: Int
//                                 ^^^ reference scip-java maven . . kotlin/Int#
//        ⌄ enclosing_range_start scip-java maven . . snapshots/Seagull#favoriteNumber.get().
          get() = 1337
//        ^^^ definition scip-java maven . . snapshots/Seagull#favoriteNumber.get().
//            kind Method
//            display_name favoriteNumber
//            signature_documentation
//            > public get(): Int
//                   ⌃ enclosing_range_end scip-java maven . . snapshots/Seagull#favoriteNumber.
//                   ⌃ enclosing_range_end scip-java maven . . snapshots/Seagull#favoriteNumber.get().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Seagull#sound().
      override fun sound(): String {
//                 ^^^^^ definition scip-java maven . . snapshots/Seagull#sound().
//                       kind Method
//                       display_name sound
//                       signature_documentation
//                       > public open override fun sound(): String
//                       relationship scip-java maven . . snapshots/Bird#sound(). implementation
//                          ^^^^^^ reference scip-java maven . . kotlin/String#
          return "squawk"
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Seagull#sound().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Seagull#
//⌃ enclosing_range_end scip-java maven . . snapshots/Seagull#`<init>`().
  
