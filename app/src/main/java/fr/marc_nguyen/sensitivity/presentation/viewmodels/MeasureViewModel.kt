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
import fr.marc_nguyen.sensitivity.domain.entities.meanStdDevOfDistancePer360
import fr.marc_nguyen.sensitivity.domain.entities.meanStdDevOfSensitivity
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
    val gameInput = MutableLiveData("CS:GO")
    val sensitivityInput = MutableLiveData("")
    val distancePer360Input = MutableLiveData("")
    val unitInput = MutableLiveData("cm")
    val targetGameInput = MutableLiveData("")

    val computeResult =
        MutableLiveData<State<String>>(State.Success("Sensitivity: Waiting for input..."))

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

    fun prefillGameFields(game: String) = viewModelScope.launch(Dispatchers.Main) {
        val measures = repository.findByGame(game)
        val meanDistancePer360 = measures.meanStdDevOfDistancePer360().first
        val meanSensitivity = measures.meanStdDevOfSensitivity().first
        if (meanSensitivity != null && meanDistancePer360 != null) {
            sensitivityInput.value = meanSensitivity.toString()
            distancePer360Input.value = meanDistancePer360.value.toString()
            unitInput.value = meanDistancePer360.unit.symbol
        }
    }

    fun updateResult() = viewModelScope.launch(Dispatchers.Main) {
        computeResult.value = try {
            // Fetch
            val (meanSensitivityPerDistancePer360, stdDevSensitivityPerDistancePer360) = repository.findByGame(
                gameInput.value!!
            ).meanStdDevOfSensitivityPerDistancePer360()
            meanSensitivityPerDistancePer360
                ?: throw NullPointerException("No game data: Please, save your sensitivity and distance per 360° of your game !")
            stdDevSensitivityPerDistancePer360 ?: throw UnknownError("Std. Dev. shouldn't be null!")
            val meanTargetDistance =
                repository.findByGame(targetGameInput.value!!).meanStdDevOfDistancePer360().first
                    ?: throw NullPointerException("No target game data")

            // Compute
            val meanSensitivity = Measure.computeNewSensitivity(
                meanSensitivityPerDistancePer360,
                meanTargetDistance,
            )
            val stdDevSensitivity = Measure.computeNewSensitivity(
                stdDevSensitivityPerDistancePer360,
                meanTargetDistance,
            )
            State.Success("Sensitivity: %.4f ± %.4f".format(meanSensitivity, stdDevSensitivity))
        } catch (e: Exception) {
            State.Failure(e)
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
