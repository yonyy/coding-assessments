package dropbox

//Implement an in-memory virtual file system supporting directories and files. Your system must support:
//
//mkdir(path) — create a directory at the given absolute path. Creates parent directories as needed (like mkdir -p). Throws if a file exists at that path.
//
//touch(path) — create an empty file at the given path. Parent directory must exist. Throws if the path already exists.
//
//ls(path) — list the contents of a directory. Returns a sorted list of names. Throws if path does not exist or is not a directory.
//
//rm(path) — remove a file or empty directory. Throws if path does not exist or directory is not empty.
//
//All paths are absolute (start with /). The root / always exists.

open class File(
    val name: String,
) {
    open fun canBeDeleted(): Boolean { TODO("Not yet implemented") }
}

class Directory(
    name: String,
): File(name) {
    private val fileNames = mutableSetOf<String>()

    override fun canBeDeleted(): Boolean { TODO("Not yet implemented") }

    fun addFile(name: String) { TODO("Not yet implemented") }

    fun removeFile(name: String) { TODO("Not yet implemented") }
}

class FileSystem {
    val root = Directory("/")
    val files = mutableMapOf<String, File>().apply {
        put(root.name, root)
    }

    fun touch(path: String) { TODO("Not yet implemented") }

    fun mkdir(path: String) { TODO("Not yet implemented") }

    fun ls(path: String): Set<String> { TODO("Not yet implemented") }

    fun rm(path: String) { TODO("Not yet implemented") }

    fun validateAbsolutePath(path: String) { TODO("Not yet implemented") }
}

fun main() {
    // val fs = FileSystem()

    // fs.mkdir("/yonatanp")
    // fs.mkdir("/yonatanp/Pictures")
    // fs.mkdir("/yonatanp/Documents")

    // fs.touch("/yonatanp/sample.txt")
    // fs.touch("/yonatanp/Documents/paper.txt")
    // fs.touch("/yonatanp/Pictures/blog.txt")
    // fs.touch("/notes.txt")

    // println("ls /yonatanp:")
    // fs.ls("/yonatanp").forEach { println(it) }

    // println("ls /:")
    // fs.ls("/").forEach { println(it) }

    // println("rm /yonatanp/sample.txt:")
    // fs.rm("/yonatanp/sample.txt")

    // println("ls /yonatanp:")
    // fs.ls("/yonatanp").forEach { println(it) }
}
