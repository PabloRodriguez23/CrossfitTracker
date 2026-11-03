package com.cftracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToLong

private val spanish: Locale = Locale.forLanguageTag("es-ES")
private val dayFormatter = DateTimeFormatter.ofPattern("EEE d MMM", spanish)
private val shortDayFormatter = DateTimeFormatter.ofPattern("d MMM", spanish)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", spanish)

/** Número con como mucho un decimal y sin ".0": 80, 82.5, 4.3. */
fun Double.fmt(): String {
    val rounded = (this * 10).roundToLong() / 10.0
    return if (rounded == Math.floor(rounded)) rounded.toLong().toString() else rounded.toString()
}

/** Acepta tanto coma como punto decimal. */
fun parseNumber(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun Long.timeLabel(): String = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(timeFormatter)

/** "hoy", "ayer", "hace 3 días" o la fecha. */
fun Long.relativeLabel(): String {
    val days = ChronoUnit.DAYS.between(toLocalDate(), LocalDate.now())
    return when {
        days <= 0 -> "hoy"
        days == 1L -> "ayer"
        days < 7 -> "hace $days días"
        else -> toLocalDate().format(shortDayFormatter)
    }
}

fun LocalDate.dayLabel(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> format(dayFormatter).replaceFirstChar { it.uppercase() }
    }
}

fun LocalDate.shortLabel(): String = format(shortDayFormatter)
