package org.meirie.project.app.command

import org.meirie.project.app.AppState
import org.meirie.project.app.ScenarioItem

class UpdateCharacterGroupFaceCommand(
    private val appState: AppState,
    private val characterName: String?,
    private val firstItemId: String,
    private val newFace: String
) : Command {
    private val oldFaces = mutableMapOf<String, String>()

    override fun execute() {
        val firstItemIndex = appState.scenarioItems.indexOfFirst { it.id == firstItemId }
        if (firstItemIndex != -1) {
            val firstItem = appState.scenarioItems[firstItemIndex]
            if (firstItem is ScenarioItem.TalkBlock && firstItem.characterName == characterName) {
                // このグループの開始位置を見つける
                var groupStart = firstItemIndex
                while (groupStart > 0) {
                    val prev = appState.scenarioItems[groupStart - 1]
                    if (prev is ScenarioItem.TalkBlock && prev.characterName == characterName && !prev.groupBreak) {
                        groupStart--
                    } else {
                        break
                    }
                }

                // このグループの終了位置を見つける
                var groupEnd = firstItemIndex
                while (groupEnd < appState.scenarioItems.size - 1) {
                    val next = appState.scenarioItems[groupEnd + 1]
                    if (next is ScenarioItem.TalkBlock && next.characterName == characterName && !next.groupBreak) {
                        groupEnd++
                    } else {
                        break
                    }
                }

                // このグループ内のすべてのTalkBlockを更新し、古い顔を保存
                for (i in groupStart..groupEnd) {
                    val groupItem = appState.scenarioItems[i]
                    if (groupItem is ScenarioItem.TalkBlock) {
                        oldFaces[groupItem.id] = groupItem.characterFace
                        appState.scenarioItems[i] = groupItem.copy(characterFace = newFace)
                    }
                }
            }
        }
    }

    override fun undo() {
        // 保存した古い顔を元に戻す
        for ((id, oldFace) in oldFaces) {
            val index = appState.scenarioItems.indexOfFirst { it.id == id }
            if (index != -1) {
                val item = appState.scenarioItems[index]
                if (item is ScenarioItem.TalkBlock) {
                    appState.scenarioItems[index] = item.copy(characterFace = oldFace)
                }
            }
        }
    }
}