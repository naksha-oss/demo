@file:Suppress("DuplicatedCode")

import naksha.base.Int64
import naksha.base.Platform
import naksha.base.PlatformMap
import naksha.geo.HereTile
import naksha.model.IStorage
import naksha.model.Naksha
import naksha.model.RandomFeatures
import naksha.model.objects.NakshaCollection
import naksha.model.objects.NakshaFeature
import naksha.model.objects.NakshaMap
import naksha.model.request.ReadFeatures
import naksha.model.request.SuccessResponse
import naksha.model.request.Write
import naksha.model.request.WriteRequest
import naksha.model.request.query.DoubleOp
import naksha.model.request.query.TagOr
import naksha.model.request.query.TagValueIsDouble
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max

fun printTupleNumber(response: SuccessResponse) {
    println("---< tuple-numbers, size: ${response.featureTupleList.size}")
    for (featureTuple in response.featureTupleList) {
        check(featureTuple != null)
        println("\t{\"tn\": \"${featureTuple.tupleNumber}\"}")
    }
}

fun printFeatureId(response: SuccessResponse) {
    println("---< feature-ids, size: ${response.features.size}")
    for (feature in response.features) {
        check(feature != null)
        //println("\t{id:\"${feature.id}\"}")
        println("\t{\"id\":\"${feature.id}\", \"feature-number\":\"${feature.tupleNumber.featureNumber}\"}")
    }
}

fun printFeature(response: SuccessResponse) {
    println("---< feature JSON, size: ${response.features.size}")
    for (feature in response.features) {
        check(feature != null)
        println("\t"+ Platform.toJSON(feature))
    }
}

fun printMeasure(msg: String, amount: Int, startNanos: Int64, endNanos: Int64 = Platform.currentNanos()) {
    val seconds = (max(1L, (endNanos - startNanos).toLong()).toDouble() / 1_000_000_000)
    println(msg)
    println("\tThis took %.2f seconds, therefore %.2f per second".format(seconds, max(0, amount)/seconds))
}

fun loadTextResource(filename: String): String {
    val classLoader = Thread.currentThread().contextClassLoader
    val txtPath = classLoader.getResource(filename)
    requireNotNull(txtPath)
    val txt = Files.readString(Paths.get(txtPath.toURI()))
    requireNotNull(txt)
    return txt
}

fun loadFeatureFromResource(filename: String): NakshaFeature {
    val json = loadTextResource(filename)
    // Use cross-platform JSON parser of Storage-Abstraction-Layer.
    val raw = Platform.fromJSON(json) as PlatformMap
    // Runtime cast the feature using Data-Model-Abstraction-Layer.
    val feature = raw.proxy(NakshaFeature::class)
    // or: feature = NakshaFeature.fromJson(json)
    println(feature.id)
    return feature
}

fun writeFeature(storage: IStorage, mapId: String, collectionId: String, feature: NakshaFeature) : NakshaFeature{
    storage.newWriteSession().use { session ->
        // Gather map and collection.
        val map = session.getMapById(mapId)
        requireNotNull(map)
        val collection = session.getCollectionById(map, collectionId)
        requireNotNull(collection)

        // Insert it into topology collection.
        val writeReq = WriteRequest().add(
            Write().createFeature(collection, feature)
        )
        val writeResp = session.execute(writeReq)
        require(writeResp is SuccessResponse) {
            println(writeResp)
        }
        session.commit()
        val f = writeResp.features[0]
        requireNotNull(f)
        return f
    }
}

fun insert_topologies_from_resources(storage: IStorage, mapId: String, collectionId: String) {
    println("Insert topologies from resources")
    val topology_urn_org = loadFeatureFromResource("topology_urn_id.json")
    val topology_urn = writeFeature(storage, mapId, collectionId, topology_urn_org)
    assert(topology_urn.id == topology_urn_org.id)

    val topology_number_org = loadFeatureFromResource("topology_numeric_id.json")
    val topology_number = writeFeature(storage, mapId, collectionId, topology_number_org)
    assert(topology_number.id == topology_number_org.id)
}

fun write_random_features(storage: IStorage, mapId: String, collectionId: String, amount: Int) {
    println("Insert $amount random features")
    storage.newWriteSession().use { session ->
        // Gather map and collection.
        val map = session.getMapById(mapId)
        requireNotNull(map)
        val collection = session.getCollectionById(map, collectionId)
        requireNotNull(collection)

        // Insert features with id `0` to `amount - 1` into collection:
        val writeReq = WriteRequest()
        for (i in 0 until amount) {
            // Get virtual groups of each 1000 features from `0` to n.
            val group = (i / 1000).toString()
            val feature = RandomFeatures.randomFeature(tagPossibility = 1.0)
            feature.id = i.toString()
            feature.properties.xyz.tags += "group:=$group"
            writeReq.add(Write().createFeature(collection, feature))
        }
        val writeResp = session.execute(writeReq)
        require(writeResp is SuccessResponse) {
            println(writeResp)
        }
        session.commit()
    }
}

