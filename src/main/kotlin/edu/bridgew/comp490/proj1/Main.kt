
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.Query
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import cafe.adriel.voyager.navigator.Navigator
import edu.bridgew.comp490.proj1.data.GoogleJobSearchServiceImpl
import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.data.SerpApiClient
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import edu.bridgew.comp490.proj1.io.JobXlsx
import edu.bridgew.comp490.proj1.screen.JobListScreen
import edu.bridgew.comp490.proj1.screenmodel.JobListScreenModel
import io.github.cdimascio.dotenv.dotenv
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.util.*

private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@Composable
fun App() {
    val jobXlsx = try {
        JobXlsx(XSSFWorkbook(File("data/Sprint3Data.xlsx").inputStream()))
    } catch (e: IllegalArgumentException) {
        throw e
    }

    val dbPath = "output/jobs.db"

    val driver = JdbcSqliteDriver(
        "jdbc:sqlite:$dbPath",
        Properties().apply { put("foreign_keys", "true") },
        JobSearchDB.Schema,
    )

    JobSearchDB.Schema.create(driver)
    val currentSchemaVersion = Query(788_663, driver, "PRAGMA USER_VERSION") { cursor -> cursor.getLong(0)!! }.executeAsOne()
    JobSearchDB.Schema.migrate(driver, currentSchemaVersion, 2)

    val db = JobSearchDB(driver)
    val retrofit = SerpApiClient(dotenv["JOBSPROJ_API_KEY"]).retrofit
    val jobSearchClient = GoogleJobSearchServiceImpl(retrofit)
    val jobRepo = JobRepository(jobSearchClient, db)

    Navigator(
        screen = JobListScreen(JobListScreenModel(jobRepo))
    )
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
