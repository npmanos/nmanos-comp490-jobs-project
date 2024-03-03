package edu.bridgew.comp490.proj1.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun rememberFilterState(
    wfhOnly: Boolean = false,
): FilterState = rememberSaveable(saver = FilterStateImpl.Saver()) {
    FilterStateImpl(
        wfhOnly,
    )
}

@Composable
fun FilterState.rememberCopy(): FilterState = rememberFilterState(
    wfhOnly,
)

fun FilterState.copyInto(other: FilterState) {
    other.wfhOnly = wfhOnly
}

interface FilterState {
    var wfhOnly: Boolean

    val isDefault: Boolean

    fun reset()
}

private class FilterStateImpl(
    wfhOnly: Boolean,
) : FilterState {
    override var wfhOnly: Boolean by mutableStateOf(wfhOnly)

    override val isDefault: Boolean
        get() = !wfhOnly

    override fun reset() {
        wfhOnly = false
    }

    companion object {
        fun Saver() = listSaver<FilterState, Any>(
            save = {
                listOf(
                    it.wfhOnly,
                )
            },
            restore = { state ->
                FilterStateImpl(
                    wfhOnly = state[0] as Boolean
                )
            }
        )
    }
}
