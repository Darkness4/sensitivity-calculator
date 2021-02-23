package fr.marc_nguyen.sensitivity.domain.entities

import android.os.Parcelable
import fr.marc_nguyen.sensitivity.core.mappers.ModelMappable
import fr.marc_nguyen.sensitivity.data.models.MeasureModel
import kotlinx.parcelize.Parcelize
import java.util.Date
import kotlin.math.sqrt

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
        if (sumSquared != null && mean != null) sumSquared / this.size.toDouble() - mean * mean else null
    val stdDev = variance?.sqrt()
    return mean to stdDev
}

fun List<Measure>.meanStdDevOfDistancePer360(): Pair<Quantity?, Quantity?> {
    var sum: Quantity? = null
    var sumSquared: Quantity? = null
    for ((index, measure) in this.withIndex()) {
        if (index == 0) {
            sum = measure.distancePer360
            sumSquared = measure.distancePer360 * measure.distancePer360
        } else {
            sum = sum!!.plus(measure.distancePer360)
            sumSquared =
                sumSquared!!.plus(measure.distancePer360 * measure.distancePer360)
        }
    }
    val mean = sum?.div(this.size.toDouble())
    val variance =
        if (sumSquared != null && mean != null) sumSquared / this.size.toDouble() - mean * mean else null
    val stdDev = variance?.sqrt()
    return mean to stdDev
}

fun List<Measure>.meanStdDevOfSensitivity(): Pair<Double?, Double?> {
    var sum: Double? = null
    var sumSquared: Double? = null
    for ((index, measure) in this.withIndex()) {
        if (index == 0) {
            sum = measure.sensitivityInGame
            sumSquared = measure.sensitivityInGame * measure.sensitivityInGame
        } else {
            sum = sum!!.plus(measure.sensitivityInGame)
            sumSquared =
                sumSquared!!.plus(measure.sensitivityInGame * measure.sensitivityInGame)
        }
    }
    val mean = sum?.div(this.size.toDouble())
    val variance =
        if (sumSquared != null && mean != null) sumSquared / this.size.toDouble() - mean * mean else null
    val stdDev = variance?.let { sqrt(it) }
    return mean to stdDev
}
