package edu.bridgew.comp490.proj1.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList

@Composable
fun rememberFilterState(
    wfhOnly: Boolean = false,
    locationFilterEnabled: Boolean = false,
    selectedLocations: MutableList<String> = mutableStateListOf()
): FilterState = rememberSaveable(saver = FilterStateImpl.Saver()) {
    FilterStateImpl(
        wfhOnly,
        locationFilterEnabled,
        selectedLocations
    )
}

@Composable
fun FilterState.rememberCopy(): FilterState = rememberFilterState(
    wfhOnly,
    locationFilterEnabled,
    selectedLocations,
)

fun FilterState.copyInto(other: FilterState) {
    other.wfhOnly = wfhOnly
    other.locationFilterEnabled = locationFilterEnabled
    other.selectedLocations.clear()
    selectedLocations.toCollection(other.selectedLocations)
}

interface FilterState {
    var wfhOnly: Boolean

    var locationFilterEnabled: Boolean
    val selectedLocations: MutableList<String>

    val activeFilterCount: Int
    val isDefault: Boolean

    fun reset()
}

private class FilterStateImpl(
    wfhOnly: Boolean,
    locationFilterEnabled: Boolean,
    selectedLocations: MutableList<String>
) : FilterState {
    override var wfhOnly: Boolean by mutableStateOf(wfhOnly)

    override var locationFilterEnabled: Boolean by mutableStateOf(locationFilterEnabled)
    override val selectedLocations: MutableList<String> = selectedLocations.toMutableStateList()

    private var _activeFilterCount by mutableStateOf(0)
    override val activeFilterCount: Int
        get() = (if (wfhOnly) 1 else 0) + (if (locationFilterEnabled) 1 else 0)

    override val isDefault: Boolean
        get() = !wfhOnly
            && !locationFilterEnabled
            && selectedLocations.isEmpty()

    override fun reset() {
        wfhOnly = false
        locationFilterEnabled = false
        selectedLocations.clear()
    }

    companion object {
        fun Saver() = listSaver<FilterState, Any>(
            save = {
                listOf(
                    it.wfhOnly,
                    it.locationFilterEnabled,
                    it.selectedLocations
                )
            },
            restore = { state ->
                FilterStateImpl(
                    wfhOnly = state[0] as Boolean,
                    locationFilterEnabled = state[1] as Boolean,
                    selectedLocations = state[2] as MutableList<String>,
                )
            }
        )
    }
}
