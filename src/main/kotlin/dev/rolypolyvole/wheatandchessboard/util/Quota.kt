package dev.rolypolyvole.wheatandchessboard.util

import com.ezylang.evalex.Expression
import java.math.BigInteger
import java.math.RoundingMode

class Quota(private val function: String) {
    private val cache = HashMap<Int, BigInteger>()

    fun forDay(day: Int): BigInteger {
        return cache.getOrPut(day) { evaluate(function, day) }
    }

    companion object {
        fun evaluate(function: String, day: Int): BigInteger {
            return Expression(function)
                .with("t", day)
                .evaluate()
                .numberValue
                .setScale(0, RoundingMode.CEILING)
                .toBigInteger()
                .max(BigInteger.ZERO)
        }
    }
}
