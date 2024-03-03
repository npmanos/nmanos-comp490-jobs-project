package edu.bridgew.comp490.proj1.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.ui.components.FilterDialog
import edu.bridgew.comp490.proj1.ui.components.JobDetails
import edu.bridgew.comp490.proj1.ui.components.JobList
import edu.bridgew.comp490.proj1.ui.screenmodel.JobListScreenModel
import edu.bridgew.comp490.proj1.ui.state.rememberFilterState
import edu.bridgew.comp490.proj1.ui.utils.HorizontalSpacer
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

data class JobListScreen(private val dbPath: String) : Screen {
    private fun readResolve(): Any = this.copy()
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<JobListScreenModel> { parametersOf(dbPath) }
        val state by screenModel.state.collectAsState()
        var jobList by remember { mutableStateOf(listOf<Job>()) }
        var selectedJob by remember { mutableStateOf<Job?>(null) }
        var selectedJobId by remember { mutableStateOf("") }
        var searchFilterText by remember { mutableStateOf("") }
        var showFilterDialog by remember { mutableStateOf(false) }
        val filterState = rememberFilterState()
        val listState = rememberLazyListState()
        val detailScrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.3f)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
            ) {
                JobList(
                    jobs = jobList,
                    selectedJobId = selectedJobId,
                    listState = listState,
                    onSelect = {
                        selectedJob = it
                        selectedJobId = it.jobId
                        coroutineScope.launch { detailScrollState.scrollTo(0) }
                    },
                    onSearchFilterTextChange = {
                        searchFilterText = if (it.isBlank()) "" else "$it*"
                    },
                    onClearFilterSearchClicked = {
                        searchFilterText = ""
                    },
                    onFilterButtonClick = {
                        showFilterDialog = true
                    }
                )

                when (state) {
                    is JobListScreenModel.State.Init,
                    is JobListScreenModel.State.Loading -> LoadingJobs()
                    is JobListScreenModel.State.Result -> jobList = (state as JobListScreenModel.State.Result).jobs
                }
            }

            HorizontalSpacer(16.dp)

            Box(modifier = Modifier.weight(0.65f)) {
                JobDetails(
                    job = selectedJob,
                    modifier = Modifier.fillMaxHeight().padding(end = 16.dp, top = 16.dp, bottom = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    scrollState = detailScrollState,
                )
            }

            if (showFilterDialog) {
                FilterDialog(
                    onApplyFilters = { showFilterDialog = false },
                    onDismissRequest = { showFilterDialog = false },
                    state = filterState,
                )
            }

            LaunchedEffect(searchFilterText) { screenModel.getJobs(searchFilterText) }
        }
    }

    @Composable
    private fun LoadingJobs() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
