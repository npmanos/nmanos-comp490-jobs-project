package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.constraintlayout.compose.ConstraintLayout
import edu.bridgew.comp490.proj1.ui.state.FilterState
import edu.bridgew.comp490.proj1.ui.state.rememberFilterState
import edu.bridgew.comp490.proj1.ui.utils.MaterialIcons

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    onApplyFilters: () -> Unit,
    onDismissRequest: () -> Unit,
    state: FilterState = rememberFilterState(),
    allLocations: List<String>,
) {
    var locationDropdownExpanded by remember { mutableStateOf(false) }
    var locationSearchText by remember { mutableStateOf("") }
    val dropdownOptionsFiltered = allLocations.filter { it.contains(locationSearchText, true) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(540.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize(),
            ) {
                val (
                    filterTitle,
                    wfhCheckbox,
                    whfLabel,
                    cancelButton,
                    applyButton,
                    locationChips,
                    locationCheckbox,
                    locationDropdown,
                ) = createRefs()

                Text(
                    "Filters",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.constrainAs(filterTitle) {
                        start.linkTo(parent.start, 16.dp)
                        top.linkTo(parent.top, 16.dp)
                    }
                )

                Checkbox(
                    checked = state.wfhOnly,
                    onCheckedChange = {
                        state.wfhOnly = it
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

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.constrainAs(locationChips) {
                        start.linkTo(filterTitle.start)
                        end.linkTo(parent.end, 16.dp)
                        horizontalBias = 0f

                        top.linkTo(wfhCheckbox.bottom, 8.dp)
                    }
                ) {
                    state.selectedLocations.forEach { location ->
                        InputChip(
                            selected = true,
                            onClick = {
                                state.selectedLocations.remove(location)
                            },
                            label = { Text(location.trim()) },
                            enabled = state.locationFilterEnabled,
                            avatar = {
                                Icon(
                                    MaterialIcons.LocationOn,
                                    null,
                                    Modifier.size(InputChipDefaults.IconSize)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    MaterialIcons.Close,
                                    null,
                                    Modifier.size(InputChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }

                Checkbox(
                    checked = state.locationFilterEnabled,
                    onCheckedChange = { state.locationFilterEnabled = it },
                    modifier = Modifier.constrainAs(locationCheckbox) {
                        start.linkTo(filterTitle.start)
                        top.linkTo(locationChips.bottom, 8.dp)
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = locationDropdownExpanded,
                    onExpandedChange = { locationDropdownExpanded = it },
                    modifier = Modifier.constrainAs(locationDropdown) {
                        start.linkTo(locationCheckbox.end, 4.dp)
                        top.linkTo(locationCheckbox.top)
                        bottom.linkTo(locationCheckbox.bottom)
                    }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = locationSearchText,
                        onValueChange = { locationSearchText = it },
                        enabled = state.locationFilterEnabled,
                        placeholder = { Text("Location") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationDropdownExpanded) },
                        singleLine = true,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )

                    if (dropdownOptionsFiltered.isNotEmpty()) {
                        DropdownMenu(
                            expanded = locationDropdownExpanded,
                            onDismissRequest = { locationDropdownExpanded = false },
                            properties = PopupProperties(focusable = false),
                            modifier = Modifier.exposedDropdownSize(true)
                        ) {
                            dropdownOptionsFiltered.forEach { locationOption ->
                                DropdownMenuItem(
                                    text = { Text(locationOption.trim()) },
                                    onClick = {
                                        if (locationOption !in state.selectedLocations) {
                                            state.selectedLocations.add(locationOption)
                                            state.selectedLocations.sort()
                                        } else {
                                            state.selectedLocations.remove(locationOption)
                                        }
                                    },
                                    trailingIcon = {
                                        if (locationOption in state.selectedLocations) {
                                            Icon(MaterialIcons.Check, null)
                                        }
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = state::reset,
                    modifier = Modifier.constrainAs(cancelButton) {
                        end.linkTo(applyButton.start, 8.dp)
                        top.linkTo(applyButton.top)
                        bottom.linkTo(applyButton.bottom)
                    },
                ) {
                    Text("Reset")
                }

                FilledTonalButton(
                    onClick = onApplyFilters,
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
