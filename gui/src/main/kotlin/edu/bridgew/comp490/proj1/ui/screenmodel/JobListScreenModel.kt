package edu.bridgew.comp490.proj1.ui.screenmodel

import androidx.compose.ui.util.fastFirstOrNull
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.relativeTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

        data class Result(val jobs: List<Job>) : State()
    }

    private val dateComparator = compareBy<Job> { job ->
        val postedAt = job.detectedExtensions?.fastFirstOrNull { it is PostedAt } as PostedAt?

        postedAt?.date
    }.reversed().thenBy { job ->
        val postedAt = job.detectedExtensions?.fastFirstOrNull { it is PostedAt } as PostedAt?

        postedAt?.date?.relativeTimeString
    }.thenBy { job -> job.title }

    suspend fun getJobs(keywordFilter: String, wfhOnly: Boolean, selectedLocations: Collection<String>?) {
        mutableState.value = State.Loading

        val result = screenModelScope.async(Dispatchers.IO) {
            State.Result(repository.getFilteredJobs(
                keywordFilter,
                if (wfhOnly) true else null,
                selectedLocations?.ifEmpty { null },
            ).sortedWith(dateComparator))
        }

        mutableState.value = result.await()
    }
}
