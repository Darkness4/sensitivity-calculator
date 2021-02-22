package fr.marc_nguyen.sensitivy.domain.entities

operator fun Double.times(other: Quantity) =
    Quantity(this * other.value, other.unit, other.unitPower)

operator fun Double.div(other: Quantity) =
    Quantity(this / other.value, other.unit, -other.unitPower)

data class Quantity(val value: Double, val unit: MeasureUnit, val unitPower: Int = 1) {
    constructor(value: Double, unitSymbol: String, unitPower: Int = 1) : this(
        value,
        MeasureUnit.fromSymbol(unitSymbol),
        unitPower
    )

    infix fun convertTo(newUnit: MeasureUnit) =
        Quantity(value * unit.convertTo(newUnit), newUnit, unitPower)

    infix fun convertTo(unitSymbol: String): Quantity {
        val newUnit = MeasureUnit.fromSymbol(unitSymbol)
        return Quantity(value * unit.convertTo(newUnit), newUnit, unitPower)
    }

    operator fun plus(other: Quantity): Quantity {
        if (unitPower != other.unitPower) throw ArithmeticException("+ with different unit.")
        return Quantity(value + other.convertTo(unit).value, unit, unitPower)
    }


    operator fun minus(other: Quantity): Quantity {
        if (unitPower != other.unitPower) throw ArithmeticException("- with different unit.")
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
