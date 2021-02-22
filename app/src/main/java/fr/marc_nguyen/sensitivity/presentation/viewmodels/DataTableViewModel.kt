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
import fr.marc_nguyen.sensitivity.domain.entities.meanStdDevOfSensitivityPerDistancePer360
import fr.marc_nguyen.sensitivity.domain.repositories.MeasureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataTableViewModel @AssistedInject constructor(
    private val repository: MeasureRepository,
    @Assisted game: String
) :
    ViewModel() {
    val measures =
        repository.watchByGame(game)
            .asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)

    val meanStd = measures.switchMap {
        liveData<State<String>>(viewModelScope.coroutineContext + Dispatchers.Default) {
            it.doOnSuccess { measures ->
                val (mean, stdDev) = measures.meanStdDevOfSensitivityPerDistancePer360()
                if (mean != null && stdDev != null && mean.unit == stdDev.unit) {
                    emit(
                        State.Success(
                            if (mean.unitPower != 1) "%.4f ± %.4f %s^%d".format(
                                mean.value,
                                stdDev.value,
                                mean.unit.symbol,
                                mean.unitPower
                            ) else "%.4f ± %.4f %s".format(
                                mean.value,
                                stdDev.value,
                                mean.unit.symbol
                            )
                        )
                    )
                } else if (mean != null && stdDev != null) {
                    emit(State.Failure(Exception("Some error in unit : mean.unit=${mean.unit} stdDev.unit=${stdDev.unit}")))
                } else {
                    emit(State.Success("Data is empty"))
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
