package com.cftracker.util

import com.cftracker.data.Exercise
import com.cftracker.data.ExerciseSummary
import com.cftracker.data.aliasList
import java.text.Normalizer

/**
 * Búsqueda tolerante: ignora mayúsculas, tildes y espacios, mira el nombre y
 * todos los alias, y entiende iniciales ("bs" → "Back Squat").
 */
object ExerciseSearch {
    private val diacritics = Regex("\\p{Mn}+")
    private val separators = Regex("[\\s\\-_/]+")

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD).replace(diacritics, "").lowercase().trim()

    fun filter(all: List<ExerciseSummary>, query: String): List<ExerciseSummary> {
        if (normalize(query).isEmpty()) return all
        // sortedByDescending es estable: a igual puntuación se mantiene el orden por uso reciente.
        return all.mapNotNull { s -> score(query, s.exercise)?.let { s to it } }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    fun isExactMatch(query: String, exercise: Exercise): Boolean {
        val q = normalize(query)
        return names(exercise).any { normalize(it) == q }
    }

    /** Puntuación de coincidencia (mayor = mejor) o null si no coincide. */
    fun score(query: String, exercise: Exercise): Int? {
        val q = normalize(query)
        if (q.isEmpty()) return 0
        val qCompact = q.replace(separators, "")
        val qTokens = q.split(separators).filter { it.isNotEmpty() }

        return names(exercise).mapNotNull { raw ->
            val n = normalize(raw)
            val words = n.split(separators).filter { it.isNotEmpty() }
            when {
                n == q -> 100
                n.startsWith(q) -> 80
                words.any { it.startsWith(q) } -> 60
                n.contains(q) -> 50
                qCompact.length >= 2 && n.replace(separators, "").contains(qCompact) -> 40
                qCompact.length >= 2 && words.joinToString("") { it.take(1) }.startsWith(qCompact) -> 35
                qTokens.all { t -> words.any { it.startsWith(t) } } -> 30
                else -> null
            }
        }.maxOrNull()
    }

    private fun names(exercise: Exercise): List<String> = listOf(exercise.name) + exercise.aliasList()
}
