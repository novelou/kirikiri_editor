package org.meirie.project.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScenarioList(
    uiItems: List<UiItem>, 
    listState: LazyListState, 
    onItemUpdate: (String, String, String) -> Unit,
    onCharacterChange: (String, Int) -> Unit,
    currentModifierKeys: Set<Key>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        items(uiItems) { item ->
            when (item) {
                is UiItem.CharacterGroup -> CharacterGroupItem(
                    item = item, 
                    onItemUpdate = onItemUpdate, 
                    onCharacterChange = onCharacterChange,
                    currentModifierKeys = currentModifierKeys
                )
                is UiItem.Event -> EventItem(item)
                is UiItem.Command -> CommandItem(item)
            }
        }
    }
}

@Composable
fun CharacterGroupItem(
    item: UiItem.CharacterGroup,
    onItemUpdate: (String, String, String) -> Unit,
    onCharacterChange: (String, Int) -> Unit,
    currentModifierKeys: Set<Key>
) {
    Column(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD), shape = MaterialTheme.shapes.medium)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        val displayName = if (!item.characterName.isNullOrEmpty()) "[${item.characterName}]" else "[None]"
        val color = if (!item.characterName.isNullOrEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        
        Text(
            text = displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .pointerInput(currentModifierKeys) {
                    detectTapGestures(
                        onTap = {
                            val charIndex = when {
                                currentModifierKeys.contains(Key.F1) -> 0
                                currentModifierKeys.contains(Key.F2) -> 1
                                currentModifierKeys.contains(Key.F3) -> 2
                                currentModifierKeys.contains(Key.F4) -> 3
                                currentModifierKeys.contains(Key.F5) -> 4
                                else -> -1
                            }
                            if (charIndex != -1 && item.boxes.isNotEmpty()) {
                                val firstLineId = item.boxes.first().lines.firstOrNull()?.id
                                if (firstLineId != null) {
                                    onCharacterChange(firstLineId, charIndex)
                                }
                            }
                        }
                    )
                }
        )
        
        item.boxes.forEachIndexed { index, box ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            DialogueBoxItem(
                box = box, 
                onItemUpdate = onItemUpdate
            )
        }
    }
}

@Composable
fun DialogueBoxItem(
    box: DialogueBox, 
    onItemUpdate: (String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = MaterialTheme.shapes.small)
            .border(1.dp, Color.LightGray, shape = MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        box.lines.forEach { line ->
            var isEditing by remember(line.id, line.text, line.endTag) { mutableStateOf(false) }
            var editText by remember(line.id, line.text) { mutableStateOf(line.text) }
            var editTag by remember(line.id, line.endTag) { mutableStateOf(line.endTag.trim()) }

            val symbol = when (val trimmedTag = line.endTag.trim()) {
                "[r]" -> "↓"
                "[l][r]" -> "▼"
                "[p]" -> "▶"
                "[l][cm]" -> "↓ (Clear)"
                else -> trimmedTag
            }
            
            val focusRequester = remember { FocusRequester() }

            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                when (event.type) {
                                    KeyEventType.KeyDown -> {
                                        when (event.key) {
                                            Key.Enter -> {
                                                val newTag = if (editTag.endsWith("\n")) editTag else "$editTag\n"
                                                onItemUpdate(line.id, editText, newTag)
                                                isEditing = false
                                                true
                                            }
                                            Key.Escape -> {
                                                editText = line.text
                                                editTag = line.endTag.trim()
                                                isEditing = false
                                                true
                                            }
                                            else -> false
                                        }
                                    }
                                    else -> false
                                }
                            },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editTag,
                        onValueChange = { editTag = it },
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                        label = { Text("Tag") }
                    )
                    Button(
                        onClick = {
                            val newTag = if (editTag.endsWith("\n")) editTag else "$editTag\n"
                            onItemUpdate(line.id, editText, newTag)
                            isEditing = false
                        }
                    ) {
                        Text("Save")
                    }
                }
                
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            isEditing = true
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = line.text,
                        modifier = Modifier.weight(1f)
                    )
                    if (symbol.isNotEmpty()) {
                        Text(
                            text = symbol,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(item: UiItem.Event) {
    Text(
        text = "[Character Event: ${item.item.characterName} isVisible=${item.item.isVisible}]",
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun CommandItem(item: UiItem.Command) {
    Text(
        text = "[Command: ${item.item.command}]",
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun CharacterSelector(selectedIndex: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("話者 (F1-F5で切替): ", fontWeight = FontWeight.Bold)
        characters.forEachIndexed { index, name ->
            val isSelected = index == selectedIndex
            Text(
                text = "F${index + 1}: $name",
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                        else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified
            )
        }
    }
}

@Composable
fun ScenarioInputArea(
    currentInput: String,
    onInputChange: (String) -> Unit,
    onKeyEvent: (KeyEvent) -> Boolean
) {
    OutlinedTextField(
        value = currentInput,
        onValueChange = onInputChange,
        modifier = Modifier
            .fillMaxWidth()
            .onPreviewKeyEvent(onKeyEvent),
        label = { Text("シナリオテキスト入力（Enter:↓ / Shift+Enter:▼ / Ctrl+Enter:▶ / Ctrl+Shift+Enter: ボックス分割）") },
        singleLine = false,
        maxLines = 5
    )
}