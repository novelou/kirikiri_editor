package org.meirie.project.app

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import org.meirie.project.app.command.AddItemCommand
import org.meirie.project.app.command.ChangeCharacterCommand
import org.meirie.project.app.command.Command
import org.meirie.project.app.command.UpdateCharaFaceCommand
import org.meirie.project.app.command.UpdateCharacterGroupFaceCommand
import org.meirie.project.app.command.UpdateItemCommand

val characters = listOf("None", "キャラA", "キャラB", "キャラC", "キャラD")
val characterFaceOptions = listOf("通常", "笑", "泣")

class AppState {
    val scenarioItems = mutableStateListOf<ScenarioItem>()
    private val undoStack = mutableListOf<Command>()
    private val redoStack = mutableListOf<Command>()
    var currentInput by mutableStateOf("")
    var selectedCharacterIndex by mutableStateOf(0)
    var pressedKeys by mutableStateOf(setOf<Key>())
    var exportFileName by mutableStateOf("output")
    var exportMessage by mutableStateOf("")


    val uiItems: List<UiItem> by derivedStateOf {
        val result = mutableListOf<UiItem>()
        var currentCharacterName: String? = null
        val currentBoxes = mutableListOf<DialogueBox>()
        val currentLines = mutableListOf<DialogueLine>()
        
        fun flushBox() {
            if (currentLines.isNotEmpty()) {
                currentBoxes.add(DialogueBox(currentLines.toList()))
                currentLines.clear()
            }
        }
        
        fun flushCharacter() {
            flushBox()
            if (currentBoxes.isNotEmpty()) {
                val characterFace = currentBoxes
                    .asSequence()
                    .flatMap { it.lines.asSequence() }
                    .mapNotNull { (it as? DialogueLine.Talk)?.item?.characterFace }
                    .firstOrNull()
                    ?: "通常"
                result.add(UiItem.CharacterGroup(currentCharacterName, currentBoxes.toList(), characterFace))
                currentBoxes.clear()
            }
        }

        var startNewBoxNext = false
        var previousItemWasGroupBreak = false

        for (item in scenarioItems) {
            when (item) {
                is ScenarioItem.TalkBlock -> {
                    if (item.characterName != currentCharacterName || previousItemWasGroupBreak) {
                        flushCharacter()
                        currentCharacterName = item.characterName
                        startNewBoxNext = false
                        previousItemWasGroupBreak = false
                    } else if (startNewBoxNext) {
                        flushBox()
                        startNewBoxNext = false
                    }
                    
                    currentLines.add(DialogueLine.Talk(item))
                    
                    if (item.endTag.contains("[cm]")) {
                        startNewBoxNext = true
                    }
                    
                    previousItemWasGroupBreak = item.groupBreak
                }
                is ScenarioItem.CharaEvent -> {
                    flushCharacter()
                    result.add(UiItem.Event(item))
                    currentCharacterName = null
                    previousItemWasGroupBreak = false
                }
                is ScenarioItem.CommandBlock -> {
                    flushCharacter()
                    result.add(UiItem.Command(item))
                    currentCharacterName = null
                    previousItemWasGroupBreak = false
                }
                is ScenarioItem.CharaFace -> {
                    if (startNewBoxNext) {
                        flushBox()
                        startNewBoxNext = false
                    }
                    currentLines.add(DialogueLine.Face(item))
                    previousItemWasGroupBreak = false
                }
            }
        }
        flushCharacter()
        result
    }

