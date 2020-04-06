@file:Suppress("unused")

package de.westermann.kobserve

import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.list.ObservableReadOnlyList
import kotlin.jvm.JvmName

// Boolean

@JvmName("propertyBooleanAnd")
infix fun ReadOnlyProperty<Boolean>.and(property: ReadOnlyProperty<Boolean>) =
    join(property, Boolean::and)

@JvmName("propertyBooleanOr")
infix fun ReadOnlyProperty<Boolean>.or(property: ReadOnlyProperty<Boolean>) =
    join(property, Boolean::or)

@JvmName("propertyBooleanXor")
infix fun ReadOnlyProperty<Boolean>.xor(property: ReadOnlyProperty<Boolean>) =
    join(property, Boolean::xor)

@JvmName("propertyBooleanImplies")
infix fun ReadOnlyProperty<Boolean>.implies(property: ReadOnlyProperty<Boolean>) =
    join(property) { a, b -> !a || b }

@JvmName("propertyBooleanNot")
operator fun ReadOnlyProperty<Boolean>.not() =
    mapBinding(Boolean::not)

/* The following part is auto generated. Do NOT edit it manually! */

// Unary minus

@JvmName("propertyIntUnaryMinus")
operator fun ReadOnlyProperty<Int>.unaryMinus() = mapBinding(Int::unaryMinus)

@JvmName("propertyLongUnaryMinus")
operator fun ReadOnlyProperty<Long>.unaryMinus() = mapBinding(Long::unaryMinus)

@JvmName("propertyFloatUnaryMinus")
operator fun ReadOnlyProperty<Float>.unaryMinus() = mapBinding(Float::unaryMinus)

@JvmName("propertyDoubleUnaryMinus")
operator fun ReadOnlyProperty<Double>.unaryMinus() = mapBinding(Double::unaryMinus)

@JvmName("propertyShortUnaryMinus")
operator fun ReadOnlyProperty<Short>.unaryMinus() = mapBinding(Short::unaryMinus)

@JvmName("propertyByteUnaryMinus")
operator fun ReadOnlyProperty<Byte>.unaryMinus() = mapBinding(Byte::unaryMinus)

// List sum

@JvmName("observableListIntSum")
fun ObservableReadOnlyList<Int>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListLongSum")
fun ObservableReadOnlyList<Long>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListFloatSum")
fun ObservableReadOnlyList<Float>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListDoubleSum")
fun ObservableReadOnlyList<Double>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListShortSum")
fun ObservableReadOnlyList<Short>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListByteSum")
fun ObservableReadOnlyList<Byte>.sumObservable() = mapBinding { it.sum() }

// List average

@JvmName("observableListIntAverage")
fun ObservableReadOnlyList<Int>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListLongAverage")
fun ObservableReadOnlyList<Long>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListFloatAverage")
fun ObservableReadOnlyList<Float>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListDoubleAverage")
fun ObservableReadOnlyList<Double>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListShortAverage")
fun ObservableReadOnlyList<Short>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListByteAverage")
fun ObservableReadOnlyList<Byte>.averageObservable() = mapBinding { it.average() }

/*
 * Property - Property
 */

// Int - Int

@JvmName("propertyIntPlusInt")
operator fun ReadOnlyProperty<Int>.plus(property: ReadOnlyProperty<Int>) = join(property, Int::plus)

@JvmName("propertyIntMinusInt")
operator fun ReadOnlyProperty<Int>.minus(property: ReadOnlyProperty<Int>) = join(property, Int::minus)

@JvmName("propertyIntTimesInt")
operator fun ReadOnlyProperty<Int>.times(property: ReadOnlyProperty<Int>) = join(property, Int::times)

@JvmName("propertyIntDivInt")
operator fun ReadOnlyProperty<Int>.div(property: ReadOnlyProperty<Int>) = join(property, Int::div)

@JvmName("propertyIntRemInt")
operator fun ReadOnlyProperty<Int>.rem(property: ReadOnlyProperty<Int>) = join(property, Int::rem)

// Int - Long

@JvmName("propertyIntPlusLong")
operator fun ReadOnlyProperty<Int>.plus(property: ReadOnlyProperty<Long>) = join(property, Int::plus)

@JvmName("propertyIntMinusLong")
operator fun ReadOnlyProperty<Int>.minus(property: ReadOnlyProperty<Long>) = join(property, Int::minus)

@JvmName("propertyIntTimesLong")
operator fun ReadOnlyProperty<Int>.times(property: ReadOnlyProperty<Long>) = join(property, Int::times)

@JvmName("propertyIntDivLong")
operator fun ReadOnlyProperty<Int>.div(property: ReadOnlyProperty<Long>) = join(property, Int::div)

@JvmName("propertyIntRemLong")
operator fun ReadOnlyProperty<Int>.rem(property: ReadOnlyProperty<Long>) = join(property, Int::rem)

// Int - Float

@JvmName("propertyIntPlusFloat")
operator fun ReadOnlyProperty<Int>.plus(property: ReadOnlyProperty<Float>) = join(property, Int::plus)

@JvmName("propertyIntMinusFloat")
operator fun ReadOnlyProperty<Int>.minus(property: ReadOnlyProperty<Float>) = join(property, Int::minus)

@JvmName("propertyIntTimesFloat")
operator fun ReadOnlyProperty<Int>.times(property: ReadOnlyProperty<Float>) = join(property, Int::times)

@JvmName("propertyIntDivFloat")
operator fun ReadOnlyProperty<Int>.div(property: ReadOnlyProperty<Float>) = join(property, Int::div)

@JvmName("propertyIntRemFloat")
operator fun ReadOnlyProperty<Int>.rem(property: ReadOnlyProperty<Float>) = join(property, Int::rem)

// Int - Double

@JvmName("propertyIntPlusDouble")
operator fun ReadOnlyProperty<Int>.plus(property: ReadOnlyProperty<Double>) = join(property, Int::plus)

@JvmName("propertyIntMinusDouble")
operator fun ReadOnlyProperty<Int>.minus(property: ReadOnlyProperty<Double>) = join(property, Int::minus)

@JvmName("propertyIntTimesDouble")
operator fun ReadOnlyProperty<Int>.times(property: ReadOnlyProperty<Double>) = join(property, Int::times)

@JvmName("propertyIntDivDouble")
operator fun ReadOnlyProperty<Int>.div(property: ReadOnlyProperty<Double>) = join(property, Int::div)

@JvmName("propertyIntRemDouble")
operator fun ReadOnlyProperty<Int>.rem(property: ReadOnlyProperty<Double>) = join(property, Int::rem)

// Int - Short

@JvmName("propertyIntPlusShort")
operator fun ReadOnlyProperty<Int>.plus(property: ReadOnlyProperty<Short>) = join(property, Int::plus)

@JvmName("propertyIntMinusShort")
operator fun ReadOnlyProperty<Int>.minus(property: ReadOnlyProperty<Short>) = join(property, Int::minus)

@JvmName("propertyIntTimesShort")
operator fun ReadOnlyProperty<Int>.times(property: ReadOnlyProperty<Short>) = join(property, Int::times)

@JvmName("propertyIntDivShort")
operator fun ReadOnlyProperty<Int>.div(property: ReadOnlyProperty<Short>) = join(property, Int::div)

@JvmName("propertyIntRemShort")
operator fun ReadOnlyProperty<Int>.rem(property: ReadOnlyProperty<Short>) = join(property, Int::rem)

// Int - Byte

@JvmName("propertyIntPlusByte")
operator fun ReadOnlyProperty<Int>.plus(property: ReadOnlyProperty<Byte>) = join(property, Int::plus)

@JvmName("propertyIntMinusByte")
operator fun ReadOnlyProperty<Int>.minus(property: ReadOnlyProperty<Byte>) = join(property, Int::minus)

@JvmName("propertyIntTimesByte")
operator fun ReadOnlyProperty<Int>.times(property: ReadOnlyProperty<Byte>) = join(property, Int::times)

@JvmName("propertyIntDivByte")
operator fun ReadOnlyProperty<Int>.div(property: ReadOnlyProperty<Byte>) = join(property, Int::div)

@JvmName("propertyIntRemByte")
operator fun ReadOnlyProperty<Int>.rem(property: ReadOnlyProperty<Byte>) = join(property, Int::rem)

// Long - Int

@JvmName("propertyLongPlusInt")
operator fun ReadOnlyProperty<Long>.plus(property: ReadOnlyProperty<Int>) = join(property, Long::plus)

@JvmName("propertyLongMinusInt")
operator fun ReadOnlyProperty<Long>.minus(property: ReadOnlyProperty<Int>) = join(property, Long::minus)

@JvmName("propertyLongTimesInt")
operator fun ReadOnlyProperty<Long>.times(property: ReadOnlyProperty<Int>) = join(property, Long::times)

