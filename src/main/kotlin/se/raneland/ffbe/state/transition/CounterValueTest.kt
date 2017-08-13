/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state.transition

import com.fasterxml.jackson.annotation.JsonProperty
import se.raneland.ffbe.state.GameState

/**
 * @author Raniz
 * @since 13/08/17.
 */
class CounterValueTest(val name: String, val value: Int, val test: ComparisonOperator) : TransitionTest {

    override fun matches(gameState: GameState): Boolean {
        return test.test(gameState.counters.getOrDefault(name, 0), value)
    }

    override fun toString(): String {
        return "CounterValueTest(${name} ${test} ${value})"
    }
}

enum class ComparisonOperator {
    @JsonProperty("lt") LT,
    @JsonProperty("le") LE,
    @JsonProperty("eq") EQ,
    @JsonProperty("ge") GE,
    @JsonProperty("gt") GT,
    @JsonProperty("not") NOT,
    ;

    fun test(a: Int, b: Int): Boolean {
        return when (this) {
            LT -> a < b
            LE -> a <= b
            EQ -> a == b
            GE -> a >= b
            GT -> a > b
            NOT -> a != b
        }
    }

    override fun toString(): String {
        return when (this) {
            LT -> "<"
            LE -> "<="
            EQ -> "=="
            GE -> ">="
            GT -> ">"
            NOT -> "!="
        }
    }
}
