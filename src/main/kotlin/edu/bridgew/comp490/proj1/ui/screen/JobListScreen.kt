package edu.bridgew.comp490.proj1.ui.screen

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import edu.bridgew.comp490.proj1.HorizontalSpacer
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

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            val listState = rememberLazyListState()

            when (state) {
                is State.Init -> {
                    LoadingJobs()
                    screenModel.getJobs()
                }

                is State.Loading -> LoadingJobs()
                is State.Result -> {
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
                            //JobListItem(job)
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = job.title,
                                        fontSize = 16.sp,
                                        lineHeight = 24.0.sp,
//                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                },
                                supportingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource("/edu/bridgew/comp490/proj1/screen/domain-24px.xml"),
                                            contentDescription = null,
                                            modifier = Modifier.size(17.dp).alignByBaseline(),
                                        )
                                        HorizontalSpacer(4.dp)
                                        Text(
                                            text = job.companyName.trim(),
                                            fontSize = 14.sp,
                                            lineHeight = 20.0.sp,
                                            modifier = Modifier.alignByBaseline(),
                                        )

                                        if (job.location != null) {
                                            HorizontalSpacer(12.dp)
                                            Icon(
                                                imageVector = Icons.Rounded.LocationOn,
                                                contentDescription = null,
                                                modifier = Modifier.size(17.dp).alignByBaseline(),
                                            )
                                            HorizontalSpacer(4.dp)
                                            Text(
                                                text = job.location.trim(),
                                                fontSize = 14.sp,
                                                lineHeight = 20.0.sp,
                                                modifier = Modifier.alignByBaseline(),
                                            )
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors()
                            )

                            if (job != jobs.last()) {
                                Divider()
                            }
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(end = 2.dp, top = 2.dp, bottom = 2.dp),
                        adapter = rememberScrollbarAdapter(listState)
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
    private fun JobListItem(job: Job) {
        Box {
            Column {
                Text(
                    text = job.title,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource("/edu/bridgew/comp490/proj1/screen/domain-24px.xml"),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    HorizontalSpacer(4.dp)
                    Text(
                        text = job.companyName.trim(),
                        fontSize = 16.sp
                    )

                    if (job.location != null) {
                        HorizontalSpacer(12.dp)
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        HorizontalSpacer(4.dp)
                        Text(
                            text = job.location.trim(),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
