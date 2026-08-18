package naksha.demo

import naksha.base.Guid
import naksha.base.NotNullProperty
import naksha.base.TupleNumber
import naksha.geo.SpFeature

class DemoFeature : SpFeature() {
    companion object DemoFeature_C {
        private val PROPERTIES = NotNullProperty<DemoFeature, DemoProperties>(DemoProperties::class)
    }

    val properties: DemoProperties by PROPERTIES

    var guid: Guid? = null
        get() {
            var guid = field
            if (guid == null) {
                val uuid = properties.xyz.uuid
                if (uuid != null) {
                    guid = Guid.fromString(uuid)
                    field = guid
                }
            }
            return guid
        }
        private set

    override fun toString()
        = "$id - ${properties.firstName} ${properties.lastName}: ${properties.age} @ ${guid?.tupleNumber?.version?:"HEAD"}"
}