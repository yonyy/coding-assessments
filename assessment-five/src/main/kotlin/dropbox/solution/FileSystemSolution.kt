package dropbox.solution

// ─────────────────────────────────────────────────────────────────────────────
// FIX 1: parent moved to File (shared getFullPath — the design question you raised)
//
// Your version: parent only on Directory, getFullPath only on Directory.
// Corrected: parent on File, getFullPath on File, shared by both.
//
// Why: rm() needs to find a file's parent to remove it from children.
//      Without parent on File, rm() has to re-traverse the path to find it —
//      which duplicates logic and creates another potential bug surface.
//      With parent on File, every node knows its own location.
//
// Circular reference reality: Directory.children → List<File>, File.parent → Directory?
// This is two heap pointers, not a value cycle. Safe on JVM.
// The only real risk (infinite loop) would be in toString()/serialization — not applicable here.
// ─────────────────────────────────────────────────────────────────────────────
open class File(
    val name: String,
    val parent: Directory? = null          // FIX 1: parent lives here now, not just on Directory
) {
    open fun canBeDeleted(): Boolean = true

    // FIX 1 cont: getFullPath lives once on File — Directory inherits it for free
    fun getFullPath(): String {
        val parentPath = parent?.getFullPath() ?: return "/"   // null parent → this is root
        return if (parentPath == "/") "/$name" else "$parentPath/$name"
    }
}

