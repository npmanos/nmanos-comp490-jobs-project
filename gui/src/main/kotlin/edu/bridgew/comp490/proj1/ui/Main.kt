package edu.bridgew.comp490.proj1.ui
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import edu.bridgew.comp490.proj1.ui.di.guiModule
import edu.bridgew.comp490.proj1.ui.screen.JobListScreen
import edu.bridgew.comp490.proj1.ui.theme.BsuTheme
import org.koin.compose.KoinApplication
import org.koin.core.logger.Level
import java.awt.Dimension

@Preview
@Composable
fun App() {
    Navigator(JobListScreen) {
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
        Window(
            onCloseRequest = ::exitApplication,
            state = state
        ) {
            // Min window size code from https://github.com/JetBrains/compose-multiplatform/issues/2285#issuecomment-1873001531
            with (LocalDensity.current) {
                val minSize = DpSize(1280.dp, 720.dp).toSize()
                window.minimumSize = Dimension(minSize.width.toInt(), minSize.height.toInt())
            }

            BsuTheme { App() }
        }
    }
}
