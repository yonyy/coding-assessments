package dropbox

data class StorageFile(
    val name: String,
    val size: Int,
    val userId: String,
)

data class UserStorage(
    val userId: String,
    val capacity: Int,
    val initialUsage: Int = 0,
) {
    var usage: Int = initialUsage
        private set

    fun getRemainingCapacity(): Int { TODO("Not yet implemented") }

    fun hasAvailableStorageFor(size: Int): Boolean { TODO("Not yet implemented") }

    fun incrementUsage(size: Int) { TODO("Not yet implemented") }

    fun decrementUsage(size: Int) { TODO("Not yet implemented") }
}

class CloudStorage {
    private val files = mutableMapOf<String, StorageFile>()
    private val userStorages = mutableMapOf<String, UserStorage>()
    private val ADMIN = "admin"

    fun getNLargest(prefix: String, n: Int): List<StorageFile> { TODO("Not yet implemented") }

    fun addFile(name: String, size: Int): Boolean { TODO("Not yet implemented") }

    fun deleteFile(name: String): Int? { TODO("Not yet implemented") }

    fun addUser(userId: String, capacity: Int): Boolean { TODO("Not yet implemented") }

    fun addFileForUser(userId: String, name: String, size: Int): Int? { TODO("Not yet implemented") }

    fun merge(userId1: String, userId2: String): Int? { TODO("Not yet implemented") }
}

fun main() {
    // val cloudStorage = CloudStorage()
    // cloudStorage.addUser("user1", 30)
    // cloudStorage.addUser("user2", 60)

    // cloudStorage.addFileForUser("user1", "/user1/file.txt", 15)
    // cloudStorage.addFileForUser("user2", "/user2/file.txt", 60)
    // println(cloudStorage.merge("user1", "user2"))  // Expected: 15
}
