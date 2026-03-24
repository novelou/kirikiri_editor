package org.meirie.project.app

sealed class UiItem {
    data class CharacterGroup(
        val characterName: String?,
        val boxes: List<DialogueBox>,
        val characterFace: String = "通常"
    ) : UiItem()
    
    data class Event(
        val item: ScenarioItem.CharaEvent
    ) : UiItem()
    
    data class Command(
        val item: ScenarioItem.CommandBlock
    ) : UiItem()
}

data class DialogueBox(
    val lines: List<ScenarioItem.TalkBlock>
)
