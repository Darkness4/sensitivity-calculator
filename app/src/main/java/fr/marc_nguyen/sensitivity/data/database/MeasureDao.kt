package fr.marc_nguyen.sensitivity.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.marc_nguyen.sensitivity.data.models.MeasureModel
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasureDao {
    @Query("SELECT * FROM measures WHERE game = :game")
    fun watchByGame(game: String): Flow<List<MeasureModel>>

    @Query("SELECT * FROM measures WHERE game = :game")
    suspend fun findByGame(game: String): List<MeasureModel>

    @Query("SELECT DISTINCT game FROM measures")
    suspend fun findGames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg items: MeasureModel)

    @Delete
    suspend fun delete(item: MeasureModel)

    @Query("DELETE FROM measures")
    suspend fun clear()
}
