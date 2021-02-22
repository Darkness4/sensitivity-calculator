package fr.marc_nguyen.sensitivity.data.repositories

import fr.marc_nguyen.sensitivity.core.state.State
import fr.marc_nguyen.sensitivity.data.database.MeasureDao
import fr.marc_nguyen.sensitivity.domain.entities.Measure
import fr.marc_nguyen.sensitivity.domain.repositories.MeasureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MeasureRepositoryImpl @Inject constructor(private val measureDao: MeasureDao) :
    MeasureRepository {
    override fun watchByGame(game: String): Flow<State<List<Measure>>> {
        return measureDao.watchByGame(game).map { models ->
            State.Success(models.map { it.asEntity() })
        }.catch<State<List<Measure>>> { e ->
            if (e !is Exception) throw e
            emit(State.Failure(e))
        }
    }

    override suspend fun findByGame(game: String): List<Measure> {
        return measureDao.findByGame(game).map { it.asEntity() }
    }

    override suspend fun findGames(): List<String> {
        return measureDao.findGames()
    }

    override suspend fun createOne(measure: Measure): State<Measure> {
        return try {
            val model = measure.asModel()
            measureDao.insert(model)
            State.Success(measure)
        } catch (e: Exception) {
            State.Failure(e)
        }
    }

    override suspend fun deleteOne(measure: Measure): State<Measure> {
        return try {
            val model = measure.asModel()
            measureDao.delete(model)
            State.Success(measure)
        } catch (e: Exception) {
            State.Failure(e)
        }
    }
}
