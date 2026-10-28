package snapshots

fun localClasses() {
    class LocalClass {
        fun localClassMethod() {}
    }
}

interface Interface {
    fun foo()
}

fun anonymousObjects() {
    // Each object and its members need distinct local symbols, with no INSTANCE field.
    val a = object : Interface {
        override fun foo() {}
    }
    val b = object : Interface {
        override fun foo() {}
    }
}

fun outer() {
    fun inner() {}
    fun innerWithReturnType(): Int = 42
    inner()
}
