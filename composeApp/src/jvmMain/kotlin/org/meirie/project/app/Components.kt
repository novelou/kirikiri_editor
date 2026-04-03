package org.meirie.project.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val characterSelectKeysForUi = listOf(
    Key.F1, Key.F2, Key.F3, Key.F4, Key.F5, Key.F6,
    Key.F7, Key.F8, Key.F9, Key.F10, Key.F11, Key.F12
)

private fun Set<Key>.toCharacterIndex(): Int? {
    for ((index, key) in characterSelectKeysForUi.withIndex()) {
        if (contains(key)) {
            return index
        }
    }
    return null
}

@Composable
fun ScenarioList(
    uiItems: List<UiItem>, 
    listState: LazyListState, 
    onItemUpdate: (String, String, String, Int) -> Unit,
    onCharacterChange: (String, Int) -> Unit,
    onCharacterGroupFaceChange: (String?, String, String) -> Unit,
    onCharaFaceChange: (String, String) -> Unit,
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
                    onCharacterGroupFaceChange = onCharacterGroupFaceChange,
                    onCharaFaceChange = onCharaFaceChange,
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
    onItemUpdate: (String, String, String, Int) -> Unit,
    onCharacterChange: (String, Int) -> Unit,
    onCharacterGroupFaceChange: (String?, String, String) -> Unit,
    onCharaFaceChange: (String, String) -> Unit,
    currentModifierKeys: Set<Key>
) {
    val faceOptions = faceOptionsForCharacter(item.characterName)
    var expandedFace by remember { mutableStateOf(false) }
    val firstTalkLineId = item.boxes
        .asSequence()
        .flatMap { it.lines.asSequence() }
        .mapNotNull { (it as? DialogueLine.Talk)?.item?.id }
        .firstOrNull()
    val canChangeGroupFace = firstTalkLineId != null

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
        
        Row(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color,
                modifier = Modifier
                    .pointerInput(currentModifierKeys) {
                        detectTapGestures(
                            onTap = {
                                val charIndex = when {
                                    else -> currentModifierKeys.toCharacterIndex() ?: -1
                                }
                                if (charIndex in 0 until Characters.size && item.boxes.isNotEmpty()) {
                                    val firstLineId = item.boxes
                                        .asSequence()
                                        .flatMap { it.lines.asSequence() }
                                        .mapNotNull { (it as? DialogueLine.Talk)?.item?.id }
                                        .firstOrNull()
                                    if (firstLineId != null) {
                                        onCharacterChange(firstLineId, charIndex)
                                    }
                                }
                            }
                        )
                    }
            )

            Box(modifier = Modifier.width(120.dp)) {
                Button(
                    onClick = { if (canChangeGroupFace) expandedFace = true },
                    enabled = canChangeGroupFace,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(item.characterFace, modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text("笆ｼ", fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = expandedFace && canChangeGroupFace,
                    onDismissRequest = { expandedFace = false }
                ) {
                    faceOptions.forEach { face ->
                        DropdownMenuItem(
                            text = { Text(face) },
                            onClick = {
                                expandedFace = false
                                onCharacterGroupFaceChange(item.characterName, face, firstTalkLineId ?: return@DropdownMenuItem)
                            }
                        )
                    }
                }
            }
        }
        
        item.boxes.forEachIndexed { index, box ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            DialogueBoxItem(
                box = box, 
                onItemUpdate = onItemUpdate,
                onCharaFaceChange = onCharaFaceChange
            )
        }
    }
}

