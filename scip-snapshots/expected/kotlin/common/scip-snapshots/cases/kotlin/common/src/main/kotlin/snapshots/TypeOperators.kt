  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/typeOperators().
//                  ⌄ enclosing_range_start scip-java maven . . snapshots/typeOperators().(x)
  fun typeOperators(x: Any) {
//    ^^^^^^^^^^^^^ definition scip-java maven . . snapshots/typeOperators().
//                  kind Method
//                  display_name typeOperators
//                  signature_documentation
//                  > public final fun typeOperators(x: Any): Unit
//                  ^ definition scip-java maven . . snapshots/typeOperators().(x)
//                    kind Parameter
//                    display_name x
//                    signature_documentation
//                    > x: Any
//                     ^^^ reference scip-java maven . . kotlin/Any#
//                       ⌃ enclosing_range_end scip-java maven . . snapshots/typeOperators().(x)
//          ⌄ enclosing_range_start local 0
      when (x) {
//          ^ definition local 0
//            kind Property
//            display_name <when-subject>
//            signature_documentation
//            > local val <when-subject>: Any
//          ^ reference scip-java maven . . snapshots/typeOperators().(x)
//          ⌃ enclosing_range_end local 0
          is Int -> true
//           ^^^ reference scip-java maven . . kotlin/Int#
          else -> x as Float
//                ^ reference scip-java maven . . snapshots/typeOperators().(x)
//                     ^^^^^ reference scip-java maven . . kotlin/Float#
      }
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/typeOperators().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Wrapper#
//⌄ enclosing_range_start scip-java maven . . snapshots/Wrapper#`<init>`().
  class Wrapper
//      ^^^^^^^ definition scip-java maven . . snapshots/Wrapper#
//              kind Class
//              display_name Wrapper
//              signature_documentation
//              > public final class Wrapper : Any
//      ^^^^^^^ definition scip-java maven . . snapshots/Wrapper#`<init>`().
//              kind Constructor
//              display_name Wrapper
//              signature_documentation
//              > public constructor(): Wrapper
//            ⌃ enclosing_range_end scip-java maven . . snapshots/Wrapper#
//            ⌃ enclosing_range_end scip-java maven . . snapshots/Wrapper#`<init>`().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/classify().
//             ⌄ enclosing_range_start scip-java maven . . snapshots/classify().(x)
  fun classify(x: Any) {
//    ^^^^^^^^ definition scip-java maven . . snapshots/classify().
//             kind Method
//             display_name classify
//             signature_documentation
//             > public final fun classify(x: Any): Unit
//             ^ definition scip-java maven . . snapshots/classify().(x)
//               kind Parameter
//               display_name x
//               signature_documentation
//               > x: Any
//                ^^^ reference scip-java maven . . kotlin/Any#
//                  ⌃ enclosing_range_end scip-java maven . . snapshots/classify().(x)
      if (x is Wrapper) {}
//        ^ reference scip-java maven . . snapshots/classify().(x)
//             ^^^^^^^ reference scip-java maven . . snapshots/Wrapper#
//    ⌄ enclosing_range_start local 1
      val s = x as? String
//        ^ definition local 1
//          kind Property
//          display_name s
//          signature_documentation
//          > local val s: String?
//            ^ reference scip-java maven . . snapshots/classify().(x)
//                  ^^^^^^ reference scip-java maven . . kotlin/String#
//                       ⌃ enclosing_range_end local 1
//    ⌄ enclosing_range_start local 2
      val w = x as Wrapper
//        ^ definition local 2
//          kind Property
//          display_name w
//          signature_documentation
//          > local val w: Wrapper
//            ^ reference scip-java maven . . snapshots/classify().(x)
//                 ^^^^^^^ reference scip-java maven . . snapshots/Wrapper#
//                       ⌃ enclosing_range_end local 2
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/classify().
  
