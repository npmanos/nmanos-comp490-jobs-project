package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.ui.utils.MaterialIcons

private const val searchBarKey = "edu.bridgew.comp490.proj1.ui.components.JobList:SearchBar"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JobList(
    jobs: List<Job>,
    onSearchFilterTextChange: (String) -> Unit,
    onClearFilterSearchClicked: () -> Unit = {},
    selectedJobId: String,
    listState: LazyListState = rememberLazyListState(),
    onSelect: (Job) -> Unit = {},
) {
    var searchText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(end = 12.dp).clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
        ) {
            stickyHeader(searchBarKey) {
                Surface(shape = CircleShape) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            onSearchFilterTextChange(it)
                        },
                        placeholder = { Text("Search jobs") },
                        leadingIcon = { Icon(MaterialIcons.Search, null) },
                        trailingIcon = {
                            if (searchText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        searchText = ""
                                        onClearFilterSearchClicked()
                                    }
                                ) { Icon(MaterialIcons.Cancel, null) }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            }

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
