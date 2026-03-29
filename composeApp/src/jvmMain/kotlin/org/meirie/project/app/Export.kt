package org.meirie.project.app

import java.io.File
import java.lang.StringBuilder
import java.nio.charset.Charset

fun getUniqueFileName(baseName: String): File {
    val name = if (baseName.endsWith(".ks")) {
        baseName.substringBeforeLast(".ks")
    } else {
        baseName
    }

    var counter = 0
    while (true) {
        val fileName = "./scenarios/${name}_$counter.ks"
        val file = File(fileName)
        if (!file.exists()) {
            return file
        }
        counter++
    }
}

fun exportScenario(items: List<ScenarioItem>, fileName: String = "output"): File {
    val stringBuilder = StringBuilder()
    var previousCharacterName: String? = null
    var startNewBoxNext = false
    var previousItemWasGroupBreak = false

    for (item in items) {
        when (item) {
            is ScenarioItem.TalkBlock -> {
                val isNewBox =
                    item.characterName != previousCharacterName ||
                    previousItemWasGroupBreak ||
                    startNewBoxNext

                // Add a character tag only once per character box.
                if (!item.characterName.isNullOrEmpty() && isNewBox) {
                    stringBuilder.append("[${item.characterName} ${item.characterFace}]\n")
                }

                if (item.text.isNotEmpty()) {
                    stringBuilder.append(item.text).append("\n")
                }
                stringBuilder.append(item.endTag)

                previousCharacterName = item.characterName
                previousItemWasGroupBreak = item.groupBreak
                if (item.endTag.contains("[cm]")) {
                    startNewBoxNext = true
                } else if (isNewBox) {
                    startNewBoxNext = false
                }
            }

            is ScenarioItem.CharaEvent -> {
                stringBuilder.append(";")
                if (item.isVisible) {
                    stringBuilder.append(" [Character ${item.characterName} Show]\n")
                } else {
                    stringBuilder.append(" [Character ${item.characterName} Hide]\n")
                }
                previousCharacterName = null
                previousItemWasGroupBreak = false
                startNewBoxNext = true
            }

            is ScenarioItem.CommandBlock -> {
                stringBuilder.append(item.command).append("\n")
                previousCharacterName = null
                previousItemWasGroupBreak = false
                startNewBoxNext = true
            }

            is ScenarioItem.CharaFace -> {
                stringBuilder.append("[${item.characterName} ${item.face}]\n")
                previousCharacterName = null
                previousItemWasGroupBreak = false
                startNewBoxNext = true
            }
        }
    }

    val file = getUniqueFileName(fileName)
    file.parentFile?.mkdirs()

    file.writeBytes(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
    file.appendText(stringBuilder.toString(), Charset.forName("UTF-8"))
    println("Exported scenario to ${file.absolutePath}")
    return file
}
