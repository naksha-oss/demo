import com.here.naksha.lib.view.View
import com.here.naksha.lib.view.ViewLayer
import com.here.naksha.lib.view.ViewLayerCollection

class ViewDemo {
    fun demo() {
        val demo = DemoSetup()
        val storage = demo.storage
        val base = ViewLayer(storage, "shared-catalog", "base-layer")
        val delta = ViewLayer(storage, "shared-catalog", "delta-layer")

        val view = View(ViewLayerCollection("my-view", delta, base))

        view.newReadSession().use { session ->

        }
    }
}