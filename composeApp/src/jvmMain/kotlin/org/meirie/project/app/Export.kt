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
    for (item in items) {
        when (item) {
            is ScenarioItem.TalkBlock -> {
                if (!item.characterName.isNullOrEmpty()) {
                    stringBuilder.append("[${item.characterName}]\n")
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
    
    val file = getUniqueFileName(fileName)
    
    // scenarios ディレクトリが存在しなければ作成
    file.parentFile?.mkdirs()
    
    file.writeBytes(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
    file.appendText(stringBuilder.toString(), Charset.forName("UTF-8"))
    println("Exported scenario to ${file.absolutePath}")
    return file
}
