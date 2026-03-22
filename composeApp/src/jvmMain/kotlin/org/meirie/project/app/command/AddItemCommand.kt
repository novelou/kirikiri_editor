package org.meirie.project.app.command

import org.meirie.project.app.AppState
import org.meirie.project.app.command.Command
import org.meirie.project.app.ScenarioItem

class AddItemCommand(
    private val appState: AppState,
    private val item: ScenarioItem
) : Command {
    override fun execute() {
        appState.scenarioItems.add(item)
    }

    override fun undo() {
        appState.scenarioItems.remove(item)
    }

}
