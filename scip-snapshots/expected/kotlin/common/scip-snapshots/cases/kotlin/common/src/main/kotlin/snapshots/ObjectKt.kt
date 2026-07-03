  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
  import java.lang.RuntimeException
//       ^^^^ reference scip-java maven . . java/
//            ^^^^ reference scip-java maven . . java/lang/
//                 ^^^^^^^^^^^^^^^^ reference scip-java maven jdk 17 java/lang/RuntimeException#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/ObjectKt#
//⌄ enclosing_range_start scip-java maven . . snapshots/ObjectKt#`<init>`().
  object ObjectKt {
//       ^^^^^^^^ definition scip-java maven . . snapshots/ObjectKt#
//                kind Class
//                display_name ObjectKt
//                signature_documentation
//                > public final object ObjectKt : Any
//       ^^^^^^^^ definition scip-java maven . . snapshots/ObjectKt#`<init>`().
//                kind Constructor
//                display_name ObjectKt
//                signature_documentation
//                > private constructor(): ObjectKt
//  ⌄ enclosing_range_start scip-java maven . . snapshots/ObjectKt#fail().
//           ⌄ enclosing_range_start scip-java maven . . snapshots/ObjectKt#fail().(message)
    fun fail(message: String?): Nothing {
//      ^^^^ definition scip-java maven . . snapshots/ObjectKt#fail().
//           kind Method
//           display_name fail
//           signature_documentation
//           > public final fun fail(message: String?): Nothing
//           ^^^^^^^ definition scip-java maven . . snapshots/ObjectKt#fail().(message)
//                   kind Parameter
//                   display_name message
//                   signature_documentation
//                   > message: String?
//                    ^^^^^^^ reference scip-java maven . . kotlin/String#
//                              ^^^^^^^ reference scip-java maven . . kotlin/Nothing#
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/ObjectKt#fail().(message)
      throw RuntimeException(message)
//          ^^^^^^^^^^^^^^^^ reference scip-java maven jdk 17 java/lang/RuntimeException#`<init>`().
//                           ^^^^^^^ reference scip-java maven . . snapshots/ObjectKt#fail().(message)
    }
//  ⌃ enclosing_range_end scip-java maven . . snapshots/ObjectKt#fail().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/ObjectKt#
//⌃ enclosing_range_end scip-java maven . . snapshots/ObjectKt#`<init>`().
  
