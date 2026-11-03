package com.cftracker.data

import kotlinx.coroutines.flow.Flow

class TrackerRepository(private val dao: TrackerDao) {

    fun observeSummaries(): Flow<List<ExerciseSummary>> = dao.observeSummaries()

    fun observeExercise(id: Long): Flow<Exercise?> = dao.observeExercise(id)

    fun observeEntries(exerciseId: Long): Flow<List<LogEntry>> = dao.observeEntries(exerciseId)

    suspend fun createExercise(name: String): Long =
        dao.insertExercise(Exercise(name = name.trim()))

    suspend fun rename(id: Long, name: String, aliases: String) {
        val cleanAliases = aliases.split(',').map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
        dao.updateNames(id, name.trim(), cleanAliases)
    }

    suspend fun setShowPercentages(id: Long, show: Boolean) = dao.updateShowPercentages(id, show)

    suspend fun setMaxWeight(id: Long, maxWeight: Double?) = dao.updateMaxWeight(id, maxWeight)

    suspend fun deleteExercise(id: Long) = dao.deleteExercise(id)

    suspend fun addEntry(exerciseId: Long, weight: Double, reps: Int) {
        dao.insertEntry(LogEntry(exerciseId = exerciseId, weight = weight, reps = reps))
    }

    /** Vuelve a insertar una serie borrada (para "Deshacer"). */
    suspend fun restoreEntry(entry: LogEntry) {
        dao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: LogEntry) = dao.deleteEntry(entry)
}
