import naksha.base.NotNullProperty
import naksha.model.objects.NakshaProperties

class Foo : NakshaProperties() {
    companion object FooC {
        private val INT = NotNullProperty<Foo, Int>(Int::class)
        private val STRING = NotNullProperty<Foo, String>(String::class)
        private val STRING_OR_NULL = NotNullProperty<Foo, String>(String::class)
    }

    val age by INT
    val name by STRING
    val firstName by STRING
    val middleName by STRING_OR_NULL
    val lastName by STRING
}