fun get_or_create(requests: ArrayList<WriteRequest>, limit: Int = 10_000): WriteRequest {
    if (requests.isEmpty()) {
        val request = WriteRequest()
        requests.add(request)
        return request
    }
    var request = requests.last
    if (request.writes.size >= limit) {
        request = WriteRequest()
        requests.add(request)
    }
    return request
}

fun write_random_features_parallel(storage: IStorage, mapId: String, collectionId: String, amount: Int, partitions: Int) {
    println("Insert $amount random features into $partitions partitions")
    val map: NakshaMap
    val collection: NakshaCollection
    storage.newWriteSession().use { session ->
        // Gather map and collection.
        map = requireNotNull(session.getMapById(mapId))
        collection = requireNotNull(session.getCollectionById(map, collectionId))
    }

    // Insert features with id `1` to `amount` into topology collection:
    print("Generate random features ...")
    val requests_by_partitions = Array(partitions) { ArrayList<WriteRequest>() }
    for (i in 0 until amount) {
        // Get virtual groups of each 1000 features from `0` to n.
        val group = (i / 1000).toString()
        val feature = RandomFeatures.randomFeature(tagPossibility = 1.0)
        feature.id = i.toString()
        feature.properties.xyz.tags += "group:=$group"
        val partition_number = Naksha.NakshaCompanion.partitionNumber(feature.id)
        val partition_index = partition_number % partitions
        val requests = requests_by_partitions[partition_index]
        val request = get_or_create(requests)
        request.add(Write().createFeature(collection, feature))
        if (i % 10_000 == 0) {
            print(".")
        }
    }
    println()
    println("Done, start threads to insert features in parallel")
    val threads = Array(partitions) { i ->
        val requests = requests_by_partitions[i]
        Thread {
            for (request in requests) {
                storage.newWriteSession().use { thread_local_session ->
                    println("Insert ${request.writes.size} features into partition $i ...")
                    val response = thread_local_session.execute(request)
                    if (response !is SuccessResponse) {
                        println(response)
                    } else {
                        thread_local_session.commit()
                        println("Done inserting ${request.writes.size} features into partition $i")
                    }
                }
            }
        }
    }
    // Start all threads
    val startNanos = Platform.currentNanos()
    for (thread in threads) {
        thread.start()
    }
    // Wait all threads
    for (thread in threads) {
        thread.join()
    }
    printMeasure("Import of $amount random features in parallel done", amount, startNanos)
}

fun read_all_features(storage: IStorage, mapId: String, collectionId: String) : SuccessResponse {
    storage.newReadSession().use { session ->
        // Gather map and collection.
        val map = session.getMapById(mapId)
        requireNotNull(map)
        val collection = session.getCollectionById(map, collectionId)
        requireNotNull(collection)

        // Insert it into topology collection.
        val readReq = ReadFeatures()
        readReq.mapId = mapId
        readReq.collectionIds += collectionId
        val readResp = session.execute(readReq)
        require(readResp is SuccessResponse) {
            // In error case, show error:
            println(readResp)
        }
        return readResp
    }
}

fun find_features_by_ref_quad(storage: IStorage, mapId: String, collectionId: String, tile: HereTile) : SuccessResponse {
    storage.newReadSession().use { session ->
        // Gather map and collection.
        val map = session.getMapById(mapId)
        requireNotNull(map)
        val collection = session.getCollectionById(map, collectionId)
        requireNotNull(collection)

        // Insert it into topology collection.
        val readReq = ReadFeatures()
        readReq.mapId = mapId
        readReq.collectionIds += collectionId
        readReq.query.refTiles += tile.intKey
        val readResp = session.execute(readReq)
        require(readResp is SuccessResponse) {
            println(readResp)
        }
        return readResp
    }
}

fun find_features_by_group(storage: IStorage, mapId: String, collectionId: String, vararg groups: Int) : SuccessResponse {
    storage.newReadSession().use { session ->
        // Gather map and collection.
        val map = session.getMapById(mapId)
        requireNotNull(map)
        val collection = session.getCollectionById(map, collectionId)
        requireNotNull(collection)

        // Insert it into topology collection.
        val readReq = ReadFeatures()
        readReq.mapId = mapId
        readReq.collectionIds += collectionId
        if (groups.size == 1) {
            readReq.query.tags = TagValueIsDouble("group", DoubleOp.EQ, groups[0].toDouble())
        } else if (groups.size > 1) {
            val or = TagOr()
            for (group in groups) {
                or += TagValueIsDouble("group", DoubleOp.EQ, group.toDouble())
            }
            readReq.query.tags = or
        }
        val readResp = session.execute(readReq)
        require(readResp is SuccessResponse) {
            println(readResp)
        }
        return readResp
    }
}