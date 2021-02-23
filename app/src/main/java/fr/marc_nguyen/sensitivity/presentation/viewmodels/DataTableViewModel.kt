package fr.marc_nguyen.sensitivity.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.marc_nguyen.sensitivity.core.state.State
import fr.marc_nguyen.sensitivity.core.state.doOnSuccess
import fr.marc_nguyen.sensitivity.domain.entities.Measure
import fr.marc_nguyen.sensitivity.domain.entities.meanStdDev
import fr.marc_nguyen.sensitivity.domain.entities.polyRegression
import fr.marc_nguyen.sensitivity.domain.repositories.MeasureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataTableViewModel @AssistedInject constructor(
    private val repository: MeasureRepository,
    @Assisted val game: String
) :
    ViewModel() {
    val measures =
        repository.watchByGame(game)
            .asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)

    val approximation = measures.switchMap {
        liveData<State<String>>(viewModelScope.coroutineContext + Dispatchers.Default) {
            it.doOnSuccess { measures ->
                try {
                    emit(State.Success(measures.polyRegression().toString()))
                } catch (e: Exception) {
                    emit(State.Failure(e))
                }
            }
        }
    }

    val factor = measures.switchMap {
        liveData<State<String>>(viewModelScope.coroutineContext + Dispatchers.Default) {
            it.doOnSuccess { measures ->
                try {
                    val (mean, stdDev) = measures.map { it.sensitivityDistanceIn360 }.meanStdDev()
                    emit(State.Success("%s ± %s".format(mean.toString(), stdDev.toString())))
                } catch (e: Exception) {
                    emit(State.Failure(e))
                }
            }
        }
    }

    fun delete(measure: Measure) = viewModelScope.launch(Dispatchers.Main) {
        repository.deleteOne(measure)
    }

    @dagger.assisted.AssistedFactory
    fun interface AssistedFactory {
        fun create(game: String): DataTableViewModel
    }

    companion object {
        fun AssistedFactory.provideFactory(game: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return this@provideFactory.create(game) as T
                }
            }
    }
}
