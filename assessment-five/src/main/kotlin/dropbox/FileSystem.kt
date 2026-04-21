package dropbox

import dropbox.Directory.Companion.ROOT_DIR


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
    val name: String
) {
    open fun canBeDeleted(): Boolean = true
}

class Directory(
    name: String,
    val parent: Directory? = null,
): File(name) {
    val children = mutableListOf<File>()

    override fun canBeDeleted(): Boolean = children.isEmpty()

    fun getFullPath(): String {
        if (parent != null) {
            if (parent.isRootDir()) {
                return "${parent.getFullPath()}$name"
            }
            return "${parent.getFullPath()}/$name"
        }

        return name
    }

    fun hasDirectory(name: String): Boolean =
        children.filterIsInstance<Directory>()
            .any { it.name == name }

    fun hasFile(name: String): Boolean =
        children.any { it.name == name }

    fun getDirectory(name: String): Directory =
        children.filterIsInstance<Directory>()
            .single { it.name == name }

    fun createDirectory(name: String) {
        if (hasDirectory(name)) {
            return
        }

        if (hasFile(name)) {
            throw IllegalArgumentException("Directory name already used in existing file")
        }

        val dir = Directory(name, this)
        children.add(dir)
    }

    fun createFile(name: String) {
        if (hasDirectory(name)) {
            throw IllegalArgumentException("File name already used in existing directory")
        }

        if (hasFile(name)) {
            return
        }

        val file = File(name)
        children.add(file)
    }

    fun logFiles() {
        children
            .sortedBy { it.name }
            .forEach {
                println(it.name)
            }
    }

    fun isRootDir(): Boolean = this.name == ROOT_DIR.name

    companion object {
        val ROOT_DIR = Directory("/")
    }
}

class FileSystem {
    val rootDir: Directory = ROOT_DIR
    var currentDirectory: Directory = ROOT_DIR
    val fileMap = mutableMapOf<String, File>().apply {
        put("/", ROOT_DIR)
    }

    fun mkdir(path: String) {
        if (path.isEmpty()) {
            throw IllegalArgumentException("path can't be empty")
        }
        if (path[0] != '/') {
            throw IllegalArgumentException("Absolute path expected: $path")
        }

        if (path == "/") {
            return
        }

        val pathList = path.split('/')
        var currDir: Directory? = null
        for (child in pathList) {
            if (currDir == null) {
                currDir = ROOT_DIR
                continue
            }

            currDir.createDirectory(child)
            currDir = currDir.getDirectory(child)
            fileMap.putIfAbsent(currDir.getFullPath(), currDir)
        }
    }

    fun ls(path: String) {
        if (path != "/" && (fileMap[path] == null || fileMap[path] !is Directory)) {
            throw IllegalArgumentException("Directory '$path' could not be found")
        }

        (fileMap[path] as Directory).logFiles()
    }

    fun touch(path: String) {
        if (path.isEmpty()) {
            throw IllegalArgumentException("path can't be empty")
        }
        if (path[0] != '/') {
            throw IllegalArgumentException("Absolute path expected: $path")
        }

        if (path == "/") {
            return
        }

        val pathList = path.split('/')
        val targetFile = pathList.last()
        var currDir: Directory? = null
        for (child in pathList) {
            if (currDir == null) {
                currDir = ROOT_DIR
                continue
            }

            if (child == targetFile) {
                currDir.createFile(child)
                return
            }

            if (!currDir.hasDirectory(child)) {
                throw IllegalArgumentException("Directory '$child' could not be found")
            }

            currDir = currDir.getDirectory(child)
        }
    }

    fun rm(path: String) {
        if (path.isEmpty()) {
            throw IllegalArgumentException("path can't be empty")
        }
        if (path[0] != '/') {
            throw IllegalArgumentException("Absolute path expected: $path")
        }

        if (path == "/") {
            return
        }

        if (fileMap[path] == null) {
            throw IllegalArgumentException("File '$path' could not be found")
        }

        val file = fileMap[path]!!
        if (file.canBeDeleted()) {
            fileMap.remove(path)
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
    fs.touch("/notes.txt")

    //fs.ls("/")
    fs.ls("/yonatanp")
}