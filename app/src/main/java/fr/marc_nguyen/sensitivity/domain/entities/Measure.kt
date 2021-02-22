package fr.marc_nguyen.sensitivity.domain.entities

import android.os.Parcelable
import fr.marc_nguyen.sensitivity.core.mappers.ModelMappable
import fr.marc_nguyen.sensitivity.data.models.MeasureModel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Measure(
    val date: Date,
    val game: String,
    val sensitivityInGame: Double,
    val distancePer360: Quantity,
) : ModelMappable<MeasureModel>, Parcelable {
    val sensitivityPerDistancePer360: Quantity
        get() = sensitivityInGame / distancePer360

    companion object {
        fun computeNewSensitivity(
            sensitivityPerDistancePer360: Quantity,
            newDistancePer360: Quantity,
        ): Double {
            val calculus = newDistancePer360 * sensitivityPerDistancePer360
            assert(calculus.unitPower == 0) { "Unit didn't cancel! Expected: 0, Got: ${calculus.unitPower}" }
            return calculus.value
        }
    }

    override fun asModel() = MeasureModel(
        date = date,
        game = game,
        sensitivityInGame = sensitivityInGame,
        distancePer360 = distancePer360,
    )
}

fun List<Measure>.meanStdDevOfSensitivityPerDistancePer360(): Pair<Quantity?, Quantity?> {
    var sum: Quantity? = null
    var sumSquared: Quantity? = null
    for ((index, measure) in this.withIndex()) {
        if (index == 0) {
            sum = measure.sensitivityPerDistancePer360
            sumSquared = measure.sensitivityPerDistancePer360 * measure.sensitivityPerDistancePer360
        } else {
            sum = sum!!.plus(measure.sensitivityPerDistancePer360)
            sumSquared =
                sumSquared!!.plus(measure.sensitivityPerDistancePer360 * measure.sensitivityPerDistancePer360)
        }
    }
    val mean = sum?.div(this.size.toDouble())
    val variance =
        if (sumSquared != null && sum != null) (sumSquared - (sum * sum) / this.size.toDouble()) / this.size.toDouble() else null
    val stdDev = variance?.sqrt()
    return mean to stdDev
}
