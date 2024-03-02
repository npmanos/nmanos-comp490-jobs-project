package edu.bridgew.comp490.proj1.di

import app.cash.sqldelight.Query
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import edu.bridgew.comp490.proj1.data.GoogleJobSearchService
import edu.bridgew.comp490.proj1.data.GoogleJobSearchServiceImpl
import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.data.SerpApiClient
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.*

val sharedModule = module {
    single(createdAtStart = true) {
        dotenv {
            ignoreIfMissing = true
            ignoreIfMalformed = true
        }
    }

    single(named("apiKey"), createdAtStart = true) {
        val dotenv by inject<Dotenv>()
        dotenv["JOBSPROJ_API_KEY"]
    }

    single { SerpApiClient(get(named("apiKey"))).retrofit }
    single { GoogleJobSearchServiceImpl(get()) } bind GoogleJobSearchService::class

    single { (dbPath: String) ->
        val driver = JdbcSqliteDriver(
            "jdbc:sqlite:$dbPath",
            Properties().apply { put("foreign_keys", "true") },
            JobSearchDB.Schema,
        )

        JobSearchDB.Schema.create(driver)
        val currentSchemaVersion = Query(788_663, driver, "PRAGMA USER_VERSION") { cursor -> cursor.getLong(0)!! }.executeAsOne()
        JobSearchDB.Schema.migrate(driver, currentSchemaVersion, 2)

        JobSearchDB(driver)
    }

    single { params -> JobRepository(get(), get(parameters = { params })) }
}
