package org.meirie.project.app

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*

val characters = listOf("None", "キャラA", "キャラB", "キャラC", "キャラD")

class AppState {
    val scenarioItems = mutableStateListOf<ScenarioItem>()
    var currentInput by mutableStateOf("")
    var selectedCharacterIndex by mutableStateOf(0)

    val uiItems: List<UiItem> by derivedStateOf {
        val result = mutableListOf<UiItem>()
        var currentCharacterName: String? = null
        val currentBoxes = mutableListOf<DialogueBox>()
        val currentLines = mutableListOf<ScenarioItem.TalkBlock>()
        
        fun flushBox() {
            if (currentLines.isNotEmpty()) {
                currentBoxes.add(DialogueBox(currentLines.toList()))
                currentLines.clear()
            }
        }
        
        fun flushCharacter() {
            flushBox()
            if (currentBoxes.isNotEmpty()) {
                result.add(UiItem.CharacterGroup(currentCharacterName, currentBoxes.toList()))
                currentBoxes.clear()
            }
        }

        var startNewBoxNext = false

        for (item in scenarioItems) {
            when (item) {
                is ScenarioItem.TalkBlock -> {
                    if (item.characterName != currentCharacterName) {
                        flushCharacter()
                        currentCharacterName = item.characterName
                        startNewBoxNext = false
                    } else if (startNewBoxNext) {
                        flushBox()
                        startNewBoxNext = false
                    }
                    
                    currentLines.add(item)
                    
                    if (item.endTag.contains("[cm]")) {
                        startNewBoxNext = true
                    }
                }
                is ScenarioItem.CharaEvent -> {
                    flushCharacter()
                    result.add(UiItem.Event(item))
                    currentCharacterName = null
                }
                is ScenarioItem.CommandBlock -> {
                    flushCharacter()
                    result.add(UiItem.Command(item))
                    currentCharacterName = null
                }
            }
        }
        flushCharacter()
        result
    }

    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type != KeyEventType.KeyDown) return false
        
        return when (keyEvent.key) {
            Key.F1 -> { selectedCharacterIndex = 0; true }
            Key.F2 -> { selectedCharacterIndex = 1; true }
            Key.F3 -> { selectedCharacterIndex = 2; true }
            Key.F4 -> { selectedCharacterIndex = 3; true }
            Key.F5 -> { selectedCharacterIndex = 4; true }
            Key.Enter -> {
                val isShiftPressed = keyEvent.isShiftPressed
                val isCtrlPressed = keyEvent.isCtrlPressed
                
                val endTag = when {
                    isCtrlPressed && isShiftPressed -> "[l][cm]\n"
                    isCtrlPressed -> "[p]\n"
                    isShiftPressed -> "[l][r]\n"
                    else -> "[r]\n"
                }
                
                val charName = if (selectedCharacterIndex > 0) characters[selectedCharacterIndex] else null
                
                if (currentInput.isNotEmpty() || endTag != "[r]\n") {
                    scenarioItems.add(ScenarioItem.TalkBlock(charName, currentInput, endTag))
                    currentInput = ""
                }
                true
            }
            else -> false
        }
    }

    fun export() {
        exportScenario(scenarioItems)
    }
}

@Composable
fun rememberAppState() = remember { AppState() }
