package naksha.demo

import com.here.naksha.lib.view.View
import com.here.naksha.lib.view.ViewLayer
import com.here.naksha.lib.view.ViewLayerCollection
import naksha.base.Version
import naksha.geo.SpBoundingBox
import naksha.model.objects.NakshaCollection

fun main(vararg args: String) {
    val INITIAL_VERSION = Version.fromString(args[0])
    val ADDED_VERSION = Version.fromString(args[1])
    val UPDATED_VERSION = Version.fromString(args[2])
    val DELETED_VERSION = Version.fromString(args[3])

    // create a new delta collection on top of the random data
    // create a view above the random data and the new delta layer
    // query the view to show data modification
    // then, query the random data to show that the random data is unmodified
    // create another "branch" from random data, change the same object differently
    // - create a new branch on top of the first one (stacking three layers)
    // - modify the base map, ensure that the branches are based on a fixed version

    // random-data: version 1
    // new branch -> delta

    //
    // Read from initial data, where there is no data in the given bbox
    //
    val demo = DemoCore()
    demo.createCollections(NakshaCollection("delta"))
    val base_init = ViewLayer(demo.storage, demo.catalog.id, RANDOM_DATA_COLLECTION_ID, INITIAL_VERSION)
    val delta_head = ViewLayer(demo.storage, demo.catalog.id, "delta")
    val view_init = View(ViewLayerCollection("my_init", delta_head, base_init))
    view_init.newReadSession().use {
        val bbox = SpBoundingBox(0.9, 0.9, 1.1, 1.1).addMargin(0.0000001).toPolygon()
        val features = demo.readFeaturesByBBox( it, demo.randomDataCollection(), version=null, bbox)
        println("------------------< Read from INIT")
        for (feature in features) println(feature)
    }


    //
    // Read from HEAD, there is data in the given bbox
    //
    val base_head = ViewLayer(demo.storage, demo.catalog.id, RANDOM_DATA_COLLECTION_ID)
    val view_head = View(ViewLayerCollection("my_head", delta_head, base_head))
    view_head.newReadSession().use {
        val bbox = SpBoundingBox(0.9, 0.9, 1.1, 1.1).addMargin(0.0000001).toPolygon()
        val features = demo.readFeaturesByBBox( it, demo.randomDataCollection(), version=null, bbox)
        println("------------------< Read from HEAD")
        for (feature in features) println(feature)
    }


    

    // ADDITIONALLY: Is view with fixed base, so base fixed to a specific version!
    // Create a new branch based upon a specific version of RANDOM_DATA_COLLECTION_ID
    // Use the current version

    // Modify delta
    // Modify base
    // Show that the "branch" aka view is not impacted by modifications of base

    println("\nEND\n\n\n")
}