package fr.marc_nguyen.sensitivity.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.createDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.marc_nguyen.sensitivity.data.database.LocalDatabase
import fr.marc_nguyen.sensitivity.data.database.MeasureDao
import fr.marc_nguyen.sensitivity.data.database.serializers.InstantPlacementSettingsModelSerializer
import fr.marc_nguyen.sensitivity.data.models.InstantPlacementSettingsModel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): LocalDatabase {
        return Room.databaseBuilder(
            context,
            LocalDatabase::class.java,
            "cache.db"
        ).build()
    }

    @Singleton
    @Provides
    fun provideMeasureDao(database: LocalDatabase): MeasureDao {
        return database.measureDao()
    }

    @Singleton
    @Provides
    fun provideInstantPlacementSettingsDataStore(@ApplicationContext context: Context): DataStore<InstantPlacementSettingsModel> {
        return context.createDataStore(
            fileName = "instant_placement_settings.pb",
            serializer = InstantPlacementSettingsModelSerializer
        )
    }
}
