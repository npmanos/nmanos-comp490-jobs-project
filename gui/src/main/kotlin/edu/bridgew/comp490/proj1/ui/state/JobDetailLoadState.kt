package edu.bridgew.comp490.proj1.ui.state

import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.ShortJob
import edu.bridgew.comp490.proj1.data.entities.ShortJobDAO
import java.io.Serializable

sealed interface JobDetailLoadState : Serializable {
    data object None : JobDetailLoadState {
        private fun readResolve(): Any = None
    }
}

sealed class ShortJobDetailLoadState(open val job: ShortJob) : JobDetailLoadState {
    data class Loading(override val job: ShortJobDAO) : ShortJobDetailLoadState(job)
    data class Result(override val job: Job) : ShortJobDetailLoadState(job)
}
