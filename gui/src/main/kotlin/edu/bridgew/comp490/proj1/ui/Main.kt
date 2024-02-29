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
            state = WindowState(placement = WindowPlacement.Maximized)
        ) {
            AppTheme(darkTheme = false) { App() }
        }
    }
}
