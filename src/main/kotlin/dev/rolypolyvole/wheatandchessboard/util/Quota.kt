package dev.rolypolyvole.wheatandchessboard.util

import com.ezylang.evalex.Expression
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import java.math.BigInteger
import java.math.RoundingMode

object Quota {
    private val cache = HashMap<Int, BigInteger>()

    fun forDay(day: Int): BigInteger {
        return cache.getOrPut(day) { evaluate(ModConfig.config.function, day) }
    }

    fun evaluate(function: String, day: Int): BigInteger {
        return Expression(function)
            .with("t", day)
            .evaluate()
            .numberValue
            .setScale(0, RoundingMode.CEILING)
            .toBigInteger()
            .max(BigInteger.ZERO)
    }

    fun clear() = cache.clear()
}
