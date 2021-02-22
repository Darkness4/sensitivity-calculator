package fr.marc_nguyen.sensitivy.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.marc_nguyen.sensitivy.data.models.MeasureModel
import fr.marc_nguyen.sensitivy.data.models.converters.EnumConverters

@Database(
    entities = [MeasureModel::class],
    version = 1
)
@TypeConverters(EnumConverters::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun measureDao(): MeasureDao
}
