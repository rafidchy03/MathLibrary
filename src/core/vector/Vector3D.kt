package core.vector

import core.linear.Matrix
import core.linear.SquareMatrix

class Vector3D(vector: DoubleVector) : DoubleVector(*vector.dimensions, mandatoryArity = 3) {
    infix fun cross(other: Vector3D): Vector3D {
        return (SquareMatrix(
                arrayOf(
                        Matrix.unitVectorArray,
                        this.dimensions.toTypedArray(),
                        other.dimensions.toTypedArray()
                )
        ).determinant as DoubleVector).to3D
    }
}