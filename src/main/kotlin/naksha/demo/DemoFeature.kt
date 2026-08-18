package naksha.demo

import naksha.base.NotNullProperty
import naksha.geo.SpFeature

class DemoFeature : SpFeature() {
    companion object DemoFeature_C {
        private val PROPERTIES = NotNullProperty<DemoFeature, DemoProperties>(DemoProperties::class)
    }

    val properties: DemoProperties by PROPERTIES
}