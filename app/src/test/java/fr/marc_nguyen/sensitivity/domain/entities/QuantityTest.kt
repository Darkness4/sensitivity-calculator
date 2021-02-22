package fr.marc_nguyen.sensitivity.domain.entities

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class QuantityTest : WordSpec({
    "2 inches convertTo cm" should {
        "returns 5.08 (with enum)" {
            // Act
            val result = Quantity(2.0, MeasureUnit.INCH).convertTo(MeasureUnit.CENTIMETER)

            // Assert
            result shouldBe Quantity(5.08, "cm")
        }

        "returns 5.08 (with string)" {
            // Act
            val result = Quantity(2.0, MeasureUnit.INCH) convertTo "cm"

            // Assert
            result shouldBe Quantity(5.08, "cm")
        }
    }
})
