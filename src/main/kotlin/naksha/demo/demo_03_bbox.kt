package naksha.demo

import naksha.base.Int64
import naksha.base.Version
import naksha.geo.SpBoundingBox
import naksha.geo.SpGeometry
import naksha.model.objects.StandardMembers
import naksha.model.request.ReadFeatures
import naksha.model.request.ops.Intersects

fun DemoCore.readFeaturesByBBox(collectionId: String, version: Int64? = null, bbox: SpGeometry): Array<DemoFeature> {
    storage.newReadSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(catalog, collectionId))
        val request = ReadFeatures()
        request.withCatalogId(collection.catalogId).withCollectionId(collection.id)
        request.version = version
        request.queryHistory = true
        request.queryMembers = Intersects(StandardMembers.Geometry, bbox)
        val response = successResponse(session.execute(request))
        return featureFromSuccessResponse(response)
    }
}



// Requires:
fun main(vararg args: String) {
    val HEAD_VERSION = Version.fromString(args[0])
    val ADDED_VERSION = Version.fromString(args[1])
    val UPDATED_VERSION = Version.fromString(args[2])
    val DELETED_VERSION = Version.fromString(args[3])

    // TODO: Read bounding box in random_data HEAD version
    // TODO: Read bounding box in random_data in some history version left over from previous test
    val demo = DemoCore()

    val bbox = SpBoundingBox(0.9, 0.9, 1.1, 1.1).addMargin(0.0000001).toPolygon()
    val features = demo.readFeaturesByBBox(RANDOM_DATA_COLLECTION_ID, HEAD_VERSION.number, bbox)
    for (feature in features) demo.printFeatureId(feature)
}
