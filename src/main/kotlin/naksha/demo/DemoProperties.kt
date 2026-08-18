package naksha.demo

import naksha.base.NotNullProperty
import naksha.base.NullableProperty
import naksha.model.objects.NakshaProperties

class DemoProperties : NakshaProperties() {
    companion object DemoPropertiesC {
        private val INT = NotNullProperty<DemoProperties, Int>(Int::class)
        private val STRING = NotNullProperty<DemoProperties, String>(String::class)
        private val STRING_OR_NULL = NullableProperty<DemoProperties, String>(String::class)
    }

    var age: Int by INT
    var name: String by STRING
    var firstName: String by STRING
    var middleName: String? by STRING_OR_NULL
    var lastName: String by STRING
    var state: String? by STRING_OR_NULL
}