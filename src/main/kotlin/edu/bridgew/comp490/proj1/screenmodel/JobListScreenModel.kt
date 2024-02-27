package edu.bridgew.comp490.proj1.screenmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.data.entities.Job
import kotlinx.coroutines.launch

class JobListScreenModel(private val repository: JobRepository) : StateScreenModel<JobListScreenModel.State>(State.Init) {
    sealed class State {
        data object Init : State()
        data object Loading : State()
        data class Result(val jobs: List<Job>) : State()
    }

    fun getJobs(query: String) {
        screenModelScope.launch {
            mutableState.value = State.Loading
            mutableState.value = State.Result(repository.getJobs())
        }
    }
}
