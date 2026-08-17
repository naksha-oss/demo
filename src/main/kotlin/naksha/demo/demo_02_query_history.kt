package naksha.demo

fun main() {
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
}