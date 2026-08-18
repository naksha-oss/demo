package naksha.demo

import com.here.naksha.lib.view.View
import com.here.naksha.lib.view.ViewLayer
import com.here.naksha.lib.view.ViewLayerCollection
import naksha.model.objects.NakshaCollection

fun main(vararg args: String) {
    // create a new delta collection on top of the random data
    // create a view above the random data and the new delta layer
    // query the view to show data modification
    // then, query the random data to show that the random data is unmodified
    // create another "branch" from random data, change the same object differently
    // - create a new branch on top of the first one (stacking three layers)
    // - modify the base map, ensure that the branches are based on a fixed version

    // random-data: version 1
    // new branch -> delta

    // STANDARD VIEW
    val demo = DemoCore()
    demo.createCollections(NakshaCollection("delta_layer"))
    val storage = demo.storage
    val base = ViewLayer(storage, demo.catalog.id, RANDOM_DATA_COLLECTION_ID)
    val delta = ViewLayer(storage, demo.catalog.id, "delta_layer")
    val view = View(ViewLayerCollection("my_view", delta, base))
    view.newReadSession().use { session ->
        // Unmodified
    }

    // ADDITIONALLY: Is view with fixed base, so base fixed to a specific version!
    // Create a new branch based upon a specific version of RANDOM_DATA_COLLECTION_ID
    // Use the current version

    // Modify delta
    // Modify base
    // Show that the "branch" aka view is not impacted by modifications of base
}