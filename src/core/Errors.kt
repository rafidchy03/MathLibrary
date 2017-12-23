package core

import kotlin.reflect.KClass

class ArityError(expected: Int, actual: Int) : Error("Expected vector of arity $expected but got $actual.")
class UnitVectorError(magnitude: Double) : Error("Expected unit vector but got vector of magnitude $magnitude.")
class MatrixDimensionError : Error("Matrix must be square shaped to take the determinant.")
class BadOperationError(a: Any, b: Any, kClass: KClass<*>) : Error("Cannot execute operation: $a ${kClass.simpleName} $b")