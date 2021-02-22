package fr.marc_nguyen.sensitivy.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.marc_nguyen.sensitivy.core.mappers.EntityMappable
import fr.marc_nguyen.sensitivy.domain.entities.Measure
import fr.marc_nguyen.sensitivy.domain.entities.Quantity

@Entity(tableName = "measures")
data class MeasureModel(
    @PrimaryKey
    val date: Int,
    val game: String,
    @ColumnInfo(name = "sensitivity_in_game") val sensitivityInGame: Double,
    @Embedded(prefix = "distance_per_360_") val distancePer360: Quantity,
) : EntityMappable<Measure> {
    override fun asEntity() = Measure(
        date = date,
        game = game,
        sensitivityInGame = sensitivityInGame,
        distancePer360 = distancePer360,
    )
}
