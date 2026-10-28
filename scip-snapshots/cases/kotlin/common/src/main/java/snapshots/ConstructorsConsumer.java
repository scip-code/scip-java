package snapshots;

// No JDK classes: with --release, newer JDKs compile against ct.sym, which omits private
// constructors, so javac's overload numbers for them depend on the JDK running the build.
public class ConstructorsConsumer {
  public static void run() {
    new kotlin.text.Regex("a");
    new Overloaded(1);
  }
}
