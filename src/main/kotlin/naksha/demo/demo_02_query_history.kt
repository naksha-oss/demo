package naksha.demo

import naksha.base.Guid
import naksha.base.Int64
import naksha.base.Platform
import naksha.base.Version
import naksha.geo.SpPoint
import naksha.model.objects.NakshaFeature
import naksha.model.objects.StandardMembers
import naksha.model.request.ReadFeatures
import naksha.model.request.SuccessResponse
import naksha.model.request.Write
import naksha.model.request.WriteRequest
import naksha.model.request.ops.IsAnyOf

// TODO: Modify `random_data`
    //       Step 1: Remember current version (find out what HEAD is - my idea: start a write session, get tx and rollback)
    //       demo.storage.newWriteSession().use { session ->
    //         val version = session.useTransaction().version.number
    //       }
    //       Step 2: Add 5 more features, remember new version
    //       Step 3: Update 3 features, remember new version
    //       Step 4: Delete 1 features, remember new version
    //       Step 5: Request in original version
    //       Step 6: Request in HEAD version
    //       Step 7: Request before update (after add)
    //       Step 8: Request before deletion (after update)


fun DemoCore.writeFeaturesReturnSuccess(collectionId: String, vararg features: NakshaFeature): SuccessResponse {
    storage.newWriteSession().use { session ->
        val collection = requireNotNull( session.getCollectionById(catalog, collectionId) )
        val request = WriteRequest()
        for (feature in features) {
            request.add(
                Write().createFeature(collection, feature)
            )
        }
        val response = successResponse( session.execute(request) )
        session.commit()
        return response
    }
}

fun DemoCore.updateFeatures(collectionId: String, vararg features: DemoFeature): Array<DemoFeature> {
    storage.newWriteSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(catalog, collectionId))
        val request = WriteRequest()
        for (feature in features) {
            request.add(
                Write().updateFeature(collection, feature.proxy(NakshaFeature::class), true)
            )
        }
        val response = successResponse(session.execute(request))
        session.commit()
        return featureFromSuccessResponse(response)
    }
}

fun DemoCore.deleteFeatures(collectionId: String, vararg features: DemoFeature): Array<DemoFeature> {
    storage.newWriteSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(catalog, collectionId))
        val request = WriteRequest()
        for (feature in features) {
            request.add(
                Write().deleteFeatureById(collection, feature.id)
            )
        }
        val response = successResponse(session.execute(request))
        session.commit()
        return featureFromSuccessResponse(response)
    }
}

fun DemoCore.readFeaturesByIds(collectionId: String, version: Int64? = null, vararg ids: String): SuccessResponse {
    storage.newReadSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(catalog, collectionId))
        val request = ReadFeatures()
        request.withCatalogId(collection.catalogId).withCollectionId(collection.id)
        request.version = version
        request.queryHistory = true
        request.queryMembers = IsAnyOf(StandardMembers.Id,*ids)
        return successResponse(session.execute(request))
    }
}

fun main(vararg args: String) {
    val INITIAL_VERSION = Version.fromString(args[0])
    println("INIT: $INITIAL_VERSION")

    val demo = DemoCore()
    println("Current head version: '$INITIAL_VERSION', corresponding to: ${INITIAL_VERSION.month}/${INITIAL_VERSION.day}/${INITIAL_VERSION.year}@${INITIAL_VERSION.seq}")

    // Add more random features.
    val random_features = demo.randomFeatures(5)
    // Fix geometry for bbox demo.
    val point = SpPoint(1.0, 1.0)
    for (feature in random_features) {
        feature.geometry = point
    }
    println("Adding 5 features with geometry being a point (1,1) to the random data collection...")
    val features_added = demo.writeFeatures(RANDOM_DATA_COLLECTION_ID, *random_features)
    val ADDED_VERSION = (Guid.fromString(features_added[0].properties.xyz.uuid!!).tupleNumber.version.toLong() and -4) or 3

    //step 3
    val update_features = features_added.copyOfRange(0, 3)
    val newPoint = SpPoint(2.0, 2.0)
    for (feature in update_features) {
        feature.properties["demoUpdate"] = "to mark update"
        feature.geometry = newPoint
    }
    println("Updating first 3 features to new geometry point (2,2)...")
    println("Input to UPDATE call must have correct UUID as returned from the previous write, to ensure atomicity.")
    val updatedFeatures = demo.updateFeatures(RANDOM_DATA_COLLECTION_ID, *update_features)
    val UPDATED_VERSION = (Guid.fromString(updatedFeatures[0].properties.xyz.uuid!!).tupleNumber.version.toLong() and -4) or 3
    //step 4
    println("Deleting the first feature '${update_features[0].id}' that was updated, to mark it as deleted in HEAD...")
    val deletedFeatures = demo.deleteFeatures(RANDOM_DATA_COLLECTION_ID, update_features[0])
    val DELETED_VERSION = (Guid.fromString(deletedFeatures[0].properties.xyz.uuid!!).tupleNumber.version.toLong() and -4) or 3
    //step 5??
    //step 6
    val readResponseOriginal =
        demo.readFeaturesByIds(RANDOM_DATA_COLLECTION_ID, INITIAL_VERSION.number, random_features[0].id, random_features[1].id)
    println("Reading features for version before insert, found number of features: ${readResponseOriginal.features.size}")
    println("Features JSON content, demonstrating JSON parser, can be Jackson or Naksha internal parser (historically from Wikvaya project): ")
    for (feature in readResponseOriginal) println(Platform.toJSON(feature))
    //step 7
    val readResponseAfterInsert =
        demo.readFeaturesByIds(RANDOM_DATA_COLLECTION_ID, Int64(ADDED_VERSION), random_features[0].id, random_features[1].id)
    println("Reading first 2 features at the specific version after insert, so even though 1st one is deleted in HEAD, its history is preserved. Found number of features: ${readResponseAfterInsert.features.size}")
    println("Features content: ")
    for (feature in demo.successResponse(readResponseAfterInsert)) println(feature)
    //step 8
    val readResponseAfterUpdate = demo.readFeaturesByIds(
        RANDOM_DATA_COLLECTION_ID,
        Int64(UPDATED_VERSION),
        random_features[0].id,
        random_features[1].id,
        random_features[2].id
    )
    println("Reading the 3 updated features, including the 1st one that is deleted in HEAD. Found number of features: ${readResponseAfterUpdate.features.size}")
    println("Features content: ")
    for (feature in demo.successResponse(readResponseAfterUpdate)) println(feature)

    println("\n\nArguments for demo_03:\n$INITIAL_VERSION $ADDED_VERSION $UPDATED_VERSION $DELETED_VERSION\n")
}
