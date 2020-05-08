@file:Suppress("unused")

package de.westermann.kobserve

import de.westermann.kobserve.base.ObservableCollection
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import kotlin.jvm.JvmName

// Boolean

@JvmName("propertyBooleanAnd")
infix fun ObservableValue<Boolean>.and(property: ObservableValue<Boolean>) =
        join(property, Boolean::and)

@JvmName("propertyBooleanOr")
infix fun ObservableValue<Boolean>.or(property: ObservableValue<Boolean>) =
        join(property, Boolean::or)

@JvmName("propertyBooleanXor")
infix fun ObservableValue<Boolean>.xor(property: ObservableValue<Boolean>) =
        join(property, Boolean::xor)

@JvmName("propertyBooleanImplies")
infix fun ObservableValue<Boolean>.implies(property: ObservableValue<Boolean>) =
        join(property) { a, b -> !a || b }

@JvmName("propertyBooleanNot")
operator fun ObservableValue<Boolean>.not(): ObservableValue<Boolean> =
        mapBinding(Boolean::not)

/* The following part is auto generated. Do NOT edit it manually! */

// Unary minus

@JvmName("propertyIntUnaryMinus")
operator fun ObservableValue<Int>.unaryMinus() = mapBinding { -it }

@JvmName("propertyLongUnaryMinus")
operator fun ObservableValue<Long>.unaryMinus() = mapBinding { -it }

@JvmName("propertyFloatUnaryMinus")
operator fun ObservableValue<Float>.unaryMinus() = mapBinding { -it }

@JvmName("propertyDoubleUnaryMinus")
operator fun ObservableValue<Double>.unaryMinus() = mapBinding { -it }

@JvmName("propertyShortUnaryMinus")
operator fun ObservableValue<Short>.unaryMinus() = mapBinding { -it }

@JvmName("propertyByteUnaryMinus")
operator fun ObservableValue<Byte>.unaryMinus() = mapBinding { -it }

// List sum

@JvmName("observableListIntSum")
fun ObservableCollection<Int>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListLongSum")
fun ObservableCollection<Long>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListFloatSum")
fun ObservableCollection<Float>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListDoubleSum")
fun ObservableCollection<Double>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListShortSum")
fun ObservableCollection<Short>.sumObservable() = mapBinding { it.sum() }

@JvmName("observableListByteSum")
fun ObservableCollection<Byte>.sumObservable() = mapBinding { it.sum() }

// List average

@JvmName("observableListIntAverage")
fun ObservableCollection<Int>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListLongAverage")
fun ObservableCollection<Long>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListFloatAverage")
fun ObservableCollection<Float>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListDoubleAverage")
fun ObservableCollection<Double>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListShortAverage")
fun ObservableCollection<Short>.averageObservable() = mapBinding { it.average() }

@JvmName("observableListByteAverage")
fun ObservableCollection<Byte>.averageObservable() = mapBinding { it.average() }

/*
 * Property - Property
 */

// Int - Int

@JvmName("propertyIntPlusInt")
operator fun ObservableValue<Int>.plus(property: ObservableValue<Int>) = join(property, Int::plus)

@JvmName("propertyIntMinusInt")
operator fun ObservableValue<Int>.minus(property: ObservableValue<Int>) = join(property, Int::minus)

@JvmName("propertyIntTimesInt")
operator fun ObservableValue<Int>.times(property: ObservableValue<Int>) = join(property, Int::times)

@JvmName("propertyIntDivInt")
operator fun ObservableValue<Int>.div(property: ObservableValue<Int>) = join(property, Int::div)

@JvmName("propertyIntRemInt")
operator fun ObservableValue<Int>.rem(property: ObservableValue<Int>) = join(property, Int::rem)

// Int - Long

@JvmName("propertyIntPlusLong")
operator fun ObservableValue<Int>.plus(property: ObservableValue<Long>) = join(property, Int::plus)

@JvmName("propertyIntMinusLong")
operator fun ObservableValue<Int>.minus(property: ObservableValue<Long>) = join(property, Int::minus)

@JvmName("propertyIntTimesLong")
operator fun ObservableValue<Int>.times(property: ObservableValue<Long>) = join(property, Int::times)

@JvmName("propertyIntDivLong")
operator fun ObservableValue<Int>.div(property: ObservableValue<Long>) = join(property, Int::div)

@JvmName("propertyIntRemLong")
operator fun ObservableValue<Int>.rem(property: ObservableValue<Long>) = join(property, Int::rem)

// Int - Float

@JvmName("propertyIntPlusFloat")
operator fun ObservableValue<Int>.plus(property: ObservableValue<Float>) = join(property, Int::plus)

@JvmName("propertyIntMinusFloat")
operator fun ObservableValue<Int>.minus(property: ObservableValue<Float>) = join(property, Int::minus)

@JvmName("propertyIntTimesFloat")
operator fun ObservableValue<Int>.times(property: ObservableValue<Float>) = join(property, Int::times)

@JvmName("propertyIntDivFloat")
operator fun ObservableValue<Int>.div(property: ObservableValue<Float>) = join(property, Int::div)

@JvmName("propertyIntRemFloat")
operator fun ObservableValue<Int>.rem(property: ObservableValue<Float>) = join(property, Int::rem)

// Int - Double

@JvmName("propertyIntPlusDouble")
operator fun ObservableValue<Int>.plus(property: ObservableValue<Double>) = join(property, Int::plus)

@JvmName("propertyIntMinusDouble")
operator fun ObservableValue<Int>.minus(property: ObservableValue<Double>) = join(property, Int::minus)

@JvmName("propertyIntTimesDouble")
operator fun ObservableValue<Int>.times(property: ObservableValue<Double>) = join(property, Int::times)

@JvmName("propertyIntDivDouble")
operator fun ObservableValue<Int>.div(property: ObservableValue<Double>) = join(property, Int::div)

@JvmName("propertyIntRemDouble")
operator fun ObservableValue<Int>.rem(property: ObservableValue<Double>) = join(property, Int::rem)

// Int - Short

@JvmName("propertyIntPlusShort")
operator fun ObservableValue<Int>.plus(property: ObservableValue<Short>) = join(property, Int::plus)

@JvmName("propertyIntMinusShort")
operator fun ObservableValue<Int>.minus(property: ObservableValue<Short>) = join(property, Int::minus)

@JvmName("propertyIntTimesShort")
operator fun ObservableValue<Int>.times(property: ObservableValue<Short>) = join(property, Int::times)

@JvmName("propertyIntDivShort")
operator fun ObservableValue<Int>.div(property: ObservableValue<Short>) = join(property, Int::div)

@JvmName("propertyIntRemShort")
operator fun ObservableValue<Int>.rem(property: ObservableValue<Short>) = join(property, Int::rem)

// Int - Byte

@JvmName("propertyIntPlusByte")
operator fun ObservableValue<Int>.plus(property: ObservableValue<Byte>) = join(property, Int::plus)

@JvmName("propertyIntMinusByte")
operator fun ObservableValue<Int>.minus(property: ObservableValue<Byte>) = join(property, Int::minus)

@JvmName("propertyIntTimesByte")
operator fun ObservableValue<Int>.times(property: ObservableValue<Byte>) = join(property, Int::times)

@JvmName("propertyIntDivByte")
operator fun ObservableValue<Int>.div(property: ObservableValue<Byte>) = join(property, Int::div)

@JvmName("propertyIntRemByte")
operator fun ObservableValue<Int>.rem(property: ObservableValue<Byte>) = join(property, Int::rem)

// Long - Int

@JvmName("propertyLongPlusInt")
operator fun ObservableValue<Long>.plus(property: ObservableValue<Int>) = join(property, Long::plus)

@JvmName("propertyLongMinusInt")
operator fun ObservableValue<Long>.minus(property: ObservableValue<Int>) = join(property, Long::minus)

@JvmName("propertyLongTimesInt")
operator fun ObservableValue<Long>.times(property: ObservableValue<Int>) = join(property, Long::times)

@JvmName("propertyLongDivInt")
operator fun ObservableValue<Long>.div(property: ObservableValue<Int>) = join(property, Long::div)

@JvmName("propertyLongRemInt")
operator fun ObservableValue<Long>.rem(property: ObservableValue<Int>) = join(property, Long::rem)

// Long - Long

