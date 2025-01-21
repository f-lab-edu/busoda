package com.chaeny.busoda.stoplist

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun addition_isNotEqual() {
        assertNotEquals(5, 2 + 2)
    }

    @Test
    fun addition_isTrue() {
        assertTrue(true)
    }

    @Test
    fun addition_isFalse() {
        assertFalse(false)
    }

    @Test
    fun addition_isNotNull() {
        assertNotNull("cy")
    }

    @Test
    fun addition_isNull() {
        assertNull(null)
    }

    private fun isEven(number: Int): Boolean {
        return number % 2 == 0
    }

    @Test
    fun testIsEven() {
        assertTrue(isEven(4))
        assertFalse(isEven(5))
    }

    private fun divide(a: Int, b: Int): Int {
        if (b == 0) throw IllegalArgumentException("나누기의 경우에는 0 이 아닌 숫자로 나누어 주세요.")
        return a / b
    }

    @Test
    fun testDivideThrowsException() {
        assertThrows(IllegalArgumentException::class.java) {
            divide(10, 0)
        }
    }
}
