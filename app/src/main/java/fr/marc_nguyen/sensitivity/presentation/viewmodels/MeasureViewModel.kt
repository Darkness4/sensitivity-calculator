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
import fr.marc_nguyen.sensitivity.domain.entities.computeNewSensitivityLinear
import fr.marc_nguyen.sensitivity.domain.entities.computeNewSensitivityQuadratic
import fr.marc_nguyen.sensitivity.domain.repositories.MeasureRepository
import fr.marc_nguyen.sensitivity.presentation.ui.fragments.DataTableFragmentArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MeasureViewModel @Inject constructor(private val repository: MeasureRepository) :
    ViewModel() {
    val sourceGameInput = MutableLiveData("CS:GO")
    val sensitivityInput = MutableLiveData("")
    val distancePer360Input = MutableLiveData("")
    val unitInput = MutableLiveData("cm")
    val targetDistancePer360Input = MutableLiveData("")
    val targetUnitInput = MutableLiveData("cm")

    val computeQuadraticResult =
        MutableLiveData<State<String>>(State.Success("Sensitivity: Waiting for input..."))
    val computeLinearResult =
        MutableLiveData<State<String>>(State.Success("Sensitivity: Waiting for input..."))

    private val _goToDataTableFragment = MutableLiveData<DataTableFragmentArgs?>()
    val goToDataTableFragment: LiveData<DataTableFragmentArgs?>
        get() = _goToDataTableFragment

    fun goToDataTableFragment() {
        _goToDataTableFragment.value = DataTableFragmentArgs(sourceGameInput.value!!)
    }

    fun goToDataTableFragmentDone() {
        _goToDataTableFragment.value = null
    }

    private val _addResult = MutableLiveData<State<Unit>>()
    val addResult: LiveData<State<Unit>>
        get() = _addResult

    fun updateResult() {
        computeQuadratic()
        computeLinear()
    }

    private fun computeLinear() = viewModelScope.launch(Dispatchers.Main) {
        computeLinearResult.value = try {
            // Assert
            val sourceGame =
                sourceGameInput.value ?: throw NullPointerException("Source game is null.")
            if (sourceGame.isEmpty()) throw NullPointerException("Source game is empty.")
            val targetDistance = targetDistancePer360Input.value
                ?: throw NullPointerException("Target distance is null.")
            if (targetDistance.isEmpty()) throw NullPointerException("Target distance is empty.")
            val targetUnit =
                targetUnitInput.value ?: throw NullPointerException("Target unit is null.")

            // Fetch
            val measures = repository.findByGame(sourceGame)

            // Compute
            val sensitivity = withContext(Dispatchers.Default) {
                measures.computeNewSensitivityLinear(
                    Quantity(targetDistance.toDouble(), targetUnit)
                )
            }

            State.Success("Sensitivity: %.4f ± %.4f".format(sensitivity.first, sensitivity.second))
        } catch (e: Exception) {
            State.Failure(e)
        }
    }

    private fun computeQuadratic() = viewModelScope.launch(Dispatchers.Main) {
        computeQuadraticResult.value = try {
            // Assert
            val sourceGame =
                sourceGameInput.value ?: throw NullPointerException("Source game is null.")
            if (sourceGame.isEmpty()) throw NullPointerException("Source game is empty.")
            val targetDistance = targetDistancePer360Input.value
                ?: throw NullPointerException("Target distance is null.")
            if (targetDistance.isEmpty()) throw NullPointerException("Target distance is empty.")
            val targetUnit =
                targetUnitInput.value ?: throw NullPointerException("Target unit is null.")

            // Fetch
            val measures = repository.findByGame(sourceGame)

            // Compute
            val sensitivity = withContext(Dispatchers.Default) {
                measures.computeNewSensitivityQuadratic(
                    Quantity(targetDistance.toDouble(), targetUnit)
                )
            }

            State.Success("Sensitivity: %.4f".format(sensitivity))
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
        game = sourceGameInput.value!!,
        sensitivityInGame = sensitivityInput.value!!.toDouble(),
        distancePer360 = Quantity(
            distancePer360Input.value!!.toDouble(),
            MeasureUnit.fromSymbol(unitInput.value!!)
        )
    )
}
