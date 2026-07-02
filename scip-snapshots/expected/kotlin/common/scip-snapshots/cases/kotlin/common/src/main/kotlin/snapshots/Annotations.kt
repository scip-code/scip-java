  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Tagged#
  @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
// ^^^^^^ reference scip-java maven . . kotlin/annotation/Target#
//        ^^^^^^^^^^^^^^^^ reference scip-java maven . . kotlin/annotation/AnnotationTarget#
//                         ^^^^^ reference scip-java maven . . kotlin/annotation/AnnotationTarget#CLASS.
//                                ^^^^^^^^^^^^^^^^ reference scip-java maven . . kotlin/annotation/AnnotationTarget#
//                                                 ^^^^^^^^ reference scip-java maven . . kotlin/annotation/AnnotationTarget#FUNCTION.
//                       ⌄ enclosing_range_start scip-java maven . . snapshots/Tagged#`<init>`().
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Tagged#`<init>`().(tag)
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Tagged#tag.
//                        ⌄ enclosing_range_start scip-java maven . . snapshots/Tagged#getTag().
  annotation class Tagged(val tag: String)
//                 ^^^^^^ definition scip-java maven . . snapshots/Tagged#
//                        display_name Tagged
//                        signature_documentation
//                        > @Target(...) public final annotation class Tagged : Annotation
//                        relationship scip-java maven . . kotlin/Annotation# implementation
//                 ^^^^^^ definition scip-java maven . . snapshots/Tagged#`<init>`().
//                        display_name Tagged
//                        signature_documentation
//                        > public constructor(tag: String): Tagged
//                            ^^^ definition scip-java maven . . snapshots/Tagged#`<init>`().(tag)
//                                display_name tag
//                                signature_documentation
//                                > tag: String
//                            ^^^ definition scip-java maven . . snapshots/Tagged#tag.
//                                display_name tag
//                                signature_documentation
//                                > public final val tag: String
//                            ^^^ reference scip-java maven . . snapshots/Tagged#`<init>`().(tag)
//                            ^^^ definition scip-java maven . . snapshots/Tagged#getTag().
//                                display_name tag
//                                signature_documentation
//                                > public get(): String
//                                 ^^^^^^ reference scip-java maven . . kotlin/String#
//                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Tagged#`<init>`().(tag)
//                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Tagged#tag.
//                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Tagged#getTag().
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/Tagged#
//                                       ⌃ enclosing_range_end scip-java maven . . snapshots/Tagged#`<init>`().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/AnnotatedService#
//⌄ enclosing_range_start scip-java maven . . snapshots/AnnotatedService#`<init>`().
  @Tagged("service")
// ^^^^^^ reference scip-java maven . . snapshots/Tagged#
  class AnnotatedService {
//      ^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/AnnotatedService#
//                       display_name AnnotatedService
//                       signature_documentation
//                       > @Tagged(...) public final class AnnotatedService : Any
//      ^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/AnnotatedService#`<init>`().
//                       display_name AnnotatedService
//                       signature_documentation
//                       > public constructor(): AnnotatedService
//    ⌄ enclosing_range_start scip-java maven . . snapshots/AnnotatedService#run().
      @Tagged("run")
//     ^^^^^^ reference scip-java maven . . snapshots/Tagged#
      fun run(): String = "running"
//        ^^^ definition scip-java maven . . snapshots/AnnotatedService#run().
//            display_name run
//            signature_documentation
//            > @Tagged(...) public final fun run(): String
//               ^^^^^^ reference scip-java maven . . kotlin/String#
//                                ⌃ enclosing_range_end scip-java maven . . snapshots/AnnotatedService#run().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/AnnotatedService#
//⌃ enclosing_range_end scip-java maven . . snapshots/AnnotatedService#`<init>`().
  
