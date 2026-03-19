package org.meirie.project.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    MaterialTheme {
        val appState = rememberAppState()
        val listState = rememberLazyListState()

        LaunchedEffect(appState.uiItems.size) {
            if (appState.uiItems.isNotEmpty()) {
                listState.animateScrollToItem(appState.uiItems.size - 1)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ScenarioList(
                uiItems = appState.uiItems,
                listState = listState,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            CharacterSelector(
                selectedIndex = appState.selectedCharacterIndex
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { appState.export() }, modifier = Modifier.fillMaxWidth()) {
                Text("チャプターをエクスポート (output.ks)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            ScenarioInputArea(
                currentInput = appState.currentInput,
                onInputChange = { appState.currentInput = it },
                onKeyEvent = { appState.handleKeyEvent(it) }
            )
        }
    }
}