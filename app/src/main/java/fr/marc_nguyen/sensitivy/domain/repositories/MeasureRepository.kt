package fr.marc_nguyen.sensitivy.domain.repositories

import fr.marc_nguyen.sensitivy.core.state.State
import fr.marc_nguyen.sensitivy.domain.entities.Measure
import kotlinx.coroutines.flow.Flow

interface MeasureRepository {
    fun watchAll(): Flow<State<List<Measure>>>
    suspend fun createOne(measure: Measure): State<Measure>
    suspend fun deleteOne(measure: Measure): State<Measure>
}
