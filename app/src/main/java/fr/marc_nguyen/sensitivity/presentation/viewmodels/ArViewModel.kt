package fr.marc_nguyen.sensitivity.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.marc_nguyen.sensitivity.domain.repositories.InstantPlacementSettingsRepository
import javax.inject.Inject

@HiltViewModel
class ArViewModel @Inject constructor(private val repository: InstantPlacementSettingsRepository) :
    ViewModel() {
    suspend fun isInstantPlacementEnabled() = repository.get()
}
