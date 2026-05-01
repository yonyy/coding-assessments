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

    fun getRemainingCapacity(): Int = capacity - usage

    fun hasAvailableStorageFor(size: Int): Boolean = usage + size <= capacity

    fun incrementUsage(size: Int) {
        usage += size
    }

    fun decrementUsage(size: Int) {
        usage -= size
    }
}

class CloudStorage {
    private val files = mutableMapOf<String, StorageFile>()
    private val userStorages = mutableMapOf<String, UserStorage>()
    private val ADMIN = "admin"

    fun getNLargest(prefix: String, n: Int): List<StorageFile> {
        return files.values
            .filter { it.name.startsWith(prefix) }
            .sortedWith(
                compareByDescending<StorageFile> { it.size }
                    .thenBy { it.name }
            )
    }

    fun addFile(name: String, size: Int): Boolean {
        if (files[name] != null) {
            return false
        }

        files[name] = StorageFile(
            name = name,
            size = size,
            userId = ADMIN
        )

        return true
    }

    fun deleteFile(name: String): Int? {
        if (files[name] == null) {
            return null
        }

        val file = files[name]!!
        files.remove(name)
        userStorages[file.userId]?.decrementUsage(file.size)

        return file.size
    }

    fun addUser(userId: String, capacity: Int): Boolean {
        if (userStorages[userId] != null) {
            return false
        }

        userStorages[userId] = UserStorage(
            userId = userId,
            capacity = capacity
        )

        return true
    }

    fun addFileForUser(userId: String, name: String, size: Int): Int? {
        if (userStorages[userId] == null || files[name] != null) {
            return null
        }

        if (!userStorages[userId]!!.hasAvailableStorageFor(size)) {
            return null
        }

        files[name] = StorageFile(
            name = name,
            size = size,
            userId = userId,
        )

        userStorages[userId]!!.incrementUsage(size)

        return userStorages[userId]!!.getRemainingCapacity()
    }

    fun merge(userId1: String, userId2: String): Int? {
        if (userStorages[userId1] == null ||
            userStorages[userId2] == null ||
            userId1 == userId2) {
            return null
        }

        val user2Files = files.values.filter { it.userId == userId2 }
        val user1Storage = userStorages[userId1]!!
        val user2Storage = userStorages[userId2]!!
        val updatedUser1Storage = user1Storage.copy(
            capacity = user1Storage.capacity + user2Storage.capacity,
            initialUsage = user1Storage.usage + user2Storage.usage
        )

        user2Files.forEach {
            files[it.name] = it.copy(userId = userId1)
        }

        userStorages.remove(userId2)
        userStorages[userId1] = updatedUser1Storage
        return updatedUser1Storage.getRemainingCapacity()
    }
}

fun main() {
    val cloudStorage = CloudStorage()
    cloudStorage.addUser("user1", 30)
    cloudStorage.addUser("user2", 60)

    cloudStorage.addFileForUser("user1", "/user1/file.txt", 15)
    cloudStorage.addFileForUser("user2", "/user2/file.txt", 60)
    println(cloudStorage.merge("user1", "user2"))
}