    fun handleKeyEvent(keyEvent: KeyEvent): Boolean {
        val previousPressedKeys = pressedKeys

        if (keyEvent.type == KeyEventType.KeyDown) {
            pressedKeys = pressedKeys + keyEvent.key
        } else if (keyEvent.type == KeyEventType.KeyUp) {
            pressedKeys = pressedKeys - keyEvent.key
        }
        
        if (keyEvent.type != KeyEventType.KeyDown) return false

        if (keyEvent.key.isFunctionKey() && previousPressedKeys.contains(keyEvent.key)) {
            // Ignore auto-repeat while the function key is held.
            return true
        }
        
        return when (keyEvent.key) {
            Key.F1, Key.F2, Key.F3, Key.F4, Key.F5 -> {
                selectedCharacterIndex = keyEvent.key.toCharacterIndex()
                true
            }
            Key.Enter -> {
                val faceCharacterIndex = when {
                    pressedKeys.contains(Key.F1) -> 0
                    pressedKeys.contains(Key.F2) -> 1
                    pressedKeys.contains(Key.F3) -> 2
                    pressedKeys.contains(Key.F4) -> 3
                    pressedKeys.contains(Key.F5) -> 4
                    else -> null
                }
                if (faceCharacterIndex != null && faceCharacterIndex < characters.size) {
                    val faceCharacterName = characters[faceCharacterIndex]
                    val faceToApply = findLatestFaceByCharacterName(faceCharacterName)
                    executeCommand(
                        AddItemCommand(
                            this,
                            ScenarioItem.CharaFace(
                                characterName = faceCharacterName,
                                face = faceToApply
                            )
                        )
                    )
                    selectedCharacterIndex = findLatestCharacterBoxIndex()
                    return true
                }

                val isShiftPressed = keyEvent.isShiftPressed
                val isCtrlPressed = keyEvent.isCtrlPressed
                
                val endTag = when {
                    isCtrlPressed && isShiftPressed -> "[l][cm]\n"
                    isCtrlPressed -> "[p]\n"
                    isShiftPressed -> "[l][r]\n"
                    else -> "[r]\n"
                }
                
                val groupBreak = isCtrlPressed && isShiftPressed
                
                val charName = if (selectedCharacterIndex > 0) characters[selectedCharacterIndex] else null
                
                if (currentInput.isNotEmpty() || endTag != "[r]\n") {
                    val faceToInherit = findLatestFaceByCharacterName(charName)
                    val newItem = ScenarioItem.TalkBlock(charName, currentInput, endTag, characterFace = faceToInherit, groupBreak = groupBreak)
                    executeCommand(AddItemCommand(this, newItem))  // Command経由で追加
                    currentInput = ""
                }
                true
            }
            else -> false
        }
    }

    fun updateItem(id: String, text: String, endTag: String, fontSizePx: Int) {
        val index = scenarioItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = scenarioItems[index]
            if (item is ScenarioItem.TalkBlock) {
                val oldText = item.text
                val oldEndTag = item.endTag
                val oldFontSizePx = item.fontSizePx
                executeCommand(
                    UpdateItemCommand(
                        this,
                        id,
                        oldText,
                        oldEndTag,
                        oldFontSizePx,
                        text,
                        endTag,
                        fontSizePx
                    )
                )
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
                
                // Go backwards - stop at groupBreak
                var start = index
                while (start > 0) {
                    val prev = scenarioItems[start - 1]
                    if (prev is ScenarioItem.TalkBlock && prev.characterName == oldCharName && !prev.groupBreak) {
                        start--
                    } else {
                        break
                    }
                }
                
                // Go forwards - stop at groupBreak
                var end = index
                while (end < scenarioItems.size - 1) {
                    val next = scenarioItems[end + 1]
                    if (next is ScenarioItem.TalkBlock && next.characterName == oldCharName && !next.groupBreak) {
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

    fun updateCharacterGroupFace(characterName: String?, newFace: String, firstItemId: String) {
        executeCommand(UpdateCharacterGroupFaceCommand(this, characterName, firstItemId, newFace))
    }

    fun updateCharaFace(id: String, newFace: String) {
        val index = scenarioItems.indexOfFirst { it.id == id }
        if (index == -1) return
        val item = scenarioItems[index]
        if (item is ScenarioItem.CharaFace) {
            if (item.face == newFace) return
            executeCommand(UpdateCharaFaceCommand(this, id, item.face, newFace))
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
        val file = exportScenario(scenarioItems, fileName)
        exportMessage = "Exported to ${file.name}"
    }

    private fun findLatestFaceByCharacterName(characterName: String?): String {
        if (characterName == null) {
            return characterFaceOptions.first()
        }

        for (i in scenarioItems.indices.reversed()) {
            when (val item = scenarioItems[i]) {
                is ScenarioItem.TalkBlock -> {
                    if (item.characterName == characterName) {
                        return item.characterFace
                    }
                    break
                }
                is ScenarioItem.CharaFace -> {
                    if (item.characterName == characterName) {
                        return item.face
                    }
                    break
                }
                else -> break
            }
        }
        return characterFaceOptions.first()
    }

    private fun findLatestCharacterBoxIndex(): Int {
        for (i in scenarioItems.indices.reversed()) {
            val item = scenarioItems[i]
            if (item is ScenarioItem.TalkBlock) {
                return characters.indexOf(item.characterName ?: "None").takeIf { it >= 0 } ?: 0
            }
        }
        return 0
    }
}

private fun Set<Key>.containsAnyFunctionKey(): Boolean {
    return contains(Key.F1) || contains(Key.F2) || contains(Key.F3) || contains(Key.F4) || contains(Key.F5)
}

private fun Key.isFunctionKey(): Boolean {
    return this == Key.F1 || this == Key.F2 || this == Key.F3 || this == Key.F4 || this == Key.F5
}

private fun Key.toCharacterIndex(): Int {
    return when (this) {
        Key.F1 -> 0
        Key.F2 -> 1
        Key.F3 -> 2
        Key.F4 -> 3
        Key.F5 -> 4
        else -> 0
    }
}

@Composable
fun rememberAppState() = remember { AppState() }
