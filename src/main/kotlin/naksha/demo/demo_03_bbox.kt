package naksha.demo

import naksha.base.Int64
import naksha.base.Version
import naksha.geo.SpBoundingBox
import naksha.geo.SpGeometry
import naksha.model.IReadSession
import naksha.model.objects.NakshaCollection
import naksha.model.objects.StandardMembers
import naksha.model.request.ReadFeatures
import naksha.model.request.ops.Intersects

fun DemoCore.readFeaturesByBBox(
    session: IReadSession,
    collection: NakshaCollection,
    bbox: SpGeometry,
    version: Int64? = null,
    queryHistory: Boolean = false,
    queryDeleted: Boolean = false): Array<DemoFeature> {
    val request = ReadFeatures()
    request.withCatalogId(collection.catalogId).withCollectionId(collection.id)
    request.version = version
    request.queryDeleted = queryDeleted
    request.queryHistory = queryHistory
    request.queryMembers = Intersects(StandardMembers.Geometry, bbox)
    val response = successResponse(session.execute(request))
    return featureFromSuccessResponse(response)
}



// Requires:
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

    println("""First, this has happened so far:
- CREATE 10 random features             = 10 features (v=$INITIAL_VERSION)
- CREATE 5 features at (1,1)            = 15 features (v=$ADDED_VERSION)
- UPDATE 3 features to (2,2)            = 15 features (v=$UPDATED_VERSION)
- DELETE 1 feature that moved to (2,2)  = 14 features (v=$DELETED_VERSION)""")

    println("Query bounding box 0.5->2.5 without history or deleted in HEAD, should only show 4 features")
    val bbox = SpBoundingBox(0.5, 0.5, 2.5, 2.5).addMargin(0.0000001).toPolygon()
    val head_features = demo.storage.newReadSession().use {
        demo.readFeaturesByBBox(it,demo.randomDataCollection(), bbox)
    }
    println("------------------< HEAD")
    for (feature in head_features) println(feature)
    println()



    println("Query bounding box in the version after we created the 5 features at (1,1), before deleting one, should return 5 features")
    val added_features = demo.storage.newReadSession().use {
        demo.readFeaturesByBBox(it,demo.randomDataCollection(), bbox, ADDED_VERSION.number, queryHistory = true)
    }
    println("------------------< ADDED")
    for (feature in added_features) println(feature)
}