@JvmName("propertyLongPlusLong")
operator fun ObservableValue<Long>.plus(property: ObservableValue<Long>) = join(property, Long::plus)

@JvmName("propertyLongMinusLong")
operator fun ObservableValue<Long>.minus(property: ObservableValue<Long>) = join(property, Long::minus)

@JvmName("propertyLongTimesLong")
operator fun ObservableValue<Long>.times(property: ObservableValue<Long>) = join(property, Long::times)

@JvmName("propertyLongDivLong")
operator fun ObservableValue<Long>.div(property: ObservableValue<Long>) = join(property, Long::div)

@JvmName("propertyLongRemLong")
operator fun ObservableValue<Long>.rem(property: ObservableValue<Long>) = join(property, Long::rem)

// Long - Float

@JvmName("propertyLongPlusFloat")
operator fun ObservableValue<Long>.plus(property: ObservableValue<Float>) = join(property, Long::plus)

@JvmName("propertyLongMinusFloat")
operator fun ObservableValue<Long>.minus(property: ObservableValue<Float>) = join(property, Long::minus)

@JvmName("propertyLongTimesFloat")
operator fun ObservableValue<Long>.times(property: ObservableValue<Float>) = join(property, Long::times)

@JvmName("propertyLongDivFloat")
operator fun ObservableValue<Long>.div(property: ObservableValue<Float>) = join(property, Long::div)

@JvmName("propertyLongRemFloat")
operator fun ObservableValue<Long>.rem(property: ObservableValue<Float>) = join(property, Long::rem)

// Long - Double

@JvmName("propertyLongPlusDouble")
operator fun ObservableValue<Long>.plus(property: ObservableValue<Double>) = join(property, Long::plus)

@JvmName("propertyLongMinusDouble")
operator fun ObservableValue<Long>.minus(property: ObservableValue<Double>) = join(property, Long::minus)

@JvmName("propertyLongTimesDouble")
operator fun ObservableValue<Long>.times(property: ObservableValue<Double>) = join(property, Long::times)

@JvmName("propertyLongDivDouble")
operator fun ObservableValue<Long>.div(property: ObservableValue<Double>) = join(property, Long::div)

@JvmName("propertyLongRemDouble")
operator fun ObservableValue<Long>.rem(property: ObservableValue<Double>) = join(property, Long::rem)

// Long - Short

@JvmName("propertyLongPlusShort")
operator fun ObservableValue<Long>.plus(property: ObservableValue<Short>) = join(property, Long::plus)

@JvmName("propertyLongMinusShort")
operator fun ObservableValue<Long>.minus(property: ObservableValue<Short>) = join(property, Long::minus)

@JvmName("propertyLongTimesShort")
operator fun ObservableValue<Long>.times(property: ObservableValue<Short>) = join(property, Long::times)

@JvmName("propertyLongDivShort")
operator fun ObservableValue<Long>.div(property: ObservableValue<Short>) = join(property, Long::div)

@JvmName("propertyLongRemShort")
operator fun ObservableValue<Long>.rem(property: ObservableValue<Short>) = join(property, Long::rem)

// Long - Byte

@JvmName("propertyLongPlusByte")
operator fun ObservableValue<Long>.plus(property: ObservableValue<Byte>) = join(property, Long::plus)

@JvmName("propertyLongMinusByte")
operator fun ObservableValue<Long>.minus(property: ObservableValue<Byte>) = join(property, Long::minus)

@JvmName("propertyLongTimesByte")
operator fun ObservableValue<Long>.times(property: ObservableValue<Byte>) = join(property, Long::times)

@JvmName("propertyLongDivByte")
operator fun ObservableValue<Long>.div(property: ObservableValue<Byte>) = join(property, Long::div)

@JvmName("propertyLongRemByte")
operator fun ObservableValue<Long>.rem(property: ObservableValue<Byte>) = join(property, Long::rem)

// Float - Int

@JvmName("propertyFloatPlusInt")
operator fun ObservableValue<Float>.plus(property: ObservableValue<Int>) = join(property, Float::plus)

@JvmName("propertyFloatMinusInt")
operator fun ObservableValue<Float>.minus(property: ObservableValue<Int>) = join(property, Float::minus)

@JvmName("propertyFloatTimesInt")
operator fun ObservableValue<Float>.times(property: ObservableValue<Int>) = join(property, Float::times)

@JvmName("propertyFloatDivInt")
operator fun ObservableValue<Float>.div(property: ObservableValue<Int>) = join(property, Float::div)

@JvmName("propertyFloatRemInt")
operator fun ObservableValue<Float>.rem(property: ObservableValue<Int>) = join(property, Float::rem)

// Float - Long

@JvmName("propertyFloatPlusLong")
operator fun ObservableValue<Float>.plus(property: ObservableValue<Long>) = join(property, Float::plus)

@JvmName("propertyFloatMinusLong")
operator fun ObservableValue<Float>.minus(property: ObservableValue<Long>) = join(property, Float::minus)

@JvmName("propertyFloatTimesLong")
operator fun ObservableValue<Float>.times(property: ObservableValue<Long>) = join(property, Float::times)

@JvmName("propertyFloatDivLong")
operator fun ObservableValue<Float>.div(property: ObservableValue<Long>) = join(property, Float::div)

@JvmName("propertyFloatRemLong")
operator fun ObservableValue<Float>.rem(property: ObservableValue<Long>) = join(property, Float::rem)

// Float - Float

@JvmName("propertyFloatPlusFloat")
operator fun ObservableValue<Float>.plus(property: ObservableValue<Float>) = join(property, Float::plus)

@JvmName("propertyFloatMinusFloat")
operator fun ObservableValue<Float>.minus(property: ObservableValue<Float>) = join(property, Float::minus)

@JvmName("propertyFloatTimesFloat")
operator fun ObservableValue<Float>.times(property: ObservableValue<Float>) = join(property, Float::times)

@JvmName("propertyFloatDivFloat")
operator fun ObservableValue<Float>.div(property: ObservableValue<Float>) = join(property, Float::div)

@JvmName("propertyFloatRemFloat")
operator fun ObservableValue<Float>.rem(property: ObservableValue<Float>) = join(property, Float::rem)

// Float - Double

@JvmName("propertyFloatPlusDouble")
operator fun ObservableValue<Float>.plus(property: ObservableValue<Double>) = join(property, Float::plus)

@JvmName("propertyFloatMinusDouble")
operator fun ObservableValue<Float>.minus(property: ObservableValue<Double>) = join(property, Float::minus)

@JvmName("propertyFloatTimesDouble")
operator fun ObservableValue<Float>.times(property: ObservableValue<Double>) = join(property, Float::times)

@JvmName("propertyFloatDivDouble")
operator fun ObservableValue<Float>.div(property: ObservableValue<Double>) = join(property, Float::div)

@JvmName("propertyFloatRemDouble")
operator fun ObservableValue<Float>.rem(property: ObservableValue<Double>) = join(property, Float::rem)

// Float - Short

@JvmName("propertyFloatPlusShort")
operator fun ObservableValue<Float>.plus(property: ObservableValue<Short>) = join(property, Float::plus)

@JvmName("propertyFloatMinusShort")
operator fun ObservableValue<Float>.minus(property: ObservableValue<Short>) = join(property, Float::minus)

@JvmName("propertyFloatTimesShort")
operator fun ObservableValue<Float>.times(property: ObservableValue<Short>) = join(property, Float::times)

@JvmName("propertyFloatDivShort")
operator fun ObservableValue<Float>.div(property: ObservableValue<Short>) = join(property, Float::div)

@JvmName("propertyFloatRemShort")
operator fun ObservableValue<Float>.rem(property: ObservableValue<Short>) = join(property, Float::rem)

// Float - Byte

@JvmName("propertyFloatPlusByte")
operator fun ObservableValue<Float>.plus(property: ObservableValue<Byte>) = join(property, Float::plus)

@JvmName("propertyFloatMinusByte")
operator fun ObservableValue<Float>.minus(property: ObservableValue<Byte>) = join(property, Float::minus)

@JvmName("propertyFloatTimesByte")
operator fun ObservableValue<Float>.times(property: ObservableValue<Byte>) = join(property, Float::times)

@JvmName("propertyFloatDivByte")
operator fun ObservableValue<Float>.div(property: ObservableValue<Byte>) = join(property, Float::div)

