  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
  import java.lang.RuntimeException
//       ^^^^ reference scip-java maven . . java/
//            ^^^^ reference scip-java maven . . java/lang/
//                 ^^^^^^^^^^^^^^^^ reference scip-java maven jdk 17 java/lang/RuntimeException#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/ObjectKt#
//⌄ enclosing_range_start scip-java maven . . snapshots/ObjectKt#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/ObjectKt#INSTANCE.
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
//       ^^^^^^^^ definition scip-java maven . . snapshots/ObjectKt#INSTANCE.
//                kind StaticField
//                display_name INSTANCE
//                signature_documentation
//                > public final static val INSTANCE: ObjectKt
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
//                    ^^^^^^ reference scip-java maven . . kotlin/String#
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
//⌃ enclosing_range_end scip-java maven . . snapshots/ObjectKt#INSTANCE.
  
//⌄ enclosing_range_start scip-java maven . . snapshots/MySingleton#
//⌄ enclosing_range_start scip-java maven . . snapshots/MySingleton#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/MySingleton#INSTANCE.
  // Named objects define an INSTANCE field for Java callers; object expressions do not.
  object MySingleton {
//       ^^^^^^^^^^^ definition scip-java maven . . snapshots/MySingleton#
//                   kind Class
//                   display_name MySingleton
//                   signature_documentation
//                   > public final object MySingleton : Any
//       ^^^^^^^^^^^ definition scip-java maven . . snapshots/MySingleton#`<init>`().
//                   kind Constructor
//                   display_name MySingleton
//                   signature_documentation
//                   > private constructor(): MySingleton
//       ^^^^^^^^^^^ definition scip-java maven . . snapshots/MySingleton#INSTANCE.
//                   kind StaticField
//                   display_name INSTANCE
//                   signature_documentation
//                   > public final static val INSTANCE: MySingleton
//    ⌄ enclosing_range_start scip-java maven . . snapshots/MySingleton#hello().
      fun hello(): String = "hi"
//        ^^^^^ definition scip-java maven . . snapshots/MySingleton#hello().
//              kind Method
//              display_name hello
//              signature_documentation
//              > public final fun hello(): String
//                 ^^^^^^ reference scip-java maven . . kotlin/String#
//                             ⌃ enclosing_range_end scip-java maven . . snapshots/MySingleton#hello().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/MySingleton#
//⌃ enclosing_range_end scip-java maven . . snapshots/MySingleton#`<init>`().
//⌃ enclosing_range_end scip-java maven . . snapshots/MySingleton#INSTANCE.
//⌄ enclosing_range_start scip-java maven . . snapshots/useSingleton().
  fun useSingleton() = MySingleton.hello()
//    ^^^^^^^^^^^^ definition scip-java maven . . snapshots/useSingleton().
//                 kind Method
//                 display_name useSingleton
//                 signature_documentation
//                 > public final fun useSingleton(): String
//                     ^^^^^^^^^^^ reference scip-java maven . . snapshots/MySingleton#
//                                 ^^^^^ reference scip-java maven . . snapshots/MySingleton#hello().
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/useSingleton().
  
