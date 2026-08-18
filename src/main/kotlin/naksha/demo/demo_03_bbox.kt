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
    queryDeleted: Boolean = false): Array<DemoFeature> {
    val request = ReadFeatures()
    request.withCatalogId(collection.catalogId).withCollectionId(collection.id)
    request.version = version
    request.queryDeleted = queryDeleted
    request.queryHistory = true
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

    // Query bounding box in HEAD state, should return 14 features
    val bbox = SpBoundingBox(0.5, 0.5, 2.5, 2.5).addMargin(0.0000001).toPolygon()
    val head_features = demo.storage.newReadSession().use {
        demo.readFeaturesByBBox(it,demo.randomDataCollection(), bbox, null, true)
    }
    println("------------------< HEAD")
    for (feature in head_features) println(feature)

    // Query bounding box in ADDED_VERSION state, should return 10 features
    val added_features = demo.storage.newReadSession().use {
        demo.readFeaturesByBBox(it,demo.randomDataCollection(), bbox, ADDED_VERSION.number, true)
    }
    println("------------------< ADDED")
    for (feature in added_features) println(feature)
}