package naksha.demo

import naksha.base.Int64
import naksha.base.Version
import naksha.geo.SpBoundingBox
import naksha.geo.SpGeometry
import naksha.model.objects.StandardMembers
import naksha.model.request.ReadFeatures
import naksha.model.request.SuccessResponse
import naksha.model.request.ops.Intersects

fun DemoCore.readFeaturesByBBox(collectionId: String, version: Int64? = null, bbox: SpGeometry): SuccessResponse {
    storage.newReadSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(catalog, collectionId))
        val request = ReadFeatures()
        request.withCatalogId(collection.catalogId).withCollectionId(collection.id)
        request.version = version
        request.queryHistory = true
        request.queryMembers = Intersects(StandardMembers.Geometry, bbox)
        return successResponse(session.execute(request))
    }
}



// Requires:
fun main(vararg args: String) {
    val HEAD = Version.fromString(args[0])

    // TODO: Read bounding box in random_data HEAD version
    // TODO: Read bounding box in random_data in some history version left over from previous test
    val demo = DemoCore()

    val bbox = SpBoundingBox(0.9, 0.9, 1.1, 1.1).addMargin(0.0000001).toPolygon()
    val response = demo.readFeaturesByBBox(RANDOM_DATA_COLLECTION_ID, HEAD.number, bbox)
    println(response)
}
