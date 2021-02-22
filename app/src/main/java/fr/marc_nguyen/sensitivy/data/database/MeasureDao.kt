package fr.marc_nguyen.sensitivy.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.marc_nguyen.sensitivy.data.models.MeasureModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasureDao {
    @Query("SELECT * FROM measures")
    fun watch(): Flow<List<MeasureModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg items: MeasureModel)

    @Delete
    suspend fun delete(item: MeasureModel)

    @Query("DELETE FROM measures")
    suspend fun clear()
}
