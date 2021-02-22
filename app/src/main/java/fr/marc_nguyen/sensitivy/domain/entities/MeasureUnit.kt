package fr.marc_nguyen.sensitivy.domain.entities

enum class MeasureUnit(val symbol: String) {
    INCH("in"),
    CENTIMETER("cm");

    companion object {
        private val conversionTable = mapOf(
            (INCH to CENTIMETER) to 2.54
        )

        fun fromSymbol(symbol: String): MeasureUnit {
            return when (symbol) {
                INCH.symbol -> INCH
                CENTIMETER.symbol -> CENTIMETER
                else -> throw NoSuchElementException("Unit symbol $symbol is not supported")
            }
        }
    }

    infix fun convertTo(unit: MeasureUnit): Double {
        if (this == unit) return 1.0

        return conversionTable.getOrElse(this to unit) {
            val ratio = conversionTable.getOrElse(unit to this) {
                throw ArithmeticException("These units cannot be converted : $this to $unit")
            }
            return 1 / ratio
        }
    }
}
