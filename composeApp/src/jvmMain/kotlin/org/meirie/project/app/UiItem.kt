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
    val lines: List<DialogueLine>
)

sealed class DialogueLine {
    data class Talk(
        val item: ScenarioItem.TalkBlock
    ) : DialogueLine()

    data class Face(
        val item: ScenarioItem.CharaFace
    ) : DialogueLine()
}
