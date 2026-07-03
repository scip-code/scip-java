  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Class#
//            ⌄ enclosing_range_start scip-java maven . . snapshots/Class#`<init>`().
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Class#`<init>`().(banana)
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Class#banana.
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Class#banana.get().
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Class#banana.set().
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Class#banana.set().(value)
//                                                 ⌄ enclosing_range_start scip-java maven . . snapshots/Class#`<init>`().(apple)
  class Class constructor(private var banana: Int, apple: String) :
//      ^^^^^ definition scip-java maven . . snapshots/Class#
//            kind Class
//            display_name Class
//            signature_documentation
//            > public final class Class : Throwable
//            relationship scip-java maven . . kotlin/Throwable# implementation
//            ^^^^^^^^^^^ definition scip-java maven . . snapshots/Class#`<init>`().
//                        kind Constructor
//                        display_name Class
//                        signature_documentation
//                        > public constructor(banana: Int, apple: String): Class
//                                    ^^^^^^ definition scip-java maven . . snapshots/Class#`<init>`().(banana)
//                                           kind Parameter
//                                           display_name banana
//                                           signature_documentation
//                                           > banana: Int
//                                    ^^^^^^ definition scip-java maven . . snapshots/Class#banana.
//                                           kind Property
//                                           display_name banana
//                                           signature_documentation
//                                           > private final var banana: Int
//                                    ^^^^^^ reference scip-java maven . . snapshots/Class#`<init>`().(banana)
//                                    ^^^^^^ definition scip-java maven . . snapshots/Class#banana.get().
//                                           kind Method
//                                           display_name banana
//                                           signature_documentation
//                                           > private get(): Int
//                                    ^^^^^^ definition scip-java maven . . snapshots/Class#banana.set().
//                                           kind Method
//                                           display_name banana
//                                           signature_documentation
//                                           > private set(value: Int): Unit
//                                    ^^^^^^ definition scip-java maven . . snapshots/Class#banana.set().(value)
//                                           kind Parameter
//                                           display_name value
//                                           signature_documentation
//                                           > value: Int
//                                            ^^^ reference scip-java maven . . kotlin/Int#
//                                                 ^^^^^ definition scip-java maven . . snapshots/Class#`<init>`().(apple)
//                                                       kind Parameter
//                                                       display_name apple
//                                                       signature_documentation
//                                                       > apple: String
//                                                        ^^^^^^ reference scip-java maven . . kotlin/String#
//                                              ⌃ enclosing_range_end scip-java maven . . snapshots/Class#`<init>`().(banana)
//                                              ⌃ enclosing_range_end scip-java maven . . snapshots/Class#banana.
//                                              ⌃ enclosing_range_end scip-java maven . . snapshots/Class#banana.get().
//                                              ⌃ enclosing_range_end scip-java maven . . snapshots/Class#banana.set().
//                                              ⌃ enclosing_range_end scip-java maven . . snapshots/Class#banana.set().(value)
//                                                             ⌃ enclosing_range_end scip-java maven . . snapshots/Class#`<init>`().(apple)
//                                                              ⌃ enclosing_range_end scip-java maven . . snapshots/Class#`<init>`().
      Throwable(banana.toString() + apple) {
//    ^^^^^^^^^ reference scip-java maven . . kotlin/Throwable#
//              ^^^^^^ reference scip-java maven . . snapshots/Class#`<init>`().(banana)
//                     ^^^^^^^^ reference scip-java maven . . kotlin/Int#toString().
//                                ^ reference scip-java maven . . kotlin/String#plus().
//                                  ^^^^^ reference scip-java maven . . snapshots/Class#`<init>`().(apple)
    init {
      println("")
//    ^^^^^^^ reference scip-java maven . . kotlin/io/println().
    }
  
//  ⌄ enclosing_range_start scip-java maven . . snapshots/Class#asdf.
//  ⌄ enclosing_range_start scip-java maven . . snapshots/Class#asdf.get().
    val asdf =
//      ^^^^ definition scip-java maven . . snapshots/Class#asdf.
//           kind Property
//           display_name asdf
//           signature_documentation
//           > public final val asdf: Any
//      ^^^^ definition scip-java maven . . snapshots/Class#asdf.get().
//           kind Method
//           display_name asdf
//           signature_documentation
//           > public get(): Any
//      ⌄ enclosing_range_start local 0
//      ⌄ enclosing_range_start local 1
        object {
//      ^^^^^^ definition local 0
//             kind Class
//             display_name <anonymous>
//             signature_documentation
//             > object : Any
//      ^^^^^^ definition local 1
//             kind Constructor
//             display_name <anonymous>
//             signature_documentation
//             > private constructor(): <anonymous>
//        ⌄ enclosing_range_start local 2
          fun doStuff() = Unit
//            ^^^^^^^ definition local 2
//                    kind Method
//                    display_name doStuff
//                    signature_documentation
//                    > public final fun doStuff(): Unit
//                        ^^^^ reference scip-java maven . . kotlin/Unit#
//                           ⌃ enclosing_range_end local 2
        }
//      ⌃ enclosing_range_end scip-java maven . . snapshots/Class#asdf.
//      ⌃ enclosing_range_end scip-java maven . . snapshots/Class#asdf.get().
//      ⌃ enclosing_range_end local 0
//      ⌃ enclosing_range_end local 1
  
//  ⌄ enclosing_range_start scip-java maven . . snapshots/Class#`<init>`(+1).
    constructor() : this(1, "")
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Class#`<init>`(+1).
//                              kind Constructor
//                              display_name Class
//                              signature_documentation
//                              > public constructor(): Class
//                            ⌃ enclosing_range_end scip-java maven . . snapshots/Class#`<init>`(+1).
  
//  ⌄ enclosing_range_start scip-java maven . . snapshots/Class#`<init>`(+2).
//              ⌄ enclosing_range_start scip-java maven . . snapshots/Class#`<init>`(+2).(banana)
    constructor(banana: Int) : this(banana, "")
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/Class#`<init>`(+2).
//                                              kind Constructor
//                                              display_name Class
//                                              signature_documentation
//                                              > public constructor(banana: Int): Class
//              ^^^^^^ definition scip-java maven . . snapshots/Class#`<init>`(+2).(banana)
//                     kind Parameter
//                     display_name banana
//                     signature_documentation
//                     > banana: Int
//                      ^^^ reference scip-java maven . . kotlin/Int#
//                                  ^^^^^^ reference scip-java maven . . snapshots/Class#`<init>`(+2).(banana)
//                        ⌃ enclosing_range_end scip-java maven . . snapshots/Class#`<init>`(+2).(banana)
//                                            ⌃ enclosing_range_end scip-java maven . . snapshots/Class#`<init>`(+2).
  
//  ⌄ enclosing_range_start scip-java maven . . snapshots/Class#run().
    fun run() {
//      ^^^ definition scip-java maven . . snapshots/Class#run().
//          kind Method
//          display_name run
//          signature_documentation
//          > public final fun run(): Unit
      println(Class::class)
//    ^^^^^^^ reference scip-java maven . . kotlin/io/println().
//            ^^^^^ reference scip-java maven . . snapshots/Class#
      println("I eat $banana for lunch")
//    ^^^^^^^ reference scip-java maven . . kotlin/io/println().
//                    ^^^^^^ reference scip-java maven . . snapshots/Class#banana.
//                    ^^^^^^ reference scip-java maven . . snapshots/Class#banana.get().
//                    ^^^^^^ reference scip-java maven . . snapshots/Class#banana.set().
      banana = 42
//    ^^^^^^ reference scip-java maven . . snapshots/Class#banana.
//    ^^^^^^ reference scip-java maven . . snapshots/Class#banana.get().
//    ^^^^^^ reference scip-java maven . . snapshots/Class#banana.set().
    }
//  ⌃ enclosing_range_end scip-java maven . . snapshots/Class#run().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Class#
  
