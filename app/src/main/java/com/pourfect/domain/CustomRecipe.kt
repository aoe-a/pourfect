package com.pourfect.domain

/** A user-built pour schedule: bloom + N equal pours on a fixed cadence. */
data class CustomRecipe(
    val name: String,
    val ratio: Double,
    val bloomMultiplier: Int,
    val pourCount: Int,
    val intervalSeconds: Int
) {
    fun encode(): String = listOf(
        name.replace("|", "").replace("\n", ""),
        ratio.toString(),
        bloomMultiplier.toString(),
        pourCount.toString(),
        intervalSeconds.toString()
    ).joinToString("|")

    companion object {
        fun decode(encoded: String): CustomRecipe? {
            val parts = encoded.split("|")
            if (parts.size != 5) return null
            val name = parts[0].trim()
            val ratio = parts[1].toDoubleOrNull() ?: return null
            val bloom = parts[2].toIntOrNull() ?: return null
            val pours = parts[3].toIntOrNull() ?: return null
            val interval = parts[4].toIntOrNull() ?: return null
            if (name.isEmpty()) return null
            return CustomRecipe(name, ratio, bloom, pours, interval)
        }

        fun encodeList(recipes: List<CustomRecipe>): String =
            recipes.joinToString("\n") { it.encode() }

        fun decodeList(encoded: String): List<CustomRecipe> =
            encoded.lines().mapNotNull { line ->
                line.takeIf { it.isNotBlank() }?.let(::decode)
            }
    }
}
