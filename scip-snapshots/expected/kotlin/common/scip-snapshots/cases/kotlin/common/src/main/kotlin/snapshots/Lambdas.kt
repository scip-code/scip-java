  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/x.
//⌄ enclosing_range_start scip-java maven . . snapshots/getX().
//                                               ⌄ enclosing_range_start local 0
//                                                  ⌄ enclosing_range_start local 1
  val x = arrayListOf<String>().forEachIndexed { i, s -> println("$i $s") }
//    ^ definition scip-java maven . . snapshots/x.
//      kind Property
//      display_name x
//      signature_documentation
//      > public final val x: Unit
//    ^ definition scip-java maven . . snapshots/getX().
//      kind Method
//      display_name x
//      signature_documentation
//      > public get(): Unit
//        ^^^^^^^^^^^ reference scip-java maven . . kotlin/collections/arrayListOf().
//                              ^^^^^^^^^^^^^^ reference scip-java maven . . kotlin/collections/forEachIndexed(+9).
//                                               ^ definition local 0
//                                                 kind Parameter
//                                                 display_name i
//                                                 signature_documentation
//                                                 > i: Int
//                                                  ^ definition local 1
//                                                    kind Parameter
//                                                    display_name s
//                                                    signature_documentation
//                                                    > s: String
//                                                       ^^^^^^^ reference scip-java maven . . kotlin/io/println().
//                                                                 ^ reference local 0
//                                                                    ^ reference local 1
//                                               ⌃ enclosing_range_end local 0
//                                                  ⌃ enclosing_range_end local 1
//                                                                        ⌃ enclosing_range_end scip-java maven . . snapshots/x.
//                                                                        ⌃ enclosing_range_end scip-java maven . . snapshots/getX().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/y.
//⌄ enclosing_range_start scip-java maven . . snapshots/getY().
  val y = "fdsa".run { this.toByteArray() }
//    ^ definition scip-java maven . . snapshots/y.
//      kind Property
//      display_name y
//      signature_documentation
//      > public final val y: ByteArray
//    ^ definition scip-java maven . . snapshots/getY().
//      kind Method
//      display_name y
//      signature_documentation
//      > public get(): ByteArray
//               ^^^ reference scip-java maven . . kotlin/run(+1).
//                          ^^^^^^^^^^^ reference scip-java maven . . kotlin/text/toByteArray().
//                                        ⌃ enclosing_range_end scip-java maven . . snapshots/y.
//                                        ⌃ enclosing_range_end scip-java maven . . snapshots/getY().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/z.
//⌄ enclosing_range_start scip-java maven . . snapshots/getZ().
//              ⌄ enclosing_range_start local 2
  val z = y.let { it.size }
//    ^ definition scip-java maven . . snapshots/z.
//      kind Property
//      display_name z
//      signature_documentation
//      > public final val z: Int
//    ^ definition scip-java maven . . snapshots/getZ().
//      kind Method
//      display_name z
//      signature_documentation
//      > public get(): Int
//        ^ reference scip-java maven . . snapshots/y.
//        ^ reference scip-java maven . . snapshots/getY().
//          ^^^ reference scip-java maven . . kotlin/let().
//              ^^^^^^^^^^^ definition local 2
//                          kind Parameter
//                          display_name it
//                          signature_documentation
//                          > it: ByteArray
//                ^^ reference local 2
//                   ^^^^ reference scip-java maven . . kotlin/ByteArray#size.
//                   ^^^^ reference scip-java maven . . kotlin/ByteArray#getSize().
//                        ⌃ enclosing_range_end scip-java maven . . snapshots/z.
//                        ⌃ enclosing_range_end scip-java maven . . snapshots/getZ().
//                        ⌃ enclosing_range_end local 2
  
