  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/CompanionOwner#
//⌄ enclosing_range_start scip-java maven . . snapshots/CompanionOwner#`<init>`().
  class CompanionOwner {
//      ^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/CompanionOwner#
//                     kind Class
//                     display_name CompanionOwner
//                     signature_documentation
//                     > public final class CompanionOwner : Any
//      ^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/CompanionOwner#`<init>`().
//                     kind Constructor
//                     display_name CompanionOwner
//                     signature_documentation
//                     > public constructor(): CompanionOwner
//  ⌄ enclosing_range_start scip-java maven . . snapshots/CompanionOwner#Companion#
//  ⌄ enclosing_range_start scip-java maven . . snapshots/CompanionOwner#Companion#`<init>`().
//  ⌄ enclosing_range_start scip-java maven . . snapshots/CompanionOwner#Companion.
    companion object {
//  ^^^^^^^^^ definition scip-java maven . . snapshots/CompanionOwner#Companion#
//            kind Class
//            display_name Companion
//            signature_documentation
//            > public final companion object Companion : Any
//  ^^^^^^^^^ definition scip-java maven . . snapshots/CompanionOwner#Companion#`<init>`().
//            kind Constructor
//            display_name Companion
//            signature_documentation
//            > private constructor(): CompanionOwner.Companion
//  ^^^^^^^^^ definition scip-java maven . . snapshots/CompanionOwner#Companion.
//            kind StaticField
//            display_name Companion
//            signature_documentation
//            > public final static val Companion: CompanionOwner.Companion
//    ⌄ enclosing_range_start scip-java maven . . snapshots/CompanionOwner#Companion#create().
      fun create(): CompanionOwner = CompanionOwner()
//        ^^^^^^ definition scip-java maven . . snapshots/CompanionOwner#Companion#create().
//               kind Method
//               display_name create
//               signature_documentation
//               > public final fun create(): CompanionOwner
//                  ^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/CompanionOwner#
//                                   ^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/CompanionOwner#`<init>`().
//                                                  ⌃ enclosing_range_end scip-java maven . . snapshots/CompanionOwner#Companion#create().
    }
//  ⌃ enclosing_range_end scip-java maven . . snapshots/CompanionOwner#Companion#
//  ⌃ enclosing_range_end scip-java maven . . snapshots/CompanionOwner#Companion#`<init>`().
//  ⌃ enclosing_range_end scip-java maven . . snapshots/CompanionOwner#Companion.
//  ⌄ enclosing_range_start scip-java maven . . snapshots/CompanionOwner#create().
    fun create(): Int = CompanionOwner.create().hashCode()
//      ^^^^^^ definition scip-java maven . . snapshots/CompanionOwner#create().
//             kind Method
//             display_name create
//             signature_documentation
//             > public final fun create(): Int
//                ^^^ reference scip-java maven . . kotlin/Int#
//                      ^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/CompanionOwner#
//                                     ^^^^^^ reference scip-java maven . . snapshots/CompanionOwner#Companion#create().
//                                              ^^^^^^^^ reference scip-java maven . . kotlin/Any#hashCode().
//                                                       ⌃ enclosing_range_end scip-java maven . . snapshots/CompanionOwner#create().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/CompanionOwner#
//⌃ enclosing_range_end scip-java maven . . snapshots/CompanionOwner#`<init>`().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Foo#
//⌄ enclosing_range_start scip-java maven . . snapshots/Foo#`<init>`().
  class Foo {
//      ^^^ definition scip-java maven . . snapshots/Foo#
//          kind Class
//          display_name Foo
//          signature_documentation
//          > public final class Foo : Any
//      ^^^ definition scip-java maven . . snapshots/Foo#`<init>`().
//          kind Constructor
//          display_name Foo
//          signature_documentation
//          > public constructor(): Foo
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Foo#Factory#
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Foo#Factory#`<init>`().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Foo#Factory.
      companion object Factory {
//                     ^^^^^^^ definition scip-java maven . . snapshots/Foo#Factory#
//                             kind Class
//                             display_name Factory
//                             signature_documentation
//                             > public final companion object Factory : Any
//                     ^^^^^^^ definition scip-java maven . . snapshots/Foo#Factory#`<init>`().
//                             kind Constructor
//                             display_name Factory
//                             signature_documentation
//                             > private constructor(): Foo.Factory
//                     ^^^^^^^ definition scip-java maven . . snapshots/Foo#Factory.
//                             kind StaticField
//                             display_name Factory
//                             signature_documentation
//                             > public final static val Factory: Foo.Factory
//        ⌄ enclosing_range_start scip-java maven . . snapshots/Foo#Factory#create().
          fun create(): Foo = Foo()
//            ^^^^^^ definition scip-java maven . . snapshots/Foo#Factory#create().
//                   kind Method
//                   display_name create
//                   signature_documentation
//                   > public final fun create(): Foo
//                      ^^^ reference scip-java maven . . snapshots/Foo#
//                            ^^^ reference scip-java maven . . snapshots/Foo#`<init>`().
//                                ⌃ enclosing_range_end scip-java maven . . snapshots/Foo#Factory#create().
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Foo#Factory#
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Foo#Factory#`<init>`().
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Foo#Factory.
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Foo#
//⌃ enclosing_range_end scip-java maven . . snapshots/Foo#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/useNamedCompanion().
  fun useNamedCompanion() = Foo.Factory.create()
//    ^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/useNamedCompanion().
//                      kind Method
//                      display_name useNamedCompanion
//                      signature_documentation
//                      > public final fun useNamedCompanion(): Foo
//                          ^^^ reference scip-java maven . . snapshots/Foo#
//                              ^^^^^^^ reference scip-java maven . . snapshots/Foo#Factory#
//                                      ^^^^^^ reference scip-java maven . . snapshots/Foo#Factory#create().
//                                             ⌃ enclosing_range_end scip-java maven . . snapshots/useNamedCompanion().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Bar#
//⌄ enclosing_range_start scip-java maven . . snapshots/Bar#`<init>`().
  class Bar {
//      ^^^ definition scip-java maven . . snapshots/Bar#
//          kind Class
//          display_name Bar
//          signature_documentation
//          > public final class Bar : Any
//      ^^^ definition scip-java maven . . snapshots/Bar#`<init>`().
//          kind Constructor
//          display_name Bar
//          signature_documentation
//          > public constructor(): Bar
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Bar#Companion#
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Bar#Companion#`<init>`().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Bar#Companion.
      companion object {
//    ^^^^^^^^^ definition scip-java maven . . snapshots/Bar#Companion#
//              kind Class
//              display_name Companion
//              signature_documentation
//              > public final companion object Companion : Any
//    ^^^^^^^^^ definition scip-java maven . . snapshots/Bar#Companion#`<init>`().
//              kind Constructor
//              display_name Companion
//              signature_documentation
//              > private constructor(): Bar.Companion
//    ^^^^^^^^^ definition scip-java maven . . snapshots/Bar#Companion.
//              kind StaticField
//              display_name Companion
//              signature_documentation
//              > public final static val Companion: Bar.Companion
//        ⌄ enclosing_range_start scip-java maven . . snapshots/Bar#Companion#instance().
          fun instance(): Bar = Bar()
//            ^^^^^^^^ definition scip-java maven . . snapshots/Bar#Companion#instance().
//                     kind Method
//                     display_name instance
//                     signature_documentation
//                     > public final fun instance(): Bar
//                        ^^^ reference scip-java maven . . snapshots/Bar#
//                              ^^^ reference scip-java maven . . snapshots/Bar#`<init>`().
//                                  ⌃ enclosing_range_end scip-java maven . . snapshots/Bar#Companion#instance().
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Bar#Companion#
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Bar#Companion#`<init>`().
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Bar#Companion.
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Bar#
//⌃ enclosing_range_end scip-java maven . . snapshots/Bar#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/useUnnamedCompanion().
  fun useUnnamedCompanion() = Bar.instance()
//    ^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/useUnnamedCompanion().
//                        kind Method
//                        display_name useUnnamedCompanion
//                        signature_documentation
//                        > public final fun useUnnamedCompanion(): Bar
//                            ^^^ reference scip-java maven . . snapshots/Bar#
//                                ^^^^^^^^ reference scip-java maven . . snapshots/Bar#Companion#instance().
//                                         ⌃ enclosing_range_end scip-java maven . . snapshots/useUnnamedCompanion().
  
