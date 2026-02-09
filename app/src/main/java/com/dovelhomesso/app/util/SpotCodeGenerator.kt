package com.dovelhomesso.app.util

object SpotCodeGenerator {
    
    /**
     * Generates a unique spot code based on room, container, and spot names.
     * Format: first 3 letters of room + first 3 letters of container + abbreviated label
     * If collision occurs, adds a numeric suffix.
     * 
     * Examples:
     * - Camera, Armadio, Cassetto 1 -> CAM-ARM-C1
     * - Cucina, Credenza, Mensola alta -> CUC-CRE-MA
     */
    fun generateCode(
        roomName: String,
        containerName: String,
        spotLabel: String,
        existingCodes: List<String>
    ): String {
        val roomPrefix = normalize(roomName).take(3).uppercase()
        val containerPrefix = normalize(containerName).take(3).uppercase()
        val spotAbbrev = abbreviateLabel(spotLabel).uppercase()
        
        val baseCode = "$roomPrefix-$containerPrefix-$spotAbbrev"
        
        // Check for collision
        if (baseCode !in existingCodes) {
            return baseCode
        }
        
        // Add numeric suffix to resolve collision
        var suffix = 2
        while ("$baseCode$suffix" in existingCodes) {
            suffix++
        }
        
        return "$baseCode$suffix"
    }
    
    /**
     * Normalizes a string by removing accents and special characters.
     */
    private fun normalize(text: String): String {
        val normalized = text
            .replace("à", "a").replace("á", "a").replace("â", "a")
            .replace("è", "e").replace("é", "e").replace("ê", "e")
            .replace("ì", "i").replace("í", "i").replace("î", "i")
            .replace("ò", "o").replace("ó", "o").replace("ô", "o")
            .replace("ù", "u").replace("ú", "u").replace("û", "u")
            .replace("'", "").replace(" ", "")
        return normalized.filter { it.isLetterOrDigit() }
    }
    
    /**
     * Abbreviates a spot label intelligently.
     * - "Cassetto 1" -> "C1"
     * - "Mensola alta" -> "MA"
     * - "Ripiano 3" -> "R3"
     * - "Primo scaffale" -> "PS"
     */
    private fun abbreviateLabel(label: String): String {
        val words = label.trim().split(Regex("\\s+"))
        
        return when {
            words.size == 1 -> {
                // Single word: take first 2 letters
                normalize(words[0]).take(2)
            }
            words.size == 2 -> {
                val firstWord = normalize(words[0])
                val secondWord = normalize(words[1])
                
                // Check if second word is a number
                if (secondWord.all { it.isDigit() }) {
                    "${firstWord.take(1)}$secondWord"
                } else {
                    "${firstWord.take(1)}${secondWord.take(1)}"
                }
            }
            else -> {
                // Multiple words: take first letter of each (max 3)
                words.take(3).map { normalize(it).firstOrNull() ?: "" }.joinToString("")
            }
        }
    }
    
    /**
     * Validates a spot code format.
     */
    fun isValidCode(code: String): Boolean {
        // Format: XXX-XXX-XX[N]
        val regex = Regex("^[A-Z]{1,3}-[A-Z]{1,3}-[A-Z0-9]{1,4}\\d*$")
        return regex.matches(code)
    }
}
