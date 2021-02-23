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
    val sensitivityDistanceIn360: Quantity
        get() = 1.0 / (distancePer360 * sensitivityInGame)

    override fun asModel() = MeasureModel(
        date = date,
        game = game,
        sensitivityInGame = sensitivityInGame,
        distancePer360 = distancePer360,
    )

    /**
     * Polynomial degree 2.
     *
     * x: Unit
     * [a]: No Unit
     * [b]: Unit^-1
     * [c]: Unit^-2
     *
     * compute(x) = [a] + [b] * x + [c] * x^2
     */
    data class Polynomial2(val a: Double, val b: Quantity, val c: Quantity) {
        fun compute(x: Quantity): Double = a + (b * x).safeToDouble() + (c * x * x).safeToDouble()

        override fun toString(): String {
            return "%.4f + %s * x + %s * x^2".format(a, b.toString(), c.toString())
        }
    }
}

fun List<Measure>.computeNewSensitivityQuadratic(targetDistancePer360: Quantity): Double {
    return 1 / this.polyRegression().compute(targetDistancePer360)
}

fun List<Measure>.computeNewSensitivityLinear(targetDistancePer360: Quantity): Pair<Double, Double> {
    val (mean, stdDev) = this.map { it.sensitivityDistanceIn360 }.meanStdDev()
    val sensitivity = (1.0 / (targetDistancePer360 * mean)).safeToDouble()
    val sensitivityStdDev =
        (stdDev / (targetDistancePer360 * (mean * mean - stdDev * stdDev))).safeToDouble()
    return sensitivity to sensitivityStdDev
}

fun List<Measure>.polyRegression(): Measure.Polynomial2 {
    val (distancesPer360, sensitivitiesInverse) = this.map { it.distancePer360 to 1 / it.sensitivityInGame }
        .unzip()
    return polyRegression(distancesPer360, sensitivitiesInverse)
}

internal fun polyRegression(x: List<Quantity>, y: List<Double>): Measure.Polynomial2 {
    if (x.isNullOrEmpty() || y.isNullOrEmpty()) return Measure.Polynomial2(
        a = Double.NaN,
        b = Quantity.nan(-1),
        c = Quantity.nan(-2)
    )

    val xm: Quantity = x.average()
    val ym: Double = y.average()
    val x2m: Quantity = x.map { it * it }.average()
    val x3m: Quantity = x.map { it * it * it }.average()
    val x4m: Quantity = x.map { it * it * it * it }.average()
    val xym: Quantity = x.zip(y).map { it.first * it.second }.average()
    val x2ym: Quantity = x.zip(y).map { it.first * it.first * it.second }.average()

    val sxx: Quantity = x2m - xm * xm
    val sxy: Quantity = xym - xm * ym
    val sxx2: Quantity = x3m - xm * x2m
    val sx2x2: Quantity = x4m - x2m * x2m
    val sx2y: Quantity = x2ym - x2m * ym

    // b unit: unit^5/unit^6 = unit^-1
    val b: Quantity = (sxy * sx2x2 - sx2y * sxx2) / (sxx * sx2x2 - sxx2 * sxx2)
    // c unit: unit^4/unit^6 = unit^-2
    val c: Quantity = (sx2y * sxx - sxy * sxx2) / (sxx * sx2x2 - sxx2 * sxx2)
    val a = ym - (b * xm).safeToDouble() - (c * x2m).safeToDouble()

    return Measure.Polynomial2(a, b, c)
}
