package snapshots

// Cross-package extensions belong to snapshots, not to the receiver's kotlin package.
fun String.foo(): Int = 42
fun useExtension(s: String) = s.foo()

val Int.asString: String get() = this.toString()
fun useExtensionProperty() = 42.asString

class ExtensionReceiver {
    fun foo() {}
}
// The extension is numbered after the receiver's member, so it gets foo(+1).
fun ExtensionReceiver.foo(x: Int) {}
