package valon

// Build in-memory DB

// val db = Database()
// db.insert("users", { "id": 0, "name": "Fred Luffy" })
// db.select("users, "name") //

data class Row(
    val id: Long,
    val data: Map<String, String>,
    val tableName: String
) {
    fun matches(conditions: Map<String, String>): Boolean { TODO("Not yet implemented") }

    fun buildQueryResult(fields: List<String>): Row { TODO("Not yet implemented") }

    fun merge(other: Map<String, String>): Row { TODO("Not yet implemented") }

    companion object {
        const val ID = "id"
        fun fromData(tableName: String, data: Map<String, String>): Row { TODO("Not yet implemented") }
    }
}

data class TableDescriptor(
    val tableName: String,
    val fields: Set<String>,
)

data class Table(
    val name: String
) {
    private val rows = mutableMapOf<Long, Row>()
    private val fields = mutableSetOf<String>()

    fun getTableDescriptor(): TableDescriptor { TODO("Not yet implemented") }

    fun insertRow(data: Map<String, String>) { TODO("Not yet implemented") }

    fun updateRows(updatedValues: Map<String, String>, filters: Map<String, String>) { TODO("Not yet implemented") }

    fun selectRows(
        fieldsToReturn: List<String> = emptyList(),
        filters: Map<String, String> = emptyMap()
    ): List<Row> { TODO("Not yet implemented") }

    fun deleteRows(filters: Map<String, String>): Int { TODO("Not yet implemented") }
}

class Database {
    private val tables = mutableMapOf<String, Table>()

    fun tables(): List<String> { TODO("Not yet implemented") }

    fun getTable(tableName: String): TableDescriptor { TODO("Not yet implemented") }

    fun insert(tableName: String, row: Map<String, String>) { TODO("Not yet implemented") }

    fun select(
        tableName: String,
        fields: List<String> = emptyList(),
        filters: Map<String, String> = emptyMap()
    ): List<Row> { TODO("Not yet implemented") }

    fun update(
        tableName: String,
        updatedValues: Map<String, String>,
        filters: Map<String, String> = emptyMap()
    ) { TODO("Not yet implemented") }

    fun delete(tableName: String, filters: Map<String, String> = emptyMap()): Int { TODO("Not yet implemented") }

    fun dropTable(tableName: String): Boolean { TODO("Not yet implemented") }
}

fun main() {
    // val db = Database()
    // db.insert("users", mapOf("id" to "0", "name" to "John Smith", "email" to "jsmith@example.com"))
    // db.insert("users", mapOf("id" to "1", "name" to "Emily Cooper", "email" to "ecooper@example.com"))
    // db.insert("users", mapOf("id" to "2", "name" to "Mike Thomas", "email" to "mthomas@example.com"))

    // println(db.tables())
    // println(db.select("users"))
    // println(db.select("users", listOf("name")))
    // println(db.select("users", listOf("name"), mapOf("name" to "John Smith")))

    // db.update("users", mapOf("status" to "active", "office" to "San Diego"))
    // println(db.select("users"))

    // db.delete("users", filters = mapOf("id" to "1"))
    // println(db.select("users"))

    // println(db.getTable("users"))
    // db.dropTable("users")
    // println(db.tables())
}
