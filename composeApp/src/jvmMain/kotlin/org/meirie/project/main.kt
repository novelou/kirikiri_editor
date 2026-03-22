package org.meirie.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.meirie.project.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "kirikiri_editor",
    ) {
        App()
    }
}
