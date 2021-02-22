package fr.marc_nguyen.sensitivy.data.repositories

import fr.marc_nguyen.sensitivy.core.state.State
import fr.marc_nguyen.sensitivy.data.database.MeasureDao
import fr.marc_nguyen.sensitivy.domain.entities.Measure
import fr.marc_nguyen.sensitivy.domain.repositories.MeasureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MeasureRepositoryImpl @Inject constructor(private val measureDao: MeasureDao) : MeasureRepository {
    override fun watchAll(): Flow<State<List<Measure>>> {
        return measureDao.watch().map { models ->
            State.Success(models.map { it.asEntity() })
        }.catch<State<List<Measure>>> { e ->
            if (e !is Exception) throw e
            emit(State.Failure(e))
        }
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
