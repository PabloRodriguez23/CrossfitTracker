package com.cftracker.util

import com.cftracker.data.LogEntry
import java.time.LocalDate

/** "80 kg × 5" o, con varias series, "5 × 5 · 80 kg". */
fun LogEntry.label(): String =
    if (sets > 1) "$sets × $reps · ${weight.fmt()} kg" else "${weight.fmt()} kg × $reps"

/** Resumen de todos los registros de un ejercicio en un día. */
class DaySummary(val date: LocalDate, val entries: List<LogEntry>) {
    // Las medias y el rendimiento van por registro: el número de series no cambia la intensidad.
    val avgWeight: Double = entries.map { it.weight }.average()
    val avgReps: Double = entries.map { it.reps }.average()
    val best1Rm: Double = entries.maxOf { Calc.estimated1Rm(it.weight, it.reps) }

    /**
     * Rendimiento del día: media del RM estimado de cada registro. Combina peso y
     * repeticiones en un solo número comparable entre días.
     */
    val performance: Double = entries.map { Calc.estimated1Rm(it.weight, it.reps) }.average()

    // Los totales sí cuentan todas las series.
    val totalSets: Int = entries.sumOf { it.sets }
    val totalReps: Int = entries.sumOf { it.reps * it.sets }
    val volume: Double = entries.sumOf { it.weight * it.reps * it.sets }
}

/** Agrupa los registros por día, el más reciente primero; dentro del día en orden cronológico. */
fun groupByDay(entries: List<LogEntry>): List<DaySummary> =
    entries.groupBy { it.timestamp.toLocalDate() }
        .map { (date, list) -> DaySummary(date, list.sortedBy { it.timestamp }) }
        .sortedByDescending { it.date }