@JvmName("propertyFloatRemByte")
operator fun ObservableValue<Float>.rem(property: ObservableValue<Byte>) = join(property, Float::rem)

// Double - Int

@JvmName("propertyDoublePlusInt")
operator fun ObservableValue<Double>.plus(property: ObservableValue<Int>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusInt")
operator fun ObservableValue<Double>.minus(property: ObservableValue<Int>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesInt")
operator fun ObservableValue<Double>.times(property: ObservableValue<Int>) = join(property, Double::times)

@JvmName("propertyDoubleDivInt")
operator fun ObservableValue<Double>.div(property: ObservableValue<Int>) = join(property, Double::div)

@JvmName("propertyDoubleRemInt")
operator fun ObservableValue<Double>.rem(property: ObservableValue<Int>) = join(property, Double::rem)

// Double - Long

@JvmName("propertyDoublePlusLong")
operator fun ObservableValue<Double>.plus(property: ObservableValue<Long>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusLong")
operator fun ObservableValue<Double>.minus(property: ObservableValue<Long>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesLong")
operator fun ObservableValue<Double>.times(property: ObservableValue<Long>) = join(property, Double::times)

@JvmName("propertyDoubleDivLong")
operator fun ObservableValue<Double>.div(property: ObservableValue<Long>) = join(property, Double::div)

@JvmName("propertyDoubleRemLong")
operator fun ObservableValue<Double>.rem(property: ObservableValue<Long>) = join(property, Double::rem)

// Double - Float

@JvmName("propertyDoublePlusFloat")
operator fun ObservableValue<Double>.plus(property: ObservableValue<Float>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusFloat")
operator fun ObservableValue<Double>.minus(property: ObservableValue<Float>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesFloat")
operator fun ObservableValue<Double>.times(property: ObservableValue<Float>) = join(property, Double::times)

@JvmName("propertyDoubleDivFloat")
operator fun ObservableValue<Double>.div(property: ObservableValue<Float>) = join(property, Double::div)

@JvmName("propertyDoubleRemFloat")
operator fun ObservableValue<Double>.rem(property: ObservableValue<Float>) = join(property, Double::rem)

// Double - Double

@JvmName("propertyDoublePlusDouble")
operator fun ObservableValue<Double>.plus(property: ObservableValue<Double>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusDouble")
operator fun ObservableValue<Double>.minus(property: ObservableValue<Double>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesDouble")
operator fun ObservableValue<Double>.times(property: ObservableValue<Double>) = join(property, Double::times)

@JvmName("propertyDoubleDivDouble")
operator fun ObservableValue<Double>.div(property: ObservableValue<Double>) = join(property, Double::div)

@JvmName("propertyDoubleRemDouble")
operator fun ObservableValue<Double>.rem(property: ObservableValue<Double>) = join(property, Double::rem)

// Double - Short

@JvmName("propertyDoublePlusShort")
operator fun ObservableValue<Double>.plus(property: ObservableValue<Short>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusShort")
operator fun ObservableValue<Double>.minus(property: ObservableValue<Short>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesShort")
operator fun ObservableValue<Double>.times(property: ObservableValue<Short>) = join(property, Double::times)

@JvmName("propertyDoubleDivShort")
operator fun ObservableValue<Double>.div(property: ObservableValue<Short>) = join(property, Double::div)

@JvmName("propertyDoubleRemShort")
operator fun ObservableValue<Double>.rem(property: ObservableValue<Short>) = join(property, Double::rem)

// Double - Byte

@JvmName("propertyDoublePlusByte")
operator fun ObservableValue<Double>.plus(property: ObservableValue<Byte>) = join(property, Double::plus)

@JvmName("propertyDoubleMinusByte")
operator fun ObservableValue<Double>.minus(property: ObservableValue<Byte>) = join(property, Double::minus)

@JvmName("propertyDoubleTimesByte")
operator fun ObservableValue<Double>.times(property: ObservableValue<Byte>) = join(property, Double::times)

@JvmName("propertyDoubleDivByte")
operator fun ObservableValue<Double>.div(property: ObservableValue<Byte>) = join(property, Double::div)

@JvmName("propertyDoubleRemByte")
operator fun ObservableValue<Double>.rem(property: ObservableValue<Byte>) = join(property, Double::rem)

// Short - Int

@JvmName("propertyShortPlusInt")
operator fun ObservableValue<Short>.plus(property: ObservableValue<Int>) = join(property, Short::plus)

@JvmName("propertyShortMinusInt")
operator fun ObservableValue<Short>.minus(property: ObservableValue<Int>) = join(property, Short::minus)

@JvmName("propertyShortTimesInt")
operator fun ObservableValue<Short>.times(property: ObservableValue<Int>) = join(property, Short::times)

@JvmName("propertyShortDivInt")
operator fun ObservableValue<Short>.div(property: ObservableValue<Int>) = join(property, Short::div)

@JvmName("propertyShortRemInt")
operator fun ObservableValue<Short>.rem(property: ObservableValue<Int>) = join(property, Short::rem)

// Short - Long

@JvmName("propertyShortPlusLong")
operator fun ObservableValue<Short>.plus(property: ObservableValue<Long>) = join(property, Short::plus)

@JvmName("propertyShortMinusLong")
operator fun ObservableValue<Short>.minus(property: ObservableValue<Long>) = join(property, Short::minus)

@JvmName("propertyShortTimesLong")
operator fun ObservableValue<Short>.times(property: ObservableValue<Long>) = join(property, Short::times)

@JvmName("propertyShortDivLong")
operator fun ObservableValue<Short>.div(property: ObservableValue<Long>) = join(property, Short::div)

@JvmName("propertyShortRemLong")
operator fun ObservableValue<Short>.rem(property: ObservableValue<Long>) = join(property, Short::rem)

// Short - Float

@JvmName("propertyShortPlusFloat")
operator fun ObservableValue<Short>.plus(property: ObservableValue<Float>) = join(property, Short::plus)

@JvmName("propertyShortMinusFloat")
operator fun ObservableValue<Short>.minus(property: ObservableValue<Float>) = join(property, Short::minus)

@JvmName("propertyShortTimesFloat")
operator fun ObservableValue<Short>.times(property: ObservableValue<Float>) = join(property, Short::times)

@JvmName("propertyShortDivFloat")
operator fun ObservableValue<Short>.div(property: ObservableValue<Float>) = join(property, Short::div)

@JvmName("propertyShortRemFloat")
operator fun ObservableValue<Short>.rem(property: ObservableValue<Float>) = join(property, Short::rem)

// Short - Double

@JvmName("propertyShortPlusDouble")
operator fun ObservableValue<Short>.plus(property: ObservableValue<Double>) = join(property, Short::plus)

@JvmName("propertyShortMinusDouble")
operator fun ObservableValue<Short>.minus(property: ObservableValue<Double>) = join(property, Short::minus)

@JvmName("propertyShortTimesDouble")
operator fun ObservableValue<Short>.times(property: ObservableValue<Double>) = join(property, Short::times)

@JvmName("propertyShortDivDouble")
operator fun ObservableValue<Short>.div(property: ObservableValue<Double>) = join(property, Short::div)

@JvmName("propertyShortRemDouble")
operator fun ObservableValue<Short>.rem(property: ObservableValue<Double>) = join(property, Short::rem)

// Short - Short

@JvmName("propertyShortPlusShort")
operator fun ObservableValue<Short>.plus(property: ObservableValue<Short>) = join(property, Short::plus)

@JvmName("propertyShortMinusShort")
operator fun ObservableValue<Short>.minus(property: ObservableValue<Short>) = join(property, Short::minus)

@JvmName("propertyShortTimesShort")
operator fun ObservableValue<Short>.times(property: ObservableValue<Short>) = join(property, Short::times)

@JvmName("propertyShortDivShort")
operator fun ObservableValue<Short>.div(property: ObservableValue<Short>) = join(property, Short::div)

@JvmName("propertyShortRemShort")
operator fun ObservableValue<Short>.rem(property: ObservableValue<Short>) = join(property, Short::rem)

// Short - Byte

@JvmName("propertyShortPlusByte")
operator fun ObservableValue<Short>.plus(property: ObservableValue<Byte>) = join(property, Short::plus)

@JvmName("propertyShortMinusByte")
operator fun ObservableValue<Short>.minus(property: ObservableValue<Byte>) = join(property, Short::minus)

@JvmName("propertyShortTimesByte")
operator fun ObservableValue<Short>.times(property: ObservableValue<Byte>) = join(property, Short::times)

@JvmName("propertyShortDivByte")
operator fun ObservableValue<Short>.div(property: ObservableValue<Byte>) = join(property, Short::div)

@JvmName("propertyShortRemByte")
operator fun ObservableValue<Short>.rem(property: ObservableValue<Byte>) = join(property, Short::rem)

// Byte - Int

@JvmName("propertyBytePlusInt")
operator fun ObservableValue<Byte>.plus(property: ObservableValue<Int>) = join(property, Byte::plus)

@JvmName("propertyByteMinusInt")
operator fun ObservableValue<Byte>.minus(property: ObservableValue<Int>) = join(property, Byte::minus)

@JvmName("propertyByteTimesInt")
operator fun ObservableValue<Byte>.times(property: ObservableValue<Int>) = join(property, Byte::times)

@JvmName("propertyByteDivInt")
operator fun ObservableValue<Byte>.div(property: ObservableValue<Int>) = join(property, Byte::div)

@JvmName("propertyByteRemInt")
operator fun ObservableValue<Byte>.rem(property: ObservableValue<Int>) = join(property, Byte::rem)

// Byte - Long

@JvmName("propertyBytePlusLong")
operator fun ObservableValue<Byte>.plus(property: ObservableValue<Long>) = join(property, Byte::plus)

@JvmName("propertyByteMinusLong")
operator fun ObservableValue<Byte>.minus(property: ObservableValue<Long>) = join(property, Byte::minus)

@JvmName("propertyByteTimesLong")
operator fun ObservableValue<Byte>.times(property: ObservableValue<Long>) = join(property, Byte::times)

@JvmName("propertyByteDivLong")
operator fun ObservableValue<Byte>.div(property: ObservableValue<Long>) = join(property, Byte::div)

@JvmName("propertyByteRemLong")
operator fun ObservableValue<Byte>.rem(property: ObservableValue<Long>) = join(property, Byte::rem)

// Byte - Float

@JvmName("propertyBytePlusFloat")
operator fun ObservableValue<Byte>.plus(property: ObservableValue<Float>) = join(property, Byte::plus)

@JvmName("propertyByteMinusFloat")
operator fun ObservableValue<Byte>.minus(property: ObservableValue<Float>) = join(property, Byte::minus)

@JvmName("propertyByteTimesFloat")
operator fun ObservableValue<Byte>.times(property: ObservableValue<Float>) = join(property, Byte::times)

@JvmName("propertyByteDivFloat")
operator fun ObservableValue<Byte>.div(property: ObservableValue<Float>) = join(property, Byte::div)

@JvmName("propertyByteRemFloat")
operator fun ObservableValue<Byte>.rem(property: ObservableValue<Float>) = join(property, Byte::rem)

// Byte - Double

@JvmName("propertyBytePlusDouble")
operator fun ObservableValue<Byte>.plus(property: ObservableValue<Double>) = join(property, Byte::plus)

@JvmName("propertyByteMinusDouble")
operator fun ObservableValue<Byte>.minus(property: ObservableValue<Double>) = join(property, Byte::minus)

@JvmName("propertyByteTimesDouble")
operator fun ObservableValue<Byte>.times(property: ObservableValue<Double>) = join(property, Byte::times)

@JvmName("propertyByteDivDouble")
operator fun ObservableValue<Byte>.div(property: ObservableValue<Double>) = join(property, Byte::div)

@JvmName("propertyByteRemDouble")
operator fun ObservableValue<Byte>.rem(property: ObservableValue<Double>) = join(property, Byte::rem)

// Byte - Short

@JvmName("propertyBytePlusShort")
operator fun ObservableValue<Byte>.plus(property: ObservableValue<Short>) = join(property, Byte::plus)

@JvmName("propertyByteMinusShort")
operator fun ObservableValue<Byte>.minus(property: ObservableValue<Short>) = join(property, Byte::minus)

@JvmName("propertyByteTimesShort")
operator fun ObservableValue<Byte>.times(property: ObservableValue<Short>) = join(property, Byte::times)

@JvmName("propertyByteDivShort")
operator fun ObservableValue<Byte>.div(property: ObservableValue<Short>) = join(property, Byte::div)

@JvmName("propertyByteRemShort")
operator fun ObservableValue<Byte>.rem(property: ObservableValue<Short>) = join(property, Byte::rem)

// Byte - Byte

@JvmName("propertyBytePlusByte")
operator fun ObservableValue<Byte>.plus(property: ObservableValue<Byte>) = join(property, Byte::plus)

@JvmName("propertyByteMinusByte")
operator fun ObservableValue<Byte>.minus(property: ObservableValue<Byte>) = join(property, Byte::minus)

@JvmName("propertyByteTimesByte")
operator fun ObservableValue<Byte>.times(property: ObservableValue<Byte>) = join(property, Byte::times)

@JvmName("propertyByteDivByte")
operator fun ObservableValue<Byte>.div(property: ObservableValue<Byte>) = join(property, Byte::div)

@JvmName("propertyByteRemByte")
operator fun ObservableValue<Byte>.rem(property: ObservableValue<Byte>) = join(property, Byte::rem)

/*
 * Property - primitive
 */

// Int - Int

@JvmName("propertyIntPlusInt")
operator fun ObservableValue<Int>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyIntMinusInt")
operator fun ObservableValue<Int>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyIntTimesInt")
operator fun ObservableValue<Int>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyIntDivInt")
operator fun ObservableValue<Int>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyIntRemInt")
operator fun ObservableValue<Int>.rem(value: Int) = mapBinding { it % value }

// Int - Long

@JvmName("propertyIntPlusLong")
operator fun ObservableValue<Int>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyIntMinusLong")
operator fun ObservableValue<Int>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyIntTimesLong")
operator fun ObservableValue<Int>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyIntDivLong")
operator fun ObservableValue<Int>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyIntRemLong")
operator fun ObservableValue<Int>.rem(value: Long) = mapBinding { it % value }

// Int - Float

@JvmName("propertyIntPlusFloat")
operator fun ObservableValue<Int>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyIntMinusFloat")
operator fun ObservableValue<Int>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyIntTimesFloat")
operator fun ObservableValue<Int>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyIntDivFloat")
operator fun ObservableValue<Int>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyIntRemFloat")
operator fun ObservableValue<Int>.rem(value: Float) = mapBinding { it % value }

// Int - Double

@JvmName("propertyIntPlusDouble")
operator fun ObservableValue<Int>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyIntMinusDouble")
operator fun ObservableValue<Int>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyIntTimesDouble")
operator fun ObservableValue<Int>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyIntDivDouble")
operator fun ObservableValue<Int>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyIntRemDouble")
operator fun ObservableValue<Int>.rem(value: Double) = mapBinding { it % value }

// Int - Short

@JvmName("propertyIntPlusShort")
operator fun ObservableValue<Int>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyIntMinusShort")
operator fun ObservableValue<Int>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyIntTimesShort")
operator fun ObservableValue<Int>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyIntDivShort")
operator fun ObservableValue<Int>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyIntRemShort")
operator fun ObservableValue<Int>.rem(value: Short) = mapBinding { it % value }

// Int - Byte

@JvmName("propertyIntPlusByte")
operator fun ObservableValue<Int>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyIntMinusByte")
operator fun ObservableValue<Int>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyIntTimesByte")
operator fun ObservableValue<Int>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyIntDivByte")
operator fun ObservableValue<Int>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyIntRemByte")
operator fun ObservableValue<Int>.rem(value: Byte) = mapBinding { it % value }

// Long - Int

@JvmName("propertyLongPlusInt")
operator fun ObservableValue<Long>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyLongMinusInt")
operator fun ObservableValue<Long>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyLongTimesInt")
operator fun ObservableValue<Long>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyLongDivInt")
operator fun ObservableValue<Long>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyLongRemInt")
operator fun ObservableValue<Long>.rem(value: Int) = mapBinding { it % value }

// Long - Long

@JvmName("propertyLongPlusLong")
operator fun ObservableValue<Long>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyLongMinusLong")
operator fun ObservableValue<Long>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyLongTimesLong")
operator fun ObservableValue<Long>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyLongDivLong")
operator fun ObservableValue<Long>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyLongRemLong")
operator fun ObservableValue<Long>.rem(value: Long) = mapBinding { it % value }

// Long - Float

@JvmName("propertyLongPlusFloat")
operator fun ObservableValue<Long>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyLongMinusFloat")
operator fun ObservableValue<Long>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyLongTimesFloat")
operator fun ObservableValue<Long>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyLongDivFloat")
operator fun ObservableValue<Long>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyLongRemFloat")
operator fun ObservableValue<Long>.rem(value: Float) = mapBinding { it % value }

// Long - Double

@JvmName("propertyLongPlusDouble")
operator fun ObservableValue<Long>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyLongMinusDouble")
operator fun ObservableValue<Long>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyLongTimesDouble")
operator fun ObservableValue<Long>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyLongDivDouble")
operator fun ObservableValue<Long>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyLongRemDouble")
operator fun ObservableValue<Long>.rem(value: Double) = mapBinding { it % value }

// Long - Short

@JvmName("propertyLongPlusShort")
operator fun ObservableValue<Long>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyLongMinusShort")
operator fun ObservableValue<Long>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyLongTimesShort")
operator fun ObservableValue<Long>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyLongDivShort")
operator fun ObservableValue<Long>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyLongRemShort")
operator fun ObservableValue<Long>.rem(value: Short) = mapBinding { it % value }

// Long - Byte

@JvmName("propertyLongPlusByte")
operator fun ObservableValue<Long>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyLongMinusByte")
operator fun ObservableValue<Long>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyLongTimesByte")
operator fun ObservableValue<Long>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyLongDivByte")
operator fun ObservableValue<Long>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyLongRemByte")
operator fun ObservableValue<Long>.rem(value: Byte) = mapBinding { it % value }

// Float - Int

@JvmName("propertyFloatPlusInt")
operator fun ObservableValue<Float>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyFloatMinusInt")
operator fun ObservableValue<Float>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyFloatTimesInt")
operator fun ObservableValue<Float>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyFloatDivInt")
operator fun ObservableValue<Float>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyFloatRemInt")
operator fun ObservableValue<Float>.rem(value: Int) = mapBinding { it % value }

// Float - Long

@JvmName("propertyFloatPlusLong")
operator fun ObservableValue<Float>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyFloatMinusLong")
operator fun ObservableValue<Float>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyFloatTimesLong")
operator fun ObservableValue<Float>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyFloatDivLong")
operator fun ObservableValue<Float>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyFloatRemLong")
operator fun ObservableValue<Float>.rem(value: Long) = mapBinding { it % value }

// Float - Float

@JvmName("propertyFloatPlusFloat")
operator fun ObservableValue<Float>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyFloatMinusFloat")
operator fun ObservableValue<Float>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyFloatTimesFloat")
operator fun ObservableValue<Float>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyFloatDivFloat")
operator fun ObservableValue<Float>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyFloatRemFloat")
operator fun ObservableValue<Float>.rem(value: Float) = mapBinding { it % value }

// Float - Double

@JvmName("propertyFloatPlusDouble")
operator fun ObservableValue<Float>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyFloatMinusDouble")
operator fun ObservableValue<Float>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyFloatTimesDouble")
operator fun ObservableValue<Float>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyFloatDivDouble")
operator fun ObservableValue<Float>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyFloatRemDouble")
operator fun ObservableValue<Float>.rem(value: Double) = mapBinding { it % value }

// Float - Short

@JvmName("propertyFloatPlusShort")
operator fun ObservableValue<Float>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyFloatMinusShort")
operator fun ObservableValue<Float>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyFloatTimesShort")
operator fun ObservableValue<Float>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyFloatDivShort")
operator fun ObservableValue<Float>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyFloatRemShort")
operator fun ObservableValue<Float>.rem(value: Short) = mapBinding { it % value }

// Float - Byte

@JvmName("propertyFloatPlusByte")
operator fun ObservableValue<Float>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyFloatMinusByte")
operator fun ObservableValue<Float>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyFloatTimesByte")
operator fun ObservableValue<Float>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyFloatDivByte")
operator fun ObservableValue<Float>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyFloatRemByte")
operator fun ObservableValue<Float>.rem(value: Byte) = mapBinding { it % value }

// Double - Int

@JvmName("propertyDoublePlusInt")
operator fun ObservableValue<Double>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyDoubleMinusInt")
operator fun ObservableValue<Double>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyDoubleTimesInt")
operator fun ObservableValue<Double>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyDoubleDivInt")
operator fun ObservableValue<Double>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyDoubleRemInt")
operator fun ObservableValue<Double>.rem(value: Int) = mapBinding { it % value }

// Double - Long

@JvmName("propertyDoublePlusLong")
operator fun ObservableValue<Double>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyDoubleMinusLong")
operator fun ObservableValue<Double>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyDoubleTimesLong")
operator fun ObservableValue<Double>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyDoubleDivLong")
operator fun ObservableValue<Double>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyDoubleRemLong")
operator fun ObservableValue<Double>.rem(value: Long) = mapBinding { it % value }

// Double - Float

@JvmName("propertyDoublePlusFloat")
operator fun ObservableValue<Double>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyDoubleMinusFloat")
operator fun ObservableValue<Double>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyDoubleTimesFloat")
operator fun ObservableValue<Double>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyDoubleDivFloat")
operator fun ObservableValue<Double>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyDoubleRemFloat")
operator fun ObservableValue<Double>.rem(value: Float) = mapBinding { it % value }

// Double - Double

@JvmName("propertyDoublePlusDouble")
operator fun ObservableValue<Double>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyDoubleMinusDouble")
operator fun ObservableValue<Double>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyDoubleTimesDouble")
operator fun ObservableValue<Double>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyDoubleDivDouble")
operator fun ObservableValue<Double>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyDoubleRemDouble")
operator fun ObservableValue<Double>.rem(value: Double) = mapBinding { it % value }

// Double - Short

@JvmName("propertyDoublePlusShort")
operator fun ObservableValue<Double>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyDoubleMinusShort")
operator fun ObservableValue<Double>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyDoubleTimesShort")
operator fun ObservableValue<Double>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyDoubleDivShort")
operator fun ObservableValue<Double>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyDoubleRemShort")
operator fun ObservableValue<Double>.rem(value: Short) = mapBinding { it % value }

// Double - Byte

@JvmName("propertyDoublePlusByte")
operator fun ObservableValue<Double>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyDoubleMinusByte")
operator fun ObservableValue<Double>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyDoubleTimesByte")
operator fun ObservableValue<Double>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyDoubleDivByte")
operator fun ObservableValue<Double>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyDoubleRemByte")
operator fun ObservableValue<Double>.rem(value: Byte) = mapBinding { it % value }

// Short - Int

@JvmName("propertyShortPlusInt")
operator fun ObservableValue<Short>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyShortMinusInt")
operator fun ObservableValue<Short>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyShortTimesInt")
operator fun ObservableValue<Short>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyShortDivInt")
operator fun ObservableValue<Short>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyShortRemInt")
operator fun ObservableValue<Short>.rem(value: Int) = mapBinding { it % value }

// Short - Long

@JvmName("propertyShortPlusLong")
operator fun ObservableValue<Short>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyShortMinusLong")
operator fun ObservableValue<Short>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyShortTimesLong")
operator fun ObservableValue<Short>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyShortDivLong")
operator fun ObservableValue<Short>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyShortRemLong")
operator fun ObservableValue<Short>.rem(value: Long) = mapBinding { it % value }

// Short - Float

@JvmName("propertyShortPlusFloat")
operator fun ObservableValue<Short>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyShortMinusFloat")
operator fun ObservableValue<Short>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyShortTimesFloat")
operator fun ObservableValue<Short>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyShortDivFloat")
operator fun ObservableValue<Short>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyShortRemFloat")
operator fun ObservableValue<Short>.rem(value: Float) = mapBinding { it % value }

// Short - Double

@JvmName("propertyShortPlusDouble")
operator fun ObservableValue<Short>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyShortMinusDouble")
operator fun ObservableValue<Short>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyShortTimesDouble")
operator fun ObservableValue<Short>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyShortDivDouble")
operator fun ObservableValue<Short>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyShortRemDouble")
operator fun ObservableValue<Short>.rem(value: Double) = mapBinding { it % value }

// Short - Short

@JvmName("propertyShortPlusShort")
operator fun ObservableValue<Short>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyShortMinusShort")
operator fun ObservableValue<Short>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyShortTimesShort")
operator fun ObservableValue<Short>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyShortDivShort")
operator fun ObservableValue<Short>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyShortRemShort")
operator fun ObservableValue<Short>.rem(value: Short) = mapBinding { it % value }

// Short - Byte

@JvmName("propertyShortPlusByte")
operator fun ObservableValue<Short>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyShortMinusByte")
operator fun ObservableValue<Short>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyShortTimesByte")
operator fun ObservableValue<Short>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyShortDivByte")
operator fun ObservableValue<Short>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyShortRemByte")
operator fun ObservableValue<Short>.rem(value: Byte) = mapBinding { it % value }

// Byte - Int

@JvmName("propertyBytePlusInt")
operator fun ObservableValue<Byte>.plus(value: Int) = mapBinding { it + value }

@JvmName("propertyByteMinusInt")
operator fun ObservableValue<Byte>.minus(value: Int) = mapBinding { it - value }

@JvmName("propertyByteTimesInt")
operator fun ObservableValue<Byte>.times(value: Int) = mapBinding { it * value }

@JvmName("propertyByteDivInt")
operator fun ObservableValue<Byte>.div(value: Int) = mapBinding { it / value }

@JvmName("propertyByteRemInt")
operator fun ObservableValue<Byte>.rem(value: Int) = mapBinding { it % value }

// Byte - Long

@JvmName("propertyBytePlusLong")
operator fun ObservableValue<Byte>.plus(value: Long) = mapBinding { it + value }

@JvmName("propertyByteMinusLong")
operator fun ObservableValue<Byte>.minus(value: Long) = mapBinding { it - value }

@JvmName("propertyByteTimesLong")
operator fun ObservableValue<Byte>.times(value: Long) = mapBinding { it * value }

@JvmName("propertyByteDivLong")
operator fun ObservableValue<Byte>.div(value: Long) = mapBinding { it / value }

@JvmName("propertyByteRemLong")
operator fun ObservableValue<Byte>.rem(value: Long) = mapBinding { it % value }

// Byte - Float

@JvmName("propertyBytePlusFloat")
operator fun ObservableValue<Byte>.plus(value: Float) = mapBinding { it + value }

@JvmName("propertyByteMinusFloat")
operator fun ObservableValue<Byte>.minus(value: Float) = mapBinding { it - value }

@JvmName("propertyByteTimesFloat")
operator fun ObservableValue<Byte>.times(value: Float) = mapBinding { it * value }

@JvmName("propertyByteDivFloat")
operator fun ObservableValue<Byte>.div(value: Float) = mapBinding { it / value }

@JvmName("propertyByteRemFloat")
operator fun ObservableValue<Byte>.rem(value: Float) = mapBinding { it % value }

// Byte - Double

@JvmName("propertyBytePlusDouble")
operator fun ObservableValue<Byte>.plus(value: Double) = mapBinding { it + value }

@JvmName("propertyByteMinusDouble")
operator fun ObservableValue<Byte>.minus(value: Double) = mapBinding { it - value }

@JvmName("propertyByteTimesDouble")
operator fun ObservableValue<Byte>.times(value: Double) = mapBinding { it * value }

@JvmName("propertyByteDivDouble")
operator fun ObservableValue<Byte>.div(value: Double) = mapBinding { it / value }

@JvmName("propertyByteRemDouble")
operator fun ObservableValue<Byte>.rem(value: Double) = mapBinding { it % value }

// Byte - Short

@JvmName("propertyBytePlusShort")
operator fun ObservableValue<Byte>.plus(value: Short) = mapBinding { it + value }

@JvmName("propertyByteMinusShort")
operator fun ObservableValue<Byte>.minus(value: Short) = mapBinding { it - value }

@JvmName("propertyByteTimesShort")
operator fun ObservableValue<Byte>.times(value: Short) = mapBinding { it * value }

@JvmName("propertyByteDivShort")
operator fun ObservableValue<Byte>.div(value: Short) = mapBinding { it / value }

@JvmName("propertyByteRemShort")
operator fun ObservableValue<Byte>.rem(value: Short) = mapBinding { it % value }

// Byte - Byte

@JvmName("propertyBytePlusByte")
operator fun ObservableValue<Byte>.plus(value: Byte) = mapBinding { it + value }

@JvmName("propertyByteMinusByte")
operator fun ObservableValue<Byte>.minus(value: Byte) = mapBinding { it - value }

@JvmName("propertyByteTimesByte")
operator fun ObservableValue<Byte>.times(value: Byte) = mapBinding { it * value }

@JvmName("propertyByteDivByte")
operator fun ObservableValue<Byte>.div(value: Byte) = mapBinding { it / value }

@JvmName("propertyByteRemByte")
operator fun ObservableValue<Byte>.rem(value: Byte) = mapBinding { it % value }

/*
 * primitive - Property
 */

// Int - Int

@JvmName("propertyIntPlusInt")
operator fun Int.plus(property: ObservableValue<Int>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusInt")
operator fun Int.minus(property: ObservableValue<Int>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesInt")
operator fun Int.times(property: ObservableValue<Int>) = property.mapBinding { this * it }

@JvmName("propertyIntDivInt")
operator fun Int.div(property: ObservableValue<Int>) = property.mapBinding { this / it }

@JvmName("propertyIntRemInt")
operator fun Int.rem(property: ObservableValue<Int>) = property.mapBinding { this % it }

// Int - Long

@JvmName("propertyIntPlusLong")
operator fun Int.plus(property: ObservableValue<Long>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusLong")
operator fun Int.minus(property: ObservableValue<Long>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesLong")
operator fun Int.times(property: ObservableValue<Long>) = property.mapBinding { this * it }

@JvmName("propertyIntDivLong")
operator fun Int.div(property: ObservableValue<Long>) = property.mapBinding { this / it }

@JvmName("propertyIntRemLong")
operator fun Int.rem(property: ObservableValue<Long>) = property.mapBinding { this % it }

// Int - Float

@JvmName("propertyIntPlusFloat")
operator fun Int.plus(property: ObservableValue<Float>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusFloat")
operator fun Int.minus(property: ObservableValue<Float>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesFloat")
operator fun Int.times(property: ObservableValue<Float>) = property.mapBinding { this * it }

@JvmName("propertyIntDivFloat")
operator fun Int.div(property: ObservableValue<Float>) = property.mapBinding { this / it }

@JvmName("propertyIntRemFloat")
operator fun Int.rem(property: ObservableValue<Float>) = property.mapBinding { this % it }

// Int - Double

@JvmName("propertyIntPlusDouble")
operator fun Int.plus(property: ObservableValue<Double>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusDouble")
operator fun Int.minus(property: ObservableValue<Double>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesDouble")
operator fun Int.times(property: ObservableValue<Double>) = property.mapBinding { this * it }

@JvmName("propertyIntDivDouble")
operator fun Int.div(property: ObservableValue<Double>) = property.mapBinding { this / it }

@JvmName("propertyIntRemDouble")
operator fun Int.rem(property: ObservableValue<Double>) = property.mapBinding { this % it }

// Int - Short

@JvmName("propertyIntPlusShort")
operator fun Int.plus(property: ObservableValue<Short>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusShort")
operator fun Int.minus(property: ObservableValue<Short>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesShort")
operator fun Int.times(property: ObservableValue<Short>) = property.mapBinding { this * it }

@JvmName("propertyIntDivShort")
operator fun Int.div(property: ObservableValue<Short>) = property.mapBinding { this / it }

@JvmName("propertyIntRemShort")
operator fun Int.rem(property: ObservableValue<Short>) = property.mapBinding { this % it }

// Int - Byte

@JvmName("propertyIntPlusByte")
operator fun Int.plus(property: ObservableValue<Byte>) = property.mapBinding { this + it }

@JvmName("propertyIntMinusByte")
operator fun Int.minus(property: ObservableValue<Byte>) = property.mapBinding { this - it }

@JvmName("propertyIntTimesByte")
operator fun Int.times(property: ObservableValue<Byte>) = property.mapBinding { this * it }

@JvmName("propertyIntDivByte")
operator fun Int.div(property: ObservableValue<Byte>) = property.mapBinding { this / it }

@JvmName("propertyIntRemByte")
operator fun Int.rem(property: ObservableValue<Byte>) = property.mapBinding { this % it }

// Long - Int

@JvmName("propertyLongPlusInt")
operator fun Long.plus(property: ObservableValue<Int>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusInt")
operator fun Long.minus(property: ObservableValue<Int>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesInt")
operator fun Long.times(property: ObservableValue<Int>) = property.mapBinding { this * it }

@JvmName("propertyLongDivInt")
operator fun Long.div(property: ObservableValue<Int>) = property.mapBinding { this / it }

@JvmName("propertyLongRemInt")
operator fun Long.rem(property: ObservableValue<Int>) = property.mapBinding { this % it }

// Long - Long

@JvmName("propertyLongPlusLong")
operator fun Long.plus(property: ObservableValue<Long>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusLong")
operator fun Long.minus(property: ObservableValue<Long>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesLong")
operator fun Long.times(property: ObservableValue<Long>) = property.mapBinding { this * it }

@JvmName("propertyLongDivLong")
operator fun Long.div(property: ObservableValue<Long>) = property.mapBinding { this / it }

@JvmName("propertyLongRemLong")
operator fun Long.rem(property: ObservableValue<Long>) = property.mapBinding { this % it }

// Long - Float

@JvmName("propertyLongPlusFloat")
operator fun Long.plus(property: ObservableValue<Float>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusFloat")
operator fun Long.minus(property: ObservableValue<Float>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesFloat")
operator fun Long.times(property: ObservableValue<Float>) = property.mapBinding { this * it }

@JvmName("propertyLongDivFloat")
operator fun Long.div(property: ObservableValue<Float>) = property.mapBinding { this / it }

@JvmName("propertyLongRemFloat")
operator fun Long.rem(property: ObservableValue<Float>) = property.mapBinding { this % it }

// Long - Double

@JvmName("propertyLongPlusDouble")
operator fun Long.plus(property: ObservableValue<Double>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusDouble")
operator fun Long.minus(property: ObservableValue<Double>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesDouble")
operator fun Long.times(property: ObservableValue<Double>) = property.mapBinding { this * it }

@JvmName("propertyLongDivDouble")
operator fun Long.div(property: ObservableValue<Double>) = property.mapBinding { this / it }

@JvmName("propertyLongRemDouble")
operator fun Long.rem(property: ObservableValue<Double>) = property.mapBinding { this % it }

// Long - Short

@JvmName("propertyLongPlusShort")
operator fun Long.plus(property: ObservableValue<Short>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusShort")
operator fun Long.minus(property: ObservableValue<Short>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesShort")
operator fun Long.times(property: ObservableValue<Short>) = property.mapBinding { this * it }

@JvmName("propertyLongDivShort")
operator fun Long.div(property: ObservableValue<Short>) = property.mapBinding { this / it }

@JvmName("propertyLongRemShort")
operator fun Long.rem(property: ObservableValue<Short>) = property.mapBinding { this % it }

// Long - Byte

@JvmName("propertyLongPlusByte")
operator fun Long.plus(property: ObservableValue<Byte>) = property.mapBinding { this + it }

@JvmName("propertyLongMinusByte")
operator fun Long.minus(property: ObservableValue<Byte>) = property.mapBinding { this - it }

@JvmName("propertyLongTimesByte")
operator fun Long.times(property: ObservableValue<Byte>) = property.mapBinding { this * it }

@JvmName("propertyLongDivByte")
operator fun Long.div(property: ObservableValue<Byte>) = property.mapBinding { this / it }

@JvmName("propertyLongRemByte")
operator fun Long.rem(property: ObservableValue<Byte>) = property.mapBinding { this % it }

// Float - Int

@JvmName("propertyFloatPlusInt")
operator fun Float.plus(property: ObservableValue<Int>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusInt")
operator fun Float.minus(property: ObservableValue<Int>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesInt")
operator fun Float.times(property: ObservableValue<Int>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivInt")
operator fun Float.div(property: ObservableValue<Int>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemInt")
operator fun Float.rem(property: ObservableValue<Int>) = property.mapBinding { this % it }

// Float - Long

@JvmName("propertyFloatPlusLong")
operator fun Float.plus(property: ObservableValue<Long>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusLong")
operator fun Float.minus(property: ObservableValue<Long>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesLong")
operator fun Float.times(property: ObservableValue<Long>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivLong")
operator fun Float.div(property: ObservableValue<Long>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemLong")
operator fun Float.rem(property: ObservableValue<Long>) = property.mapBinding { this % it }

// Float - Float

@JvmName("propertyFloatPlusFloat")
operator fun Float.plus(property: ObservableValue<Float>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusFloat")
operator fun Float.minus(property: ObservableValue<Float>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesFloat")
operator fun Float.times(property: ObservableValue<Float>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivFloat")
operator fun Float.div(property: ObservableValue<Float>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemFloat")
operator fun Float.rem(property: ObservableValue<Float>) = property.mapBinding { this % it }

// Float - Double

@JvmName("propertyFloatPlusDouble")
operator fun Float.plus(property: ObservableValue<Double>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusDouble")
operator fun Float.minus(property: ObservableValue<Double>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesDouble")
operator fun Float.times(property: ObservableValue<Double>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivDouble")
operator fun Float.div(property: ObservableValue<Double>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemDouble")
operator fun Float.rem(property: ObservableValue<Double>) = property.mapBinding { this % it }

// Float - Short

@JvmName("propertyFloatPlusShort")
operator fun Float.plus(property: ObservableValue<Short>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusShort")
operator fun Float.minus(property: ObservableValue<Short>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesShort")
operator fun Float.times(property: ObservableValue<Short>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivShort")
operator fun Float.div(property: ObservableValue<Short>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemShort")
operator fun Float.rem(property: ObservableValue<Short>) = property.mapBinding { this % it }

// Float - Byte

@JvmName("propertyFloatPlusByte")
operator fun Float.plus(property: ObservableValue<Byte>) = property.mapBinding { this + it }

@JvmName("propertyFloatMinusByte")
operator fun Float.minus(property: ObservableValue<Byte>) = property.mapBinding { this - it }

@JvmName("propertyFloatTimesByte")
operator fun Float.times(property: ObservableValue<Byte>) = property.mapBinding { this * it }

@JvmName("propertyFloatDivByte")
operator fun Float.div(property: ObservableValue<Byte>) = property.mapBinding { this / it }

@JvmName("propertyFloatRemByte")
operator fun Float.rem(property: ObservableValue<Byte>) = property.mapBinding { this % it }

// Double - Int

@JvmName("propertyDoublePlusInt")
operator fun Double.plus(property: ObservableValue<Int>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusInt")
operator fun Double.minus(property: ObservableValue<Int>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesInt")
operator fun Double.times(property: ObservableValue<Int>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivInt")
operator fun Double.div(property: ObservableValue<Int>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemInt")
operator fun Double.rem(property: ObservableValue<Int>) = property.mapBinding { this % it }

// Double - Long

@JvmName("propertyDoublePlusLong")
operator fun Double.plus(property: ObservableValue<Long>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusLong")
operator fun Double.minus(property: ObservableValue<Long>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesLong")
operator fun Double.times(property: ObservableValue<Long>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivLong")
operator fun Double.div(property: ObservableValue<Long>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemLong")
operator fun Double.rem(property: ObservableValue<Long>) = property.mapBinding { this % it }

// Double - Float

@JvmName("propertyDoublePlusFloat")
operator fun Double.plus(property: ObservableValue<Float>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusFloat")
operator fun Double.minus(property: ObservableValue<Float>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesFloat")
operator fun Double.times(property: ObservableValue<Float>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivFloat")
operator fun Double.div(property: ObservableValue<Float>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemFloat")
operator fun Double.rem(property: ObservableValue<Float>) = property.mapBinding { this % it }

// Double - Double

@JvmName("propertyDoublePlusDouble")
operator fun Double.plus(property: ObservableValue<Double>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusDouble")
operator fun Double.minus(property: ObservableValue<Double>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesDouble")
operator fun Double.times(property: ObservableValue<Double>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivDouble")
operator fun Double.div(property: ObservableValue<Double>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemDouble")
operator fun Double.rem(property: ObservableValue<Double>) = property.mapBinding { this % it }

// Double - Short

@JvmName("propertyDoublePlusShort")
operator fun Double.plus(property: ObservableValue<Short>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusShort")
operator fun Double.minus(property: ObservableValue<Short>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesShort")
operator fun Double.times(property: ObservableValue<Short>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivShort")
operator fun Double.div(property: ObservableValue<Short>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemShort")
operator fun Double.rem(property: ObservableValue<Short>) = property.mapBinding { this % it }

// Double - Byte

@JvmName("propertyDoublePlusByte")
operator fun Double.plus(property: ObservableValue<Byte>) = property.mapBinding { this + it }

@JvmName("propertyDoubleMinusByte")
operator fun Double.minus(property: ObservableValue<Byte>) = property.mapBinding { this - it }

@JvmName("propertyDoubleTimesByte")
operator fun Double.times(property: ObservableValue<Byte>) = property.mapBinding { this * it }

@JvmName("propertyDoubleDivByte")
operator fun Double.div(property: ObservableValue<Byte>) = property.mapBinding { this / it }

@JvmName("propertyDoubleRemByte")
operator fun Double.rem(property: ObservableValue<Byte>) = property.mapBinding { this % it }

// Short - Int

@JvmName("propertyShortPlusInt")
operator fun Short.plus(property: ObservableValue<Int>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusInt")
operator fun Short.minus(property: ObservableValue<Int>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesInt")
operator fun Short.times(property: ObservableValue<Int>) = property.mapBinding { this * it }

@JvmName("propertyShortDivInt")
operator fun Short.div(property: ObservableValue<Int>) = property.mapBinding { this / it }

@JvmName("propertyShortRemInt")
operator fun Short.rem(property: ObservableValue<Int>) = property.mapBinding { this % it }

// Short - Long

@JvmName("propertyShortPlusLong")
operator fun Short.plus(property: ObservableValue<Long>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusLong")
operator fun Short.minus(property: ObservableValue<Long>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesLong")
operator fun Short.times(property: ObservableValue<Long>) = property.mapBinding { this * it }

@JvmName("propertyShortDivLong")
operator fun Short.div(property: ObservableValue<Long>) = property.mapBinding { this / it }

@JvmName("propertyShortRemLong")
operator fun Short.rem(property: ObservableValue<Long>) = property.mapBinding { this % it }

// Short - Float

@JvmName("propertyShortPlusFloat")
operator fun Short.plus(property: ObservableValue<Float>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusFloat")
operator fun Short.minus(property: ObservableValue<Float>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesFloat")
operator fun Short.times(property: ObservableValue<Float>) = property.mapBinding { this * it }

@JvmName("propertyShortDivFloat")
operator fun Short.div(property: ObservableValue<Float>) = property.mapBinding { this / it }

@JvmName("propertyShortRemFloat")
operator fun Short.rem(property: ObservableValue<Float>) = property.mapBinding { this % it }

// Short - Double

@JvmName("propertyShortPlusDouble")
operator fun Short.plus(property: ObservableValue<Double>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusDouble")
operator fun Short.minus(property: ObservableValue<Double>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesDouble")
operator fun Short.times(property: ObservableValue<Double>) = property.mapBinding { this * it }

@JvmName("propertyShortDivDouble")
operator fun Short.div(property: ObservableValue<Double>) = property.mapBinding { this / it }

@JvmName("propertyShortRemDouble")
operator fun Short.rem(property: ObservableValue<Double>) = property.mapBinding { this % it }

// Short - Short

@JvmName("propertyShortPlusShort")
operator fun Short.plus(property: ObservableValue<Short>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusShort")
operator fun Short.minus(property: ObservableValue<Short>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesShort")
operator fun Short.times(property: ObservableValue<Short>) = property.mapBinding { this * it }

@JvmName("propertyShortDivShort")
operator fun Short.div(property: ObservableValue<Short>) = property.mapBinding { this / it }

@JvmName("propertyShortRemShort")
operator fun Short.rem(property: ObservableValue<Short>) = property.mapBinding { this % it }

// Short - Byte

@JvmName("propertyShortPlusByte")
operator fun Short.plus(property: ObservableValue<Byte>) = property.mapBinding { this + it }

@JvmName("propertyShortMinusByte")
operator fun Short.minus(property: ObservableValue<Byte>) = property.mapBinding { this - it }

@JvmName("propertyShortTimesByte")
operator fun Short.times(property: ObservableValue<Byte>) = property.mapBinding { this * it }

@JvmName("propertyShortDivByte")
operator fun Short.div(property: ObservableValue<Byte>) = property.mapBinding { this / it }

@JvmName("propertyShortRemByte")
operator fun Short.rem(property: ObservableValue<Byte>) = property.mapBinding { this % it }

// Byte - Int

@JvmName("propertyBytePlusInt")
operator fun Byte.plus(property: ObservableValue<Int>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusInt")
operator fun Byte.minus(property: ObservableValue<Int>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesInt")
operator fun Byte.times(property: ObservableValue<Int>) = property.mapBinding { this * it }

@JvmName("propertyByteDivInt")
operator fun Byte.div(property: ObservableValue<Int>) = property.mapBinding { this / it }

@JvmName("propertyByteRemInt")
operator fun Byte.rem(property: ObservableValue<Int>) = property.mapBinding { this % it }

// Byte - Long

@JvmName("propertyBytePlusLong")
operator fun Byte.plus(property: ObservableValue<Long>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusLong")
operator fun Byte.minus(property: ObservableValue<Long>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesLong")
operator fun Byte.times(property: ObservableValue<Long>) = property.mapBinding { this * it }

@JvmName("propertyByteDivLong")
operator fun Byte.div(property: ObservableValue<Long>) = property.mapBinding { this / it }

@JvmName("propertyByteRemLong")
operator fun Byte.rem(property: ObservableValue<Long>) = property.mapBinding { this % it }

// Byte - Float

@JvmName("propertyBytePlusFloat")
operator fun Byte.plus(property: ObservableValue<Float>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusFloat")
operator fun Byte.minus(property: ObservableValue<Float>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesFloat")
operator fun Byte.times(property: ObservableValue<Float>) = property.mapBinding { this * it }

@JvmName("propertyByteDivFloat")
operator fun Byte.div(property: ObservableValue<Float>) = property.mapBinding { this / it }

@JvmName("propertyByteRemFloat")
operator fun Byte.rem(property: ObservableValue<Float>) = property.mapBinding { this % it }

// Byte - Double

@JvmName("propertyBytePlusDouble")
operator fun Byte.plus(property: ObservableValue<Double>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusDouble")
operator fun Byte.minus(property: ObservableValue<Double>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesDouble")
operator fun Byte.times(property: ObservableValue<Double>) = property.mapBinding { this * it }

@JvmName("propertyByteDivDouble")
operator fun Byte.div(property: ObservableValue<Double>) = property.mapBinding { this / it }

@JvmName("propertyByteRemDouble")
operator fun Byte.rem(property: ObservableValue<Double>) = property.mapBinding { this % it }

// Byte - Short

@JvmName("propertyBytePlusShort")
operator fun Byte.plus(property: ObservableValue<Short>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusShort")
operator fun Byte.minus(property: ObservableValue<Short>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesShort")
operator fun Byte.times(property: ObservableValue<Short>) = property.mapBinding { this * it }

@JvmName("propertyByteDivShort")
operator fun Byte.div(property: ObservableValue<Short>) = property.mapBinding { this / it }

@JvmName("propertyByteRemShort")
operator fun Byte.rem(property: ObservableValue<Short>) = property.mapBinding { this % it }

// Byte - Byte

@JvmName("propertyBytePlusByte")
operator fun Byte.plus(property: ObservableValue<Byte>) = property.mapBinding { this + it }

@JvmName("propertyByteMinusByte")
operator fun Byte.minus(property: ObservableValue<Byte>) = property.mapBinding { this - it }

@JvmName("propertyByteTimesByte")
operator fun Byte.times(property: ObservableValue<Byte>) = property.mapBinding { this * it }

@JvmName("propertyByteDivByte")
operator fun Byte.div(property: ObservableValue<Byte>) = property.mapBinding { this / it }

@JvmName("propertyByteRemByte")
operator fun Byte.rem(property: ObservableValue<Byte>) = property.mapBinding { this % it }
