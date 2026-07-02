package snapshots

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Tagged(val tag: String)

@Tagged("service")
class AnnotatedService {
    @Tagged("run")
    fun run(): String = "running"
}
