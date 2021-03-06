package fr.marc_nguyen.sensitivity.data.models.converters

import androidx.room.TypeConverter
import fr.marc_nguyen.sensitivity.domain.entities.MeasureUnit

class EnumConverters {
    @TypeConverter
    fun fromMeasureUnit(value: MeasureUnit?): String? {
        return value?.symbol
    }

    @TypeConverter
    fun symbolToMeasureUnit(symbol: String?): MeasureUnit? {
        return symbol?.let { MeasureUnit.fromSymbol(it) }
    }
}
