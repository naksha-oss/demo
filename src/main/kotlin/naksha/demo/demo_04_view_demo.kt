package naksha.demo

import com.here.naksha.lib.view.View
import com.here.naksha.lib.view.ViewLayer
import com.here.naksha.lib.view.ViewLayerCollection
import naksha.base.Version
import naksha.geo.SpBoundingBox
import naksha.geo.SpPoint
import naksha.model.objects.NakshaCollection
import kotlin.use

fun DemoCore.queryBbox(view: View, queryDeleted: Boolean = false): Array<DemoFeature> {
    return view.newReadSession().use {
        val bbox = SpBoundingBox(0.5, 0.5, 2.5, 2.5).addMargin(0.0000001).toPolygon()
        val features = readFeaturesByBBox( it, randomDataCollection(), bbox, queryDeleted = queryDeleted)
        println("------------------< Read from ${view.id}")
        for (feature in features) println(feature)
        println()
        features
    }
}

fun main(vararg args: String) {
    val INITIAL_VERSION = Version.fromString(args[0])
    val ADDED_VERSION = Version.fromString(args[1])
    val UPDATED_VERSION = Version.fromString(args[2])
    val DELETED_VERSION = Version.fromString(args[3])
    println("INIT: $INITIAL_VERSION")
    println("ADDED: $ADDED_VERSION")
    println("UPDATED: $UPDATED_VERSION")
    println("DELETED: $DELETED_VERSION")
    val demo = DemoCore()

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
    // 1.
    //
    println("Create 'delta' collection where we put new objects into")
    demo.createCollections(NakshaCollection("delta"))

    println("Create a view 'view_deleted' about the base in the latest known version ($DELETED_VERSION) and 'delta' in HEAD")
    val base_deleted = ViewLayer(demo.storage, demo.catalog.id, RANDOM_DATA_COLLECTION_ID, DELETED_VERSION)
    val delta_head = ViewLayer(demo.storage, demo.catalog.id, "delta")
    val view_deleted = View(ViewLayerCollection("view_deleted", base_deleted, delta_head))

    println("The query for bbox in 'view_deleted', there should be 10 features")
    val deleted_bbox = demo.queryBbox(view_deleted)


    //
    // 2.
    //
    println("Modify one feature in base layer, not in the view, moving it out of the bbox")
    val modify_feature = deleted_bbox[0]
    val second_feature = deleted_bbox[1]
    modify_feature.geometry = SpPoint(10.0, 10.0)
    val modified_feature = demo.updateFeatures(RANDOM_DATA_COLLECTION_ID, modify_feature)[0]
    println("\nModify a feature")
    println(modified_feature)
    val MODIFIED_VERSION = Version( (modified_feature.guid!!.tupleNumber.version.toLong() and -4L) or 3L )

    //
    // 3.
    //
    println("Create a new BRANCH that is now based upon the new version: $MODIFIED_VERSION")
    println("Create a view 'view_modified' about the base in the modified version ($MODIFIED_VERSION) and 'delta' in HEAD")
    val base_modified = ViewLayer(demo.storage, demo.catalog.id, RANDOM_DATA_COLLECTION_ID, MODIFIED_VERSION)
    val view_modified = View(ViewLayerCollection("view_modified", base_modified, delta_head))
    println("The query for bbox in 'view_modified' branch, it should return only 9 features, because we moved one outside the view bbox")
    demo.queryBbox(view_modified)
    println("Query the for bbox in 'view_deleted' branch, it should still show 10 features, because the moved feature is still there in this version")
    demo.queryBbox(view_deleted)

    //
    // Update the second feature in the delta
    //
    second_feature.properties.name = "HELLO"
    //val updated_second_feature = demo.updateFeatures(RANDOM_DATA_COLLECTION_ID, second_feature)[0]

    //
    // Create a view that uses base in HEAD state.
    // The query for bbox, where there is no data in the given bbox
    //
    //val base_head = ViewLayer(demo.storage, demo.catalog.id, RANDOM_DATA_COLLECTION_ID)
    //val view_head = View(ViewLayerCollection("view_head", base_head, delta_head))
    //val head_features = demo.queryBbox(view_head, false)




    //
    // Create a new branch




    // ADDITIONALLY: Is view with fixed base, so base fixed to a specific version!
    // Create a new branch based upon a specific version of RANDOM_DATA_COLLECTION_ID
    // Use the current version

    // Modify delta
    // Modify base
    // Show that the "branch" aka view is not impacted by modifications of base

    println("\nEND\n\n\n")
}