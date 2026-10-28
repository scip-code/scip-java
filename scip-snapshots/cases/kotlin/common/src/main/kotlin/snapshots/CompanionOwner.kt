package snapshots

class CompanionOwner {
  companion object {
    fun create(): CompanionOwner = CompanionOwner()
  }
  fun create(): Int = CompanionOwner.create().hashCode()
}

class Foo {
    companion object Factory {
        fun create(): Foo = Foo()
    }
}
fun useNamedCompanion() = Foo.Factory.create()

class Bar {
    companion object {
        fun instance(): Bar = Bar()
    }
}
fun useUnnamedCompanion() = Bar.instance()
