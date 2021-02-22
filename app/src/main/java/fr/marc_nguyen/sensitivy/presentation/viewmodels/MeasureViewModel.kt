package fr.marc_nguyen.sensitivy.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.marc_nguyen.sensitivy.domain.entities.Measure
import fr.marc_nguyen.sensitivy.domain.repositories.MeasureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeasureViewModel @Inject constructor(private val repository: MeasureRepository) : ViewModel() {
    private fun add(measure: Measure) = viewModelScope.launch(Dispatchers.Main) {
        repository.createOne(measure)
    }
}