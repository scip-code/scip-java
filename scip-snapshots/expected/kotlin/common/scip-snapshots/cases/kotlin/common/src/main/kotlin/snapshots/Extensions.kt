  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/String#foo().
  // Cross-package extensions belong to snapshots, not to the receiver's kotlin package.
  fun String.foo(): Int = 42
//    ^^^^^^ reference scip-java maven . . kotlin/String#
//           ^^^ definition scip-java maven . . snapshots/String#foo().
//               kind Method
//               display_name foo
//               signature_documentation
//               > public final fun String.foo(): Int
//                  ^^^ reference scip-java maven . . kotlin/Int#
//                         ⌃ enclosing_range_end scip-java maven . . snapshots/String#foo().
//⌄ enclosing_range_start scip-java maven . . snapshots/useExtension().
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/useExtension().(s)
  fun useExtension(s: String) = s.foo()
//    ^^^^^^^^^^^^ definition scip-java maven . . snapshots/useExtension().
//                 kind Method
//                 display_name useExtension
//                 signature_documentation
//                 > public final fun useExtension(s: String): Int
//                 ^ definition scip-java maven . . snapshots/useExtension().(s)
//                   kind Parameter
//                   display_name s
//                   signature_documentation
//                   > s: String
//                    ^^^^^^ reference scip-java maven . . kotlin/String#
//                              ^ reference scip-java maven . . snapshots/useExtension().(s)
//                                ^^^ reference scip-java maven . . snapshots/String#foo().
//                         ⌃ enclosing_range_end scip-java maven . . snapshots/useExtension().(s)
//                                    ⌃ enclosing_range_end scip-java maven . . snapshots/useExtension().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Int#asString.
//                         ⌄ enclosing_range_start scip-java maven . . snapshots/Int#getAsString().
  val Int.asString: String get() = this.toString()
//    ^^^ reference scip-java maven . . kotlin/Int#
//        ^^^^^^^^ definition scip-java maven . . snapshots/Int#asString.
//                 kind Property
//                 display_name asString
//                 signature_documentation
//                 > public final val Int.asString: String
//                  ^^^^^^ reference scip-java maven . . kotlin/String#
//                         ^^^ definition scip-java maven . . snapshots/Int#getAsString().
//                             kind Method
//                             display_name asString
//                             signature_documentation
//                             > public get(): String
//                                      ^^^^^^^^ reference scip-java maven . . kotlin/Int#toString().
//                                               ⌃ enclosing_range_end scip-java maven . . snapshots/Int#asString.
//                                               ⌃ enclosing_range_end scip-java maven . . snapshots/Int#getAsString().
//⌄ enclosing_range_start scip-java maven . . snapshots/useExtensionProperty().
  fun useExtensionProperty() = 42.asString
//    ^^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/useExtensionProperty().
//                         kind Method
//                         display_name useExtensionProperty
//                         signature_documentation
//                         > public final fun useExtensionProperty(): String
//                                ^^^^^^^^ reference scip-java maven . . snapshots/Int#asString.
//                                ^^^^^^^^ reference scip-java maven . . snapshots/Int#getAsString().
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/useExtensionProperty().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/ExtensionReceiver#
//⌄ enclosing_range_start scip-java maven . . snapshots/ExtensionReceiver#`<init>`().
  class ExtensionReceiver {
//      ^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/ExtensionReceiver#
//                        kind Class
//                        display_name ExtensionReceiver
//                        signature_documentation
//                        > public final class ExtensionReceiver : Any
//      ^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/ExtensionReceiver#`<init>`().
//                        kind Constructor
//                        display_name ExtensionReceiver
//                        signature_documentation
//                        > public constructor(): ExtensionReceiver
//    ⌄ enclosing_range_start scip-java maven . . snapshots/ExtensionReceiver#foo().
      fun foo() {}
//        ^^^ definition scip-java maven . . snapshots/ExtensionReceiver#foo().
//            kind Method
//            display_name foo
//            signature_documentation
//            > public final fun foo(): Unit
//               ⌃ enclosing_range_end scip-java maven . . snapshots/ExtensionReceiver#foo().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/ExtensionReceiver#
//⌃ enclosing_range_end scip-java maven . . snapshots/ExtensionReceiver#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/ExtensionReceiver#foo(+1).
  // The extension is numbered after the receiver's member, so it gets foo(+1).
//                          ⌄ enclosing_range_start scip-java maven . . snapshots/ExtensionReceiver#foo(+1).(x)
  fun ExtensionReceiver.foo(x: Int) {}
//    ^^^^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/ExtensionReceiver#
//                      ^^^ definition scip-java maven . . snapshots/ExtensionReceiver#foo(+1).
//                          kind Method
//                          display_name foo
//                          signature_documentation
//                          > public final fun ExtensionReceiver.foo(x: Int): Unit
//                          ^ definition scip-java maven . . snapshots/ExtensionReceiver#foo(+1).(x)
//                            kind Parameter
//                            display_name x
//                            signature_documentation
//                            > x: Int
//                             ^^^ reference scip-java maven . . kotlin/Int#
//                               ⌃ enclosing_range_end scip-java maven . . snapshots/ExtensionReceiver#foo(+1).(x)
//                                   ⌃ enclosing_range_end scip-java maven . . snapshots/ExtensionReceiver#foo(+1).
  
