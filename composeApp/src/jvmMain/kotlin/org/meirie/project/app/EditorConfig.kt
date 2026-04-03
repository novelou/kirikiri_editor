package org.meirie.project.app

const val DefaultFontSize = 12
const val NoneCharacterName = "None"
const val DefaultFace = "通常"

val Characters = listOf(
    NoneCharacterName,
    "キャラA",
    "キャラB",
    "キャラC",
    "キャラD"
)

val FontSizePresets = listOf(10, 12, 14, 16, 18, 20, 24, 28, 32)

val CharacterFaceOptionsByCharacter: Map<String, List<String>> = mapOf(
    "キャラA" to listOf("通常", "笑", "泣"),
    "キャラB" to listOf("通常", "笑", "泣"),
    "キャラC" to listOf("通常", "笑", "泣"),
    "キャラD" to listOf("通常", "笑", "泣")
)

fun faceOptionsForCharacter(characterName: String?): List<String> {
    if (characterName == null || characterName == NoneCharacterName) {
        return listOf(DefaultFace)
    }
    return CharacterFaceOptionsByCharacter[characterName] ?: listOf(DefaultFace)
}

fun defaultFaceForCharacter(characterName: String?): String {
    return faceOptionsForCharacter(characterName).firstOrNull() ?: DefaultFace
}
