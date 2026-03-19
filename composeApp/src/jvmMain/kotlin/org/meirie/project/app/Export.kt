package org.meirie.project.app

import java.io.File
import java.lang.StringBuilder
import java.nio.charset.Charset

fun exportScenario(items: List<ScenarioItem>) {
    val stringBuilder = StringBuilder()
    for (item in items) {
        when (item) {
            is ScenarioItem.TalkBlock -> {
                if (!item.characterName.isNullOrEmpty()) {
                    stringBuilder.append("{${item.characterName}}\n")
                }
                if (item.text.isNotEmpty()) {
                    stringBuilder.append(item.text).append("\n")
                }
                stringBuilder.append(item.endTag)
            }
            is ScenarioItem.CharaEvent -> {
                stringBuilder.append(";")
                if (item.isVisible) {
                    stringBuilder.append(" [Character ${item.characterName} Show]\n")
                } else {
                    stringBuilder.append(" [Character ${item.characterName} Hide]\n")
                }
            }
            is ScenarioItem.CommandBlock -> {
                stringBuilder.append(item.command).append("\n")
            }
        }
    }
    
    val file = File("output.ks")
    file.writeBytes(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
    file.appendText(stringBuilder.toString(), Charset.forName("UTF-8"))
    println("Exported scenario to ${file.absolutePath}")
}