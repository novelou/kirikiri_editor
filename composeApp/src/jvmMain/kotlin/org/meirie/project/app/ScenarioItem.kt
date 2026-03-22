package org.meirie.project.app

sealed class ScenarioItem {
    abstract val id: String

    data class TalkBlock(
        val characterName: String?,
        val text: String,
        val endTag: String,
        override val id: String = java.util.UUID.randomUUID().toString()
    ) : ScenarioItem()

    data class CharaEvent(
        val characterName: String,
        val isVisible: Boolean,
        override val id: String = java.util.UUID.randomUUID().toString()
    ) : ScenarioItem()

    data class CommandBlock(
        val command: String,
        override val id: String = java.util.UUID.randomUUID().toString()
    ) : ScenarioItem()
}
