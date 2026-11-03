package com.cftracker.util

import com.cftracker.data.LogEntry
import java.time.LocalDate

/** Resumen de todas las series de un ejercicio en un día. */
class DaySummary(val date: LocalDate, val entries: List<LogEntry>) {
    val avgWeight: Double = entries.map { it.weight }.average()
    val avgReps: Double = entries.map { it.reps }.average()
    val volume: Double = entries.sumOf { it.weight * it.reps }
    val best1Rm: Double = entries.maxOf { Calc.estimated1Rm(it.weight, it.reps) }

    /**
     * Rendimiento del día: media del RM estimado de cada serie. Combina peso y
     * repeticiones en un solo número comparable entre días.
     */
    val performance: Double = entries.map { Calc.estimated1Rm(it.weight, it.reps) }.average()
}

/** Agrupa las series por día, el más reciente primero; dentro del día en orden cronológico. */
fun groupByDay(entries: List<LogEntry>): List<DaySummary> =
    entries.groupBy { it.timestamp.toLocalDate() }
        .map { (date, list) -> DaySummary(date, list.sortedBy { it.timestamp }) }
        .sortedByDescending { it.date }
