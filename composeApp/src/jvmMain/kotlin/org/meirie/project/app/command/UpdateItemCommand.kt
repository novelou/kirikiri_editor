package org.meirie.project.app.command

import org.meirie.project.app.AppState
import org.meirie.project.app.ScenarioItem

class UpdateItemCommand(
    private val appState: AppState,
    private val itemId: String,
    private val oldText: String,
    private val oldEndTag: String,
    private val oldFontSizePx: Int,
    private val newText: String,
    private val newEndTag: String,
    private val newFontSizePx: Int
) : Command {
    override fun execute() {
        // 直接scenarioItemsを更新（循環参照を避ける）
        val index = appState.scenarioItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = appState.scenarioItems[index]
            if (item is ScenarioItem.TalkBlock) {
                appState.scenarioItems[index] = item.copy(
                    text = newText,
                    endTag = newEndTag,
                    fontSizePx = newFontSizePx
                )
            }
        }
    }

    override fun undo() {
        // 直接scenarioItemsを更新
        val index = appState.scenarioItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = appState.scenarioItems[index]
            if (item is ScenarioItem.TalkBlock) {
                appState.scenarioItems[index] = item.copy(
                    text = oldText,
                    endTag = oldEndTag,
                    fontSizePx = oldFontSizePx
                )
            }
        }
    }
}
