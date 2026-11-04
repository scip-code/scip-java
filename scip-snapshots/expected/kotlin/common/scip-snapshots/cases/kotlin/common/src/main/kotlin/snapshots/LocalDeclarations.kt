  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/localClasses().
  fun localClasses() {
//    ^^^^^^^^^^^^ definition scip-java maven . . snapshots/localClasses().
//                 kind Method
//                 display_name localClasses
//                 signature_documentation
//                 > public final fun localClasses(): Unit
//    ⌄ enclosing_range_start local 0
//    ⌄ enclosing_range_start local 1
      class LocalClass {
//          ^^^^^^^^^^ definition local 0
//                     kind Class
//                     display_name LocalClass
//                     signature_documentation
//                     > local final class LocalClass : Any
//          ^^^^^^^^^^ definition local 1
//                     kind Constructor
//                     display_name LocalClass
//                     signature_documentation
//                     > public constructor(): LocalClass
//        ⌄ enclosing_range_start local 2
          fun localClassMethod() {}
//            ^^^^^^^^^^^^^^^^ definition local 2
//                             kind Method
//                             display_name localClassMethod
//                             signature_documentation
//                             > public final fun localClassMethod(): Unit
//                                ⌃ enclosing_range_end local 2
      }
//    ⌃ enclosing_range_end local 0
//    ⌃ enclosing_range_end local 1
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/localClasses().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Interface#
  interface Interface {
//          ^^^^^^^^^ definition scip-java maven . . snapshots/Interface#
//                    kind Interface
//                    display_name Interface
//                    signature_documentation
//                    > public abstract interface Interface : Any
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Interface#foo().
      fun foo()
//        ^^^ definition scip-java maven . . snapshots/Interface#foo().
//            kind Method
//            display_name foo
//            signature_documentation
//            > public abstract fun foo(): Unit
//            > 
//            ⌃ enclosing_range_end scip-java maven . . snapshots/Interface#foo().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Interface#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/anonymousObjects().
  fun anonymousObjects() {
//    ^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/anonymousObjects().
//                     kind Method
//                     display_name anonymousObjects
//                     signature_documentation
//                     > public final fun anonymousObjects(): Unit
      // Each object and its members need distinct local symbols, with no INSTANCE field.
//    ⌄ enclosing_range_start local 3
//            ⌄ enclosing_range_start local 4
//            ⌄ enclosing_range_start local 5
      val a = object : Interface {
//        ^ definition local 3
//          kind Property
//          display_name a
//          signature_documentation
//          > local val a: `<anonymous>`
//            ^^^^^^ definition local 4
//                   kind Class
//                   display_name <anonymous>
//                   signature_documentation
//                   > object : Interface
//                   relationship scip-java maven . . snapshots/Interface# implementation
//            ^^^^^^ definition local 5
//                   kind Constructor
//                   display_name <anonymous>
//                   signature_documentation
//                   > private constructor(): <anonymous>
//                     ^^^^^^^^^ reference scip-java maven . . snapshots/Interface#
//        ⌄ enclosing_range_start local 6
          override fun foo() {}
//                     ^^^ definition local 6
//                         kind Method
//                         display_name foo
//                         signature_documentation
//                         > public open override fun foo(): Unit
//                         relationship scip-java maven . . snapshots/Interface#foo(). implementation
//                            ⌃ enclosing_range_end local 6
      }
//    ⌃ enclosing_range_end local 3
//    ⌃ enclosing_range_end local 4
//    ⌃ enclosing_range_end local 5
//    ⌄ enclosing_range_start local 7
//            ⌄ enclosing_range_start local 8
//            ⌄ enclosing_range_start local 9
      val b = object : Interface {
//        ^ definition local 7
//          kind Property
//          display_name b
//          signature_documentation
//          > local val b: `<anonymous>`
//            ^^^^^^ definition local 8
//                   kind Class
//                   display_name <anonymous>
//                   signature_documentation
//                   > object : Interface
//                   relationship scip-java maven . . snapshots/Interface# implementation
//            ^^^^^^ definition local 9
//                   kind Constructor
//                   display_name <anonymous>
//                   signature_documentation
//                   > private constructor(): <anonymous>
//                     ^^^^^^^^^ reference scip-java maven . . snapshots/Interface#
//        ⌄ enclosing_range_start local 10
          override fun foo() {}
//                     ^^^ definition local 10
//                         kind Method
//                         display_name foo
//                         signature_documentation
//                         > public open override fun foo(): Unit
//                         relationship scip-java maven . . snapshots/Interface#foo(). implementation
//                            ⌃ enclosing_range_end local 10
      }
//    ⌃ enclosing_range_end local 7
//    ⌃ enclosing_range_end local 8
//    ⌃ enclosing_range_end local 9
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/anonymousObjects().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/outer().
  fun outer() {
//    ^^^^^ definition scip-java maven . . snapshots/outer().
//          kind Method
//          display_name outer
//          signature_documentation
//          > public final fun outer(): Unit
//    ⌄ enclosing_range_start local 11
      fun inner() {}
//        ^^^^^ definition local 11
//              kind Method
//              display_name inner
//              signature_documentation
//              > local final fun inner(): Unit
//                 ⌃ enclosing_range_end local 11
//    ⌄ enclosing_range_start local 12
      fun innerWithReturnType(): Int = 42
//        ^^^^^^^^^^^^^^^^^^^ definition local 12
//                            kind Method
//                            display_name innerWithReturnType
//                            signature_documentation
//                            > local final fun innerWithReturnType(): Int
//                               ^^^ reference scip-java maven . . kotlin/Int#
//                                      ⌃ enclosing_range_end local 12
      inner()
//    ^^^^^ reference local 11
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/outer().
  
