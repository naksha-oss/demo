package naksha.demo

import naksha.base.Platform
import naksha.base.PlatformMap
import naksha.model.IStorage
import naksha.model.Naksha
import naksha.model.NakshaContext
import naksha.model.RandomFeatures
import naksha.model.objects.NakshaCatalog
import naksha.model.objects.NakshaFeature
import naksha.model.objects.NakshaStorage
import naksha.model.request.ErrorResponse
import naksha.model.request.Response
import naksha.model.request.SuccessResponse
import naksha.model.request.Write
import naksha.model.request.WriteRequest
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class DemoSetup {
    companion object DemoSetup_C {
        const val APP_NAME = "demo_app"
        const val APP_ID = "demo_app"
        const val CATALOG_ID = "demo_map"
    }
    val storage: IStorage
    val catalog: NakshaCatalog
    init {
        // Read storage-config and create storage.
        NakshaContext.defaultAppName.set(APP_NAME)
        NakshaContext.defaultAppId.set(APP_ID)
        var storage_file = System.getenv("DEMO_STORAGE_FILE")
        if (storage_file.isNullOrEmpty()) storage_file = "storage.json"
        val raw = loadTextResource(storage_file)
        val config = NakshaStorage.fromJSON(raw)
        storage = Naksha.useStorage(config)
        storage.newWriteSession().use { session ->
            var catalog = session.getCatalogById(CATALOG_ID)
            if (catalog == null) {
                catalog = NakshaCatalog(CATALOG_ID)
                val request = WriteRequest()
                val write = Write().createMap(catalog)
                request.add(write)
                val response = successResponse(session.execute(request))
                catalog = response.asFeatureCollection().features[0]!!.proxy(NakshaCatalog::class)
                session.commit()
            }
            this.catalog = catalog
        }
    }
}

fun successResponse(response: Response): SuccessResponse {
    if (response is SuccessResponse) return response
    if (response is ErrorResponse) println(response.error) else println("Unknown response")
    exitProcess(1)
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
    val raw = Platform.fromJSON(json) as PlatformMap
    val feature = raw.proxy(NakshaFeature::class)
    return feature
}

