package naksha.demo

import naksha.base.Platform
import naksha.geo.SpFeatureCollection
import naksha.model.RandomFeatures
import naksha.model.objects.NakshaCollection
import naksha.model.objects.NakshaFeature
import naksha.model.request.Write
import naksha.model.request.WriteRequest

const val RANDOM_DATA_COLLECTION_ID = "random_data"

fun DemoSetup.createCollections(vararg collections: NakshaCollection): Array<NakshaCollection> {
    storage.newWriteSession().use { session ->
        val request = WriteRequest()
        val byId = HashMap<String, NakshaCollection>()
        for (i in collections.indices) {
            val collection = collections[i]
            val id = collection.id
            val existing = session.getCollectionById(catalog, id)
            if (existing != null) {
                byId[id] = existing
            } else {
                collection.catalogId = this.catalog.id
                request.add(
                    Write().createCollection(collection)
                )
            }
        }
        if (byId.size == collections.size) {
            return Array(collections.size) {
                val collection = collections[it]
                byId[collection.id]!!
            }
        }
        val response = successResponse( session.execute(request) )
        session.commit()
        val featureCollection: SpFeatureCollection = response.asFeatureCollection()
        var fci = 0
        val array = Array(collections.size) {
            val collection = collections[it]
            val existing = byId[collection.id]
            existing ?: featureCollection.features[fci++]!!.proxy(NakshaCollection::class)
        }
        return array
    }
}

fun DemoSetup.writeFeatures(collectionId: String, vararg features: NakshaFeature): Array<NakshaFeature> {
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
        val featureCollection = response.asFeatureCollection()
        val array = Array(featureCollection.features.size) { i ->
            featureCollection.features[i]!!.proxy(NakshaFeature::class)
        }
        return array
    }
}

fun DemoSetup.printFeatureId(feature: NakshaFeature) {
    println("\t{\"id\":\"${feature.id}\", \"uuid\":\"${feature.properties.xyz.uuid}\"}")
}

fun DemoSetup.printFeature(feature: NakshaFeature) {
    println("\t${Platform.toJSON(feature)}")
}

fun main(vararg args: String) {
    val demo = DemoSetup()
    demo.createCollections(NakshaCollection(RANDOM_DATA_COLLECTION_ID))
    val random_features = Array(10) { RandomFeatures.randomFeature(tagPossibility = 1.0) }
    val features = demo.writeFeatures(RANDOM_DATA_COLLECTION_ID, *random_features)
    for (feature in features) {
        demo.printFeature(feature)
    }
}

