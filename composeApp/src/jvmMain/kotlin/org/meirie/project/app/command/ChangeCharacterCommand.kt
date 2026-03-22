package org.meirie.project.app.command

import org.meirie.project.app.AppState
import org.meirie.project.app.command.Command

class ChangeCharacterCommand(
    private val appState: AppState,
    private val itemId: String,
    private val oldCharacterIndex: Int,
    private val newCharacterIndex: Int
) : Command {
    override fun execute() {
        appState.changeCharacter(itemId, newCharacterIndex)
    }

    override fun undo() {
        appState.changeCharacter(itemId, oldCharacterIndex)
    }
}