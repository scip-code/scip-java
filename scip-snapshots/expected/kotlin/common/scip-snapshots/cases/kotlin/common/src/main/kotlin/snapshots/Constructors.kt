  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Overloaded#
//      ⌄ enclosing_range_start scip-java maven . . snapshots/Overloaded#`<init>`().
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/Overloaded#`<init>`().(s)
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/Overloaded#s.
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/Overloaded#s.get().
  class Overloaded(val s: String) {
//      ^^^^^^^^^^ definition scip-java maven . . snapshots/Overloaded#
//                 kind Class
//                 display_name Overloaded
//                 signature_documentation
//                 > public final class Overloaded : Any
//      ^^^^^^^^^^ definition scip-java maven . . snapshots/Overloaded#`<init>`().
//                 kind Constructor
//                 display_name Overloaded
//                 signature_documentation
//                 > public constructor(s: String): Overloaded
//                     ^ definition scip-java maven . . snapshots/Overloaded#`<init>`().(s)
//                       kind Parameter
//                       display_name s
//                       signature_documentation
//                       > s: String
//                     ^ definition scip-java maven . . snapshots/Overloaded#s.
//                       kind Property
//                       display_name s
//                       signature_documentation
//                       > public final val s: String
//                     ^ reference scip-java maven . . snapshots/Overloaded#`<init>`().(s)
//                     ^ definition scip-java maven . . snapshots/Overloaded#s.get().
//                       kind Method
//                       display_name s
//                       signature_documentation
//                       > public get(): String
//                        ^^^^^^ reference scip-java maven . . kotlin/String#
//                             ⌃ enclosing_range_end scip-java maven . . snapshots/Overloaded#`<init>`().(s)
//                             ⌃ enclosing_range_end scip-java maven . . snapshots/Overloaded#s.
//                             ⌃ enclosing_range_end scip-java maven . . snapshots/Overloaded#s.get().
//                              ⌃ enclosing_range_end scip-java maven . . snapshots/Overloaded#`<init>`().
//  ⌄ enclosing_range_start scip-java maven . . snapshots/Overloaded#`<init>`(+1).
//              ⌄ enclosing_range_start scip-java maven . . snapshots/Overloaded#`<init>`(+1).(i)
    constructor(i: Int) : this(i.toString())
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Overloaded#`<init>`(+1).
//                                           kind Constructor
//                                           display_name Overloaded
//                                           signature_documentation
//                                           > public constructor(i: Int): Overloaded
//              ^ definition scip-java maven . . snapshots/Overloaded#`<init>`(+1).(i)
//                kind Parameter
//                display_name i
//                signature_documentation
//                > i: Int
//                 ^^^ reference scip-java maven . . kotlin/Int#
//                             ^ reference scip-java maven . . snapshots/Overloaded#`<init>`(+1).(i)
//                               ^^^^^^^^ reference scip-java maven . . kotlin/Int#toString().
//                   ⌃ enclosing_range_end scip-java maven . . snapshots/Overloaded#`<init>`(+1).(i)
//                                         ⌃ enclosing_range_end scip-java maven . . snapshots/Overloaded#`<init>`(+1).
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Overloaded#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/OverloadedAlias#
  typealias OverloadedAlias = Overloaded
//          ^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/OverloadedAlias#
//                          kind TypeAlias
//                          display_name OverloadedAlias
//                          signature_documentation
//                          > public final typealias OverloadedAlias = Overloaded
//                          > 
//                            ^^^^^^^^^^ reference scip-java maven . . snapshots/Overloaded#
//                                     ⌃ enclosing_range_end scip-java maven . . snapshots/OverloadedAlias#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/StringIntMap#
  typealias StringIntMap = HashMap<String, Int>
//          ^^^^^^^^^^^^ definition scip-java maven . . snapshots/StringIntMap#
//                       kind TypeAlias
//                       display_name StringIntMap
//                       signature_documentation
//                       > public final typealias StringIntMap = HashMap<String, Int>
//                       > 
//                         ^^^^^^^ reference scip-java maven jdk 17 java/util/HashMap#
//                                 ^^^^^^ reference scip-java maven . . kotlin/String#
//                                         ^^^ reference scip-java maven . . kotlin/Int#
//                                            ⌃ enclosing_range_end scip-java maven . . snapshots/StringIntMap#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/constructors().
  // `Regex` and `OverloadedAlias` reference the same symbols as ConstructorsConsumer.java.
  fun constructors() {
//    ^^^^^^^^^^^^ definition scip-java maven . . snapshots/constructors().
//                 kind Method
//                 display_name constructors
//                 signature_documentation
//                 > public final fun constructors(): Unit
    // Library constructors are numbered like javac, private ones included.
    java.io.File("a", "b")
//  ^^^^ reference scip-java maven . . java/
//       ^^ reference scip-java maven . . java/io/
//          ^^^^ reference scip-java maven jdk 17 java/io/File#`<init>`(+3).
    Regex("a")
//  ^^^^^ reference scip-java maven . . kotlin/text/Regex#`<init>`(+1).
    // Calls through type aliases reference the aliased class's constructor.
    StringBuilder("x")
//  ^^^^^^^^^^^^^ reference scip-java maven jdk 17 java/lang/StringBuilder#`<init>`(+2).
    StringIntMap()
//  ^^^^^^^^^^^^ reference scip-java maven jdk 17 java/util/HashMap#`<init>`(+2).
    OverloadedAlias(1)
//  ^^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/Overloaded#`<init>`(+1).
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/constructors().
  
