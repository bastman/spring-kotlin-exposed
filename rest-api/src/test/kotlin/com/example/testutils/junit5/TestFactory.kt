package com.example.testutils.junit5

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.function.Executable
import java.util.stream.Stream

class TestContainerBuilder(private var name: String) : TestProvider, ContainerProvider {
    private val nodes: MutableList<DynamicNode> = mutableListOf()
    fun name(value: String) {
        name = value
    }

    fun name(): String = name

    override fun test(name: String, test: () -> Any?) {
        val node = dynamicTest(name, test)
        nodes.add(node)
    }

    override fun container(name: String, init: TestContainerBuilder.() -> Unit) {
        val node = containerBuilder(name = name, init = init)
        nodes.add(node)
    }

    operator fun invoke(): DynamicContainer = build()
    private fun build(): DynamicContainer = dynamicContainer(name, nodes.toList())
}

private fun containerBuilder(name: String, init: TestContainerBuilder.() -> Unit): DynamicContainer {
    return TestContainerBuilder(name = name)
            .apply(init)()
}

class TestFactoryBuilder : TestProvider, ContainerProvider {
    private val nodes: MutableList<DynamicNode> = mutableListOf()

    override fun test(name: String, test: () -> Any?) {
        val node = dynamicTest(name, test)
        nodes.add(node)
    }

    override fun container(name: String, init: TestContainerBuilder.() -> Unit) {
        val node = containerBuilder(name = name, init = init)
        nodes.add(node)
    }

    operator fun invoke(): Stream<out DynamicNode> = nodes.stream()
}

fun testFactory(init: TestFactoryBuilder.() -> Unit): Stream<out DynamicNode> = TestFactoryBuilder()
        .apply(init)()

private interface TestProvider {
    fun test(name: String, test: () -> Any?)
}

private interface ContainerProvider {
    fun container(name: String, init: TestContainerBuilder.() -> Unit)
}

private fun dynamicContainer(name: String, nodes: List<DynamicNode>): DynamicContainer =
        DynamicContainer.dynamicContainer(name, nodes)

private fun dynamicTest(name: String, test: () -> Any?): DynamicTest =
        DynamicTest.dynamicTest(name, executable(test))

private fun executable(test: () -> Any?): Executable =
        Executable {
            test.invoke()
            Unit
        }
