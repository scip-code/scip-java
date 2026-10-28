  package snapshots;
  
  // No JDK classes: with --release, newer JDKs compile against ct.sym, which omits private
  // constructors, so javac's overload numbers for them depend on the JDK running the build.
//⌄ enclosing_range_start scip-java maven . . snapshots/ConstructorsConsumer#
  public class ConstructorsConsumer {
//             ^^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/ConstructorsConsumer#
//                                  kind Class
//                                  display_name ConstructorsConsumer
//                                  signature_documentation
//                                  > public class ConstructorsConsumer
//             ^^^^^^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/ConstructorsConsumer#`<init>`().
//                                  kind Constructor
//                                  display_name <init>
//                                  signature_documentation
//                                  > public ConstructorsConsumer()
//  ⌄ enclosing_range_start scip-java maven . . snapshots/ConstructorsConsumer#run().
    public static void run() {
//                     ^^^ definition scip-java maven . . snapshots/ConstructorsConsumer#run().
//                         kind StaticMethod
//                         display_name run
//                         signature_documentation
//                         > public static void run()
      new kotlin.text.Regex("a");
//        ^^^^^^ reference scip-java maven . . kotlin/
//               ^^^^ reference scip-java maven . . kotlin/text/
//                    ^^^^^ reference scip-java maven . . kotlin/text/Regex#`<init>`(+1).
      new Overloaded(1);
//        ^^^^^^^^^^ reference scip-java maven . . snapshots/Overloaded#`<init>`(+1).
    }
//  ⌃ enclosing_range_end scip-java maven . . snapshots/ConstructorsConsumer#run().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/ConstructorsConsumer#
  
