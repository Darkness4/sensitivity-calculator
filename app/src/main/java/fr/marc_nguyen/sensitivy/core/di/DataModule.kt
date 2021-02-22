package fr.marc_nguyen.sensitivy.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.marc_nguyen.sensitivy.data.database.LocalDatabase
import fr.marc_nguyen.sensitivy.data.database.MeasureDao
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
        )
            .build()
    }

    @Singleton
    @Provides
    fun provideMeasureDao(database: LocalDatabase): MeasureDao {
        return database.measureDao()
    }
}
