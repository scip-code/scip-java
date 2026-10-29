package snapshots

class Overloaded(val s: String) {
  constructor(i: Int) : this(i.toString())
}

typealias OverloadedAlias = Overloaded

typealias StringIntMap = HashMap<String, Int>

// `Regex` and `OverloadedAlias` reference the same symbols as ConstructorsConsumer.java.
fun constructors() {
  // Library constructors are numbered like javac, private ones included.
  java.io.File("a", "b")
  Regex("a")
  // Calls through type aliases reference the aliased class's constructor.
  StringBuilder("x")
  StringIntMap()
  OverloadedAlias(1)
}
