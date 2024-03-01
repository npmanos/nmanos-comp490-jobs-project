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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.bridgew.comp490.proj1.data.entities.Job

@Composable
fun JobList(
    jobs: List<Job>,
    listState: LazyListState = rememberLazyListState(),
    onSelect: (Job) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
        ) {

            items(
                items = jobs,
                key = { it.jobId },
            ) { job ->
                JobListItem(
                    job,
                    onClick = { onSelect(it) }
                )
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(end = 2.dp, top = 4.dp, bottom = 4.dp),
            adapter = rememberScrollbarAdapter(listState),
        )
    }
}
