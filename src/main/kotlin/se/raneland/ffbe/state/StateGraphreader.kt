/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.state

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import se.raneland.ffbe.state.action.GameAction
import se.raneland.ffbe.state.transition.StateTransition
import se.raneland.ffbe.state.transition.TransitionTest
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.stream.Collectors

class DurationDeserialiser: JsonDeserializer<Duration>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Duration = Duration.parse(p.valueAsString)
}

private val yamlMapper = ObjectMapper(YAMLFactory()).apply {
    findAndRegisterModules()
    registerModule(object: SimpleModule() {
        init {
            addDeserializer(Duration::class.java, DurationDeserialiser())
        }
    })
    enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
}

private val bundledGraphs : Map<String, URL> = mapOf(
        "earth-shrine.yml" to getResource("/machines/earth-shrine.yml"),
        "earth-shrine-no-lapis-refresh.yml" to getResource("/machines/earth-shrine-no-lapis-refresh.yml"),
        "beast-on-the-plains-no-lapis-refresh.yml" to getResource("/machines/beast-on-the-plains-no-lapis-refresh.yml")
)

private fun getResource(path: String) = StringStateGraph::class.java.getResource(path)

fun listAvailableGraphs(): Map<String, URL>  {
    val files : MutableMap<String, URL> = Files.walk(Paths.get("."))
            .filter { it.fileName.toString().endsWith(".yml") || it.fileName.toString().endsWith(".yaml") }
            .collect(Collectors.toMap({ it.relativize(Paths.get(".")).toString() }, { it.toUri().toURL() }, { a, b -> b }))
    bundledGraphs.forEach { name, url -> files.putIfAbsent(name, url) }
    return files
}

/**
 * @author Raniz
 * @since 2017-04-10.
 */
fun readStateGraph(yml: String): StateGraph {
    val graph = yamlMapper.readValue<StringStateGraph>(yml)
    val states = graph.states.mapValues { (name, gState) -> State(name, actions = gState.actions) }
    graph.states.forEach { (name, gState) ->
        val state = states[name]!!
        gState.transitions
                .map { (nextState, test) -> StateTransition(test, states[nextState] ?: error("State ${nextState} not found")) }
                .forEach { state.transitions.add(it) }
    }
    return StateGraph(graph.name, graph.description, states[graph.initialState]!!)
}

data class StateGraph(val name: String, val description: String, val initialState: State)

data class StringStateGraph(val name: String, val description: String, val initialState: String, val states: Map<String, StringState>)

data class StringState(val transitions: Map<String, TransitionTest> = mapOf(), val actions: List<GameAction> = listOf())
