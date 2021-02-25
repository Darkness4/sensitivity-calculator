package fr.marc_nguyen.sensitivity.data.repositories

import androidx.datastore.core.DataStore
import fr.marc_nguyen.sensitivity.data.models.InstantPlacementSettingsModel
import fr.marc_nguyen.sensitivity.domain.repositories.InstantPlacementSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class InstantPlacementSettingsRepositoryImpl @Inject constructor(private val dataStore: DataStore<InstantPlacementSettingsModel>) :
    InstantPlacementSettingsRepository {

    override fun watch(): Flow<Boolean> {
        return dataStore.data.map {
            it.instantPlacementEnabled
        }.catch { e ->
            if (e is IOException) {
                Timber.e(e, "Error reading instant placement settings.")
                emit(InstantPlacementSettingsModel.getDefaultInstance().instantPlacementEnabled)
            } else {
                throw e
            }
        }
    }

    override suspend fun get(): Boolean {
        return try {
            dataStore.data.first().instantPlacementEnabled
        } catch (e: IOException) {
            Timber.e(e, "Error reading instant placement settings.")
            InstantPlacementSettingsModel.getDefaultInstance().instantPlacementEnabled
        }
    }

    override suspend fun set(enabled: Boolean) {
        dataStore.updateData {
            it.toBuilder()
                .setInstantPlacementEnabled(enabled)
                .build()
        }
    }
}
