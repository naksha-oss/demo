package naksha.demo

import naksha.model.objects.Index
import naksha.model.objects.Int32Member
import naksha.model.objects.JsonPath
import naksha.model.objects.NakshaCollection
import naksha.model.objects.StringMember
import naksha.model.request.ReadFeatures
import naksha.model.request.SuccessResponse
import naksha.model.request.ops.And
import naksha.model.request.ops.Gte
import naksha.model.request.ops.StartsWith
import kotlin.use

const val MEMBER_COLLECTION_ID = "custom_members_demo"

fun main() {
    val demo = DemoCore()

    val last_name_member = StringMember("last_name", JsonPath("properties","lastName"))
    val last_name_index = Index("last_name_index", last_name_member.name)

    val age_member = Int32Member("age", JsonPath("properties","age"))
    val age_index = Index("age_index", age_member.name)

    println("Creating collection '$MEMBER_COLLECTION_ID' with custom members (Postgres columns refflecting values from specific JSON attributes) 'name' and 'age' and (Postgres) indices on them...")
    demo.createCollections(
        NakshaCollection(MEMBER_COLLECTION_ID)
            .withMembers(last_name_member, age_member)
            .withIndices(last_name_index, age_index)
    )


    var age = 5
    val random_features = demo.randomFeatures(5)
    for (feature in random_features) {
        feature.properties["age"] = age
        age++
    }
    val features = demo.writeFeatures(MEMBER_COLLECTION_ID, *random_features)
    println("Random features with custom members 'name' and 'age' written into collection '$MEMBER_COLLECTION_ID': ")
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
    println("Read from DB features with age >= 8, based on member 'age' created alongside the collection: ")
    for (feature in featureFromSuccessResponse) println(feature.toString())


    val successResponseCombo : SuccessResponse
    demo.storage.newReadSession().use { session ->
        val collection = requireNotNull(session.getCollectionById(demo.catalog, MEMBER_COLLECTION_ID))
        val request = ReadFeatures()
        request.withCatalogId(collection.catalogId).withCollectionId(collection.id)
        request.queryMembers = And(
            Gte(age_member, 8),
            StartsWith(last_name_member, "B")
        )
        successResponseCombo = demo.successResponse(session.execute(request))
    }
    val featureFromSuccessResponseCombo = demo.featureFromSuccessResponse(successResponseCombo)
    println()
    println("Read from DB features with age >= 8 and last name starting with 'B', based on members 'age' and 'last_name' created alongside the collection: ")
    for (feature in featureFromSuccessResponseCombo) println(feature.toString())
}