  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/sampleText().
//               ⌄ enclosing_range_start scip-java maven . . snapshots/sampleText().(x)
  fun sampleText(x: String = "") {
//    ^^^^^^^^^^ definition scip-java maven . . snapshots/sampleText().
//               kind Method
//               display_name sampleText
//               signature_documentation
//               > public final fun sampleText(x: String = ...): Unit
//               ^ definition scip-java maven . . snapshots/sampleText().(x)
//                 kind Parameter
//                 display_name x
//                 signature_documentation
//                 > x: String = ...
//                  ^^^^^^ reference scip-java maven . . kotlin/String#
//                            ⌃ enclosing_range_end scip-java maven . . snapshots/sampleText().(x)
    println(x)
//  ^^^^^^^ reference scip-java maven . . kotlin/io/println().
//          ^ reference scip-java maven . . snapshots/sampleText().(x)
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/sampleText().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/MyClass#
//⌄ enclosing_range_start scip-java maven . . snapshots/MyClass#`<init>`().
  class MyClass
//      ^^^^^^^ definition scip-java maven . . snapshots/MyClass#
//              kind Class
//              display_name MyClass
//              signature_documentation
//              > public final class MyClass : Any
//      ^^^^^^^ definition scip-java maven . . snapshots/MyClass#`<init>`().
//              kind Constructor
//              display_name MyClass
//              signature_documentation
//              > public constructor(): MyClass
//            ⌃ enclosing_range_end scip-java maven . . snapshots/MyClass#
//            ⌃ enclosing_range_end scip-java maven . . snapshots/MyClass#`<init>`().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/bar().
  fun bar(): MyClass = MyClass()
//    ^^^ definition scip-java maven . . snapshots/bar().
//        kind Method
//        display_name bar
//        signature_documentation
//        > public final fun bar(): MyClass
//           ^^^^^^^ reference scip-java maven . . snapshots/MyClass#
//                     ^^^^^^^ reference scip-java maven . . snapshots/MyClass#`<init>`().
//                             ⌃ enclosing_range_end scip-java maven . . snapshots/bar().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/greet().
//          ⌄ enclosing_range_start scip-java maven . . snapshots/greet().(name)
  fun greet(name: String) = "Hello, $name!"
//    ^^^^^ definition scip-java maven . . snapshots/greet().
//          kind Method
//          display_name greet
//          signature_documentation
//          > public final fun greet(name: String): String
//          ^^^^ definition scip-java maven . . snapshots/greet().(name)
//               kind Parameter
//               display_name name
//               signature_documentation
//               > name: String
//                ^^^^^^ reference scip-java maven . . kotlin/String#
//                                   ^^^^ reference scip-java maven . . snapshots/greet().(name)
//                     ⌃ enclosing_range_end scip-java maven . . snapshots/greet().(name)
//                                        ⌃ enclosing_range_end scip-java maven . . snapshots/greet().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/add().
//        ⌄ enclosing_range_start scip-java maven . . snapshots/add().(x)
//                ⌄ enclosing_range_start scip-java maven . . snapshots/add().(y)
  fun add(x: Int, y: Int): Int = x + y
//    ^^^ definition scip-java maven . . snapshots/add().
//        kind Method
//        display_name add
//        signature_documentation
//        > public final fun add(x: Int, y: Int): Int
//        ^ definition scip-java maven . . snapshots/add().(x)
//          kind Parameter
//          display_name x
//          signature_documentation
//          > x: Int
//           ^^^ reference scip-java maven . . kotlin/Int#
//                ^ definition scip-java maven . . snapshots/add().(y)
//                  kind Parameter
//                  display_name y
//                  signature_documentation
//                  > y: Int
//                   ^^^ reference scip-java maven . . kotlin/Int#
//                         ^^^ reference scip-java maven . . kotlin/Int#
//                               ^ reference scip-java maven . . snapshots/add().(x)
//                                 ^ reference scip-java maven . . kotlin/Int#plus(+2).
//                                   ^ reference scip-java maven . . snapshots/add().(y)
//             ⌃ enclosing_range_end scip-java maven . . snapshots/add().(x)
//                     ⌃ enclosing_range_end scip-java maven . . snapshots/add().(y)
//                                   ⌃ enclosing_range_end scip-java maven . . snapshots/add().
//⌄ enclosing_range_start scip-java maven . . snapshots/add(+1).
//        ⌄ enclosing_range_start scip-java maven . . snapshots/add(+1).(x)
//                   ⌄ enclosing_range_start scip-java maven . . snapshots/add(+1).(y)
  fun add(x: Double, y: Double): Double = x + y
//    ^^^ definition scip-java maven . . snapshots/add(+1).
//        kind Method
//        display_name add
//        signature_documentation
//        > public final fun add(x: Double, y: Double): Double
//        ^ definition scip-java maven . . snapshots/add(+1).(x)
//          kind Parameter
//          display_name x
//          signature_documentation
//          > x: Double
//           ^^^^^^ reference scip-java maven . . kotlin/Double#
//                   ^ definition scip-java maven . . snapshots/add(+1).(y)
//                     kind Parameter
//                     display_name y
//                     signature_documentation
//                     > y: Double
//                      ^^^^^^ reference scip-java maven . . kotlin/Double#
//                               ^^^^^^ reference scip-java maven . . kotlin/Double#
//                                        ^ reference scip-java maven . . snapshots/add(+1).(x)
//                                          ^ reference scip-java maven . . kotlin/Double#plus(+5).
//                                            ^ reference scip-java maven . . snapshots/add(+1).(y)
//                ⌃ enclosing_range_end scip-java maven . . snapshots/add(+1).(x)
//                           ⌃ enclosing_range_end scip-java maven . . snapshots/add(+1).(y)
//                                            ⌃ enclosing_range_end scip-java maven . . snapshots/add(+1).
//⌄ enclosing_range_start scip-java maven . . snapshots/add(+2).
//        ⌄ enclosing_range_start scip-java maven . . snapshots/add(+2).(x)
//                   ⌄ enclosing_range_start scip-java maven . . snapshots/add(+2).(y)
  fun add(x: String, y: String): String = x + y
//    ^^^ definition scip-java maven . . snapshots/add(+2).
//        kind Method
//        display_name add
//        signature_documentation
//        > public final fun add(x: String, y: String): String
//        ^ definition scip-java maven . . snapshots/add(+2).(x)
//          kind Parameter
//          display_name x
//          signature_documentation
//          > x: String
//           ^^^^^^ reference scip-java maven . . kotlin/String#
//                   ^ definition scip-java maven . . snapshots/add(+2).(y)
//                     kind Parameter
//                     display_name y
//                     signature_documentation
//                     > y: String
//                      ^^^^^^ reference scip-java maven . . kotlin/String#
//                               ^^^^^^ reference scip-java maven . . kotlin/String#
//                                        ^ reference scip-java maven . . snapshots/add(+2).(x)
//                                          ^ reference scip-java maven . . kotlin/String#plus().
//                                            ^ reference scip-java maven . . snapshots/add(+2).(y)
//                ⌃ enclosing_range_end scip-java maven . . snapshots/add(+2).(x)
//                           ⌃ enclosing_range_end scip-java maven . . snapshots/add(+2).(y)
//                                            ⌃ enclosing_range_end scip-java maven . . snapshots/add(+2).
  
//⌄ enclosing_range_start scip-java maven . . snapshots/useOverloads().
  fun useOverloads() {
//    ^^^^^^^^^^^^ definition scip-java maven . . snapshots/useOverloads().
//                 kind Method
//                 display_name useOverloads
//                 signature_documentation
//                 > public final fun useOverloads(): Unit
      add(1, 2)
//    ^^^ reference scip-java maven . . snapshots/add().
      add(1.0, 2.0)
//    ^^^ reference scip-java maven . . snapshots/add(+1).
      add("a", "b")
//    ^^^ reference scip-java maven . . snapshots/add(+2).
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/useOverloads().
  
