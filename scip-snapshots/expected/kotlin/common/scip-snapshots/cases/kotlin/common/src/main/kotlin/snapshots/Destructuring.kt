  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Point#
//           ⌄ enclosing_range_start scip-java maven . . snapshots/Point#`<init>`().
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Point#copy().
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/Point#`<init>`().(x)
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/Point#x.
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/Point#getX().
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/Point#component1().
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/Point#copy().(x)
//                             ⌄ enclosing_range_start scip-java maven . . snapshots/Point#`<init>`().(y)
//                             ⌄ enclosing_range_start scip-java maven . . snapshots/Point#y.
//                             ⌄ enclosing_range_start scip-java maven . . snapshots/Point#getY().
//                             ⌄ enclosing_range_start scip-java maven . . snapshots/Point#component2().
//                             ⌄ enclosing_range_start scip-java maven . . snapshots/Point#copy().(y)
  data class Point(val x: Int, val y: Int)
//           ^^^^^ definition scip-java maven . . snapshots/Point#
//                 display_name Point
//                 signature_documentation
//                 > public final data class Point : Any
//           ^^^^^ definition scip-java maven . . snapshots/Point#`<init>`().
//                 display_name Point
//                 signature_documentation
//                 > public constructor(x: Int, y: Int): Point
//                ^^^^^^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Point#copy().
//                                         display_name copy
//                                         signature_documentation
//                                         > public final fun copy(x: Int = ..., y: Int = ...): Point
//                                         > 
//                     ^ definition scip-java maven . . snapshots/Point#`<init>`().(x)
//                       display_name x
//                       signature_documentation
//                       > x: Int
//                     ^ definition scip-java maven . . snapshots/Point#x.
//                       display_name x
//                       signature_documentation
//                       > public final val x: Int
//                     ^ reference scip-java maven . . snapshots/Point#`<init>`().(x)
//                     ^ definition scip-java maven . . snapshots/Point#getX().
//                       display_name x
//                       signature_documentation
//                       > public get(): Int
//                     ^ definition scip-java maven . . snapshots/Point#component1().
//                       display_name component1
//                       signature_documentation
//                       > public final operator fun component1(): Int
//                       > 
//                     ^ definition scip-java maven . . snapshots/Point#copy().(x)
//                       display_name x
//                       signature_documentation
//                       > x: Int = ...
//                     ^ reference scip-java maven . . snapshots/Point#x.
//                     ^ reference scip-java maven . . snapshots/Point#getX().
//                        ^^^ reference scip-java maven . . kotlin/Int#
//                                 ^ definition scip-java maven . . snapshots/Point#`<init>`().(y)
//                                   display_name y
//                                   signature_documentation
//                                   > y: Int
//                                 ^ definition scip-java maven . . snapshots/Point#y.
//                                   display_name y
//                                   signature_documentation
//                                   > public final val y: Int
//                                 ^ reference scip-java maven . . snapshots/Point#`<init>`().(y)
//                                 ^ definition scip-java maven . . snapshots/Point#getY().
//                                   display_name y
//                                   signature_documentation
//                                   > public get(): Int
//                                 ^ definition scip-java maven . . snapshots/Point#component2().
//                                   display_name component2
//                                   signature_documentation
//                                   > public final operator fun component2(): Int
//                                   > 
//                                 ^ definition scip-java maven . . snapshots/Point#copy().(y)
//                                   display_name y
//                                   signature_documentation
//                                   > y: Int = ...
//                                 ^ reference scip-java maven . . snapshots/Point#y.
//                                 ^ reference scip-java maven . . snapshots/Point#getY().
//                                    ^^^ reference scip-java maven . . kotlin/Int#
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Point#`<init>`().(x)
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Point#x.
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Point#getX().
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Point#component1().
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Point#copy().(x)
//                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Point#`<init>`().(y)
//                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Point#y.
//                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Point#getY().
//                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Point#component2().
//                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Point#copy().(y)
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/Point#
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/Point#`<init>`().
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/Point#copy().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/destructure().
  fun destructure(): Int {
//    ^^^^^^^^^^^ definition scip-java maven . . snapshots/destructure().
//                display_name destructure
//                signature_documentation
//                > public final fun destructure(): Int
//                   ^^^ reference scip-java maven . . kotlin/Int#
//    ⌄ enclosing_range_start local 0
//         ⌄ enclosing_range_start local 1
//            ⌄ enclosing_range_start local 2
      val (x, y) = Point(1, 2)
//    ^^^^^^^^^^^^^^^^^^^^^^^^ definition local 0
//                             display_name <destruct>
//                             signature_documentation
//                             > local val <destruct>: Point
//         ^ definition local 1
//           display_name x
//           signature_documentation
//           > local val x: Int
//         ^ reference scip-java maven . . snapshots/Point#component1().
//         ^ reference local 0
//            ^ definition local 2
//              display_name y
//              signature_documentation
//              > local val y: Int
//            ^ reference scip-java maven . . snapshots/Point#component2().
//            ^ reference local 0
//                 ^^^^^ reference scip-java maven . . snapshots/Point#`<init>`().
//         ⌃ enclosing_range_end local 1
//            ⌃ enclosing_range_end local 2
//                           ⌃ enclosing_range_end local 0
//    ⌄ enclosing_range_start local 3
      val labeled = listOf(Point(3, 4) to "label")
//        ^^^^^^^ definition local 3
//                display_name labeled
//                signature_documentation
//                > local val labeled: List<Pair<Point, String>>
//                  ^^^^^^ reference scip-java maven . . kotlin/collections/listOf().
//                         ^^^^^ reference scip-java maven . . snapshots/Point#`<init>`().
//                                     ^^ reference scip-java maven . . kotlin/to().
//                                               ⌃ enclosing_range_end local 3
//    ⌄ enclosing_range_start local 4
//                                ⌄ enclosing_range_start local 5
//                                 ⌄ enclosing_range_start local 6
//                                        ⌄ enclosing_range_start local 7
      val total = labeled.sumOf { (point, label) -> point.x + label.length }
//        ^^^^^ definition local 4
//              display_name total
//              signature_documentation
//              > local val total: Int
//                ^^^^^^^ reference local 3
//                        ^^^^^ reference scip-java maven . . kotlin/collections/sumOf(+66).
//                                ^^^^^^^^^^^^^^ definition local 5
//                                               display_name <destruct>
//                                               signature_documentation
//                                               > <destruct>: Pair<Point, String>
//                                 ^^^^^ definition local 6
//                                       display_name point
//                                       signature_documentation
//                                       > local val point: Point
//                                 ^^^^^ reference scip-java maven . . kotlin/Pair#component1().
//                                 ^^^^^ reference local 5
//                                        ^^^^^ definition local 7
//                                              display_name label
//                                              signature_documentation
//                                              > local val label: String
//                                        ^^^^^ reference scip-java maven . . kotlin/Pair#component2().
//                                        ^^^^^ reference local 5
//                                                  ^^^^^ reference local 6
//                                                        ^ reference scip-java maven . . snapshots/Point#x.
//                                                        ^ reference scip-java maven . . snapshots/Point#getX().
//                                                          ^ reference scip-java maven . . kotlin/Int#plus(+2).
//                                                            ^^^^^ reference local 7
//                                                                  ^^^^^^ reference scip-java maven . . kotlin/String#length.
//                                                                  ^^^^^^ reference scip-java maven . . kotlin/String#getLength().
//                                     ⌃ enclosing_range_end local 6
//                                            ⌃ enclosing_range_end local 7
//                                             ⌃ enclosing_range_end local 5
//                                                                         ⌃ enclosing_range_end local 4
      return x + y + total
//           ^ reference local 1
//             ^ reference scip-java maven . . kotlin/Int#plus(+2).
//               ^ reference local 2
//                 ^ reference scip-java maven . . kotlin/Int#plus(+2).
//                   ^^^^^ reference local 4
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/destructure().
  
