package assessment3

/*
 * Question 2: Data Structure Implementation (Medium)
 * Target Time: 20 Minutes
 * Tags: Stack, Design, O(1) Undo
 *
 * Task: The "Limited History" Buffer — TextEditor
 *
 * Implement a class TextEditor that supports three operations:
 *   1. insert(text): Adds text to the end of the current document.
 *   2. delete(k):    Deletes the last k characters (if fewer than k chars exist, delete all).
 *   3. undo():       Reverts the last insert or delete operation.
 *
 * Constraints:
 *   - undo() is O(1) and must handle at least 100 consecutive undo operations.
 *   - undo() on an empty history is a no-op.
 *
 * Strategy: Use a history stack. Each entry is the full document string before
 *           the operation — O(1) undo by popping and restoring.
 */

class TextEditor {
    private var document = ""
    private val history = ArrayDeque<String>()

    fun insert(text: String) {
        // TODO: push current document onto history, then append text
        TODO("Not yet implemented")
    }

    fun delete(k: Int) {
        // TODO: push current document onto history, then drop last k chars
        TODO("Not yet implemented")
    }

    fun undo() {
        // TODO: pop from history and restore document (no-op if history is empty)
        TODO("Not yet implemented")
    }

    fun getText(): String = document
}

fun main() {
    val editor = TextEditor()
    editor.insert("hello")
    editor.insert(" world")
    println(editor.getText())  // Expected: "hello world"

    editor.delete(6)
    println(editor.getText())  // Expected: "hello"

    editor.undo()
    println(editor.getText())  // Expected: "hello world"

    editor.undo()
    println(editor.getText())  // Expected: "hello"

    editor.undo()
    println(editor.getText())  // Expected: ""

    editor.undo()              // no-op — history empty
    println(editor.getText())  // Expected: ""
}