@JvmName("propertyLongDivInt")
operator fun ReadOnlyProperty<Long>.div(property: ReadOnlyProperty<Int>) = join(property, Long::div)

@JvmName("propertyLongRemInt")
operator fun ReadOnlyProperty<Long>.rem(property: ReadOnlyProperty<Int>) = join(property, Long::rem)

// Long - Long

@JvmName("propertyLongPlusLong")
operator fun ReadOnlyProperty<Long>.plus(property: ReadOnlyProperty<Long>) = join(property, Long::plus)

@JvmName("propertyLongMinusLong")
operator fun ReadOnlyProperty<Long>.minus(property: ReadOnlyProperty<Long>) = join(property, Long::minus)

@JvmName("propertyLongTimesLong")
operator fun ReadOnlyProperty<Long>.times(property: ReadOnlyProperty<Long>) = join(property, Long::times)

@JvmName("propertyLongDivLong")
operator fun ReadOnlyProperty<Long>.div(property: ReadOnlyProperty<Long>) = join(property, Long::div)

@JvmName("propertyLongRemLong")
operator fun ReadOnlyProperty<Long>.rem(property: ReadOnlyProperty<Long>) = join(property, Long::rem)

// Long - Float

@JvmName("propertyLongPlusFloat")
operator fun ReadOnlyProperty<Long>.plus(property: ReadOnlyProperty<Float>) = join(property, Long::plus)

@JvmName("propertyLongMinusFloat")
operator fun ReadOnlyProperty<Long>.minus(property: ReadOnlyProperty<Float>) = join(property, Long::minus)

@JvmName("propertyLongTimesFloat")
operator fun ReadOnlyProperty<Long>.times(property: ReadOnlyProperty<Float>) = join(property, Long::times)

@JvmName("propertyLongDivFloat")
operator fun ReadOnlyProperty<Long>.div(property: ReadOnlyProperty<Float>) = join(property, Long::div)

@JvmName("propertyLongRemFloat")
operator fun ReadOnlyProperty<Long>.rem(property: ReadOnlyProperty<Float>) = join(property, Long::rem)

// Long - Double

@JvmName("propertyLongPlusDouble")
operator fun ReadOnlyProperty<Long>.plus(property: ReadOnlyProperty<Double>) = join(property, Long::plus)

@JvmName("propertyLongMinusDouble")
operator fun ReadOnlyProperty<Long>.minus(property: ReadOnlyProperty<Double>) = join(property, Long::minus)

@JvmName("propertyLongTimesDouble")
operator fun ReadOnlyProperty<Long>.times(property: ReadOnlyProperty<Double>) = join(property, Long::times)

@JvmName("propertyLongDivDouble")
operator fun ReadOnlyProperty<Long>.div(property: ReadOnlyProperty<Double>) = join(property, Long::div)

@JvmName("propertyLongRemDouble")
operator fun ReadOnlyProperty<Long>.rem(property: ReadOnlyProperty<Double>) = join(property, Long::rem)

// Long - Short

@JvmName("propertyLongPlusShort")
operator fun ReadOnlyProperty<Long>.plus(property: ReadOnlyProperty<Short>) = join(property, Long::plus)

@JvmName("propertyLongMinusShort")
operator fun ReadOnlyProperty<Long>.minus(property: ReadOnlyProperty<Short>) = join(property, Long::minus)

@JvmName("propertyLongTimesShort")
operator fun ReadOnlyProperty<Long>.times(property: ReadOnlyProperty<Short>) = join(property, Long::times)

@JvmName("propertyLongDivShort")
operator fun ReadOnlyProperty<Long>.div(property: ReadOnlyProperty<Short>) = join(property, Long::div)

@JvmName("propertyLongRemShort")
operator fun ReadOnlyProperty<Long>.rem(property: ReadOnlyProperty<Short>) = join(property, Long::rem)

// Long - Byte

@JvmName("propertyLongPlusByte")
operator fun ReadOnlyProperty<Long>.plus(property: ReadOnlyProperty<Byte>) = join(property, Long::plus)

@JvmName("propertyLongMinusByte")
operator fun ReadOnlyProperty<Long>.minus(property: ReadOnlyProperty<Byte>) = join(property, Long::minus)

@JvmName("propertyLongTimesByte")
operator fun ReadOnlyProperty<Long>.times(property: ReadOnlyProperty<Byte>) = join(property, Long::times)

@JvmName("propertyLongDivByte")
operator fun ReadOnlyProperty<Long>.div(property: ReadOnlyProperty<Byte>) = join(property, Long::div)

@JvmName("propertyLongRemByte")
operator fun ReadOnlyProperty<Long>.rem(property: ReadOnlyProperty<Byte>) = join(property, Long::rem)

// Float - Int

@JvmName("propertyFloatPlusInt")
operator fun ReadOnlyProperty<Float>.plus(property: ReadOnlyProperty<Int>) = join(property, Float::plus)

@JvmName("propertyFloatMinusInt")
operator fun ReadOnlyProperty<Float>.minus(property: ReadOnlyProperty<Int>) = join(property, Float::minus)

@JvmName("propertyFloatTimesInt")
operator fun ReadOnlyProperty<Float>.times(property: ReadOnlyProperty<Int>) = join(property, Float::times)

@JvmName("propertyFloatDivInt")
operator fun ReadOnlyProperty<Float>.div(property: ReadOnlyProperty<Int>) = join(property, Float::div)

@JvmName("propertyFloatRemInt")
operator fun ReadOnlyProperty<Float>.rem(property: ReadOnlyProperty<Int>) = join(property, Float::rem)

// Float - Long

@JvmName("propertyFloatPlusLong")
operator fun ReadOnlyProperty<Float>.plus(property: ReadOnlyProperty<Long>) = join(property, Float::plus)

@JvmName("propertyFloatMinusLong")
operator fun ReadOnlyProperty<Float>.minus(property: ReadOnlyProperty<Long>) = join(property, Float::minus)

@JvmName("propertyFloatTimesLong")
operator fun ReadOnlyProperty<Float>.times(property: ReadOnlyProperty<Long>) = join(property, Float::times)

@JvmName("propertyFloatDivLong")
operator fun ReadOnlyProperty<Float>.div(property: ReadOnlyProperty<Long>) = join(property, Float::div)

@JvmName("propertyFloatRemLong")
operator fun ReadOnlyProperty<Float>.rem(property: ReadOnlyProperty<Long>) = join(property, Float::rem)

// Float - Float

@JvmName("propertyFloatPlusFloat")
operator fun ReadOnlyProperty<Float>.plus(property: ReadOnlyProperty<Float>) = join(property, Float::plus)

@JvmName("propertyFloatMinusFloat")
operator fun ReadOnlyProperty<Float>.minus(property: ReadOnlyProperty<Float>) = join(property, Float::minus)

@JvmName("propertyFloatTimesFloat")
operator fun ReadOnlyProperty<Float>.times(property: ReadOnlyProperty<Float>) = join(property, Float::times)

@JvmName("propertyFloatDivFloat")
operator fun ReadOnlyProperty<Float>.div(property: ReadOnlyProperty<Float>) = join(property, Float::div)

@JvmName("propertyFloatRemFloat")
operator fun ReadOnlyProperty<Float>.rem(property: ReadOnlyProperty<Float>) = join(property, Float::rem)

// Float - Double

@JvmName("propertyFloatPlusDouble")
operator fun ReadOnlyProperty<Float>.plus(property: ReadOnlyProperty<Double>) = join(property, Float::plus)

@JvmName("propertyFloatMinusDouble")
operator fun ReadOnlyProperty<Float>.minus(property: ReadOnlyProperty<Double>) = join(property, Float::minus)

@JvmName("propertyFloatTimesDouble")
operator fun ReadOnlyProperty<Float>.times(property: ReadOnlyProperty<Double>) = join(property, Float::times)

@JvmName("propertyFloatDivDouble")
operator fun ReadOnlyProperty<Float>.div(property: ReadOnlyProperty<Double>) = join(property, Float::div)

@JvmName("propertyFloatRemDouble")
operator fun ReadOnlyProperty<Float>.rem(property: ReadOnlyProperty<Double>) = join(property, Float::rem)

// Float - Short

@JvmName("propertyFloatPlusShort")
operator fun ReadOnlyProperty<Float>.plus(property: ReadOnlyProperty<Short>) = join(property, Float::plus)

@JvmName("propertyFloatMinusShort")
operator fun ReadOnlyProperty<Float>.minus(property: ReadOnlyProperty<Short>) = join(property, Float::minus)

@JvmName("propertyFloatTimesShort")
operator fun ReadOnlyProperty<Float>.times(property: ReadOnlyProperty<Short>) = join(property, Float::times)

