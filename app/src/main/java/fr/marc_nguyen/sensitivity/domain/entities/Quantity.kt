package fr.marc_nguyen.sensitivity.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.pow

operator fun Double.times(other: Quantity) =
    Quantity(this * other.value, other.unit, other.unitPower)

operator fun Double.div(other: Quantity) =
    Quantity(this / other.value, other.unit, -other.unitPower)

@Parcelize
data class Quantity(val value: Double, val unit: MeasureUnit, val unitPower: Int = 1) : Parcelable {
    companion object {
        fun nan(unitPower: Int) = Quantity(Double.NaN, MeasureUnit.NOT_A_NUMBER, unitPower)
        fun zero(unit: MeasureUnit, unitPower: Int = 1) = Quantity(0.0, unit, unitPower)
    }

    constructor(value: Double, unitSymbol: String, unitPower: Int = 1) : this(
        value,
        MeasureUnit.fromSymbol(unitSymbol),
        unitPower
    )

    infix fun convertTo(newUnit: MeasureUnit) =
        Quantity(value * unit.convertTo(newUnit).pow(unitPower), newUnit, unitPower)

    infix fun convertTo(unitSymbol: String): Quantity {
        val newUnit = MeasureUnit.fromSymbol(unitSymbol)
        return convertTo(newUnit)
    }

    fun sqrt(): Quantity {
        if (unitPower % 2 != 0) throw ArithmeticException("sqrt is not allowed when unitPower is not a multiple of 2: unitPower=$unitPower")
        return Quantity(kotlin.math.sqrt(value), unit, unitPower / 2)
    }

    operator fun plus(other: Quantity): Quantity {
        if (unitPower != other.unitPower) throw ArithmeticException("+ with different unit: this.unitPower=${this.unitPower}, other.unitPower=${other.unitPower}")
        return Quantity(value + other.convertTo(unit).value, unit, unitPower)
    }

    operator fun minus(other: Quantity): Quantity {
        if (unitPower != other.unitPower) throw ArithmeticException("- with different unit: this.unitPower=${this.unitPower}, other.unitPower=${other.unitPower}")
        return Quantity(value - other.convertTo(unit).value, unit, unitPower)
    }

    operator fun times(other: Quantity) =
        Quantity(
            value * other.convertTo(unit).value,
            unit,
            unitPower + other.unitPower
        )

    operator fun div(other: Quantity) =
        Quantity(
            value / other.convertTo(unit).value,
            unit,
            unitPower - other.unitPower
        )

    fun safeToDouble(): Double {
        if (unitPower != 0) throw ArithmeticException("Unit cannot be removed: unitPower=$unitPower")
        return value
    }

    operator fun times(other: Double) = Quantity(value * other, unit, unitPower)
    operator fun div(other: Double) = Quantity(value / other, unit, unitPower)

    override fun toString(): String {
        return if (unitPower != 1) "%.4f %s^%d".format(value, unit.symbol, unitPower)
        else "%.4f %s".format(value, unit.symbol, unitPower)
    }
}

fun List<Quantity>.meanStdDev(): Pair<Quantity, Quantity> {
    if (this.isNullOrEmpty()) return Quantity.nan(0) to Quantity.nan(0)
    var sum: Quantity = Quantity.zero(first().unit, first().unitPower)
    var sumSquared: Quantity = Quantity.zero(first().unit, first().unitPower * 2)
    for (element in this) {
        sum += element
        sumSquared += element * element
    }
    val mean = sum / this.size.toDouble()
    val variance = sumSquared / this.size.toDouble() - mean * mean
    val stdDev = variance.sqrt()
    return mean to stdDev
}

fun List<Quantity>.average(): Quantity {
    if (this.isNullOrEmpty()) return Quantity.nan(0)
    var sum: Quantity = Quantity.zero(first().unit, first().unitPower)
    for (element in this) {
        sum += element
    }
    return sum / this.size.toDouble()
}
