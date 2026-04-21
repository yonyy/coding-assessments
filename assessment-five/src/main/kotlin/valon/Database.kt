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
    fun matches(conditions: Map<String, String>): Boolean {
        return conditions.entries.all {
            data[it.key] == it.value
        }
    }

    fun buildQueryResult(fields: List<String>): Row {
        val data = this.data.filterKeys { fields.isEmpty() || fields.contains(it) }
        return Row(
            id = this.id,
            data = data,
            tableName = this.tableName
        )
    }

    fun merge(other: Map<String, String>): Row {
        return Row(
            id = this.id,
            data = this.data + other,
            tableName = this.tableName
        )
    }

    companion object {
        const val ID = "id"
        fun fromData(tableName: String, data: Map<String, String>): Row {
            if (!data.containsKey(ID)) {
                throw IllegalArgumentException("ID does not exist")
            }

            return Row(
                id = data[ID]!!.toLong(),
                data = data,
                tableName = tableName
            )
        }
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

    fun getTableDescriptor() = TableDescriptor(
        tableName = name,
        fields = fields
    )

    fun insertRow(data: Map<String, String>) {
        data.entries.forEach { fields.add(it.key) }
        val row = Row.fromData(name, data)

        if (rows.containsKey(row.id)) {
            println("Row with id '${row.id}' already exists, skipping")
            return
        }

        rows[row.id] = row
    }

    fun updateRows(updatedValues: Map<String, String>, filters: Map<String, String>) {
        updatedValues.entries.forEach { fields.add(it.key) }

        selectRows(emptyList(), filters)
            .forEach {
                rows[it.id] = it.merge(updatedValues)
            }
    }

    fun selectRows(
        fieldsToReturn: List<String> = emptyList(),
        filters: Map<String, String> = emptyMap()
    ): List<Row> {
        return rows.values
            .filter { it.matches(filters) }
            .map { it.buildQueryResult(fieldsToReturn) }
    }

    fun deleteRows(filters: Map<String, String>): Int {
        val matchedIds = selectRows(filters = filters).map { it.id }

        matchedIds.forEach { id -> rows.remove(id) }
        return matchedIds.size
    }
}

class Database {
    private val tables = mutableMapOf<String, Table>()

    fun tables(): List<String> {
        return tables.keys.toList()
    }

    fun getTable(tableName: String): TableDescriptor {
        return tables[tableName]?.getTableDescriptor() ?: throw IllegalArgumentException("Table with name '$tableName' not found")
    }

    fun insert(tableName: String, row: Map<String, String>) {
        if (tables.containsKey(tableName)) {
            tables[tableName]!!.insertRow(row)
        } else {
            tables[tableName] = Table(tableName).apply {
                insertRow(row)
            }
        }
    }

    fun select(
        tableName: String,
        fields: List<String> = emptyList(),
        filters: Map<String, String> = emptyMap()
    ): List<Row> {
        if (tables.containsKey(tableName)) {
            return tables[tableName]!!.selectRows(fields, filters)
        }

        throw IllegalArgumentException("table '$tableName' does not exist")
    }

    fun update(
        tableName: String,
        updatedValues: Map<String, String>,
        filters: Map<String, String> = emptyMap()
    ) {
        if (tables.containsKey(tableName)) {
            val updatedValuesWithoutId = updatedValues
                .toMutableMap()
                .apply {
                    remove(Row.ID)  // explicitly remove id overrides
                }

            tables[tableName]!!.updateRows(updatedValuesWithoutId, filters)
            return
        }

        throw IllegalArgumentException("table '$tableName' does not exist")
    }

    fun delete(tableName: String, filters: Map<String, String> = emptyMap()): Int {
        if (tables.containsKey(tableName)) {
            return tables[tableName]!!.deleteRows(filters)
        }

        throw IllegalArgumentException("table '$tableName' does not exist")
    }

    fun dropTable(tableName: String): Boolean {
        return tables.remove(tableName) != null
    }
}

fun main() {
    val db = Database()
    db.insert(
        "users", mapOf(
            "id" to "0",
            "name" to "John Smith",
            "email" to "jsmith@example.com"
        )
    )

    db.insert(
        "users", mapOf(
            "id" to "1",
            "name" to "Emily Cooper",
            "email" to "ecooper@example.com"
        )
    )

    db.insert(
        "users", mapOf(
            "id" to "2",
            "name" to "Mike Thomas",
            "email" to "mthomas@example.com"
        )
    )

    db.insert(
        "users", mapOf(
            "id" to "2",
            "name" to "Mike Thomas (2)",
        )
    )

    println(db.tables())
    println(db.select("users"))
    println(db.select("users", listOf("name")))
    println(db.select("users", listOf("name"), mapOf("name" to "John Smith")))

    db.update("users", mapOf("status" to "active", "office" to "San Diego"))
    println(db.select("users"))
    db.update("users", mapOf("name" to "Mike Thomas-Lee"), mapOf("id" to "2"))
    println(db.select("users", filters = mapOf("id" to "2")))

    db.insert(
        "emails", mapOf(
            "id" to "0",
            "subject" to "Urgent Meeting!",
            "body" to "Meeting asap",
            "to" to "jsmith@example.com"
        )
    )
    println(db.select("emails"))

    db.delete("users", filters = mapOf("id" to "1"))
    println(db.select("users"))

    println(db.getTable("users"))
    println(db.getTable("emails"))
    println(db.tables())
    db.dropTable("emails")
    println(db.tables())
    db.dropTable("users")
    println(db.tables())
}