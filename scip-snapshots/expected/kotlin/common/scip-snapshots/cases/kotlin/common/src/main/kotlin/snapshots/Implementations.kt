  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Overrides#
//⌄ enclosing_range_start scip-java maven . . snapshots/Overrides#`<init>`().
  class Overrides : AutoCloseable {
//      ^^^^^^^^^ definition scip-java maven . . snapshots/Overrides#
//                kind Class
//                display_name Overrides
//                signature_documentation
//                > public final class Overrides : AutoCloseable
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
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Animal#getFavoriteNumber().
      val favoriteNumber: Int
//        ^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Animal#favoriteNumber.
//                       kind Property
//                       display_name favoriteNumber
//                       signature_documentation
//                       > public abstract val favoriteNumber: Int
//        ^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Animal#getFavoriteNumber().
//                       kind Method
//                       display_name favoriteNumber
//                       signature_documentation
//                       > public get(): Int
//                        ^^^ reference scip-java maven . . kotlin/Int#
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Animal#favoriteNumber.
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Animal#getFavoriteNumber().
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
//        ⌄ enclosing_range_start scip-java maven . . snapshots/Bird#getFavoriteNumber().
          get() = 42
//        ^^^ definition scip-java maven . . snapshots/Bird#getFavoriteNumber().
//            kind Method
//            display_name favoriteNumber
//            signature_documentation
//            > public get(): Int
//                 ⌃ enclosing_range_end scip-java maven . . snapshots/Bird#favoriteNumber.
//                 ⌃ enclosing_range_end scip-java maven . . snapshots/Bird#getFavoriteNumber().
  
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
//        ⌄ enclosing_range_start scip-java maven . . snapshots/Seagull#getFavoriteNumber().
          get() = 1337
//        ^^^ definition scip-java maven . . snapshots/Seagull#getFavoriteNumber().
//            kind Method
//            display_name favoriteNumber
//            signature_documentation
//            > public get(): Int
//                   ⌃ enclosing_range_end scip-java maven . . snapshots/Seagull#favoriteNumber.
//                   ⌃ enclosing_range_end scip-java maven . . snapshots/Seagull#getFavoriteNumber().
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
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Named#
  interface Named
//          ^^^^^ definition scip-java maven . . snapshots/Named#
//                kind Interface
//                display_name Named
//                signature_documentation
//                > public abstract interface Named : Any
//              ⌃ enclosing_range_end scip-java maven . . snapshots/Named#
//⌄ enclosing_range_start scip-java maven . . snapshots/Speakable#
  interface Speakable
//          ^^^^^^^^^ definition scip-java maven . . snapshots/Speakable#
//                    kind Interface
//                    display_name Speakable
//                    signature_documentation
//                    > public abstract interface Speakable : Any
//                  ⌃ enclosing_range_end scip-java maven . . snapshots/Speakable#
//⌄ enclosing_range_start scip-java maven . . snapshots/Person#
//⌄ enclosing_range_start scip-java maven . . snapshots/Person#`<init>`().
  class Person : Named, Speakable
//      ^^^^^^ definition scip-java maven . . snapshots/Person#
//             kind Class
//             display_name Person
//             signature_documentation
//             > public final class Person : Named, Speakable
//             relationship scip-java maven . . snapshots/Named# implementation
//             relationship scip-java maven . . snapshots/Speakable# implementation
//      ^^^^^^ definition scip-java maven . . snapshots/Person#`<init>`().
//             kind Constructor
//             display_name Person
//             signature_documentation
//             > public constructor(): Person
//               ^^^^^ reference scip-java maven . . snapshots/Named#
//                      ^^^^^^^^^ reference scip-java maven . . snapshots/Speakable#
//                              ⌃ enclosing_range_end scip-java maven . . snapshots/Person#
//                              ⌃ enclosing_range_end scip-java maven . . snapshots/Person#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/usePerson().
//              ⌄ enclosing_range_start scip-java maven . . snapshots/usePerson().(p)
  fun usePerson(p: Person): Named = p
//    ^^^^^^^^^ definition scip-java maven . . snapshots/usePerson().
//              kind Method
//              display_name usePerson
//              signature_documentation
//              > public final fun usePerson(p: Person): Named
//              ^ definition scip-java maven . . snapshots/usePerson().(p)
//                kind Parameter
//                display_name p
//                signature_documentation
//                > p: Person
//                 ^^^^^^ reference scip-java maven . . snapshots/Person#
//                          ^^^^^ reference scip-java maven . . snapshots/Named#
//                                  ^ reference scip-java maven . . snapshots/usePerson().(p)
//                      ⌃ enclosing_range_end scip-java maven . . snapshots/usePerson().(p)
//                                  ⌃ enclosing_range_end scip-java maven . . snapshots/usePerson().
  
