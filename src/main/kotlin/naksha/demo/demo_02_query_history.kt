package naksha.demo

import naksha.base.Guid
import naksha.base.Int64
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

fun main() {
    val demo = DemoCore()
    // Gather current version.
    val HEAD: Version = demo.storage.newWriteSession().use { session ->
        session.useTransaction().version
    }
    println("Current head version: '$HEAD', corresponding to day, month, year, seq number: ${HEAD.day}, ${HEAD.month}, ${HEAD.year}, ${HEAD.seq}")

    // Add more random features.
    val random_features = demo.randomFeatures(5)
    // Fix geometry for bbox demo.
    val point = SpPoint(1.0, 1.0)
    for (feature in random_features) {
        feature.geometry = point
    }
    val features_added = demo.writeFeatures(RANDOM_DATA_COLLECTION_ID, *random_features)
    val headVersionAfterInsert = Guid.fromString(features_added[0].properties.xyz.uuid!!).tupleNumber.version

    //step 3
    val update_features = features_added.copyOfRange(0, 3)
    val newPoint = SpPoint(2.0, 2.0)
    for (feature in update_features) {
        feature.properties["demoUpdate"] = "to mark update"
        feature.geometry = newPoint
    }
    val updatedFeatures = demo.updateFeatures(RANDOM_DATA_COLLECTION_ID, *update_features)
    val headVersionAfterUpdate = Guid.fromString(updatedFeatures[0].properties.xyz.uuid!!).tupleNumber.version
    //step 4
    val deletedFeatures = demo.deleteFeatures(RANDOM_DATA_COLLECTION_ID, update_features[0])
    val tombstoneVersion = Guid.fromString(deletedFeatures[0].properties.xyz.uuid!!).tupleNumber.version
    //step 5??
    //step 6
    val readResponseOriginal =
        demo.readFeaturesByIds(RANDOM_DATA_COLLECTION_ID, HEAD.number, random_features[0].id, random_features[1].id)
    println("Reading features for version before insert, found number of features: ${readResponseOriginal.features.size}")
    //step 7
    val readResponseAfterInsert =
        demo.readFeaturesByIds(RANDOM_DATA_COLLECTION_ID, headVersionAfterInsert, random_features[0].id, random_features[1].id)
    println("Reading first 2 features after insert, even though 1st one is deleted in HEAD: ${readResponseAfterInsert.features}")
    //step 8
    val readResponseAfterUpdate = demo.readFeaturesByIds(
        RANDOM_DATA_COLLECTION_ID,
        headVersionAfterUpdate,
        random_features[0].id,
        random_features[1].id,
        random_features[2].id
    )
    println("Reading the 3 updated features, including the 1st one that is deleted in HEAD: ${readResponseAfterUpdate.features}")

    println("\n\n\n\nArguments for demo_03:\n\n$HEAD\n\n")
}
