package fr.marc_nguyen.sensitivity.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.marc_nguyen.sensitivity.data.repositories.InstantPlacementSettingsRepositoryImpl
import fr.marc_nguyen.sensitivity.data.repositories.MeasureRepositoryImpl
import fr.marc_nguyen.sensitivity.domain.repositories.InstantPlacementSettingsRepository
import fr.marc_nguyen.sensitivity.domain.repositories.MeasureRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DomainModule {
    @Binds
    @Singleton
    fun bindStationRepository(repository: MeasureRepositoryImpl): MeasureRepository

    @Binds
    @Singleton
    fun bindPlacementSettingsRepository(repository: InstantPlacementSettingsRepositoryImpl): InstantPlacementSettingsRepository
}
