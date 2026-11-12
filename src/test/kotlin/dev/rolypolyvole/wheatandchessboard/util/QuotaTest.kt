package dev.rolypolyvole.wheatandchessboard.util

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class QuotaTest {
    @Test
    fun doublesEachDay() {
        assertEquals(BigInteger.ONE, Quota.evaluate("2^(t-1)", 1))
        assertEquals(BigInteger.valueOf(8192), Quota.evaluate("2^(t-1)", 14))
    }

    @Test
    fun staysExactBeyondLongRange() {
        assertEquals(BigInteger.TWO.pow(63), Quota.evaluate("2^(t-1)", 64))
        assertEquals(BigInteger.TWO.pow(99), Quota.evaluate("2^(t-1)", 100))
    }

    @Test
    fun roundsFractionsUp() {
        assertEquals(BigInteger.valueOf(4), Quota.evaluate("1.5^t", 3))
        assertEquals(BigInteger.valueOf(15), Quota.evaluate("10 + t / 2", 9))
    }

    @Test
    fun neverGoesNegative() {
        assertEquals(BigInteger.ZERO, Quota.evaluate("t - 10", 3))
    }
}
