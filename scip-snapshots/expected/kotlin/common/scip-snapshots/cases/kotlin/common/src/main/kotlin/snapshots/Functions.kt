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
  
