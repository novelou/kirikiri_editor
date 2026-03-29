package org.meirie.project.app.command

import org.meirie.project.app.AppState
import org.meirie.project.app.ScenarioItem

class UpdateCharaFaceCommand(
    private val appState: AppState,
    private val itemId: String,
    private val oldFace: String,
    private val newFace: String
) : Command {
    override fun execute() {
        update(newFace)
    }

    override fun undo() {
        update(oldFace)
    }

    private fun update(face: String) {
        val index = appState.scenarioItems.indexOfFirst { it.id == itemId }
        if (index == -1) return
        val item = appState.scenarioItems[index]
        if (item is ScenarioItem.CharaFace) {
            appState.scenarioItems[index] = item.copy(face = face)
        }
    }
}
