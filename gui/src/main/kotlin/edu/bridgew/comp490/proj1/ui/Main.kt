package edu.bridgew.comp490.proj1.ui
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import edu.bridgew.comp490.proj1.ui.di.guiModule
import edu.bridgew.comp490.proj1.ui.screen.JobListScreen
import edu.bridgew.comp490.proj1.ui.theme.AppTheme
import io.github.cdimascio.dotenv.dotenv
import org.koin.compose.KoinApplication
import org.koin.core.logger.Level

private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@Preview
@Composable
fun App() {
//    val dbPath = "output/jobs.db"
//
//    val driver = JdbcSqliteDriver(
//        "jdbc:sqlite:$dbPath",
//        Properties().apply { put("foreign_keys", "true") },
//        JobSearchDB.Schema,
//    )
//
//    JobSearchDB.Schema.create(driver)
//    val currentSchemaVersion = Query(788_663, driver, "PRAGMA USER_VERSION") { cursor -> cursor.getLong(0)!! }.executeAsOne()
//    JobSearchDB.Schema.migrate(driver, currentSchemaVersion, 2)
//
//    val db = JobSearchDB(driver)
//    val retrofit = SerpApiClient(dotenv["JOBSPROJ_API_KEY"]).retrofit
//    val jobSearchClient = GoogleJobSearchServiceImpl(retrofit)
//    val jobRepo = JobRepository(jobSearchClient, db)

    Navigator(JobListScreen) {
        Scaffold(
            content = { CurrentScreen() }
        )
    }
}

fun main() = application {
    KoinApplication(application = {
        printLogger(Level.INFO)
        modules(guiModule)
    }) {
        Window(
            onCloseRequest = ::exitApplication,
        ) {
            AppTheme(darkTheme = false) { App() }
        }
    }
}
