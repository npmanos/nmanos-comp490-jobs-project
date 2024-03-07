package edu.bridgew.comp490.proj1.ui.di

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import edu.bridgew.comp490.proj1.data.GoogleJobSearchService
import edu.bridgew.comp490.proj1.data.GoogleJobSearchServiceImpl
import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.data.SerpApiClient
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import edu.bridgew.comp490.proj1.data.db.SalaryDAO
import edu.bridgew.comp490.proj1.db.Migrations
import edu.bridgew.comp490.proj1.ui.screenmodel.JobListScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.*

val guiModule = module {
//    includes(sharedModule)
    factory { params -> JobListScreenModel(get(parameters = { params })) }

    single { SerpApiClient().retrofit }
    single { GoogleJobSearchServiceImpl(get()) } bind GoogleJobSearchService::class

    single { (dbPath: String) ->
        val driver = JdbcSqliteDriver(
            "jdbc:sqlite:$dbPath",
            Properties().apply { put("foreign_keys", "true") },
            JobSearchDB.Schema,
            callbacks = arrayOf(AfterVersion(3, Migrations.After3::onMigration)),
        )

        JobSearchDB(
            driver,
            SalaryDAO.Adapter(EnumColumnAdapter()),
        )
    }

    single { params -> JobRepository(get(), get(parameters = { params })) }
}
