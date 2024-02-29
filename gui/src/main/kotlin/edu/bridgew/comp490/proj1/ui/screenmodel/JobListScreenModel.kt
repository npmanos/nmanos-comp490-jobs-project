package edu.bridgew.comp490.proj1.ui.screenmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.data.entities.Job
import kotlinx.coroutines.launch
import java.io.Serializable

class JobListScreenModel(private val repository: JobRepository) : StateScreenModel<JobListScreenModel.State>(State.Init) {
    sealed class State : Serializable {
        data object Init : State() {
            private fun readResolve(): Any = Init
        }

        data object Loading : State() {
            private fun readResolve(): Any = Loading
        }

        data class Result(val jobs: List<Job>) : State()
    }

    fun getJobs() {
        screenModelScope.launch {
            mutableState.value = State.Loading
            mutableState.value = State.Result(repository.getJobs())
        }
    }
}
