package org.meirie.project.app

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import org.meirie.project.app.command.AddItemCommand
import org.meirie.project.app.command.ChangeCharacterCommand
import org.meirie.project.app.command.Command
import org.meirie.project.app.command.UpdateItemCommand

val characters = listOf("None", "キャラA", "キャラB", "キャラC", "キャラD")

class AppState {
    val scenarioItems = mutableStateListOf<ScenarioItem>()
    private val undoStack = mutableListOf<Command>()
    private val redoStack = mutableListOf<Command>()
    var currentInput by mutableStateOf("")
    var selectedCharacterIndex by mutableStateOf(0)
    var pressedKeys by mutableStateOf(setOf<Key>())
    var exportFileName by mutableStateOf("output")


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
        if (keyEvent.type == KeyEventType.KeyDown) {
            pressedKeys = pressedKeys + keyEvent.key
        } else if (keyEvent.type == KeyEventType.KeyUp) {
            pressedKeys = pressedKeys - keyEvent.key
        }
        
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
                    val newItem = ScenarioItem.TalkBlock(charName, currentInput, endTag)
                    executeCommand(AddItemCommand(this, newItem))  // Command経由で追加
                    currentInput = ""
                }
                true
            }
            else -> false
        }
    }

    fun updateItem(id: String, text: String, endTag: String) {
        val index = scenarioItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = scenarioItems[index]
            if (item is ScenarioItem.TalkBlock) {
                val oldText = item.text
                val oldEndTag = item.endTag
                executeCommand(UpdateItemCommand(this, id, oldText, oldEndTag, text, endTag))
            }
        }
    }

    fun changeCharacter(id: String, characterIndex: Int) {
        val index = scenarioItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val charName = if (characterIndex > 0) characters[characterIndex] else null

            val clickedItem = scenarioItems[index]
            if (clickedItem is ScenarioItem.TalkBlock) {
                val oldCharName = clickedItem.characterName
                
                // Go backwards
                var start = index
                while (start > 0) {
                    val prev = scenarioItems[start - 1]
                    if (prev is ScenarioItem.TalkBlock && prev.characterName == oldCharName) {
                        start--
                    } else {
                        break
                    }
                }
                
                // Go forwards
                var end = index
                while (end < scenarioItems.size - 1) {
                    val next = scenarioItems[end + 1]
                    if (next is ScenarioItem.TalkBlock && next.characterName == oldCharName) {
                        end++
                    } else {
                        break
                    }
                }
                
                for (i in start..end) {
                    val item = scenarioItems[i]
                    if (item is ScenarioItem.TalkBlock) {
                        scenarioItems[i] = item.copy(characterName = charName)
                    }
                }
            }
        }
    }

    fun changeCharacterViaCommand(id: String, newCharacterIndex: Int) {
        val index = scenarioItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val clickedItem = scenarioItems[index]
            if (clickedItem is ScenarioItem.TalkBlock) {
                val oldCharName = clickedItem.characterName
                val oldCharacterIndex = characters.indexOf(oldCharName ?: "None")  // oldCharNameからインデックスを取得
                executeCommand(ChangeCharacterCommand(this, id, oldCharacterIndex, newCharacterIndex))
            }
        }
    }

    fun undo(){
        if (undoStack.isNotEmpty()) {
            val command = undoStack.removeLast()
            command.undo()
            redoStack.add(command)
            if (redoStack.size > 50) {
                redoStack.removeAt(0)
            }
        }
    }

    fun redo(){
        if (redoStack.isNotEmpty()) {
            val command = redoStack.removeLast()
            command.execute()
            undoStack.add(command)
            if(undoStack.size > 50){
                undoStack.removeAt(0)
            }
        }
    }

    private fun executeCommand(command: Command){
        command.execute()
        undoStack.add(command)
        if(undoStack.size > 50){
            undoStack.removeAt(0)
        }
        redoStack.clear()
    }

    fun export(fileName: String = "output.kt") {
        exportScenario(scenarioItems,fileName)
    }
}

@Composable
fun rememberAppState() = remember { AppState() }