@Composable
fun DialogueBoxItem(
    box: DialogueBox,
    onItemUpdate: (String, String, String, Int) -> Unit,
    onCharaFaceChange: (String, String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = MaterialTheme.shapes.small)
            .border(1.dp, Color.LightGray, shape = MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        box.lines.forEach { dialogueLine ->
            when (dialogueLine) {
                is DialogueLine.Talk -> {
                    val line = dialogueLine.item
                    var isEditing by remember(line.id, line.text, line.endTag, line.fontSizePx) { mutableStateOf(false) }
                    var editText by remember(line.id, line.text) { mutableStateOf(line.text) }
                    var editTag by remember(line.id, line.endTag) { mutableStateOf(line.endTag.trim()) }
                    var editFontSizeText by remember(line.id, line.fontSizePx) { mutableStateOf(line.fontSizePx.toString()) }
                    var expandedFontSizeMenu by remember(line.id) { mutableStateOf(false) }
                    val focusRequester = remember { FocusRequester() }

                    val symbol = when (val trimmedTag = line.endTag.trim()) {
                        "[r]" -> "↓"
                        "[l][r]" -> "▼"
                        "[p]" -> "▶"
                        "[l][cm]" -> "↓ (Clear)"
                        else -> trimmedTag
                    }

                    if (isEditing) {
                        fun resolveFontSizePx(): Int {
                            return editFontSizeText.trim().toIntOrNull()?.takeIf { it > 0 } ?: DefaultFontSize
                        }
                        fun saveEdit() {
                            val newTag = if (editTag.endsWith("\n")) editTag else "$editTag\n"
                            onItemUpdate(line.id, editText, newTag, resolveFontSizePx())
                            isEditing = false
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = editText,
                                onValueChange = { editText = it },
                                modifier = Modifier
                                    .weight(4f)
                                    .focusRequester(focusRequester)
                                    .onPreviewKeyEvent { event ->
                                        when (event.type) {
                                            KeyEventType.KeyDown -> {
                                                when (event.key) {
                                                    Key.Enter -> {
                                                        saveEdit()
                                                        true
                                                    }
                                                    Key.Escape -> {
                                                        editText = line.text
                                                        editTag = line.endTag.trim()
                                                        editFontSizeText = line.fontSizePx.toString()
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
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Tag") }
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = editFontSizeText,
                                    onValueChange = { input ->
                                        editFontSizeText = input.filter { it.isDigit() }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("px") },
                                    suffix = { Text("px") },
                                    trailingIcon = {
                                        Text(
                                            text = "笆ｼ",
                                            modifier = Modifier.clickable { expandedFontSizeMenu = true }
                                        )
                                    }
                                )
                                DropdownMenu(
                                    expanded = expandedFontSizeMenu,
                                    onDismissRequest = { expandedFontSizeMenu = false }
                                ) {
                                        FontSizePresets.forEach { px ->
                                        DropdownMenuItem(
                                            text = { Text("${px}") },
                                            onClick = {
                                                editFontSizeText = px.toString()
                                                expandedFontSizeMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = { saveEdit() },
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(54.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("保\n存", lineHeight = 12.sp)
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = line.text,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${line.fontSizePx}px",
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(start = 8.dp)
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

                is DialogueLine.Face -> {
                    val faceItem = dialogueLine.item
                    val faceOptions = faceOptionsForCharacter(faceItem.characterName)
                    var selectedFace by remember(faceItem.id, faceItem.face) { mutableStateOf(faceItem.face) }
                    var expandedFace by remember(faceItem.id) { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${faceItem.characterName}: 表情",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Box(modifier = Modifier.width(120.dp)) {
                            Button(
                                onClick = { expandedFace = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedFace, modifier = Modifier.weight(1f), fontSize = 12.sp)
                                Text("笆ｼ", fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = expandedFace,
                                onDismissRequest = { expandedFace = false }
                            ) {
                                faceOptions.forEach { face ->
                                    DropdownMenuItem(
                                        text = { Text(face) },
                                        onClick = {
                                            selectedFace = face
                                            expandedFace = false
                                            onCharaFaceChange(faceItem.id, face)
                                        }
                                    )
                                }
                            }
                        }
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
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        if (selectedIndex in Characters.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("話者 (F1-F12で切替): ", fontWeight = FontWeight.Bold)
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(Characters) { index, name ->
                val isSelected = index == selectedIndex
                Text(
                    text = "F${index + 1}: $name",
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified
                )
            }
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
        label = { Text("シナリオテキスト入力 (Enter:↓ / Shift+Enter:▼ / Ctrl+Enter:▶ / Ctrl+Shift+Enter: ボックス分割)") },
        singleLine = false,
        maxLines = 5
    )
}


