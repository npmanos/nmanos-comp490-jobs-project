package edu.bridgew.comp490.proj1.ui.screen

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import edu.bridgew.comp490.proj1.HorizontalSpacer
import edu.bridgew.comp490.proj1.MaterialIcons
import edu.bridgew.comp490.proj1.data.entities.Job
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
            modifier = Modifier.fillMaxHeight().fillMaxWidth(1f / 3f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.background)
        ) {

            val listState = rememberLazyListState()
            when (state) {
                is State.Init -> {
                    LoadingJobs()
                    screenModel.getJobs()
                }

                is State.Loading -> LoadingJobs()
                is State.Result -> Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        state = listState,
                    ) {
                        val jobs = (state as State.Result).jobs
                        items(
                            items = jobs,
                            key = { it.jobId },
                        ) { job ->
                            JobListItem(job)

                            if (job != jobs.last()) {
                                Divider()
                            }
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(end = 2.dp, top = 2.dp, bottom = 2.dp),
                        adapter = rememberScrollbarAdapter(listState),
                    )
                }
            }
        }


    }

    @Composable
    private fun LoadingJobs() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun JobListItem(job: Job) = ListItem(
        headlineContent = {
            Text(
                text = job.title,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = MaterialIcons.Domain,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp).alignByBaseline(),
                )
                HorizontalSpacer(4.dp)
                Text(
                    text = job.companyName.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alignByBaseline(),
                )

                if (job.location != null) {
                    HorizontalSpacer(12.dp)
                    Icon(
                        imageVector = MaterialIcons.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp).alignByBaseline(),
                    )
                    HorizontalSpacer(4.dp)
                    Text(
                        text = job.location.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
            }
        },
        colors = JobListItemColors(selected = false)
    )

    @Composable
    private fun JobListItemColors(selected: Boolean) = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.background,
        headlineColor = contentColorFor(MaterialTheme.colorScheme.background),
        supportingColor = contentColorFor(MaterialTheme.colorScheme.background),
    )
}
