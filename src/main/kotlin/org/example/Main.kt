package org.example

import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Compose Desktop App") {
        MaterialTheme {
            var text = remember { mutableStateOf("Hello, Compose!") }

            Button(onClick = { text.value = "Clicked!" }) {
                Text(text.value)
            }
        }
    }
}