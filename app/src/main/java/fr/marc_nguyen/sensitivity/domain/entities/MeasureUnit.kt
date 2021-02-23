package fr.marc_nguyen.sensitivity.domain.entities

enum class MeasureUnit(val symbol: String) {
    INCH("in"),
    CENTIMETER("cm"),
    NOT_A_NUMBER("NaN");

    companion object {
        val symbols = values().map { it.symbol }

        private val conversionTable = mapOf(
            (INCH to CENTIMETER) to 2.54
        )

        fun fromSymbol(symbol: String): MeasureUnit {
            return values().associateBy({ it.symbol }, { it })
                .getOrElse(symbol) { throw NoSuchElementException("Unit symbol $symbol is not supported") }
        }
    }

    infix fun convertTo(unit: MeasureUnit): Double {
        if (this == unit) return 1.0
        if (this == NOT_A_NUMBER || unit == NOT_A_NUMBER) return 1.0

        return conversionTable.getOrElse(this to unit) {
            val ratio = conversionTable.getOrElse(unit to this) {
                throw ArithmeticException("These units cannot be converted : $this to $unit")
            }
            return 1 / ratio
        }
    }
}
