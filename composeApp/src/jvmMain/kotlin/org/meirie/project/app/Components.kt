package org.meirie.project.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScenarioList(uiItems: List<UiItem>, listState: LazyListState, modifier: Modifier = Modifier) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        items(uiItems) { item ->
            when (item) {
                is UiItem.CharacterGroup -> CharacterGroupItem(item)
                is UiItem.Event -> EventItem(item)
                is UiItem.Command -> CommandItem(item)
            }
        }
    }
}

@Composable
fun CharacterGroupItem(item: UiItem.CharacterGroup) {
    Column(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD), shape = MaterialTheme.shapes.medium)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        if (!item.characterName.isNullOrEmpty()) {
            Text(
                text = "[${item.characterName}]",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item.boxes.forEachIndexed { index, box ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            DialogueBoxItem(box)
        }
    }
}

@Composable
fun DialogueBoxItem(box: DialogueBox) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = MaterialTheme.shapes.small)
            .border(1.dp, Color.LightGray, shape = MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        box.lines.forEach { line ->
            val symbol = when {
                line.endTag.startsWith("[r]") -> "↓"
                line.endTag.startsWith("[l][r]") -> "▼"
                line.endTag.startsWith("[p]") -> "▶"
                line.endTag.startsWith("[l][cm]") -> "↓ (Clear)"
                else -> ""
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
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