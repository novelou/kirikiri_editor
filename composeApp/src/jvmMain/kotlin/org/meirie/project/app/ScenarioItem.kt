package org.meirie.project.app

sealed class ScenarioItem {
    data class TalkBlock(
        val characterName: String?,
        val text: String,
        val endTag: String
    ) : ScenarioItem()

    data class CharaEvent(
        val characterName: String,
        val isVisible: Boolean
    ) : ScenarioItem()

    data class CommandBlock(
        val command: String
    ) : ScenarioItem()
}
