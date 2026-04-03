package org.meirie.project.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    val appState = rememberAppState()

    MaterialTheme {
        val listState = rememberLazyListState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(appState.uiItems.size) {
            if (appState.uiItems.isNotEmpty()) {
                listState.animateScrollToItem(appState.uiItems.size - 1)
            }
        }

        LaunchedEffect(appState.exportMessage) {
            if (appState.exportMessage.isNotEmpty()) {
                snackbarHostState.showSnackbar(appState.exportMessage)
                appState.exportMessage = ""
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFFFFFF)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            appState.pressedKeys += event.key
                        } else if (event.type == KeyEventType.KeyUp) {
                            appState.pressedKeys -= event.key
                        }
                        false
                    }
            ) {
                Row {
                    Button(onClick = { appState.undo() }) {
                        Text("Undo")
                    }
                    Button(onClick = { appState.redo() }) {
                        Text("Redo")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextField(
                        value = appState.exportFileName,
                        onValueChange = { appState.exportFileName = it },
                        label = { Text("ファイル名") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ScenarioList(
                    uiItems = appState.uiItems,
                    listState = listState,
                    onItemUpdate = { id, text, endTag, fontSizePx ->
                        appState.updateItem(id, text, endTag, fontSizePx)
                    },
                    onCharacterChange = { id, charIndex ->
                        appState.changeCharacterViaCommand(id, charIndex)
                    },
                    onCharacterGroupFaceChange = { characterName, face, firstItemId ->
                        appState.updateCharacterGroupFace(characterName, face, firstItemId)
                    },
                    onCharaFaceChange = { id, face ->
                        appState.updateCharaFace(id, face)
                    },
                    currentModifierKeys = appState.pressedKeys,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    CharacterSelector(
                        selectedIndex = appState.selectedCharacterIndex
                    )

                    Button(onClick = {
                        if (appState.exportFileName.isNotEmpty()) {
                            appState.export(appState.exportFileName)
                        }
                    }) {
                        Text("保存")
                    }
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
}
