package com.cftracker.util

import kotlin.math.roundToLong

object Calc {
    /** Porcentajes que se muestran en la tabla, de 40 % a 100 %. */
    val PERCENTAGES: List<Int> = (40..100 step 5).toList()

    /** RM estimado de una serie con la fórmula de Epley: peso × (1 + reps / 30). */
    fun estimated1Rm(weight: Double, reps: Int): Double =
        if (reps <= 1) weight else weight * (1 + reps / 30.0)

    /** Si [weight] es el [percent] %, devuelve el 100 %. */
    fun fromPercent(weight: Double, percent: Double): Double = weight * 100.0 / percent

    /** Redondea al múltiplo de [step] más cercano (0,5 kg por defecto, lo que permiten los discos). */
    fun roundToStep(value: Double, step: Double = 0.5): Double = (value / step).roundToLong() * step
}
