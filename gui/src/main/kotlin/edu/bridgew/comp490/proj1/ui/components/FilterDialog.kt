package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import edu.bridgew.comp490.proj1.ui.state.FilterState
import edu.bridgew.comp490.proj1.ui.state.copyInto
import edu.bridgew.comp490.proj1.ui.state.rememberCopy
import edu.bridgew.comp490.proj1.ui.state.rememberFilterState

@Composable
fun FilterDialog(
    onApplyFilters: () -> Unit,
    onDismissRequest: () -> Unit,
    state: FilterState = rememberFilterState(),
) {
    val localState = state.rememberCopy()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            backgroundColor = MaterialTheme.colorScheme.background
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize(),
            ) {
                val (filterTitle, wfhCheckbox, whfLabel, resetButton, cancelButton, applyButton) = createRefs()

                Text(
                    "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.constrainAs(filterTitle) {
                        start.linkTo(parent.start, 16.dp)
                        top.linkTo(parent.top, 16.dp)
                    }
                )

                Checkbox(
                    checked = localState.wfhOnly,
                    onCheckedChange = {
                        localState.wfhOnly = it
                    },
                    modifier = Modifier.constrainAs(wfhCheckbox) {
                        start.linkTo(filterTitle.start)
                        top.linkTo(filterTitle.bottom, 8.dp)
                    }
                )

                Text(
                    "Work from home only",
                    modifier = Modifier.constrainAs(whfLabel) {
                        start.linkTo(wfhCheckbox.end, 4.dp)
                        top.linkTo(wfhCheckbox.top)
                        bottom.linkTo(wfhCheckbox.bottom)
                    }
                )

                TextButton(
                    onClick = localState::reset,
                    enabled = !localState.isDefault,
                    modifier = Modifier.constrainAs(resetButton) {
                        start.linkTo(parent.start, 16.dp)
                        top.linkTo(cancelButton.top)
                        bottom.linkTo(cancelButton.bottom)
                    }
                ) {
                    Text("Reset")
                }

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.constrainAs(cancelButton) {
                        end.linkTo(applyButton.start, 8.dp)
                        top.linkTo(applyButton.top)
                        bottom.linkTo(applyButton.bottom)
                    }
                ) {
                    Text("Cancel")
                }

                FilledTonalButton(
                    onClick = {
                        localState.copyInto(state)
                        onApplyFilters()
                    },
                    modifier = Modifier.constrainAs(applyButton) {
                        end.linkTo(parent.end, 16.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                    }
                ) {
                    Text("Apply")
                }
            }
        }
    }
}
