  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
  import java.io.Serializable
//       ^^^^ reference scip-java maven . . java/
//            ^^ reference scip-java maven . . java/io/
//               ^^^^^^^^^^^^ reference scip-java maven jdk 17 java/io/Serializable#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/DocstringSuperclass#
//⌄ enclosing_range_start scip-java maven . . snapshots/DocstringSuperclass#`<init>`().
  abstract class DocstringSuperclass
//               ^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/DocstringSuperclass#
//                                   kind Class
//                                   display_name DocstringSuperclass
//                                   signature_documentation
//                                   > public abstract class DocstringSuperclass : Any
//               ^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/DocstringSuperclass#`<init>`().
//                                   kind Constructor
//                                   display_name DocstringSuperclass
//                                   signature_documentation
//                                   > public constructor(): DocstringSuperclass
//                                 ⌃ enclosing_range_end scip-java maven . . snapshots/DocstringSuperclass#
//                                 ⌃ enclosing_range_end scip-java maven . . snapshots/DocstringSuperclass#`<init>`().
//⌄ enclosing_range_start scip-java maven . . snapshots/Docstrings#
//⌄ enclosing_range_start scip-java maven . . snapshots/Docstrings#`<init>`().
  /** Example class docstring. */
  class Docstrings :  DocstringSuperclass(), Serializable {
//      ^^^^^^^^^^ definition scip-java maven . . snapshots/Docstrings#
//                 kind Class
//                 display_name Docstrings
//                 signature_documentation
//                 > public final class Docstrings : DocstringSuperclass, Serializable
//                 documentation
//                 > Example class docstring.
//                 relationship scip-java maven . . snapshots/DocstringSuperclass# implementation
//                 relationship scip-java maven jdk 17 java/io/Serializable# implementation
//      ^^^^^^^^^^ definition scip-java maven . . snapshots/Docstrings#`<init>`().
//                 kind Constructor
//                 display_name Docstrings
//                 signature_documentation
//                 > public constructor(): Docstrings
//                 documentation
//                 > Example class docstring.
//                    ^^^^^^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/DocstringSuperclass#
//                                           ^^^^^^^^^^^^ reference scip-java maven jdk 17 java/io/Serializable#
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Docstrings#
//⌃ enclosing_range_end scip-java maven . . snapshots/Docstrings#`<init>`().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/docstrings().
  /** Example method docstring. */
  fun docstrings() { }
//    ^^^^^^^^^^ definition scip-java maven . . snapshots/docstrings().
//               kind Method
//               display_name docstrings
//               signature_documentation
//               > public final fun docstrings(): Unit
//               documentation
//               > Example method docstring.
//                   ⌃ enclosing_range_end scip-java maven . . snapshots/docstrings().
  
