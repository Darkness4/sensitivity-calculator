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

    operator fun times(other: Double) = Quantity(value * other, unit, unitPower)
    operator fun div(other: Double) = Quantity(value / other, unit, unitPower)

    override fun toString(): String {
        return if (unitPower != 1) "$value ${unit.symbol}^$unitPower"
        else "$value ${unit.symbol}"
    }
}