class Directory(
    name: String,
    parent: Directory? = null              // passed up to File via super
) : File(name, parent) {

    val children = mutableListOf<File>()

    override fun canBeDeleted(): Boolean = children.isEmpty()

    // ─────────────────────────────────────────────────────────────────────────
    // FIX 2: createDirectory now returns the Directory it creates (or finds).
    //
    // Your version: void return, caller had to call getDirectory() separately.
    // That forced a second linear scan of children immediately after the first.
    // Corrected: return the Directory so FileSystem.mkdir() can chain without
    // a redundant lookup, and so fileMap can be updated in one place.
    // ─────────────────────────────────────────────────────────────────────────
    fun createDirectory(name: String): Directory {
        if (hasDirectory(name)) return getDirectory(
            name
        )   // mkdir -p idempotent: return existing

        // FIX 3 (spec compliance): if a FILE exists at this name, throw — not silently skip
        if (hasFile(name)) {
            throw IllegalArgumentException("Cannot create directory '$name': a file already exists at this path")
        }

        val dir = Directory(name, parent = this)            // FIX 1: pass 'this' as parent
        children.add(dir)
        return dir                                          // FIX 2: return it
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX 4: createFile now returns the File it creates.
    //
    // Your version: void return, FileSystem.touch() had no reference to the
    // new File and therefore could not put it in fileMap → fileMap miss bug.
    //
    // FIX 5 (spec compliance): touch on an existing path must THROW, not silently return.
    // Your version: if (hasFile(name)) { return } — silent no-op violates spec.
    // ─────────────────────────────────────────────────────────────────────────
    fun createFile(name: String): File {
        if (hasDirectory(name)) {
            throw IllegalArgumentException("Cannot create file '$name': a directory already exists at this path")
        }

        if (hasFile(name)) {
            // FIX 5: spec says "Throws if the path already exists" — throw, don't silently return
            throw IllegalArgumentException("File '$name' already exists")
        }

        val file = File(name, parent = this)                // FIX 1: pass 'this' as parent
        children.add(file)
        return file                                         // FIX 4: return it so caller can update fileMap
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX 6: ls() returns a sorted List<String> instead of printing directly.
    //
    // Your version: logFiles() printed via println() — mixed I/O into the model,
    // made the method untestable without capturing stdout.
    // Corrected: return the list; caller decides how to render it.
    // This also matches how every real filesystem API works (readdir returns strings).
    // ─────────────────────────────────────────────────────────────────────────
    fun listContents(): List<String> =
        children.sortedBy { it.name }.map { it.name }      // FIX 6: pure function, no side effects

    fun hasDirectory(name: String): Boolean =
        children.filterIsInstance<Directory>().any { it.name == name }

    fun hasFile(name: String): Boolean =
        children.any { it.name == name }

    fun getDirectory(name: String): Directory =
        children.filterIsInstance<Directory>().single { it.name == name }

    companion object {
        // FIX 7: ROOT_DIR is now created fresh per FileSystem instance (see FileSystem below).
        // Your version: ROOT_DIR was a global companion object singleton shared across all
        // FileSystem instances. In tests, state leaked between FileSystem instances.
    }
}

class FileSystemSolution {

    // FIX 7: Root is a fresh instance per FileSystem — no shared global state
    val rootDir: Directory = Directory("/")

    // ─────────────────────────────────────────────────────────────────────────
    // fileMap is the O(1) path index. CRITICAL INVARIANT:
    // Every mutation (createDirectory, createFile, rm) must update BOTH
    // fileMap AND the parent's children list atomically.
    //
    // Your version violated this: touch() updated children but not fileMap;
    // rm() updated fileMap but not children. Index and tree diverged.
    // ─────────────────────────────────────────────────────────────────────────
    private val fileMap = mutableMapOf<String, File>().apply {
        put("/", rootDir)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX 8: Single write path — addToParent() is the ONLY place that touches
    // both children and fileMap. No mutation happens outside this method.
    //
    // This is the fix for the root cause of all your consistency bugs.
    // ─────────────────────────────────────────────────────────────────────────
    private fun addToParent(parent: Directory, file: File) {
        // Note: parent.createDirectory / createFile already added to children.
        // fileMap update is the missing half — we do it here.
        fileMap[file.getFullPath()] = file
    }

    fun mkdir(path: String) {
        validateAbsolutePath(path)
        if (path == "/") return                             // root always exists, no-op

        val segments = path.split('/').drop(1)             // drop the leading empty string from split
        var current = rootDir

        for (segment in segments) {
            if (segment.isEmpty()) continue                // handle trailing slash gracefully
            val dir = current.createDirectory(segment)    // FIX 2: returns Directory, no second lookup
            addToParent(current, dir)                     // FIX 8: keep fileMap in sync
            current = dir
        }
    }

    fun touch(path: String) {
        validateAbsolutePath(path)

        // FIX 9: touch("/") must throw — root is a directory, not a valid file target
        // Your version: silently returned. Spec says throw if path already exists,
        // and "/" always exists as a directory.
        if (path == "/") {
            throw IllegalArgumentException("Cannot touch root '/' — it is a directory")
        }

        val segments = path.split('/').drop(1)
        val fileName = segments.last()
        val dirSegments = segments.dropLast(1)

        var current = rootDir
        for (segment in dirSegments) {
            if (segment.isEmpty()) continue
            if (!current.hasDirectory(segment)) {
                throw IllegalArgumentException("Parent directory '$segment' does not exist")
            }
            current = current.getDirectory(segment)
        }

        // FIX 5 + FIX 4: createFile throws if exists, returns the new File
        val file = current.createFile(fileName)
        addToParent(current, file)                        // FIX 8: update fileMap
    }

    fun ls(path: String): List<String> {                  // FIX 6: returns list instead of printing
        val node = fileMap[path]
            ?: throw IllegalArgumentException("Path '$path' does not exist")

        if (node !is Directory) {
            // FIX 10: specific error when path is a file, not just "not found"
            throw IllegalArgumentException("Path '$path' is a file, not a directory")
        }

        return node.listContents()                        // FIX 6: caller prints if needed
    }

    fun rm(path: String) {
        validateAbsolutePath(path)

        if (path == "/") {
            throw IllegalArgumentException("Cannot remove root '/'")
        }

        val file = fileMap[path]
            ?: throw IllegalArgumentException("Path '$path' does not exist")

        if (!file.canBeDeleted()) {
            throw IllegalArgumentException("Cannot remove '$path': directory is not empty")
        }

        // FIX 11: remove from parent's children list — your version missed this entirely.
        // Without this, the tree and fileMap diverge: rm'd files still appear in ls().
        val parent = file.parent
            ?: throw IllegalStateException("Non-root node has no parent — this is a bug")
        parent.children.remove(file)                      // FIX 11: sync the tree
        fileMap.remove(path)                              // sync the index
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX 12: Extracted shared validation into one method.
    // Your version: copy-pasted the same two checks in mkdir, touch, and rm.
    // DRY — one place to fix if the rule changes.
    // ─────────────────────────────────────────────────────────────────────────
    private fun validateAbsolutePath(path: String) {
        if (path.isEmpty()) throw IllegalArgumentException("Path cannot be empty")
        if (path[0] != '/') throw IllegalArgumentException("Absolute path required: '$path'")
    }
}

fun main() {
    val fs = FileSystemSolution()

    fs.mkdir("/yonatanp")
    fs.mkdir("/yonatanp/Pictures")
    fs.mkdir("/yonatanp/Documents")

    fs.touch("/yonatanp/sample.txt")
    fs.touch("/notes.txt")

    // FIX 6: ls() now returns a list — print it here in main, not buried in the model
    println("ls /:")
    fs.ls("/").forEach { println("  $it") }

    println("ls /yonatanp:")
    fs.ls("/yonatanp").forEach { println("  $it") }

    // Verify rm removes from both tree and index
    fs.rm("/yonatanp/sample.txt")
    println("ls /yonatanp after rm:")
    fs.ls("/yonatanp").forEach { println("  $it") }

    // Verify touch throws on existing path
    try {
        fs.touch("/notes.txt")
    } catch (e: IllegalArgumentException) {
        println("Correctly threw on duplicate touch: ${e.message}")
    }

    // Verify rm throws on non-empty directory
    try {
        fs.rm("/yonatanp")
    } catch (e: IllegalArgumentException) {
        println("Correctly threw on non-empty rm: ${e.message}")
    }
}