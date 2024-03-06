package edu.bridgew.comp490.proj1.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import edu.bridgew.comp490.proj1.data.Wage
import io.nacular.measured.units.Measure

@Composable
fun rememberFilterState(
    wfhOnly: Boolean = false,
    locationFilterEnabled: Boolean = false,
    selectedLocations: MutableList<String> = mutableStateListOf(),
    salaryFilterEnabled: Boolean = false,
    minimumSalary: Measure<Wage>? = null,
): FilterState = rememberSaveable(saver = FilterStateImpl.Saver()) {
    FilterStateImpl(
        wfhOnly,
        locationFilterEnabled,
        selectedLocations,
        salaryFilterEnabled,
        minimumSalary,
    )
}

@Composable
fun FilterState.copy(
    wfhOnly: Boolean = this.wfhOnly,
    locationFilterEnabled: Boolean = this.locationFilterEnabled,
    selectedLocations: MutableList<String> = this.selectedLocations.toMutableStateList(),
    salaryFilterEnabled: Boolean = this.salaryFilterEnabled,
    minimumSalary: Measure<Wage>? = this.minimumSalary,
) = rememberFilterState(wfhOnly, locationFilterEnabled, selectedLocations, salaryFilterEnabled, minimumSalary)

fun FilterState.copyFrom(other: FilterState) {
    wfhOnly = other.wfhOnly
    locationFilterEnabled = other.locationFilterEnabled
    selectedLocations.clear()
    selectedLocations.addAll(other.selectedLocations)
}

interface FilterState {
    var wfhOnly: Boolean
    operator fun component1() = wfhOnly

    var locationFilterEnabled: Boolean
    val selectedLocations: MutableList<String>
    operator fun component2() = locationFilterEnabled
    operator fun component3() = selectedLocations

    var salaryFilterEnabled: Boolean
    var minimumSalary: Measure<Wage>?
    operator fun component4() = salaryFilterEnabled
    operator fun component5() = minimumSalary

    val activeFilterCount: Int
    val isDefault: Boolean

    fun reset()
}

private class FilterStateImpl(
    wfhOnly: Boolean,
    locationFilterEnabled: Boolean,
    selectedLocations: MutableList<String>,
    salaryFilterEnabled: Boolean,
    minimumSalary: Measure<Wage>?,
) : FilterState {
    override var wfhOnly: Boolean by mutableStateOf(wfhOnly)

    override var locationFilterEnabled: Boolean by mutableStateOf(locationFilterEnabled)
    override val selectedLocations: MutableList<String> = selectedLocations.toMutableStateList()

    override val activeFilterCount: Int by derivedStateOf {
        (if (wfhOnly) 1 else 0) +
        (if (locationFilterEnabled) 1 else 0)
    }

    override var salaryFilterEnabled: Boolean by mutableStateOf(salaryFilterEnabled)
    override var minimumSalary: Measure<Wage>? by mutableStateOf(minimumSalary)

    override val isDefault: Boolean
        get() = !wfhOnly
            && !locationFilterEnabled
            && selectedLocations.isEmpty()
            && !salaryFilterEnabled
            && minimumSalary == null

    override fun reset() {
        wfhOnly = false
        locationFilterEnabled = false
        selectedLocations.clear()
        salaryFilterEnabled = false
        minimumSalary = null
    }

    override fun toString(): String {
        return "FilterStateImpl(wfhOnly=$wfhOnly, locationFilterEnabled=$locationFilterEnabled, selectedLocations=$selectedLocations)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilterStateImpl

        if (wfhOnly != other.wfhOnly) return false
        if (locationFilterEnabled != other.locationFilterEnabled) return false
        if (selectedLocations.toList() != other.selectedLocations.toList()) return false
        if (salaryFilterEnabled != other.salaryFilterEnabled) return false
        if (minimumSalary != other.minimumSalary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = wfhOnly.hashCode()
        result = 31 * result + locationFilterEnabled.hashCode()
        result = 31 * result + selectedLocations.toList().hashCode()
        result = 31 * result + activeFilterCount
        result = 31 * result + salaryFilterEnabled.hashCode()
        result = 31 * result + minimumSalary.hashCode()
        return result
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun Saver() = listSaver<FilterState, Any?>(
            save = {
                listOf(
                    it.wfhOnly,
                    it.locationFilterEnabled,
                    it.selectedLocations,
                    it.salaryFilterEnabled,
                    it.minimumSalary,
                )
            },
            restore = { state ->
                FilterStateImpl(
                    wfhOnly = state[0] as Boolean,
                    locationFilterEnabled = state[1] as Boolean,
                    selectedLocations = state[2] as MutableList<String>,
                    salaryFilterEnabled = state[3] as Boolean,
                    minimumSalary = state[4] as Measure<Wage>?,
                )
            }
        )
    }


}
