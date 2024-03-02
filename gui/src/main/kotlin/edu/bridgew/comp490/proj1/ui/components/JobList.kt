package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import edu.bridgew.comp490.proj1.data.entities.Job

@Composable
fun JobList(
    jobs: List<Job>,
    selectedJobId: String,
    listState: LazyListState = rememberLazyListState(),
    onSelect: (Job) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(end = 12.dp).clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
        ) {

            items(
                items = jobs,
                key = { it.jobId },
            ) { job ->
                JobListItem(
                    job = job,
                    selected = job.jobId == selectedJobId,
                    onClick = { onSelect(it) },
                )
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(listState),
        )
    }
}
