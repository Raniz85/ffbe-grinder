/*
 * Copyright (c) 2017, Daniel Raniz Raneland
 */

package se.raneland.ffbe.ui

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import net.miginfocom.swing.MigLayout
import se.raneland.ffbe.state.MachineState
import se.raneland.ffbe.state.action.GameAction
import se.raneland.ffbe.state.transition.StateTransition
import se.raneland.ffbe.state.transition.TransitionTest
import java.awt.Dimension
import java.io.IOException
import java.io.InputStream
import java.time.Duration
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JTextArea

val DESCRIPTIONS = listOf(
        GraphDescription("Earth Shrine", "Runs the Earth Shrine over and over, uses lapis to refresh when out of NRG", "/machines/earth-shrine.yml"),
        GraphDescription("Earth Shrine (no lapis)", "Runs the Earth Shrine over and over, waits for energy to recover over time", "/machines/earth-shrine-no-lapis-refresh.yml"),
        GraphDescription("Floating Continent - PRO (no lapis)", "Runs the Floating Continent - PRO over and over, waits for energy to recover over time", "/machines/the-floating-continent-pro.yml"),
        GraphDescription("Craft", "Crafts the first item in the ATK tab over and over and over again", "/machines/craft.yml")
)


data class StateGraph(val name: String, val description: String, val initialState: MachineState)

/**
 * @author Raniz
 * @since 2017-04-19.
 */
class GraphSelector(owner: JFrame) : JDialog(owner, "Select", true) {

    val descriptions: JList<GraphDescription>
    val descriptionField: JTextArea
    val cancelButton: JButton
    val okButton: JButton

    var selectedGraph: StateGraph? = null

    init {
        layout = MigLayout(
                "fill",
                "[align left][align right]",
                "[fill][grow 0, shrink 0]"
        )

        descriptions = JList(DESCRIPTIONS.toTypedArray())
        descriptionField = JTextArea()
        cancelButton = JButton("Cancel")
        okButton = JButton("OK")

        add(descriptions, "cell 0 0, growy")
        add(descriptionField, "cell 1 0, grow")
        add(cancelButton, "cell 0 1")
        add(okButton, "cell 1 1")

        descriptions.addListSelectionListener {
            val selectedDescription = descriptions.selectedValue
            if (selectedDescription == null) {
                descriptionField.text = ""
                okButton.isEnabled = false
            } else {
                descriptionField.text = selectedDescription.description
                okButton.isEnabled = true
            }
        }
        descriptions.selectedIndex = 0

        descriptionField.lineWrap = true
        descriptionField.isEditable = false

        cancelButton.addActionListener {
            selectedGraph = null
            isVisible = false
        }

        okButton.addActionListener {
            val selectedDescription = descriptions.selectedValue
            if (selectedDescription == null) {
                JOptionPane.showMessageDialog(this, "No graph selected", "Error", JOptionPane.ERROR_MESSAGE)
            }
            selectedGraph = getResource(selectedDescription.path).use {
                if (it == null) {
                    JOptionPane.showMessageDialog(this, "Graph not found", "Error", JOptionPane.ERROR_MESSAGE)
                    return@use null
                }
                try {
                    return@use readStateGraph(it)
                } catch(e: IOException) {
                    JOptionPane.showMessageDialog(this, "Could not read graph: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
                    return@use null
                }
            }
            if (selectedGraph != null) {
                isVisible = false
            }
        }
        pack()
        minimumSize = Dimension(600, 600)
        preferredSize = minimumSize
        setLocationRelativeTo(owner)
    }

    fun select(): StateGraph? {
        isVisible = true
        return selectedGraph
    }
}

data class GraphDescription(val name: String, val description: String, val path: String) {
    override fun toString() = name
}

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

private fun getResource(path: String) = StringStateGraph::class.java.getResourceAsStream(path)

/**
 * @author Raniz
 * @since 2017-04-10.
 */
fun readStateGraph(yml: InputStream): StateGraph {
    val graph = yamlMapper.readValue<StringStateGraph>(yml)
    val states = graph.states.mapValues { (name, gState) -> MachineState(name, actions = gState.actions) }
    graph.states.forEach { (name, gState) ->
        val state = states[name]!!
        gState.transitions
                .map { (nextState, test) -> StateTransition(test, states[nextState] ?: error("State ${nextState} not found")) }
                .forEach { state.transitions.add(it) }
    }
    return StateGraph(graph.name, graph.description, states[graph.initialState]!!)
}

data class StringStateGraph(val name: String, val description: String, val initialState: String, val states: Map<String, StringState>)

data class StringState(val transitions: Map<String, TransitionTest> = mapOf(), val actions: List<GameAction> = listOf())
