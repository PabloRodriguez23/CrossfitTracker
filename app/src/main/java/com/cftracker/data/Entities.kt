package com.cftracker.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Otros nombres separados por comas, p. ej. "BS, sentadilla trasera". */
    val aliases: String = "",
    /** Si es true, la ficha muestra el RM y la tabla de porcentajes. */
    val showPercentages: Boolean = false,
    /** Peso máximo (RM) en kg; es el 100 % de la tabla de porcentajes. */
    val maxWeight: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

fun Exercise.aliasList(): List<String> =
    aliases.split(',').map { it.trim() }.filter { it.isNotEmpty() }

/** Una serie: peso × repeticiones en un momento dado. */
@Entity(tableName = "log_entries", indices = [Index("exerciseId")])
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val weight: Double,
    val reps: Int,
    val timestamp: Long = System.currentTimeMillis(),
)

/** Ejercicio con su última serie, para la lista de búsqueda. */
data class ExerciseSummary(
    @Embedded val exercise: Exercise,
    val lastWeight: Double?,
    val lastReps: Int?,
    val lastTimestamp: Long?,
)
