package org.meirie.project.app.command

interface Command {
    fun execute()
    fun undo()
}
