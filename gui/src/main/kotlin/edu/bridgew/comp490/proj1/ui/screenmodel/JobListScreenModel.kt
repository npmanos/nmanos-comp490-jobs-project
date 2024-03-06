package edu.bridgew.comp490.proj1.ui.screenmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.data.entities.ShortJobDAO
import edu.bridgew.comp490.proj1.ui.state.FilterState
import edu.bridgew.comp490.proj1.ui.state.JobDetailLoadState
import edu.bridgew.comp490.proj1.ui.state.ShortJobDetailLoadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.Serializable

class JobListScreenModel(private val repository: JobRepository) : StateScreenModel<JobListScreenModel.State>(State.Init) {
    val locations by lazy { repository.getLocations().sortedBy { it.trim() } }

    sealed class State : Serializable {
        data object Init : State() {
            private fun readResolve(): Any = Init
        }

        data object Loading : State() {
            private fun readResolve(): Any = Loading
        }

        data class Result(val jobs: List<ShortJobDAO>) : State()
    }

    suspend fun getJobs(
        keywordFilter: String,
        filterState: FilterState,
    ) {
        mutableState.value = State.Loading

        val result = screenModelScope.async(Dispatchers.IO) {
            State.Result(
                repository.getFilteredShortJobs(
                    keywordFilter,
                    if (filterState.wfhOnly) true else null,
                    filterState.locationFilterEnabled,
                    filterState.selectedLocations,
                    filterState.salaryFilterEnabled,
                    filterState.minimumSalary,
                )
            )
        }

        mutableState.value = result.await()
    }

    private val mutableDetailState = MutableStateFlow<JobDetailLoadState>(JobDetailLoadState.None)
    val detailState = mutableDetailState.asStateFlow()

    suspend fun getFullJob(job: ShortJobDAO) {
        mutableDetailState.value = ShortJobDetailLoadState.Loading(job)

        val result = screenModelScope.async(Dispatchers.IO) {
            ShortJobDetailLoadState.Result(repository.getJob(job.jobId))
        }

        mutableDetailState.value = result.await()
    }
}