@JvmName("propertyFloatDivShort")
operator fun ReadOnlyProperty<Float>.div(property: ReadOnlyProperty<Short>) = join(property, Float::div)

@JvmName("propertyFloatRemShort")
operator fun ReadOnlyProperty<Float>.rem(property: ReadOnlyProperty<Short>) = join(property, Float::rem)

// Float - Byte

@JvmName("propertyFloatPlusByte")
operator fun ReadOnlyProperty<Float>.plus(property: ReadOnlyProperty<Byte>) = join(property, Float::plus)

@JvmName("propertyFloatMinusByte")
operator fun ReadOnlyProperty<Float>.minus(property: ReadOnlyProperty<Byte>) = join(property, Float::minus)

@JvmName("propertyFloatTimesByte")
operator fun ReadOnlyProperty<Float>.times(property: ReadOnlyProperty<Byte>) = join(property, Float::times)

@JvmName("propertyFloatDivByte")
operator fun ReadOnlyProperty<Float>.div(property: ReadOnlyProperty<Byte>) = join(property, Float::div)

@JvmName("propertyFloatRemByte")
operator fun ReadOnlyProperty<Float>.rem(property: ReadOnlyProperty<Byte>) = join(property, Float::rem)

// Double - Int

@JvmName("propertyDoublePlusInt")
operator fun ReadOnlyProperty<Double>.plus(property: ReadOnlyProperty<Int>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusInt")
operator fun ReadOnlyProperty<Double>.minus(property: ReadOnlyProperty<Int>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesInt")
operator fun ReadOnlyProperty<Double>.times(property: ReadOnlyProperty<Int>) = join(property, Double::times)

@JvmName("propertyDoubleDivInt")
operator fun ReadOnlyProperty<Double>.div(property: ReadOnlyProperty<Int>) = join(property, Double::div)

@JvmName("propertyDoubleRemInt")
operator fun ReadOnlyProperty<Double>.rem(property: ReadOnlyProperty<Int>) = join(property, Double::rem)

// Double - Long

@JvmName("propertyDoublePlusLong")
operator fun ReadOnlyProperty<Double>.plus(property: ReadOnlyProperty<Long>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusLong")
operator fun ReadOnlyProperty<Double>.minus(property: ReadOnlyProperty<Long>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesLong")
operator fun ReadOnlyProperty<Double>.times(property: ReadOnlyProperty<Long>) = join(property, Double::times)

@JvmName("propertyDoubleDivLong")
operator fun ReadOnlyProperty<Double>.div(property: ReadOnlyProperty<Long>) = join(property, Double::div)

@JvmName("propertyDoubleRemLong")
operator fun ReadOnlyProperty<Double>.rem(property: ReadOnlyProperty<Long>) = join(property, Double::rem)

// Double - Float

@JvmName("propertyDoublePlusFloat")
operator fun ReadOnlyProperty<Double>.plus(property: ReadOnlyProperty<Float>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusFloat")
operator fun ReadOnlyProperty<Double>.minus(property: ReadOnlyProperty<Float>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesFloat")
operator fun ReadOnlyProperty<Double>.times(property: ReadOnlyProperty<Float>) = join(property, Double::times)

@JvmName("propertyDoubleDivFloat")
operator fun ReadOnlyProperty<Double>.div(property: ReadOnlyProperty<Float>) = join(property, Double::div)

@JvmName("propertyDoubleRemFloat")
operator fun ReadOnlyProperty<Double>.rem(property: ReadOnlyProperty<Float>) = join(property, Double::rem)

// Double - Double

@JvmName("propertyDoublePlusDouble")
operator fun ReadOnlyProperty<Double>.plus(property: ReadOnlyProperty<Double>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusDouble")
operator fun ReadOnlyProperty<Double>.minus(property: ReadOnlyProperty<Double>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesDouble")
operator fun ReadOnlyProperty<Double>.times(property: ReadOnlyProperty<Double>) = join(property, Double::times)

@JvmName("propertyDoubleDivDouble")
operator fun ReadOnlyProperty<Double>.div(property: ReadOnlyProperty<Double>) = join(property, Double::div)

@JvmName("propertyDoubleRemDouble")
operator fun ReadOnlyProperty<Double>.rem(property: ReadOnlyProperty<Double>) = join(property, Double::rem)

// Double - Short

@JvmName("propertyDoublePlusShort")
operator fun ReadOnlyProperty<Double>.plus(property: ReadOnlyProperty<Short>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusShort")
operator fun ReadOnlyProperty<Double>.minus(property: ReadOnlyProperty<Short>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesShort")
operator fun ReadOnlyProperty<Double>.times(property: ReadOnlyProperty<Short>) = join(property, Double::times)

@JvmName("propertyDoubleDivShort")
operator fun ReadOnlyProperty<Double>.div(property: ReadOnlyProperty<Short>) = join(property, Double::div)

@JvmName("propertyDoubleRemShort")
operator fun ReadOnlyProperty<Double>.rem(property: ReadOnlyProperty<Short>) = join(property, Double::rem)

// Double - Byte

@JvmName("propertyDoublePlusByte")
operator fun ReadOnlyProperty<Double>.plus(property: ReadOnlyProperty<Byte>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusByte")
operator fun ReadOnlyProperty<Double>.minus(property: ReadOnlyProperty<Byte>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesByte")
operator fun ReadOnlyProperty<Double>.times(property: ReadOnlyProperty<Byte>) = join(property, Double::times)

@JvmName("propertyDoubleDivByte")
operator fun ReadOnlyProperty<Double>.div(property: ReadOnlyProperty<Byte>) = join(property, Double::div)

@JvmName("propertyDoubleRemByte")
operator fun ReadOnlyProperty<Double>.rem(property: ReadOnlyProperty<Byte>) = join(property, Double::rem)

// Short - Int

@JvmName("propertyShortPlusInt")
operator fun ReadOnlyProperty<Short>.plus(property: ReadOnlyProperty<Int>) = join(property, Short::plus)

@JvmName("propertyShortMinusInt")
operator fun ReadOnlyProperty<Short>.minus(property: ReadOnlyProperty<Int>) = join(property, Short::minus)

@JvmName("propertyShortTimesInt")
operator fun ReadOnlyProperty<Short>.times(property: ReadOnlyProperty<Int>) = join(property, Short::times)

@JvmName("propertyShortDivInt")
operator fun ReadOnlyProperty<Short>.div(property: ReadOnlyProperty<Int>) = join(property, Short::div)

@JvmName("propertyShortRemInt")
operator fun ReadOnlyProperty<Short>.rem(property: ReadOnlyProperty<Int>) = join(property, Short::rem)

// Short - Long

@JvmName("propertyShortPlusLong")
operator fun ReadOnlyProperty<Short>.plus(property: ReadOnlyProperty<Long>) = join(property, Short::plus)

@JvmName("propertyShortMinusLong")
operator fun ReadOnlyProperty<Short>.minus(property: ReadOnlyProperty<Long>) = join(property, Short::minus)

@JvmName("propertyShortTimesLong")
operator fun ReadOnlyProperty<Short>.times(property: ReadOnlyProperty<Long>) = join(property, Short::times)

@JvmName("propertyShortDivLong")
operator fun ReadOnlyProperty<Short>.div(property: ReadOnlyProperty<Long>) = join(property, Short::div)

@JvmName("propertyShortRemLong")
operator fun ReadOnlyProperty<Short>.rem(property: ReadOnlyProperty<Long>) = join(property, Short::rem)

// Short - Float

@JvmName("propertyShortPlusFloat")
operator fun ReadOnlyProperty<Short>.plus(property: ReadOnlyProperty<Float>) = join(property, Short::plus)

@JvmName("propertyShortMinusFloat")
operator fun ReadOnlyProperty<Short>.minus(property: ReadOnlyProperty<Float>) = join(property, Short::minus)

@JvmName("propertyShortTimesFloat")
operator fun ReadOnlyProperty<Short>.times(property: ReadOnlyProperty<Float>) = join(property, Short::times)

@JvmName("propertyShortDivFloat")
operator fun ReadOnlyProperty<Short>.div(property: ReadOnlyProperty<Float>) = join(property, Short::div)

@JvmName("propertyShortRemFloat")
operator fun ReadOnlyProperty<Short>.rem(property: ReadOnlyProperty<Float>) = join(property, Short::rem)

// Short - Double

@JvmName("propertyShortPlusDouble")
operator fun ReadOnlyProperty<Short>.plus(property: ReadOnlyProperty<Double>) = join(property, Short::plus)

@JvmName("propertyShortMinusDouble")
operator fun ReadOnlyProperty<Short>.minus(property: ReadOnlyProperty<Double>) = join(property, Short::minus)

@JvmName("propertyShortTimesDouble")
operator fun ReadOnlyProperty<Short>.times(property: ReadOnlyProperty<Double>) = join(property, Short::times)

@JvmName("propertyShortDivDouble")
operator fun ReadOnlyProperty<Short>.div(property: ReadOnlyProperty<Double>) = join(property, Short::div)

@JvmName("propertyShortRemDouble")
operator fun ReadOnlyProperty<Short>.rem(property: ReadOnlyProperty<Double>) = join(property, Short::rem)

// Short - Short

@JvmName("propertyShortPlusShort")
operator fun ReadOnlyProperty<Short>.plus(property: ReadOnlyProperty<Short>) = join(property, Short::plus)

@JvmName("propertyShortMinusShort")
operator fun ReadOnlyProperty<Short>.minus(property: ReadOnlyProperty<Short>) = join(property, Short::minus)

@JvmName("propertyShortTimesShort")
operator fun ReadOnlyProperty<Short>.times(property: ReadOnlyProperty<Short>) = join(property, Short::times)

@JvmName("propertyShortDivShort")
operator fun ReadOnlyProperty<Short>.div(property: ReadOnlyProperty<Short>) = join(property, Short::div)

@JvmName("propertyShortRemShort")
operator fun ReadOnlyProperty<Short>.rem(property: ReadOnlyProperty<Short>) = join(property, Short::rem)

// Short - Byte

@JvmName("propertyShortPlusByte")
operator fun ReadOnlyProperty<Short>.plus(property: ReadOnlyProperty<Byte>) = join(property, Short::plus)

@JvmName("propertyShortMinusByte")
operator fun ReadOnlyProperty<Short>.minus(property: ReadOnlyProperty<Byte>) = join(property, Short::minus)

@JvmName("propertyShortTimesByte")
operator fun ReadOnlyProperty<Short>.times(property: ReadOnlyProperty<Byte>) = join(property, Short::times)

@JvmName("propertyShortDivByte")
operator fun ReadOnlyProperty<Short>.div(property: ReadOnlyProperty<Byte>) = join(property, Short::div)

@JvmName("propertyShortRemByte")
operator fun ReadOnlyProperty<Short>.rem(property: ReadOnlyProperty<Byte>) = join(property, Short::rem)

// Byte - Int

@JvmName("propertyBytePlusInt")
operator fun ReadOnlyProperty<Byte>.plus(property: ReadOnlyProperty<Int>) = join(property, Byte::plus)

@JvmName("propertyByteMinusInt")
operator fun ReadOnlyProperty<Byte>.minus(property: ReadOnlyProperty<Int>) = join(property, Byte::minus)

@JvmName("propertyByteTimesInt")
operator fun ReadOnlyProperty<Byte>.times(property: ReadOnlyProperty<Int>) = join(property, Byte::times)

@JvmName("propertyByteDivInt")
operator fun ReadOnlyProperty<Byte>.div(property: ReadOnlyProperty<Int>) = join(property, Byte::div)

@JvmName("propertyByteRemInt")
operator fun ReadOnlyProperty<Byte>.rem(property: ReadOnlyProperty<Int>) = join(property, Byte::rem)

// Byte - Long

@JvmName("propertyBytePlusLong")
operator fun ReadOnlyProperty<Byte>.plus(property: ReadOnlyProperty<Long>) = join(property, Byte::plus)

@JvmName("propertyByteMinusLong")
operator fun ReadOnlyProperty<Byte>.minus(property: ReadOnlyProperty<Long>) = join(property, Byte::minus)

@JvmName("propertyByteTimesLong")
operator fun ReadOnlyProperty<Byte>.times(property: ReadOnlyProperty<Long>) = join(property, Byte::times)

@JvmName("propertyByteDivLong")
operator fun ReadOnlyProperty<Byte>.div(property: ReadOnlyProperty<Long>) = join(property, Byte::div)

@JvmName("propertyByteRemLong")
operator fun ReadOnlyProperty<Byte>.rem(property: ReadOnlyProperty<Long>) = join(property, Byte::rem)

// Byte - Float

@JvmName("propertyBytePlusFloat")
operator fun ReadOnlyProperty<Byte>.plus(property: ReadOnlyProperty<Float>) = join(property, Byte::plus)

@JvmName("propertyByteMinusFloat")
operator fun ReadOnlyProperty<Byte>.minus(property: ReadOnlyProperty<Float>) = join(property, Byte::minus)

@JvmName("propertyByteTimesFloat")
operator fun ReadOnlyProperty<Byte>.times(property: ReadOnlyProperty<Float>) = join(property, Byte::times)

@JvmName("propertyByteDivFloat")
operator fun ReadOnlyProperty<Byte>.div(property: ReadOnlyProperty<Float>) = join(property, Byte::div)

@JvmName("propertyByteRemFloat")
operator fun ReadOnlyProperty<Byte>.rem(property: ReadOnlyProperty<Float>) = join(property, Byte::rem)

// Byte - Double

@JvmName("propertyBytePlusDouble")
operator fun ReadOnlyProperty<Byte>.plus(property: ReadOnlyProperty<Double>) = join(property, Byte::plus)

@JvmName("propertyByteMinusDouble")
operator fun ReadOnlyProperty<Byte>.minus(property: ReadOnlyProperty<Double>) = join(property, Byte::minus)

@JvmName("propertyByteTimesDouble")
operator fun ReadOnlyProperty<Byte>.times(property: ReadOnlyProperty<Double>) = join(property, Byte::times)

@JvmName("propertyByteDivDouble")
operator fun ReadOnlyProperty<Byte>.div(property: ReadOnlyProperty<Double>) = join(property, Byte::div)

@JvmName("propertyByteRemDouble")
operator fun ReadOnlyProperty<Byte>.rem(property: ReadOnlyProperty<Double>) = join(property, Byte::rem)

// Byte - Short

@JvmName("propertyBytePlusShort")
operator fun ReadOnlyProperty<Byte>.plus(property: ReadOnlyProperty<Short>) = join(property, Byte::plus)

@JvmName("propertyByteMinusShort")
operator fun ReadOnlyProperty<Byte>.minus(property: ReadOnlyProperty<Short>) = join(property, Byte::minus)

@JvmName("propertyByteTimesShort")
operator fun ReadOnlyProperty<Byte>.times(property: ReadOnlyProperty<Short>) = join(property, Byte::times)

@JvmName("propertyByteDivShort")
operator fun ReadOnlyProperty<Byte>.div(property: ReadOnlyProperty<Short>) = join(property, Byte::div)

@JvmName("propertyByteRemShort")
operator fun ReadOnlyProperty<Byte>.rem(property: ReadOnlyProperty<Short>) = join(property, Byte::rem)

// Byte - Byte

@JvmName("propertyBytePlusByte")
operator fun ReadOnlyProperty<Byte>.plus(property: ReadOnlyProperty<Byte>) = join(property, Byte::plus)

@JvmName("propertyByteMinusByte")
operator fun ReadOnlyProperty<Byte>.minus(property: ReadOnlyProperty<Byte>) = join(property, Byte::minus)

@JvmName("propertyByteTimesByte")
operator fun ReadOnlyProperty<Byte>.times(property: ReadOnlyProperty<Byte>) = join(property, Byte::times)

@JvmName("propertyByteDivByte")
operator fun ReadOnlyProperty<Byte>.div(property: ReadOnlyProperty<Byte>) = join(property, Byte::div)

@JvmName("propertyByteRemByte")
operator fun ReadOnlyProperty<Byte>.rem(property: ReadOnlyProperty<Byte>) = join(property, Byte::rem)

/*
 * Property - primitive
 */

// Int - Int

@JvmName("propertyIntPlusInt")
operator fun ReadOnlyProperty<Int>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyIntMinusInt")
operator fun ReadOnlyProperty<Int>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyIntTimesInt")
operator fun ReadOnlyProperty<Int>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyIntDivInt")
operator fun ReadOnlyProperty<Int>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyIntRemInt")
operator fun ReadOnlyProperty<Int>.rem(value: Int) = mapBinding { it % value }

// Int - Long

@JvmName("propertyIntPlusLong")
operator fun ReadOnlyProperty<Int>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyIntMinusLong")
operator fun ReadOnlyProperty<Int>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyIntTimesLong")
operator fun ReadOnlyProperty<Int>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyIntDivLong")
operator fun ReadOnlyProperty<Int>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyIntRemLong")
operator fun ReadOnlyProperty<Int>.rem(value: Long) = mapBinding { it % value }

// Int - Float

@JvmName("propertyIntPlusFloat")
operator fun ReadOnlyProperty<Int>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyIntMinusFloat")
operator fun ReadOnlyProperty<Int>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyIntTimesFloat")
operator fun ReadOnlyProperty<Int>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyIntDivFloat")
operator fun ReadOnlyProperty<Int>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyIntRemFloat")
operator fun ReadOnlyProperty<Int>.rem(value: Float) = mapBinding { it % value }

// Int - Double

@JvmName("propertyIntPlusDouble")
operator fun ReadOnlyProperty<Int>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyIntMinusDouble")
operator fun ReadOnlyProperty<Int>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyIntTimesDouble")
operator fun ReadOnlyProperty<Int>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyIntDivDouble")
operator fun ReadOnlyProperty<Int>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyIntRemDouble")
operator fun ReadOnlyProperty<Int>.rem(value: Double) = mapBinding { it % value }

// Int - Short

@JvmName("propertyIntPlusShort")
operator fun ReadOnlyProperty<Int>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyIntMinusShort")
operator fun ReadOnlyProperty<Int>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyIntTimesShort")
operator fun ReadOnlyProperty<Int>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyIntDivShort")
operator fun ReadOnlyProperty<Int>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyIntRemShort")
operator fun ReadOnlyProperty<Int>.rem(value: Short) = mapBinding { it % value }

// Int - Byte

@JvmName("propertyIntPlusByte")
operator fun ReadOnlyProperty<Int>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyIntMinusByte")
operator fun ReadOnlyProperty<Int>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyIntTimesByte")
operator fun ReadOnlyProperty<Int>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyIntDivByte")
operator fun ReadOnlyProperty<Int>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyIntRemByte")
operator fun ReadOnlyProperty<Int>.rem(value: Byte) = mapBinding { it % value }

// Long - Int

@JvmName("propertyLongPlusInt")
operator fun ReadOnlyProperty<Long>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyLongMinusInt")
operator fun ReadOnlyProperty<Long>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyLongTimesInt")
operator fun ReadOnlyProperty<Long>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyLongDivInt")
operator fun ReadOnlyProperty<Long>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyLongRemInt")
operator fun ReadOnlyProperty<Long>.rem(value: Int) = mapBinding { it % value }

// Long - Long

@JvmName("propertyLongPlusLong")
operator fun ReadOnlyProperty<Long>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyLongMinusLong")
operator fun ReadOnlyProperty<Long>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyLongTimesLong")
operator fun ReadOnlyProperty<Long>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyLongDivLong")
operator fun ReadOnlyProperty<Long>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyLongRemLong")
operator fun ReadOnlyProperty<Long>.rem(value: Long) = mapBinding { it % value }

// Long - Float

@JvmName("propertyLongPlusFloat")
operator fun ReadOnlyProperty<Long>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyLongMinusFloat")
operator fun ReadOnlyProperty<Long>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyLongTimesFloat")
operator fun ReadOnlyProperty<Long>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyLongDivFloat")
operator fun ReadOnlyProperty<Long>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyLongRemFloat")
operator fun ReadOnlyProperty<Long>.rem(value: Float) = mapBinding { it % value }

// Long - Double

@JvmName("propertyLongPlusDouble")
operator fun ReadOnlyProperty<Long>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyLongMinusDouble")
operator fun ReadOnlyProperty<Long>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyLongTimesDouble")
operator fun ReadOnlyProperty<Long>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyLongDivDouble")
operator fun ReadOnlyProperty<Long>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyLongRemDouble")
operator fun ReadOnlyProperty<Long>.rem(value: Double) = mapBinding { it % value }

// Long - Short

@JvmName("propertyLongPlusShort")
operator fun ReadOnlyProperty<Long>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyLongMinusShort")
operator fun ReadOnlyProperty<Long>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyLongTimesShort")
operator fun ReadOnlyProperty<Long>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyLongDivShort")
operator fun ReadOnlyProperty<Long>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyLongRemShort")
operator fun ReadOnlyProperty<Long>.rem(value: Short) = mapBinding { it % value }

// Long - Byte

@JvmName("propertyLongPlusByte")
operator fun ReadOnlyProperty<Long>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyLongMinusByte")
operator fun ReadOnlyProperty<Long>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyLongTimesByte")
operator fun ReadOnlyProperty<Long>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyLongDivByte")
operator fun ReadOnlyProperty<Long>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyLongRemByte")
operator fun ReadOnlyProperty<Long>.rem(value: Byte) = mapBinding { it % value }

// Float - Int

@JvmName("propertyFloatPlusInt")
operator fun ReadOnlyProperty<Float>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyFloatMinusInt")
operator fun ReadOnlyProperty<Float>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyFloatTimesInt")
operator fun ReadOnlyProperty<Float>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyFloatDivInt")
operator fun ReadOnlyProperty<Float>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyFloatRemInt")
operator fun ReadOnlyProperty<Float>.rem(value: Int) = mapBinding { it % value }

// Float - Long

@JvmName("propertyFloatPlusLong")
operator fun ReadOnlyProperty<Float>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyFloatMinusLong")
operator fun ReadOnlyProperty<Float>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyFloatTimesLong")
operator fun ReadOnlyProperty<Float>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyFloatDivLong")
operator fun ReadOnlyProperty<Float>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyFloatRemLong")
operator fun ReadOnlyProperty<Float>.rem(value: Long) = mapBinding { it % value }

// Float - Float

@JvmName("propertyFloatPlusFloat")
operator fun ReadOnlyProperty<Float>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyFloatMinusFloat")
operator fun ReadOnlyProperty<Float>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyFloatTimesFloat")
operator fun ReadOnlyProperty<Float>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyFloatDivFloat")
operator fun ReadOnlyProperty<Float>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyFloatRemFloat")
operator fun ReadOnlyProperty<Float>.rem(value: Float) = mapBinding { it % value }

// Float - Double

@JvmName("propertyFloatPlusDouble")
operator fun ReadOnlyProperty<Float>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyFloatMinusDouble")
operator fun ReadOnlyProperty<Float>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyFloatTimesDouble")
operator fun ReadOnlyProperty<Float>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyFloatDivDouble")
operator fun ReadOnlyProperty<Float>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyFloatRemDouble")
operator fun ReadOnlyProperty<Float>.rem(value: Double) = mapBinding { it % value }

// Float - Short

@JvmName("propertyFloatPlusShort")
operator fun ReadOnlyProperty<Float>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyFloatMinusShort")
operator fun ReadOnlyProperty<Float>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyFloatTimesShort")
operator fun ReadOnlyProperty<Float>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyFloatDivShort")
operator fun ReadOnlyProperty<Float>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyFloatRemShort")
operator fun ReadOnlyProperty<Float>.rem(value: Short) = mapBinding { it % value }

// Float - Byte

@JvmName("propertyFloatPlusByte")
operator fun ReadOnlyProperty<Float>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyFloatMinusByte")
operator fun ReadOnlyProperty<Float>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyFloatTimesByte")
operator fun ReadOnlyProperty<Float>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyFloatDivByte")
operator fun ReadOnlyProperty<Float>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyFloatRemByte")
operator fun ReadOnlyProperty<Float>.rem(value: Byte) = mapBinding { it % value }

// Double - Int

@JvmName("propertyDoublePlusInt")
operator fun ReadOnlyProperty<Double>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyDoubleMinusInt")
operator fun ReadOnlyProperty<Double>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyDoubleTimesInt")
operator fun ReadOnlyProperty<Double>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyDoubleDivInt")
operator fun ReadOnlyProperty<Double>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyDoubleRemInt")
operator fun ReadOnlyProperty<Double>.rem(value: Int) = mapBinding { it % value }

// Double - Long

@JvmName("propertyDoublePlusLong")
operator fun ReadOnlyProperty<Double>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyDoubleMinusLong")
operator fun ReadOnlyProperty<Double>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyDoubleTimesLong")
operator fun ReadOnlyProperty<Double>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyDoubleDivLong")
operator fun ReadOnlyProperty<Double>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyDoubleRemLong")
operator fun ReadOnlyProperty<Double>.rem(value: Long) = mapBinding { it % value }

// Double - Float

@JvmName("propertyDoublePlusFloat")
operator fun ReadOnlyProperty<Double>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyDoubleMinusFloat")
operator fun ReadOnlyProperty<Double>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyDoubleTimesFloat")
operator fun ReadOnlyProperty<Double>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyDoubleDivFloat")
operator fun ReadOnlyProperty<Double>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyDoubleRemFloat")
operator fun ReadOnlyProperty<Double>.rem(value: Float) = mapBinding { it % value }

// Double - Double

@JvmName("propertyDoublePlusDouble")
operator fun ReadOnlyProperty<Double>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyDoubleMinusDouble")
operator fun ReadOnlyProperty<Double>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyDoubleTimesDouble")
operator fun ReadOnlyProperty<Double>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyDoubleDivDouble")
operator fun ReadOnlyProperty<Double>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyDoubleRemDouble")
operator fun ReadOnlyProperty<Double>.rem(value: Double) = mapBinding { it % value }

// Double - Short

@JvmName("propertyDoublePlusShort")
operator fun ReadOnlyProperty<Double>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyDoubleMinusShort")
operator fun ReadOnlyProperty<Double>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyDoubleTimesShort")
operator fun ReadOnlyProperty<Double>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyDoubleDivShort")
operator fun ReadOnlyProperty<Double>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyDoubleRemShort")
operator fun ReadOnlyProperty<Double>.rem(value: Short) = mapBinding { it % value }

// Double - Byte

@JvmName("propertyDoublePlusByte")
operator fun ReadOnlyProperty<Double>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyDoubleMinusByte")
operator fun ReadOnlyProperty<Double>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyDoubleTimesByte")
operator fun ReadOnlyProperty<Double>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyDoubleDivByte")
operator fun ReadOnlyProperty<Double>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyDoubleRemByte")
operator fun ReadOnlyProperty<Double>.rem(value: Byte) = mapBinding { it % value }

// Short - Int

@JvmName("propertyShortPlusInt")
operator fun ReadOnlyProperty<Short>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyShortMinusInt")
operator fun ReadOnlyProperty<Short>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyShortTimesInt")
operator fun ReadOnlyProperty<Short>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyShortDivInt")
operator fun ReadOnlyProperty<Short>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyShortRemInt")
operator fun ReadOnlyProperty<Short>.rem(value: Int) = mapBinding { it % value }

// Short - Long

@JvmName("propertyShortPlusLong")
operator fun ReadOnlyProperty<Short>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyShortMinusLong")
operator fun ReadOnlyProperty<Short>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyShortTimesLong")
operator fun ReadOnlyProperty<Short>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyShortDivLong")
operator fun ReadOnlyProperty<Short>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyShortRemLong")
operator fun ReadOnlyProperty<Short>.rem(value: Long) = mapBinding { it % value }

// Short - Float

@JvmName("propertyShortPlusFloat")
operator fun ReadOnlyProperty<Short>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyShortMinusFloat")
operator fun ReadOnlyProperty<Short>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyShortTimesFloat")
operator fun ReadOnlyProperty<Short>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyShortDivFloat")
operator fun ReadOnlyProperty<Short>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyShortRemFloat")
operator fun ReadOnlyProperty<Short>.rem(value: Float) = mapBinding { it % value }

// Short - Double

@JvmName("propertyShortPlusDouble")
operator fun ReadOnlyProperty<Short>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyShortMinusDouble")
operator fun ReadOnlyProperty<Short>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyShortTimesDouble")
operator fun ReadOnlyProperty<Short>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyShortDivDouble")
operator fun ReadOnlyProperty<Short>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyShortRemDouble")
operator fun ReadOnlyProperty<Short>.rem(value: Double) = mapBinding { it % value }

// Short - Short

@JvmName("propertyShortPlusShort")
operator fun ReadOnlyProperty<Short>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyShortMinusShort")
operator fun ReadOnlyProperty<Short>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyShortTimesShort")
operator fun ReadOnlyProperty<Short>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyShortDivShort")
operator fun ReadOnlyProperty<Short>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyShortRemShort")
operator fun ReadOnlyProperty<Short>.rem(value: Short) = mapBinding { it % value }

// Short - Byte

@JvmName("propertyShortPlusByte")
operator fun ReadOnlyProperty<Short>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyShortMinusByte")
operator fun ReadOnlyProperty<Short>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyShortTimesByte")
operator fun ReadOnlyProperty<Short>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyShortDivByte")
operator fun ReadOnlyProperty<Short>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyShortRemByte")
operator fun ReadOnlyProperty<Short>.rem(value: Byte) = mapBinding { it % value }

// Byte - Int

@JvmName("propertyBytePlusInt")
operator fun ReadOnlyProperty<Byte>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyByteMinusInt")
operator fun ReadOnlyProperty<Byte>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyByteTimesInt")
operator fun ReadOnlyProperty<Byte>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyByteDivInt")
operator fun ReadOnlyProperty<Byte>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyByteRemInt")
operator fun ReadOnlyProperty<Byte>.rem(value: Int) = mapBinding { it % value }

// Byte - Long

@JvmName("propertyBytePlusLong")
operator fun ReadOnlyProperty<Byte>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyByteMinusLong")
operator fun ReadOnlyProperty<Byte>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyByteTimesLong")
operator fun ReadOnlyProperty<Byte>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyByteDivLong")
operator fun ReadOnlyProperty<Byte>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyByteRemLong")
operator fun ReadOnlyProperty<Byte>.rem(value: Long) = mapBinding { it % value }

// Byte - Float

@JvmName("propertyBytePlusFloat")
operator fun ReadOnlyProperty<Byte>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyByteMinusFloat")
operator fun ReadOnlyProperty<Byte>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyByteTimesFloat")
operator fun ReadOnlyProperty<Byte>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyByteDivFloat")
operator fun ReadOnlyProperty<Byte>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyByteRemFloat")
operator fun ReadOnlyProperty<Byte>.rem(value: Float) = mapBinding { it % value }

// Byte - Double

@JvmName("propertyBytePlusDouble")
operator fun ReadOnlyProperty<Byte>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyByteMinusDouble")
operator fun ReadOnlyProperty<Byte>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyByteTimesDouble")
operator fun ReadOnlyProperty<Byte>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyByteDivDouble")
operator fun ReadOnlyProperty<Byte>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyByteRemDouble")
operator fun ReadOnlyProperty<Byte>.rem(value: Double) = mapBinding { it % value }

// Byte - Short

@JvmName("propertyBytePlusShort")
operator fun ReadOnlyProperty<Byte>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyByteMinusShort")
operator fun ReadOnlyProperty<Byte>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyByteTimesShort")
operator fun ReadOnlyProperty<Byte>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyByteDivShort")
operator fun ReadOnlyProperty<Byte>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyByteRemShort")
operator fun ReadOnlyProperty<Byte>.rem(value: Short) = mapBinding { it % value }

// Byte - Byte

@JvmName("propertyBytePlusByte")
operator fun ReadOnlyProperty<Byte>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyByteMinusByte")
operator fun ReadOnlyProperty<Byte>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyByteTimesByte")
operator fun ReadOnlyProperty<Byte>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyByteDivByte")
operator fun ReadOnlyProperty<Byte>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyByteRemByte")
operator fun ReadOnlyProperty<Byte>.rem(value: Byte) = mapBinding { it % value }

/*
 * primitive - Property
 */

// Int - Int

@JvmName("propertyIntPlusInt")
operator fun Int.plus(property: ReadOnlyProperty<Int>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusInt")
operator fun Int.minus(property: ReadOnlyProperty<Int>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesInt")
operator fun Int.times(property: ReadOnlyProperty<Int>) = property.mapBinding { this * it }

@JvmName("propertyIntDivInt")
operator fun Int.div(property: ReadOnlyProperty<Int>) = property.mapBinding { this / it }

@JvmName("propertyIntRemInt")
operator fun Int.rem(property: ReadOnlyProperty<Int>) = property.mapBinding { this % it }

// Int - Long

@JvmName("propertyIntPlusLong")
operator fun Int.plus(property: ReadOnlyProperty<Long>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusLong")
operator fun Int.minus(property: ReadOnlyProperty<Long>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesLong")
operator fun Int.times(property: ReadOnlyProperty<Long>) = property.mapBinding { this * it }

@JvmName("propertyIntDivLong")
operator fun Int.div(property: ReadOnlyProperty<Long>) = property.mapBinding { this / it }

@JvmName("propertyIntRemLong")
operator fun Int.rem(property: ReadOnlyProperty<Long>) = property.mapBinding { this % it }

// Int - Float

@JvmName("propertyIntPlusFloat")
operator fun Int.plus(property: ReadOnlyProperty<Float>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusFloat")
operator fun Int.minus(property: ReadOnlyProperty<Float>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesFloat")
operator fun Int.times(property: ReadOnlyProperty<Float>) = property.mapBinding { this * it }

@JvmName("propertyIntDivFloat")
operator fun Int.div(property: ReadOnlyProperty<Float>) = property.mapBinding { this / it }

@JvmName("propertyIntRemFloat")
operator fun Int.rem(property: ReadOnlyProperty<Float>) = property.mapBinding { this % it }

// Int - Double

@JvmName("propertyIntPlusDouble")
operator fun Int.plus(property: ReadOnlyProperty<Double>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusDouble")
operator fun Int.minus(property: ReadOnlyProperty<Double>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesDouble")
operator fun Int.times(property: ReadOnlyProperty<Double>) = property.mapBinding { this * it }

@JvmName("propertyIntDivDouble")
operator fun Int.div(property: ReadOnlyProperty<Double>) = property.mapBinding { this / it }

@JvmName("propertyIntRemDouble")
operator fun Int.rem(property: ReadOnlyProperty<Double>) = property.mapBinding { this % it }

// Int - Short

@JvmName("propertyIntPlusShort")
operator fun Int.plus(property: ReadOnlyProperty<Short>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusShort")
operator fun Int.minus(property: ReadOnlyProperty<Short>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesShort")
operator fun Int.times(property: ReadOnlyProperty<Short>) = property.mapBinding { this * it }

@JvmName("propertyIntDivShort")
operator fun Int.div(property: ReadOnlyProperty<Short>) = property.mapBinding { this / it }

@JvmName("propertyIntRemShort")
operator fun Int.rem(property: ReadOnlyProperty<Short>) = property.mapBinding { this % it }

// Int - Byte

@JvmName("propertyIntPlusByte")
operator fun Int.plus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusByte")
operator fun Int.minus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesByte")
operator fun Int.times(property: ReadOnlyProperty<Byte>) = property.mapBinding { this * it }

@JvmName("propertyIntDivByte")
operator fun Int.div(property: ReadOnlyProperty<Byte>) = property.mapBinding { this / it }

@JvmName("propertyIntRemByte")
operator fun Int.rem(property: ReadOnlyProperty<Byte>) = property.mapBinding { this % it }

// Long - Int

@JvmName("propertyLongPlusInt")
operator fun Long.plus(property: ReadOnlyProperty<Int>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusInt")
operator fun Long.minus(property: ReadOnlyProperty<Int>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesInt")
operator fun Long.times(property: ReadOnlyProperty<Int>) = property.mapBinding { this * it }

@JvmName("propertyLongDivInt")
operator fun Long.div(property: ReadOnlyProperty<Int>) = property.mapBinding { this / it }

@JvmName("propertyLongRemInt")
operator fun Long.rem(property: ReadOnlyProperty<Int>) = property.mapBinding { this % it }

// Long - Long

@JvmName("propertyLongPlusLong")
operator fun Long.plus(property: ReadOnlyProperty<Long>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusLong")
operator fun Long.minus(property: ReadOnlyProperty<Long>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesLong")
operator fun Long.times(property: ReadOnlyProperty<Long>) = property.mapBinding { this * it }

@JvmName("propertyLongDivLong")
operator fun Long.div(property: ReadOnlyProperty<Long>) = property.mapBinding { this / it }

@JvmName("propertyLongRemLong")
operator fun Long.rem(property: ReadOnlyProperty<Long>) = property.mapBinding { this % it }

// Long - Float

@JvmName("propertyLongPlusFloat")
operator fun Long.plus(property: ReadOnlyProperty<Float>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusFloat")
operator fun Long.minus(property: ReadOnlyProperty<Float>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesFloat")
operator fun Long.times(property: ReadOnlyProperty<Float>) = property.mapBinding { this * it }

@JvmName("propertyLongDivFloat")
operator fun Long.div(property: ReadOnlyProperty<Float>) = property.mapBinding { this / it }

@JvmName("propertyLongRemFloat")
operator fun Long.rem(property: ReadOnlyProperty<Float>) = property.mapBinding { this % it }

// Long - Double

@JvmName("propertyLongPlusDouble")
operator fun Long.plus(property: ReadOnlyProperty<Double>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusDouble")
operator fun Long.minus(property: ReadOnlyProperty<Double>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesDouble")
operator fun Long.times(property: ReadOnlyProperty<Double>) = property.mapBinding { this * it }

@JvmName("propertyLongDivDouble")
operator fun Long.div(property: ReadOnlyProperty<Double>) = property.mapBinding { this / it }

@JvmName("propertyLongRemDouble")
operator fun Long.rem(property: ReadOnlyProperty<Double>) = property.mapBinding { this % it }

// Long - Short

@JvmName("propertyLongPlusShort")
operator fun Long.plus(property: ReadOnlyProperty<Short>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusShort")
operator fun Long.minus(property: ReadOnlyProperty<Short>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesShort")
operator fun Long.times(property: ReadOnlyProperty<Short>) = property.mapBinding { this * it }

@JvmName("propertyLongDivShort")
operator fun Long.div(property: ReadOnlyProperty<Short>) = property.mapBinding { this / it }

@JvmName("propertyLongRemShort")
operator fun Long.rem(property: ReadOnlyProperty<Short>) = property.mapBinding { this % it }

// Long - Byte

@JvmName("propertyLongPlusByte")
operator fun Long.plus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusByte")
operator fun Long.minus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesByte")
operator fun Long.times(property: ReadOnlyProperty<Byte>) = property.mapBinding { this * it }

@JvmName("propertyLongDivByte")
operator fun Long.div(property: ReadOnlyProperty<Byte>) = property.mapBinding { this / it }

@JvmName("propertyLongRemByte")
operator fun Long.rem(property: ReadOnlyProperty<Byte>) = property.mapBinding { this % it }

// Float - Int

@JvmName("propertyFloatPlusInt")
operator fun Float.plus(property: ReadOnlyProperty<Int>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusInt")
operator fun Float.minus(property: ReadOnlyProperty<Int>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesInt")
operator fun Float.times(property: ReadOnlyProperty<Int>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivInt")
operator fun Float.div(property: ReadOnlyProperty<Int>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemInt")
operator fun Float.rem(property: ReadOnlyProperty<Int>) = property.mapBinding { this % it }

// Float - Long

@JvmName("propertyFloatPlusLong")
operator fun Float.plus(property: ReadOnlyProperty<Long>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusLong")
operator fun Float.minus(property: ReadOnlyProperty<Long>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesLong")
operator fun Float.times(property: ReadOnlyProperty<Long>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivLong")
operator fun Float.div(property: ReadOnlyProperty<Long>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemLong")
operator fun Float.rem(property: ReadOnlyProperty<Long>) = property.mapBinding { this % it }

// Float - Float

@JvmName("propertyFloatPlusFloat")
operator fun Float.plus(property: ReadOnlyProperty<Float>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusFloat")
operator fun Float.minus(property: ReadOnlyProperty<Float>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesFloat")
operator fun Float.times(property: ReadOnlyProperty<Float>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivFloat")
operator fun Float.div(property: ReadOnlyProperty<Float>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemFloat")
operator fun Float.rem(property: ReadOnlyProperty<Float>) = property.mapBinding { this % it }

// Float - Double

@JvmName("propertyFloatPlusDouble")
operator fun Float.plus(property: ReadOnlyProperty<Double>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusDouble")
operator fun Float.minus(property: ReadOnlyProperty<Double>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesDouble")
operator fun Float.times(property: ReadOnlyProperty<Double>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivDouble")
operator fun Float.div(property: ReadOnlyProperty<Double>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemDouble")
operator fun Float.rem(property: ReadOnlyProperty<Double>) = property.mapBinding { this % it }

// Float - Short

@JvmName("propertyFloatPlusShort")
operator fun Float.plus(property: ReadOnlyProperty<Short>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusShort")
operator fun Float.minus(property: ReadOnlyProperty<Short>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesShort")
operator fun Float.times(property: ReadOnlyProperty<Short>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivShort")
operator fun Float.div(property: ReadOnlyProperty<Short>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemShort")
operator fun Float.rem(property: ReadOnlyProperty<Short>) = property.mapBinding { this % it }

// Float - Byte

@JvmName("propertyFloatPlusByte")
operator fun Float.plus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusByte")
operator fun Float.minus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesByte")
operator fun Float.times(property: ReadOnlyProperty<Byte>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivByte")
operator fun Float.div(property: ReadOnlyProperty<Byte>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemByte")
operator fun Float.rem(property: ReadOnlyProperty<Byte>) = property.mapBinding { this % it }

// Double - Int

@JvmName("propertyDoublePlusInt")
operator fun Double.plus(property: ReadOnlyProperty<Int>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusInt")
operator fun Double.minus(property: ReadOnlyProperty<Int>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesInt")
operator fun Double.times(property: ReadOnlyProperty<Int>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivInt")
operator fun Double.div(property: ReadOnlyProperty<Int>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemInt")
operator fun Double.rem(property: ReadOnlyProperty<Int>) = property.mapBinding { this % it }

// Double - Long

@JvmName("propertyDoublePlusLong")
operator fun Double.plus(property: ReadOnlyProperty<Long>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusLong")
operator fun Double.minus(property: ReadOnlyProperty<Long>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesLong")
operator fun Double.times(property: ReadOnlyProperty<Long>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivLong")
operator fun Double.div(property: ReadOnlyProperty<Long>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemLong")
operator fun Double.rem(property: ReadOnlyProperty<Long>) = property.mapBinding { this % it }

// Double - Float

@JvmName("propertyDoublePlusFloat")
operator fun Double.plus(property: ReadOnlyProperty<Float>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusFloat")
operator fun Double.minus(property: ReadOnlyProperty<Float>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesFloat")
operator fun Double.times(property: ReadOnlyProperty<Float>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivFloat")
operator fun Double.div(property: ReadOnlyProperty<Float>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemFloat")
operator fun Double.rem(property: ReadOnlyProperty<Float>) = property.mapBinding { this % it }

// Double - Double

@JvmName("propertyDoublePlusDouble")
operator fun Double.plus(property: ReadOnlyProperty<Double>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusDouble")
operator fun Double.minus(property: ReadOnlyProperty<Double>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesDouble")
operator fun Double.times(property: ReadOnlyProperty<Double>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivDouble")
operator fun Double.div(property: ReadOnlyProperty<Double>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemDouble")
operator fun Double.rem(property: ReadOnlyProperty<Double>) = property.mapBinding { this % it }

// Double - Short

@JvmName("propertyDoublePlusShort")
operator fun Double.plus(property: ReadOnlyProperty<Short>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusShort")
operator fun Double.minus(property: ReadOnlyProperty<Short>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesShort")
operator fun Double.times(property: ReadOnlyProperty<Short>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivShort")
operator fun Double.div(property: ReadOnlyProperty<Short>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemShort")
operator fun Double.rem(property: ReadOnlyProperty<Short>) = property.mapBinding { this % it }

// Double - Byte

@JvmName("propertyDoublePlusByte")
operator fun Double.plus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusByte")
operator fun Double.minus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesByte")
operator fun Double.times(property: ReadOnlyProperty<Byte>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivByte")
operator fun Double.div(property: ReadOnlyProperty<Byte>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemByte")
operator fun Double.rem(property: ReadOnlyProperty<Byte>) = property.mapBinding { this % it }

// Short - Int

@JvmName("propertyShortPlusInt")
operator fun Short.plus(property: ReadOnlyProperty<Int>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusInt")
operator fun Short.minus(property: ReadOnlyProperty<Int>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesInt")
operator fun Short.times(property: ReadOnlyProperty<Int>) = property.mapBinding { this * it }

@JvmName("propertyShortDivInt")
operator fun Short.div(property: ReadOnlyProperty<Int>) = property.mapBinding { this / it }

@JvmName("propertyShortRemInt")
operator fun Short.rem(property: ReadOnlyProperty<Int>) = property.mapBinding { this % it }

// Short - Long

@JvmName("propertyShortPlusLong")
operator fun Short.plus(property: ReadOnlyProperty<Long>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusLong")
operator fun Short.minus(property: ReadOnlyProperty<Long>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesLong")
operator fun Short.times(property: ReadOnlyProperty<Long>) = property.mapBinding { this * it }

@JvmName("propertyShortDivLong")
operator fun Short.div(property: ReadOnlyProperty<Long>) = property.mapBinding { this / it }

@JvmName("propertyShortRemLong")
operator fun Short.rem(property: ReadOnlyProperty<Long>) = property.mapBinding { this % it }

// Short - Float

@JvmName("propertyShortPlusFloat")
operator fun Short.plus(property: ReadOnlyProperty<Float>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusFloat")
operator fun Short.minus(property: ReadOnlyProperty<Float>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesFloat")
operator fun Short.times(property: ReadOnlyProperty<Float>) = property.mapBinding { this * it }

@JvmName("propertyShortDivFloat")
operator fun Short.div(property: ReadOnlyProperty<Float>) = property.mapBinding { this / it }

@JvmName("propertyShortRemFloat")
operator fun Short.rem(property: ReadOnlyProperty<Float>) = property.mapBinding { this % it }

// Short - Double

@JvmName("propertyShortPlusDouble")
operator fun Short.plus(property: ReadOnlyProperty<Double>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusDouble")
operator fun Short.minus(property: ReadOnlyProperty<Double>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesDouble")
operator fun Short.times(property: ReadOnlyProperty<Double>) = property.mapBinding { this * it }

@JvmName("propertyShortDivDouble")
operator fun Short.div(property: ReadOnlyProperty<Double>) = property.mapBinding { this / it }

@JvmName("propertyShortRemDouble")
operator fun Short.rem(property: ReadOnlyProperty<Double>) = property.mapBinding { this % it }

// Short - Short

@JvmName("propertyShortPlusShort")
operator fun Short.plus(property: ReadOnlyProperty<Short>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusShort")
operator fun Short.minus(property: ReadOnlyProperty<Short>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesShort")
operator fun Short.times(property: ReadOnlyProperty<Short>) = property.mapBinding { this * it }

@JvmName("propertyShortDivShort")
operator fun Short.div(property: ReadOnlyProperty<Short>) = property.mapBinding { this / it }

@JvmName("propertyShortRemShort")
operator fun Short.rem(property: ReadOnlyProperty<Short>) = property.mapBinding { this % it }

// Short - Byte

@JvmName("propertyShortPlusByte")
operator fun Short.plus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusByte")
operator fun Short.minus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesByte")
operator fun Short.times(property: ReadOnlyProperty<Byte>) = property.mapBinding { this * it }

@JvmName("propertyShortDivByte")
operator fun Short.div(property: ReadOnlyProperty<Byte>) = property.mapBinding { this / it }

@JvmName("propertyShortRemByte")
operator fun Short.rem(property: ReadOnlyProperty<Byte>) = property.mapBinding { this % it }

// Byte - Int

@JvmName("propertyBytePlusInt")
operator fun Byte.plus(property: ReadOnlyProperty<Int>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusInt")
operator fun Byte.minus(property: ReadOnlyProperty<Int>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesInt")
operator fun Byte.times(property: ReadOnlyProperty<Int>) = property.mapBinding { this * it }

@JvmName("propertyByteDivInt")
operator fun Byte.div(property: ReadOnlyProperty<Int>) = property.mapBinding { this / it }

@JvmName("propertyByteRemInt")
operator fun Byte.rem(property: ReadOnlyProperty<Int>) = property.mapBinding { this % it }

// Byte - Long

@JvmName("propertyBytePlusLong")
operator fun Byte.plus(property: ReadOnlyProperty<Long>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusLong")
operator fun Byte.minus(property: ReadOnlyProperty<Long>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesLong")
operator fun Byte.times(property: ReadOnlyProperty<Long>) = property.mapBinding { this * it }

@JvmName("propertyByteDivLong")
operator fun Byte.div(property: ReadOnlyProperty<Long>) = property.mapBinding { this / it }

@JvmName("propertyByteRemLong")
operator fun Byte.rem(property: ReadOnlyProperty<Long>) = property.mapBinding { this % it }

// Byte - Float

@JvmName("propertyBytePlusFloat")
operator fun Byte.plus(property: ReadOnlyProperty<Float>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusFloat")
operator fun Byte.minus(property: ReadOnlyProperty<Float>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesFloat")
operator fun Byte.times(property: ReadOnlyProperty<Float>) = property.mapBinding { this * it }

@JvmName("propertyByteDivFloat")
operator fun Byte.div(property: ReadOnlyProperty<Float>) = property.mapBinding { this / it }

@JvmName("propertyByteRemFloat")
operator fun Byte.rem(property: ReadOnlyProperty<Float>) = property.mapBinding { this % it }

// Byte - Double

@JvmName("propertyBytePlusDouble")
operator fun Byte.plus(property: ReadOnlyProperty<Double>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusDouble")
operator fun Byte.minus(property: ReadOnlyProperty<Double>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesDouble")
operator fun Byte.times(property: ReadOnlyProperty<Double>) = property.mapBinding { this * it }

@JvmName("propertyByteDivDouble")
operator fun Byte.div(property: ReadOnlyProperty<Double>) = property.mapBinding { this / it }

@JvmName("propertyByteRemDouble")
operator fun Byte.rem(property: ReadOnlyProperty<Double>) = property.mapBinding { this % it }

// Byte - Short

@JvmName("propertyBytePlusShort")
operator fun Byte.plus(property: ReadOnlyProperty<Short>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusShort")
operator fun Byte.minus(property: ReadOnlyProperty<Short>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesShort")
operator fun Byte.times(property: ReadOnlyProperty<Short>) = property.mapBinding { this * it }

@JvmName("propertyByteDivShort")
operator fun Byte.div(property: ReadOnlyProperty<Short>) = property.mapBinding { this / it }

@JvmName("propertyByteRemShort")
operator fun Byte.rem(property: ReadOnlyProperty<Short>) = property.mapBinding { this % it }

// Byte - Byte

@JvmName("propertyBytePlusByte")
operator fun Byte.plus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusByte")
operator fun Byte.minus(property: ReadOnlyProperty<Byte>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesByte")
operator fun Byte.times(property: ReadOnlyProperty<Byte>) = property.mapBinding { this * it }

@JvmName("propertyByteDivByte")
operator fun Byte.div(property: ReadOnlyProperty<Byte>) = property.mapBinding { this / it }

@JvmName("propertyByteRemByte")
operator fun Byte.rem(property: ReadOnlyProperty<Byte>) = property.mapBinding { this % it }
