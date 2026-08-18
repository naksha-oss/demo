package naksha.demo

import naksha.model.objects.Index
import naksha.model.objects.Int32Member
import naksha.model.objects.JsonPath
import naksha.model.objects.NakshaCollection
import naksha.model.objects.StringMember
import naksha.model.request.ReadFeatures
import naksha.model.request.SuccessResponse
import naksha.model.request.ops.Gte
import kotlin.use

const val MEMBER_COLLECTION_ID = "custom_members_demo"

fun main() {
    val demo = DemoCore()

    val name_member = StringMember("name", JsonPath("properties","name"))
    val name_index = Index("name_index", name_member.name)

    val age_member = Int32Member("age", JsonPath("properties","age"))
    val age_index = Index("age_index", age_member.name)

    demo.createCollections(
        NakshaCollection(MEMBER_COLLECTION_ID)
            .withMembers(name_member, age_member)
            .withIndices(name_index, age_index)
    )


    var age = 5
    val random_features = demo.randomFeatures(5)
    for (feature in random_features) {
        feature.properties["name"] = "Named ${feature.id}"
        feature.properties["age"] = age
        age++
    }
    val features = demo.writeFeatures(MEMBER_COLLECTION_ID, *random_features)
    for (feature in features) println(feature.toString())

    val successResponse : SuccessResponse

    demo.storage.newReadSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(demo.catalog, MEMBER_COLLECTION_ID))
        val request = ReadFeatures()
        request.withCatalogId(collection.catalogId).withCollectionId(collection.id)
        request.queryMembers = Gte(age_member,8)
        successResponse = demo.successResponse(session.execute(request))
    }
    val featureFromSuccessResponse = demo.featureFromSuccessResponse(successResponse)
    println()
    println("Features with age >= 8: ")
    for (feature in featureFromSuccessResponse) println(feature.toString())
}