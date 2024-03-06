package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.rounded.AttachMoney
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import edu.bridgew.comp490.proj1.data.Currency.Companion.dollars
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.hour
import edu.bridgew.comp490.proj1.ui.state.FilterState
import edu.bridgew.comp490.proj1.ui.state.copy
import edu.bridgew.comp490.proj1.ui.state.rememberFilterState
import edu.bridgew.comp490.proj1.ui.utils.MaterialIcons
import io.nacular.measured.units.div
import io.nacular.measured.units.times

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    onApplyRequest: () -> Unit,
    onDismissRequest: () -> Unit,
    state: FilterState = rememberFilterState(),
    allLocations: List<String>,
) {
    val localState = state.copy()
    var locationDropdownExpanded by remember { mutableStateOf(false) }
    var locationSearchText by remember { mutableStateOf("") }
    val dropdownOptionsFiltered by remember { derivedStateOf { allLocations.filter { it.contains(locationSearchText, true) } } }
    var minimumSalaryText by remember { mutableStateOf(localState.minimumSalary?.amount?.toString() ?: "") }
    var invalidSalaryInput by remember { mutableStateOf(false) }

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
                    resetButton,
                    cancelButton,
                    applyButton,
                    locationChips,
                    locationCheckbox,
                    locationDropdown,
                    salaryCheckbox,
                    salaryInput,
                    salaryUnitDropdown,
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

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.constrainAs(locationChips) {
                        start.linkTo(locationDropdown.start)
                        end.linkTo(parent.end, 16.dp)
                        horizontalBias = 0f
                        width = Dimension.fillToConstraints

                        top.linkTo(locationDropdown.bottom, 4.dp)
                    }
                ) {
                    localState.selectedLocations.forEach { location ->
                        InputChip(
                            selected = true,
                            onClick = {
                                localState.selectedLocations.remove(location)
                            },
                            label = { Text(location.trim()) },
                            enabled = localState.locationFilterEnabled,
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
                            },
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }

                Checkbox(
                    checked = localState.locationFilterEnabled,
                    onCheckedChange = { localState.locationFilterEnabled = it },
                    modifier = Modifier.constrainAs(locationCheckbox) {
                        start.linkTo(filterTitle.start)
                        top.linkTo(locationDropdown.top)
                        bottom.linkTo(locationDropdown.bottom)
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = locationDropdownExpanded  && localState.locationFilterEnabled,
                    onExpandedChange = { locationDropdownExpanded = it },
                    modifier = Modifier.constrainAs(locationDropdown) {
                        start.linkTo(locationCheckbox.end, 4.dp)
                        top.linkTo(wfhCheckbox.bottom, 8.dp)
                    }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = locationSearchText,
                        onValueChange = {
                            locationSearchText = it
                            locationDropdownExpanded = true
                        },
                        enabled = localState.locationFilterEnabled,
                        placeholder = { Text("Location") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(locationDropdownExpanded  && localState.locationFilterEnabled) },
                        singleLine = true,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )

                    if (dropdownOptionsFiltered.isNotEmpty()) {
                        DropdownMenu(
                            expanded = locationDropdownExpanded && localState.locationFilterEnabled,
                            onDismissRequest = { locationDropdownExpanded = false },
                            properties = PopupProperties(focusable = false),
                            modifier = Modifier.exposedDropdownSize(true)
                        ) {
                            dropdownOptionsFiltered.forEach { locationOption ->
                                DropdownMenuItem(
                                    text = { Text(locationOption.trim()) },
                                    onClick = {
                                        if (locationOption !in localState.selectedLocations) {
                                            localState.selectedLocations.add(locationOption)
                                            localState.selectedLocations.sortBy { it.trim() }
                                        } else {
                                            localState.selectedLocations.remove(locationOption)
                                        }
                                        locationDropdownExpanded = false
                                    },
                                    trailingIcon = {
                                        if (locationOption in localState.selectedLocations) {
                                            Icon(MaterialIcons.Check, null)
                                        }
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }

                Checkbox(
                    checked = localState.salaryFilterEnabled,
                    onCheckedChange = { localState.salaryFilterEnabled = it },
                    modifier = Modifier.constrainAs(salaryCheckbox) {
                        start.linkTo(filterTitle.start)
                        top.linkTo(salaryInput.top)
                        bottom.linkTo(salaryInput.bottom)
                    }
                )

                TextField(
                    value = minimumSalaryText,
                    onValueChange = {
                        if (it.isBlank()) {
                            minimumSalaryText = ""
                            localState.minimumSalary = null
                        } else {
                            try {
                                localState.minimumSalary = it.toDouble() * dollars / hour
                                minimumSalaryText = it
                                invalidSalaryInput = false
                            } catch (e: NumberFormatException) {
                                invalidSalaryInput = true
                            }
                        }
                    },
                    enabled = localState.salaryFilterEnabled,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    placeholder = { Text("Minimum", textAlign = TextAlign.End) },
                    leadingIcon = { Icon(MaterialIcons.AttachMoney, null) },
                    isError = invalidSalaryInput,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.constrainAs(salaryInput) {
                        start.linkTo(salaryCheckbox.end, 4.dp)
                        top.linkTo(locationChips.bottom, 8.dp)
                    }
                )

                TextButton(
                    onClick = localState::reset,
                    enabled = !localState.isDefault,
                    modifier = Modifier.constrainAs(resetButton) {
                        start.linkTo(parent.start, 16.dp)
                        top.linkTo(applyButton.top)
                        bottom.linkTo(applyButton.bottom)
                    },
                ) {
                    Text("Reset")
                }

                TextButton(
                    onClick = {
                        localState.wfhOnly = state.wfhOnly
                        localState.locationFilterEnabled = state.locationFilterEnabled
                        localState.selectedLocations.clear()
                        localState.selectedLocations.addAll(state.selectedLocations)

                        onDismissRequest()
                    },
                    modifier = Modifier.constrainAs(cancelButton) {
                        end.linkTo(applyButton.start, 8.dp)
                        top.linkTo(applyButton.top)
                        bottom.linkTo(applyButton.bottom)
                    },
                ) {
                    Text("Cancel")
                }

                FilledTonalButton(
                    onClick = {
                        state.wfhOnly = localState.wfhOnly
                        state.locationFilterEnabled = localState.locationFilterEnabled
                        state.selectedLocations.clear()
                        state.selectedLocations.addAll(localState.selectedLocations)
                        state.salaryFilterEnabled = localState.salaryFilterEnabled
                        state.minimumSalary = localState.minimumSalary

                        onApplyRequest()
                    },
                    enabled = localState != state,
                    modifier = Modifier.constrainAs(applyButton) {
                        end.linkTo(parent.end, 16.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                    },
                ) {
                    Text("Apply")
                }
            }
        }
    }
}
