package naksha.demo

import naksha.base.Int64
import naksha.base.Version
import naksha.model.RandomFeatures
import naksha.model.objects.NakshaFeature
import naksha.model.request.ReadFeatures
import naksha.model.request.SuccessResponse
import naksha.model.request.Write
import naksha.model.request.WriteRequest

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

    fun main(vararg args: String) {
        val demo = DemoSetup()
        //step 1
        val headVersion : Version
        demo.storage.newWriteSession().use { session ->
            headVersion = session.useTransaction().version
        }
        println("Current head version: $headVersion, corresponding to day, month, year, seq number: ${headVersion.day}, ${headVersion.month}, ${headVersion.year}, ${headVersion.seq}")
        //step 2
        val random_features = Array(5) { RandomFeatures.randomFeature(tagPossibility = 1.0) }
        val successResponse = demo.writeFeaturesReturnSuccess(RANDOM_DATA_COLLECTION_ID, *random_features)
        val headVersionAfterInsert = successResponse.featureTupleList[0]?.tupleNumber?.version
        //step 3
        val update_features = random_features.copyOfRange(0, 3)
        for (feature in update_features) {
            feature.properties["demoUpdate"] = "to mark update"
        }
        val successResponseUpdate = demo.updateFeaturesReturnSuccess(RANDOM_DATA_COLLECTION_ID, *update_features)
        val headVersionAfterUpdate = successResponseUpdate.featureTupleList[0]?.tupleNumber?.version
        //step 4
        val successResponseAfterDelete = demo.deleteFeaturesReturnSuccess(RANDOM_DATA_COLLECTION_ID, update_features[0])
        val tombstoneVersion = successResponseAfterDelete.featureTupleList[0]?.tupleNumber?.version
        //step 5??
        //step 6
        val readResponseOriginal = demo.readFeatures(RANDOM_DATA_COLLECTION_ID,headVersion.number,random_features[0].id,random_features[1].id)
        print("Reading features for version before insert, found number of features: ${readResponseOriginal.features.size}")
        //step 7
        val readResponseAfterInsert = demo.readFeatures(RANDOM_DATA_COLLECTION_ID,headVersionAfterInsert,random_features[0].id,random_features[1].id)
        print("Reading first 2 features after insert: ${readResponseAfterInsert.features}")
        //step 8
        val readResponseAfterUpdate = demo.readFeatures(RANDOM_DATA_COLLECTION_ID,headVersionAfterUpdate,random_features[0].id,random_features[1].id, random_features[2].id)
        print("Reading the 3 updated features: ${readResponseAfterUpdate.features}")
    }


fun DemoSetup.writeFeaturesReturnSuccess(collectionId: String, vararg features: NakshaFeature): SuccessResponse {
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

fun DemoSetup.updateFeaturesReturnSuccess(collectionId: String, vararg features: NakshaFeature): SuccessResponse {
    storage.newWriteSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(catalog, collectionId))
        val request = WriteRequest()
        for (feature in features) {
            request.add(
                Write().updateFeature(collection, feature, true)
            )
        }
        val response = successResponse(session.execute(request))
        session.commit()
        return response
    }
}

fun DemoSetup.deleteFeaturesReturnSuccess(collectionId: String, vararg features: NakshaFeature): SuccessResponse {
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
        return response
    }
}

fun DemoSetup.readFeatures(collectionId: String, version: Int64? = null, vararg ids: String): SuccessResponse {
    storage.newReadSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(catalog, collectionId))
        val request = ReadFeatures()
        request.withCatalogId(collection.catalogId).withCollectionId(collection.id)
        request.version = version
        return successResponse(session.execute(request))
    }
}