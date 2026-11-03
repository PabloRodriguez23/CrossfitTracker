package com.cftracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackerDao {

    /** Todos los ejercicios, los usados más recientemente primero. */
    @Query(
        """
        SELECT e.*,
            (SELECT l.weight FROM log_entries l WHERE l.exerciseId = e.id ORDER BY l.timestamp DESC LIMIT 1) AS lastWeight,
            (SELECT l.reps FROM log_entries l WHERE l.exerciseId = e.id ORDER BY l.timestamp DESC LIMIT 1) AS lastReps,
            (SELECT MAX(l.timestamp) FROM log_entries l WHERE l.exerciseId = e.id) AS lastTimestamp
        FROM exercises e
        ORDER BY lastTimestamp IS NULL, lastTimestamp DESC, e.name COLLATE NOCASE
        """
    )
    abstract fun observeSummaries(): Flow<List<ExerciseSummary>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    abstract fun observeExercise(id: Long): Flow<Exercise?>

    @Insert
    abstract suspend fun insertExercise(exercise: Exercise): Long

    @Query("UPDATE exercises SET name = :name, aliases = :aliases WHERE id = :id")
    abstract suspend fun updateNames(id: Long, name: String, aliases: String)

    @Query("UPDATE exercises SET showPercentages = :show WHERE id = :id")
    abstract suspend fun updateShowPercentages(id: Long, show: Boolean)

    @Query("UPDATE exercises SET maxWeight = :maxWeight WHERE id = :id")
    abstract suspend fun updateMaxWeight(id: Long, maxWeight: Double?)

    @Query("DELETE FROM exercises WHERE id = :id")
    abstract suspend fun deleteExerciseRow(id: Long)

    @Query("DELETE FROM log_entries WHERE exerciseId = :exerciseId")
    abstract suspend fun deleteEntriesOf(exerciseId: Long)

    @Transaction
    open suspend fun deleteExercise(id: Long) {
        deleteEntriesOf(id)
        deleteExerciseRow(id)
    }

    @Query("SELECT * FROM log_entries WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    abstract fun observeEntries(exerciseId: Long): Flow<List<LogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEntry(entry: LogEntry): Long

    @Delete
    abstract suspend fun deleteEntry(entry: LogEntry)
}
