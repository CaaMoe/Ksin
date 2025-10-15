package moe.caa.multilogin.ksin.internal.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.ksin.internal.main.Ksin
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import java.io.FileReader
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

object DatabaseHandler {
    private lateinit var dataSource: HikariDataSource
    private lateinit var database: Database

    fun initDatabase() {
        val hikariConfigurationPath =
            Ksin.INSTANCE.bootstrap.dataDirectory.resolve(Ksin.INSTANCE.config.databaseConfiguration.get())

        if (!Files.exists(hikariConfigurationPath)) {
            Objects.requireNonNull<InputStream>(Ksin.INSTANCE.getResourceAsStream("default_hikari.properties"))
                .use {
                    var input = String(it.readAllBytes(), StandardCharsets.UTF_8)
                    input = input.replace(
                        "{{data_directory}}",
                        Ksin.INSTANCE.bootstrap.dataDirectory.toFile().absolutePath.replace("\\", "/")
                    )
                    Files.writeString(hikariConfigurationPath, input, StandardCharsets.UTF_8)
                }
        }

        val properties = Properties()
        FileReader(hikariConfigurationPath.toFile()).use { reader ->
            properties.load(reader)
        }
        val config = HikariConfig(properties)
        dataSource = HikariDataSource(config)
        database = Database.connect(dataSource)
        transaction(database) {
            SchemaUtils.create(PlayerCurrentUseTable, RepairedSkinTable)
            MigrationUtils.statementsRequiredForDatabaseMigration(PlayerCurrentUseTable, RepairedSkinTable)
        }
    }

    fun close() {
        if (this::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}
