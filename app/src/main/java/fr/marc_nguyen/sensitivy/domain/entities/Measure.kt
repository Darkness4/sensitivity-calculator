package fr.marc_nguyen.sensitivy.domain.entities

import fr.marc_nguyen.sensitivy.core.mappers.ModelMappable
import fr.marc_nguyen.sensitivy.data.models.MeasureModel

data class Measure(
    val date: Int,
    val game: String,
    val sensitivityInGame: Double,
    val distancePer360: Quantity,
) : ModelMappable<MeasureModel> {
    val sensitivityPerDistancePer360: Quantity
        get() = sensitivityInGame / distancePer360

    companion object {
        fun computeNewSensitivity(
            sensitivityPerDistancePer360: Quantity,
            newDistancePer360: Quantity,
            destinationUnit: MeasureUnit
        ): Double {
            return (newDistancePer360 * sensitivityPerDistancePer360).convertTo(destinationUnit).value
        }
    }

    override fun asModel() = MeasureModel(
        date = date,
        game = game,
        sensitivityInGame = sensitivityInGame,
        distancePer360 = distancePer360,
    )
}
