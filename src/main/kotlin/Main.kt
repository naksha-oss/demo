@file:Suppress("DuplicatedCode")

import naksha.base.Platform
import naksha.geo.HereTile
import naksha.model.*
import naksha.model.objects.NakshaCollection
import naksha.model.objects.NakshaFeature
import naksha.model.objects.NakshaMap
import naksha.model.objects.NakshaStorage
import naksha.model.request.FeatureTuple
import naksha.model.request.SuccessResponse
import naksha.model.request.Write
import naksha.model.request.WriteRequest
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess

const val APP_NAME = "demo_app"
const val APP_ID = "demo_app"
const val MAP_ID = "demo_map"
const val COLLECTION_ID = "demo_collection"
const val PARTITIONS = 12

fun main(vararg args: String) {
    if (args.isEmpty()) {
        println("Please specify a command")
        exitProcess(1)
    }

    // Read storage-config and create storage.
    NakshaContext.defaultAppName.set(APP_NAME)
    NakshaContext.defaultAppId.set(APP_ID)
    val raw = loadTextResource("storage.json")
    val config = NakshaStorage.fromJSON(raw)
    val storage = Naksha.useStorage(config)

    // Execute the command.
    val cmd = args[0]
    when (cmd) {
        "init" -> do_init(storage)
        "clear" -> do_clear(storage)
        "import" -> if (args.size > 1) do_import(storage, args[1].toInt()) else do_import(storage)
        "import_parallel" -> if (args.size > 1) do_import_parallel(storage, args[1].toInt()) else do_import_parallel(storage)
        "all_features" -> {
            // Requires override of IntelliJ cyclic buffer size to see all!
            val response = read_all_features(storage, MAP_ID, COLLECTION_ID)
            printTupleNumber(response)
        }
        "find_ref_quad" -> {
            if (args.size < 4) {
                println("Please use 'find_ref_quad` with arguments:")
                println("\tfind_ref_quad <longitude> <latitude> <level>")
            }
            val lon = args[1].toDouble()
            val lat = args[2].toDouble()
            val level = args[3].toInt()
            val response = find_features_by_ref_quad(storage, MAP_ID, COLLECTION_ID, HereTile(lon, lat, level))
            printFeatureId(response)
        }
        "find_groups" -> {
            if (args.size < 2) {
                println("Please use 'find_groups` with at least one argument:")
                println("\tfind_groups <group-number>...")
            }
            val groups = IntArray(args.size-1) { i ->
                args[i+1].toInt()
            }
            do_find_groups(storage, groups)
        }
        "performance_demo" -> performance_demo(storage)
        else -> {
            println("Unknown command: $cmd")
        }
    }
}

fun do_init(storage: IStorage) {
    storage.newWriteSession().use { session ->
        // Create the map.
        var request = WriteRequest()
        request.add(Write().createMap(NakshaMap(MAP_ID)))
        var response = session.execute(request)
        check(response is SuccessResponse) {
            println(response)
        }
        session.commit()

        // Create the collection.
        request = WriteRequest()
        request.add(Write().createCollection(NakshaCollection(
            id = COLLECTION_ID,
            mapId = MAP_ID,
            partitions = PARTITIONS,
        ).withIndices("id", "here_tile", "tags", "ref_point", "gist_geo_2d")))
        response = session.execute(request)
        check(response is SuccessResponse) {
            println(response)
        }
        session.commit()
    }
}

fun do_clear(storage: IStorage) {
    storage.newWriteSession().use { session ->
        val map = session.getMapById(MAP_ID)
        if (map != null) {
            val request = WriteRequest()
            request.add(Write().deleteMap(map, false))
            val response = session.execute(request)
            check(response is SuccessResponse) {
                println(response)
            }
            session.commit()
        }
    }
}

fun do_import(storage: IStorage, amount: Int = 20_000) {
    write_random_features(storage, MAP_ID, COLLECTION_ID, amount)
}

fun do_import_parallel(storage: IStorage, amount: Int = 200_000) {
    write_random_features_parallel(storage, MAP_ID, COLLECTION_ID, amount, PARTITIONS)
}

fun do_find_groups(storage: IStorage, groups: IntArray) {
    val response = find_features_by_group(storage, MAP_ID, COLLECTION_ID, *groups)
    printFeatureId(response)
}

fun process_features_in_parallel(response: SuccessResponse): ConcurrentHashMap<String, NakshaFeature> {
    val featureTupleList = response.featureTupleList
    val result = ConcurrentHashMap<String, NakshaFeature>()
    val threads = Array(PARTITIONS) { partition ->
        Thread {
            // Each thread iterates all tuple-numbers, and collects all matching his partition.
            val myFeatureTuples = ArrayList<FeatureTuple>()
            for (i in 0 until featureTupleList.size) {
                val featureTuple = requireNotNull(featureTupleList[i])
                val tupleNumber = featureTuple.tupleNumber
                if (tupleNumber.partitionNumber % PARTITIONS == partition) {
                    myFeatureTuples.add(featureTuple)
                }
            }
            // Load from cache or storage, and add into result.
            Naksha.cache.loadFromCacheOrStorage(myFeatureTuples).forEach { featureTuple ->
                val id = requireNotNull(featureTuple.id)
                if (id.endsWith("0000")) {
                    result[id] = requireNotNull(featureTuple.feature)
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
    printMeasure("Processing of ${response.length} features in parallel done", response.length, startNanos)
    return result
}

fun print_result(result: ConcurrentHashMap<String, NakshaFeature>) {
    println("Found ${result.size} features")
    print("\t")
    var first = true
    result.values.forEach { feature ->
        if (first) {
            first = false
            print(feature.id)
        } else {
            print(", ${feature.id}")
        }
    }
    println()
}

fun performance_demo(storage: IStorage) {
    // We read all features.
    var startNanos = Platform.currentNanos()
    val all_response = read_all_features(storage, MAP_ID, COLLECTION_ID)
    printMeasure("Read ${all_response.length} features", all_response.length, startNanos)

    // Process all features by partitions in parallel.
    var result = process_features_in_parallel(all_response)
    print_result(result)

    // Insert a new feature
    val feature = RandomFeatures.randomFeature()
    feature.id += "0000"
    val properties = feature.properties.proxy(FooBuilder::class)
    properties.name = "Eliot"
    println("Insert a new feature '${feature.id}'")
    storage.newWriteSession().use { session ->
        val request = WriteRequest()
        request.add(Write().createFeature(MAP_ID, COLLECTION_ID, feature))
        val response = session.execute(request)
        check(response is SuccessResponse) {
            println(response)
        }
        session.commit()
    }
    println("Inserted another feature, lets read features again, we expect that it is now in the result")
    startNanos = Platform.currentNanos()
    val new_response = read_all_features(storage, MAP_ID, COLLECTION_ID)
    printMeasure("Read ${new_response.length} features", new_response.length, startNanos)
    result = process_features_in_parallel(new_response)
    print_result(result)
}