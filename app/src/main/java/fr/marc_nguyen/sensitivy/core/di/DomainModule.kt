package fr.marc_nguyen.sensitivy.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.marc_nguyen.sensitivy.data.repositories.MeasureRepositoryImpl
import fr.marc_nguyen.sensitivy.domain.repositories.MeasureRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DomainModule {
    @Binds
    @Singleton
    fun bindStationRepository(repository: MeasureRepositoryImpl): MeasureRepository
}
