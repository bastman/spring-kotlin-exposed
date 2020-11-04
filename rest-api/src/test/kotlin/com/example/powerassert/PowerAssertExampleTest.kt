package com.example.powerassert

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class PowerAssertExampleTest {

    @Test
    fun foo() {
        1 shouldBeEqualTo 1
        val a=1
        val b=1
        assert(a==b)
    }
}
