package edu.bridgew.comp490.proj1.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import edu.bridgew.comp490.proj1.ui.components.JobList
import edu.bridgew.comp490.proj1.ui.screenmodel.JobListScreenModel
import edu.bridgew.comp490.proj1.ui.screenmodel.JobListScreenModel.State

data class JobListScreen(private val screenModel: JobListScreenModel) : Screen {
    private fun readResolve(): Any = JobListScreen(screenModel)
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { screenModel }
        val state by screenModel.state.collectAsState()

        Surface(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.background)
        ) {
            when (state) {
                is State.Init -> {
                    LoadingJobs()
                    screenModel.getJobs()
                }
                is State.Loading -> LoadingJobs()
                is State.Result -> JobList((state as State.Result).jobs)
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth(0.1f))

        Surface(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.6f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.background)
        ) {

        }
    }

    @Composable
    private fun LoadingJobs() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
