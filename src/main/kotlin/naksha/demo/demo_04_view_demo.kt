package naksha.demo

import com.here.naksha.lib.view.View
import com.here.naksha.lib.view.ViewLayer
import com.here.naksha.lib.view.ViewLayerCollection
import naksha.base.Int64
import naksha.base.Version
import naksha.geo.SpBoundingBox
import naksha.geo.SpPoint
import naksha.model.objects.NakshaCollection
import kotlin.use

fun DemoCore.queryBbox(
    view: View,
    queryHistory: Boolean = false,
    queryDeleted: Boolean = false
): Array<DemoFeature> {
    return view.newReadSession().use {
        val bbox = SpBoundingBox(0.5, 0.5, 2.5, 2.5).addMargin(0.0000001).toPolygon()
        val features = readFeaturesByBBox( it, randomDataCollection(), bbox, queryHistory=queryHistory, queryDeleted=queryDeleted)
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
    println("""First, this has happened so far:
- CREATE 10 random features             = 10 features (v=$INITIAL_VERSION)
- CREATE 5 features at (1,1)            = 15 features (v=$ADDED_VERSION)
- UPDATE 3 features to (2,2)            = 15 features (v=$UPDATED_VERSION)
- DELETE 1 feature that moved to (2,2)  = 14 features (v=$DELETED_VERSION)""")
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

    println("Create a view 'view_deleted' about the base in the deleted version ($DELETED_VERSION) and 'delta' in HEAD")
    val base_deleted = ViewLayer(demo.storage, demo.catalog.id, RANDOM_DATA_COLLECTION_ID, DELETED_VERSION)
    val delta_head = ViewLayer(demo.storage, demo.catalog.id, "delta")
    val view_deleted = View(ViewLayerCollection("view_deleted", delta_head, base_deleted))

    println("The query for bbox in 'view_deleted', there should be 4 features")
    val deleted_bbox = demo.queryBbox(view_deleted, queryHistory = true, queryDeleted = false)


    //
    // 2.
    //
    val modify_feature = deleted_bbox[0]
    val second_feature = deleted_bbox[1]
    println("Modify ${modify_feature.id} in base layer, not in the view, moving it out of the bbox to (10, 10)")
    modify_feature.geometry = SpPoint(10.0, 10.0)
    val modified_feature = demo.updateFeatures(RANDOM_DATA_COLLECTION_ID, modify_feature)[0]
    println("\tmodified feature: $modified_feature")
    val MODIFIED_VERSION = Version( (modified_feature.guid!!.tupleNumber.version.toLong() and -4L) or 3L )
    println("MODIFIED VERSION: $MODIFIED_VERSION\n\n")

    //
    // 3.
    //
    println("Create a new BRANCH that is now based upon the new version: $MODIFIED_VERSION")
    println("Create a view 'view_modified' about the base in the modified version ($MODIFIED_VERSION) and 'delta' in HEAD")
    val base_modified = ViewLayer(demo.storage, demo.catalog.id, RANDOM_DATA_COLLECTION_ID, MODIFIED_VERSION)
    val view_modified = View(ViewLayerCollection("view_modified", delta_head, base_modified))

    println("The query for bbox in 'view_modified' branch, it should return only be 3 features, because we moved ${modified_feature.id} outside the view bbox")
    demo.queryBbox(view_modified, queryHistory = true, queryDeleted = false)

    println("Query the for bbox in 'view_deleted' branch again, it should still show 4 features")
    demo.queryBbox(view_deleted, queryHistory = true, queryDeleted = false)

    //
    //
    //
    println("Update feature ${second_feature.id} in the delta, set name=HELLO")
    second_feature.properties.name = "HELLO"
    demo.writeFeatures("delta", second_feature)[0]

    println("The query for bbox in 'view_modified' branch, as we read 'delta' from HEAD we need to see HELLO")
    demo.queryBbox(view_modified, queryHistory = true, queryDeleted = false)

    println("Query the for bbox in 'view_deleted' branch, as we read 'delta' from HEAD we need to see HELLO")
    demo.queryBbox(view_deleted, queryHistory = true, queryDeleted = false)





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