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
    open fun canBeDeleted(): Boolean = true
}

class Directory(
    name: String,
): File(name) {
    private val fileNames = mutableSetOf<String>()

    override fun canBeDeleted(): Boolean {
        return fileNames.isEmpty()
    }

    fun addFile(name: String) {
        fileNames.add(name)
    }

    fun removeFile(name: String) {
        fileNames.remove(name)
    }
}

class FileSystem {
    val root = Directory("/")
    val files = mutableMapOf<String, File>().apply {
        put(root.name, root)
    }

    fun touch(path: String) {
        validateAbsolutePath(path)
        if (files.containsKey(path)) {
            throw IllegalArgumentException("Path $path already exists.")
        }

        val parentDirPath = path.substringBeforeLast('/').takeIf { it.isNotEmpty() } ?: "/"
        if (!files.containsKey(parentDirPath)) {
            throw IllegalArgumentException("Directory $parentDirPath does not exist.")
        }

        (files[parentDirPath] as Directory).addFile(path)
        files[path] = File(path)
    }

    fun mkdir(path: String) {
        validateAbsolutePath(path)

        if (files.containsKey(path)) {
            return
        }

        val segments = path.split('/').drop(1)  // drop leading empty string due to starting '/'
        val currPath = StringBuilder()
        segments.forEach { segment ->
            if (segment.isEmpty()) {
                return@forEach
            }
            currPath.append("/$segment")
            if (files.containsKey(currPath.toString()) && files[currPath.toString()] !is Directory) {
                throw IllegalArgumentException("Path $currPath already exists as non-directory.")
            }

            files.putIfAbsent(currPath.toString(), Directory(currPath.toString()))
        }
    }

    fun ls(path: String): Set<String> {
        validateAbsolutePath(path)

        if (!files.containsKey(path)) {
            throw IllegalArgumentException("Directory $path does not exist")
        }

        val delimiter = if (path == "/") "/" else "$path/"
        return files.keys
            .filter { it.substringAfter(delimiter)
                .let { substring ->
                    substring.isNotEmpty() && !substring.contains("/")
                }
            }
            .map { it.substringAfter(delimiter) }
            .sorted()
            .toSet()
    }

    fun rm(path: String) {
        validateAbsolutePath(path)

        if (!files.containsKey(path)) {
            throw IllegalArgumentException("Path $path does not exist.")
        }

        if (!files[path]!!.canBeDeleted()) {
            throw IllegalArgumentException("Directory $path is not empty. Can't delete")
        }

        val parentDirPath = path.substringBeforeLast('/').takeIf { it.isNotEmpty() } ?: "/"
        (files[parentDirPath]!! as Directory).removeFile(path)
        files.remove(path)

    }

    fun validateAbsolutePath(path: String) {
        if (path == "/") {
            return
        }

        if (path.isEmpty()) {
            throw IllegalArgumentException("path can not be empty")
        }

        if (!path.startsWith("/")) {
            throw IllegalArgumentException("Absolute path expected")
        }
    }
}

fun main() {
    val fs = FileSystem()

    fs.mkdir("/yonatanp")
    fs.mkdir("/yonatanp/Pictures")
    fs.mkdir("/yonatanp/Documents")
//    fs.ls("/")
//    fs.ls("/yonatanp")

    fs.touch("/yonatanp/sample.txt")
    fs.touch("/yonatanp/Documents/paper.txt")
    fs.touch("/yonatanp/Pictures/blog.txt")
    fs.touch("/notes.txt")

    //fs.ls("/")
    println("ls /yonatanp:")
    fs.ls("/yonatanp").forEach { println(it) }

    println("ls /:")
    fs.ls("/").forEach { println(it) }

    println("rm /yonatanp/sample.txt:")
    fs.rm("/yonatanp/sample.txt")

    println("ls /yonatanp:")
    fs.ls("/yonatanp").forEach { println(it) }
}