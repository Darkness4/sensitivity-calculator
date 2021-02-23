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

    "2 cm^-1 convertTo in^-1" should {
        "returns 5.08 (with enum)" {
            // Act
            val result = Quantity(2.0, "cm", -1).convertTo("in")

            // Assert
            result shouldBe Quantity(5.08, "in", -1)
        }
    }

    "1 in^2 convertTo cm^2" should {
        "returns 6.4516 (with enum)" {
            // Act
            val result = Quantity(1.0, MeasureUnit.INCH, 2).convertTo(MeasureUnit.CENTIMETER)

            // Assert
            result shouldBe Quantity(6.4516, "cm", 2)
        }
    }

    "2 in^-2 convertTo cm^-2" should {
        "returns 12.9032 (with enum)" {
            // Act
            val result = Quantity(2.0, MeasureUnit.INCH, 2).convertTo(MeasureUnit.CENTIMETER)

            // Assert
            result shouldBe Quantity(12.9032, "cm", 2)
        }
    }

    "sqrt(4 cm^2)" should {
        "returns 2 cm" {
            // Act
            val result = Quantity(4.0, "cm", 2).sqrt()

            // Assert
            result shouldBe Quantity(2.0, "cm")
        }
    }

    "1cm + 1in" should {
        "returns 3.54 cm" {
            // Act
            val result = Quantity(1.0, "cm") + Quantity(1.0, "in")

            // Assert
            result shouldBe Quantity(3.54, "cm")
        }
    }

    "1in + 1cm" should {
        "returns around 1.39370079 in" {
            // Act
            val result = Quantity(1.0, "in") + Quantity(1.0, "cm")

            // Assert
            result shouldBe Quantity(1.0 + 1.0 / 2.54, "in")
        }
    }

    "2cm * 2in^2" should {
        "returns 25.8064 cm^3" {
            // Act
            val result = Quantity(2.0, "cm") * Quantity(2.0, "in", 2)

            // Assert
            result shouldBe Quantity(25.8064, "cm", 3)
        }
    }

    "2cm / 2in^2" should {
        "returns 0.15500031 cm^-1" {
            // Act
            val result = Quantity(2.0, "cm") / Quantity(2.0, "in", 2)

            // Assert
            result shouldBe Quantity(2.0 / (2.0 * 6.4516), "cm", -1)
        }
    }
})
