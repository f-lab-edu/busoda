package com.chaeny.busoda.stopdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
interface Operator {
    fun operate(a: Int, b: Int): Int
}

class PlusOperator : Operator {
    override fun operate(a: Int, b: Int): Int {
        return a + b
    }
}

interface AsyncOperator {
    suspend fun operate(a: Int, b: Int): Int
}

class ExampleUnitTest {

    @Test
    fun `mock test`() {
        val calculator = mockk<Operator>(relaxed = true)
        val result = calculator.operate(2, 3)
        assertEquals(0, result)
    }

    @Test
    fun `stub test`() {
        val calculator = mockk<Operator>()
        every { calculator.operate(2, 3) } returns 5
        val result = calculator.operate(2, 3)
        assertEquals(5, result)
    }

    @Test
    fun `any test`() {
        val calculator = mockk<Operator>()
        every { calculator.operate(any(), any()) } returns 5
        val result = calculator.operate(3, 7)
        assertEquals(5, result)
    }

    @Test
    fun `spy test`() {
        val spyCalculator = spyk(PlusOperator())
        every { spyCalculator.operate(2, 3) } returns 100

        val result1 = spyCalculator.operate(2, 3)
        val result2 = spyCalculator.operate(5, 5)

        assertEquals(100, result1)
        assertEquals(10, result2)
    }

    @Test
    fun `verify test`() {
        val calculator = mockk<Operator>()
        every { calculator.operate(2, 3) } returns 5

        calculator.operate(2, 3)
        calculator.operate(2, 3)

        verify(atLeast = 1) { calculator.operate(2, 3) }
        verify(exactly = 2) { calculator.operate(2, 3) }
    }

    @Test
    fun `answers test`() {
        val calculator = mockk<Operator>()
        every { calculator.operate(any(), any()) } answers {
            val a = firstArg<Int>()
            val b = secondArg<Int>()
            a * b
        }

        val result = calculator.operate(2, 3)
        assertEquals(6, result)
    }

    @Test
    fun `slot test`() {
        val calculator = mockk<Operator>()
        val slot = slot<Int>()
        every { calculator.operate(capture(slot), any()) } returns 5
        calculator.operate(2, 3)
        assertEquals(2, slot.captured)
    }

    @Test
    fun `capture test`() {
        val calculator = mockk<Operator>()
        val list = mutableListOf<Int>()
        every { calculator.operate(capture(list), any()) } returns 10

        calculator.operate(1, 2)
        calculator.operate(3, 4)
        calculator.operate(5, 6)

        assertEquals(listOf(1, 3, 5), list)
    }

    @Test
    fun `coEvery test`() = runBlocking {
        val calculator = mockk<AsyncOperator>()
        coEvery { calculator.operate(2, 3) } returns 5
        val result = calculator.operate(2, 3)
        assertEquals(5, result)
    }

    @Test
    fun `coVerify test`() = runBlocking {
        val calculator = mockk<AsyncOperator>()
        coEvery { calculator.operate(any(), any()) } returns 10

        calculator.operate(1, 2)
        calculator.operate(3, 4)

        coVerify(exactly = 2) { calculator.operate(any(), any()) }
    }

    @Test
    fun `mockk DSL test`() {
        val calculator = mockk<Operator> {
            every { operate(1, 2) } returns 10
            every { operate(any(), any()) } returns 50
        }

        calculator.operate(1, 2)
        calculator.operate(3, 4)

        verify { calculator.operate(1, 2) }
        verify(atLeast = 1) { calculator.operate(any(), any()) }
    }
}
