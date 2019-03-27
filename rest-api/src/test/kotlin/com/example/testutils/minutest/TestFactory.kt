package com.example.testutils.minutest

import dev.minutest.TestContextBuilder
import dev.minutest.junit.toTestFactory
import dev.minutest.rootContext
import org.junit.jupiter.api.DynamicNode
import java.util.stream.Stream

@JvmName("minuTestFactoryForClass")
inline fun <reified F> minuTestFactory(
        noinline builder: TestContextBuilder<Unit, F>.() -> Unit
): Stream<out DynamicNode> = rootContext(builder).toTestFactory()


fun minuTestFactory(builder: TestContextBuilder<Unit, Unit>.() -> Unit): Stream<out DynamicNode> =
        minuTestFactory<Unit>(builder)
