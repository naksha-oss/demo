import naksha.base.NotNullProperty
import naksha.model.objects.NakshaProperties

class FooBuilder : NakshaProperties() {
    companion object FooBuilderC {
        private val INT = NotNullProperty<FooBuilder, Int>(Int::class)
        private val STRING = NotNullProperty<FooBuilder, String>(String::class)
        private val STRING_OR_NULL = NotNullProperty<FooBuilder, String>(String::class)
    }

    var age by INT
    var name by STRING
    var firstName by STRING
    var middleName by STRING_OR_NULL
    var lastName by STRING
}