package fr.marc_nguyen.sensitivity.domain.repositories

import kotlinx.coroutines.flow.Flow

interface InstantPlacementSettingsRepository {
    fun watch(): Flow<Boolean>
    suspend fun get(): Boolean
    suspend fun set(enabled: Boolean)
}
