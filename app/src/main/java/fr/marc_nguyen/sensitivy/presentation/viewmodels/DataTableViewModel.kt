package fr.marc_nguyen.sensitivy.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.marc_nguyen.sensitivy.core.state.doOnSuccess
import fr.marc_nguyen.sensitivy.domain.entities.Measure
import fr.marc_nguyen.sensitivy.domain.entities.Quantity
import fr.marc_nguyen.sensitivy.domain.repositories.MeasureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class DataTableViewModel @Inject constructor(private val repository: MeasureRepository) :
    ViewModel() {
    val measures =
        repository.watchAll().asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)

    val meanStd = measures.switchMap {
        liveData<String>(viewModelScope.coroutineContext + Dispatchers.Default) {
            it.doOnSuccess { measures ->
                val (mean, stdDev) = measures.meanStdDev()
                if (mean != null && stdDev != null && mean.unit == stdDev.unit) {
                    emit("${mean.value} ± ${stdDev.value} ${mean.unit}")
                } else if (mean != null && stdDev != null) {
                    Timber.e("Some error in unit : mean.unit=${mean.unit} stdDev.unit=${stdDev.unit}")
                }
            }
        }
    }

    private fun delete(measure: Measure) = viewModelScope.launch(Dispatchers.Main) {
        repository.deleteOne(measure)
    }
}

private fun List<Measure>.meanStdDev(): Pair<Quantity?, Quantity?> {
    var mean: Quantity? = null
    var s: Quantity? = null
    for ((index, measure) in this.withIndex()) {
        if (index == 0) {
            mean = measure.sensitivityPerDistancePer360
            s = Quantity(0.0, mean.unit, mean.unitPower)
        } else {
            val newMean =
                (mean!! + (measure.sensitivityPerDistancePer360 - mean)) / (index + 1.0)
            s =
                s!! + (measure.sensitivityPerDistancePer360 - mean) * (measure.sensitivityPerDistancePer360 - newMean)
            mean = newMean
        }
    }
    val variance = s?.div((this.size - 1.0))
    val stdDevValue = variance?.let { sqrt(variance.value) }
    val stdDev = stdDevValue?.let {
        Quantity(stdDevValue, variance.unit, variance.unitPower / 2)
    }
    return mean to stdDev
}