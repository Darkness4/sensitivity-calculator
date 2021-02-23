package fr.marc_nguyen.sensitivity.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.marc_nguyen.sensitivity.core.state.State
import fr.marc_nguyen.sensitivity.core.state.map
import fr.marc_nguyen.sensitivity.domain.entities.Measure
import fr.marc_nguyen.sensitivity.domain.entities.MeasureUnit
import fr.marc_nguyen.sensitivity.domain.entities.Quantity
import fr.marc_nguyen.sensitivity.domain.entities.meanStdDevOfSensitivityPerDistancePer360
import fr.marc_nguyen.sensitivity.domain.repositories.MeasureRepository
import fr.marc_nguyen.sensitivity.presentation.ui.fragments.DataTableFragmentArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MeasureViewModel @Inject constructor(private val repository: MeasureRepository) :
    ViewModel() {
    val gameInput = MutableLiveData("")
    val sensitivityInput = MutableLiveData("")
    val distancePer360Input = MutableLiveData("")
    val unitInput = MutableLiveData("cm")
    val targetPer360Input = MutableLiveData("")
    val targetUnitInput = MutableLiveData("cm")
    val computeResult =
        MutableLiveData<State<String>>(State.Success("Result: Waiting for input..."))

    private val _goToDataTableFragment = MutableLiveData<DataTableFragmentArgs?>()
    val goToDataTableFragment: LiveData<DataTableFragmentArgs?>
        get() = _goToDataTableFragment

    fun goToDataTableFragment() {
        _goToDataTableFragment.value = DataTableFragmentArgs(gameInput.value!!)
    }

    fun goToDataTableFragmentDone() {
        _goToDataTableFragment.value = null
    }

    private val _addResult = MutableLiveData<State<Unit>>()
    val addResult: LiveData<State<Unit>>
        get() = _addResult

    fun updateResult() = viewModelScope.launch(Dispatchers.Main) {
        try {
            val measures = repository.findByGame(gameInput.value!!)
            val (mean, stdDev) = measures.meanStdDevOfSensitivityPerDistancePer360()
            val meanSensitivity = Measure.computeNewSensitivity(
                mean!!,
                Quantity(
                    targetPer360Input.value!!.toDouble(),
                    MeasureUnit.fromSymbol(targetUnitInput.value!!)
                ),
            )
            val stdDevSensitivity = Measure.computeNewSensitivity(
                stdDev!!,
                Quantity(
                    targetPer360Input.value!!.toDouble(),
                    MeasureUnit.fromSymbol(targetUnitInput.value!!)
                ),
            )
            computeResult.value =
                State.Success("Result: %.4f ± %.4f".format(meanSensitivity, stdDevSensitivity))
        } catch (e: NullPointerException) {
            computeResult.value = State.Success("Result: No data")
        } catch (e: NumberFormatException) {
            computeResult.value = State.Success("Result: No target distance")
        } catch (e: Exception) {
            computeResult.value = State.Failure(e)
        }
    }

    fun add() = viewModelScope.launch(Dispatchers.Main) {
        _addResult.value = try {
            val measure = toMeasure()
            repository.createOne(measure).map { }
        } catch (e: Exception) {
            State.Failure(e)
        }
    }

    private fun toMeasure() = Measure(
        date = Date(),
        game = gameInput.value!!,
        sensitivityInGame = sensitivityInput.value!!.toDouble(),
        distancePer360 = Quantity(
            distancePer360Input.value!!.toDouble(),
            MeasureUnit.fromSymbol(unitInput.value!!)
        )
    )
}
