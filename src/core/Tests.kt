package core

import applications.mechanics.Mass
import applications.mechanics.Projectile
import core.complex.Complex
import core.differential.*
import core.linear.*
import core.quantum.QuantumBasis
import core.quantum.QuantumCircuit
import core.quantum.QuantumGate
import core.vector.*
import kotlin.math.*

fun main(args: Array<String>) {
    fun section(name: String, block: (() -> Unit)) {
        print("\u001B[1;31m\n$name\n\n\u001B[0m")
        block()
    }

    fun shouldError(block: (() -> Unit)) {
        try {
            block()

            println("No error occurred.")
        } catch (e: Error) {
            println("Error: ${e.message}")
            println("\u001B[1;32m\u2705 Success!\u001B[0m")
        }
    }

    section("Constants") {
        println("i\u0302: ${DoubleVector.i}")
        println("j\u0302: ${DoubleVector.j}")
        println("k\u0302: ${DoubleVector.k}")

        println("Constant: ${1.v}")
    }

    section("Arity Checks") {
        println("This should error, as 3D vectors should have arities of exactly 3.")
        shouldError { 1.v.to3D }
    }

    val vector1 = DoubleVector(2.0, 3.0, 4.0)
    val vector2 = DoubleVector(5.0, 6.0, 7.0)

    section("Vector Operations") {
        println("$vector1 + $vector2 = ${vector1 + vector2}")
        println("$vector1 - $vector2 = ${vector1 - vector2}")
        println("$vector1 ⋅ $vector2 = ${vector1 * vector2}")
        println("$vector1 × $vector2 = ${vector1.to3D cross vector2.to3D}")
        println("$vector1 = \n${vector1.column}")
        println("$vector1' = ${vector1.row}")
        println("$vector1 ⊗ $vector2 = \n${vector1 outer vector2}")
        println("‖$vector1‖ = ${vector1.magnitude}")
        println("$vector1\u0302 = ${vector1.unit}")
        println("proj $vector1($vector2) = ${vector1 projectionOnto vector2}")
        println("rej $vector1($vector2) = ${vector1 rejectionFrom vector2}")
        println("Angle between $vector1 and $vector2: ${vector1 angleFrom vector2} radians")
    }

    val doubleFunction = DoubleFunction { i: Double -> i.pow(2.0) * 5 }
    val scalarField = ScalarField { vector: DoubleVector -> vector[0].pow(2.0) * vector[1] }
    val rotatingVectorField = VectorField { vector: DoubleVector -> vector.to3D cross DoubleVector.i }

    val outwardsField = VectorField { vector: DoubleVector -> vector * vector.magnitude }

    section("Derivatives") {
        println("(i ^ 2 * 5)'|(i=3) = ${doubleFunction.differentiate(3.0)}")
        println("∇(x ^ 2 * y)|(3, 2) = ${scalarField.gradient(DoubleVector(3.0, 2.0))}")
        println("δ(\u0305v × i\u0302)/δy|(2, 3, 4) = ${rotatingVectorField.partialDerivative(1, vector1)}")
        println()
        println("d/di (i ^ 2 * 5)|(i=3) = ${(Derivative * doubleFunction)(3.0)}")
        println("δ/δx (x ^ 2 * y)|(3, 2) = ${(DirectionalDerivative.d_dx * scalarField)(DoubleVector(3.0, 2.0))}")
        println("δ/δy (\u0305v × i\u0302)|(0, 0, 0) = ${(DirectionalDerivative.d_dy * rotatingVectorField)(DoubleVector.`0`)}")
        println()
        println("d/dt 1 = ${Derivative * 1.0}")
        println("δ/δx 1 = ${DirectionalDerivative.d_dx * 1.0}")
        println("δ/δx i\u0302 = ${DirectionalDerivative.d_dx * DoubleVector.i}")
    }

    section("Nabla Operations") {
        println("∇(x ^ 2 * y)|(3, 2) = ${Nabla(scalarField)(DoubleVector(3.0, 2.0))}")
        println("∇ ⋅ (‖\u0305v‖ * \u0305v)|(2, 3, 4) = ${(Nabla * outwardsField)(vector1)}")
        println("∇ × (\u0305v × i\u0302)|(2, 3, 4) = ${(Nabla cross rotatingVectorField)(vector1)}")
        println("∇<4.0, 3.0>(x ^ 2 * y)|(3, 2) = ${(Nabla[DoubleVector(4.0, 3.0)] * scalarField)(DoubleVector(3.0, 2.0))}")
    }

    val circle = VectorValuedFunction { t -> DoubleVector(4 * cos(2 * t), 4 * sin(2 * t)) }

    section("VVF Operations") {
        println("For the circle \u0305r = 4cos(2t)i\u0302 + 4sin(2t)j\u0302 at (0,0):")
        println()
        println("Unit tangent = ${circle.unitTangent(0.0)}")
        println("Principal unit normal = ${circle.principalUnitNormal(0.0)}")
        println("Curvature = ${circle.curvature(0.0)}")
    }

    val twoByTwo = SquareMatrix(arrayOf(arrayOf(1.0, 3.0), arrayOf(1.0, 4.0)))
    val threeByThree = SquareMatrix(arrayOf(arrayOf(0.0, 1.0, 2.0), arrayOf(3.0, 4.0, 5.0), arrayOf(6.0, 7.0, 8.0)))

    section("Type-Agnostic Operations") {
        println("<1, 2, 3> * 2.5 = ${Multiply(DoubleVector(1.0, 2.0, 3.0), 2.5)}")

        val derivativeOfVectorField = Multiply(DirectionalDerivative.d_dy, rotatingVectorField) as VectorField
        println("δ/δy (\u0305v × i\u0302)|(2, 3, 4) = ${derivativeOfVectorField(DoubleVector.`0`)}")
        println()

        println("This should error, as scalars and vectors cannot be added.")
        shouldError { Add(DoubleVector(1.0, 2.0, 3.0), 2.5) }
    }

    section("Matrices and Determinants") {
        println("$twoByTwo = ${twoByTwo.determinant}")
        println()
        println("$threeByThree = ${threeByThree.determinant}")
        println()
        println("$threeByThree' = \n\n${threeByThree.transpose}")
        println()
        println("R2 = ${threeByThree.row[1]}")
        println()
        println("A where R2 is replaced = \n\n${threeByThree.row.replace(1, Row(10.0, 20.0, 30.0))}")
        println()
        println("C3 = \n${threeByThree.column[2]}")
        println()
        println("A where C3 is replaced = \n\n${threeByThree.column.replace(2, Column(10.0, 20.0, 30.0))}")
        println()
        println("This should error, as the matrix is misshapen.")
        shouldError { Matrix(arrayOf(arrayOf(1, 2, 3), arrayOf(5, 6, 7, 8))) }
        println()
        println("This should error, as the matrix isn't square.")
        shouldError { SquareMatrix(arrayOf(arrayOf(1, 2, 3, 4), arrayOf(5, 6, 7, 8))) }
        println()
        println("(Direct sum)\nA ⊕ I_4 = \n${threeByThree directAdd Matrix.eye(4)}")
    }

    val projectile = Projectile(10.0, 10.0, PI / 4)
    val mass = Mass { position -> ln(position.magnitude) }

    section("Other Applications (and cool Physics!)") {
        println(projectile)
        println()
        println("Projectile position at 3s: ${projectile(3.0)} m")
        println("Projectile mass at 3s: ${mass(projectile)(3.0)} kg")
        println("Projectile density at 3s: ${mass.density(projectile)(3.0)}")
        println("Force gravity on projectile at 3s: ${(projectile gravityOn mass)(3.0)} N")
        println("Torque gravity about (0, 1, 0) on projectile at 3s: " +
                "${(projectile gravityOn mass).torqueAbout(DoubleVector(0.0, 1.0, 0.0).to3D)(3.0)} Nm")
    }

    section("Complex Arithmetic") {
        val complexNumber = Complex(1.0, 1.0)

        println("x = $complexNumber")
        println("\u0305x = ${complexNumber.conjugate}")
        println("x^2 = ${complexNumber * complexNumber}")
        println("|x| = ${complexNumber.magnitude}")
    }

    section("Quantum Gates") {
        // Gates!
        println("H = \n${QuantumGate.H}\n")
        println("√X = \n${QuantumGate.sqrtNOT}\n")
        println("X = \n${QuantumGate.X}\n")
        println("Y = \n${QuantumGate.Y}\n")
        println("Z = \n${QuantumGate.Z}\n")
        println("√S = \n${QuantumGate.sqrtS}\n")
        println("S = \n${QuantumGate.S}\n")
        println("cX = \n${QuantumGate.cX}\n")
        println("cY = \n${QuantumGate.cY}\n")
        println("cZ = \n${QuantumGate.cZ}\n")
        println("CCNOT = \n${QuantumGate.CCNOT}\n")
        println("cS = \n${QuantumGate.cS}\n")
    }

    section("Quantum Computer Simulator") {
        val circuit = QuantumCircuit.circuit(3) {
            parallel {
                applyGate(1 until 2, QuantumGate.H)
            }

            parallel {
                // Undo original Hadamard transformation.
                applyGate(1 until 2, QuantumGate.H)
            }
        }

        println("Equivalent gate for first step of circuit: \n${circuit.parallelLegs[0].evaluate}\n")

        // This should equal I if everything works out, since H^2 = I.
        println("Equivalent gate for entire circuit: \n${circuit.evaluate}\n")

        val input = QuantumBasis.eyeBasis(3).states[5]

        println("Input state: \n$input\n")

        val output = circuit apply input

        println("Input run through the quantum gate: \n$output\n")
        println("Input run through the quantum gate and measured: \n${output.measure()}\n")
    }
}