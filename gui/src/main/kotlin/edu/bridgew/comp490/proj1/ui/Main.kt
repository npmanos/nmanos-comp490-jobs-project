package edu.bridgew.comp490.proj1.ui
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import edu.bridgew.comp490.proj1.ui.di.guiModule
import edu.bridgew.comp490.proj1.ui.screen.JobListScreen
import edu.bridgew.comp490.proj1.ui.theme.BsuTheme
import io.github.cdimascio.dotenv.Dotenv
import org.apache.commons.lang3.SystemUtils
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.logger.Level
import java.awt.Dimension
import java.io.File

@Preview
@Composable
fun App(dbPath: String) {
    Navigator(JobListScreen(dbPath)) {
        Scaffold(
            content = { CurrentScreen() },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

fun main() = application {
    KoinApplication(application = {
        printLogger(Level.INFO)
        modules(guiModule)
    }) {
        val state = rememberWindowState(width = 1280.dp, height = 720.dp)
        var windowTitle by remember { mutableStateOf("Job Browser") }

        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = windowTitle,
        ) {
            // Min window size code from https://github.com/JetBrains/compose-multiplatform/issues/2285#issuecomment-1873001531
            with(LocalDensity.current) {
                val minSize = DpSize(1040.dp, 585.dp).toSize()
                window.minimumSize = Dimension(minSize.width.toInt(), minSize.height.toInt())
            }

            var showFilePicker by remember { mutableStateOf(true) }
            var dbPath by remember { mutableStateOf<String?>(null) }

            MenuBar {
                Menu("File", mnemonic = 'F') {
                    Item(
                        "Open...",
                        onClick = { showFilePicker = true },
                        shortcut = KeyShortcut(Key.O, ctrl = !SystemUtils.IS_OS_MAC, meta = SystemUtils.IS_OS_MAC),
                    )
                    Separator()
                    Item("Exit", onClick = { exitApplication() })
                }
            }

            val dotenv: Dotenv = koinInject()

            if (dotenv["JOBSPROJ_DEBUG_DB"] != null) {
                dbPath = dotenv["JOBSPROJ_DEBUG_DB"]
                showFilePicker = false
                windowTitle = "Job Browser - $dbPath"
            }

            FilePicker(showFilePicker, fileExtensions = listOf("db", "sqlite", "sqlite3"), title = "Select jobs database...") {
                if (it == null) exitApplication()

                val selected = (it as MPFile<File>)

                dbPath = selected.platformFile.path
                windowTitle = "Job Browser - ${selected.platformFile.name}"
                showFilePicker = false
            }

            if (dbPath != null) {
                BsuTheme(darkTheme = true) { App(dbPath!!) }
            }
        }
    }
}
