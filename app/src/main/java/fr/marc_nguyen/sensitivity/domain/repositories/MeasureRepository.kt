package fr.marc_nguyen.sensitivity.domain.repositories

import fr.marc_nguyen.sensitivity.core.state.State
import fr.marc_nguyen.sensitivity.domain.entities.Measure
import kotlinx.coroutines.flow.Flow

interface MeasureRepository {
    fun watchByGame(game: String): Flow<State<List<Measure>>>
    suspend fun findByGame(game: String): List<Measure>
    suspend fun findGames(): List<String>
    suspend fun createOne(measure: Measure): State<Measure>
    suspend fun deleteOne(measure: Measure): State<Measure>
}
