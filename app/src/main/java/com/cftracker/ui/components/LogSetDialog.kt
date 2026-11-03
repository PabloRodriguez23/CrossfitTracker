package com.cftracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.cftracker.util.Calc
import com.cftracker.util.fmt
import com.cftracker.util.parseNumber
import kotlin.math.roundToInt

private const val WEIGHT_STEP = 2.5

/**
 * Diálogo rápido para apuntar una serie. Viene relleno con la última serie y el
 * peso seleccionado, así que basta con escribir el nuevo valor o pulsar Guardar.
 */
@Composable
fun LogSetDialog(
    title: String,
    initialWeight: Double?,
    initialReps: Int?,
    initialSets: Int?,
    maxWeight: Double?,
    onDismiss: () -> Unit,
    onSave: (weight: Double, reps: Int, sets: Int) -> Unit,
) {
    val initialWeightText = initialWeight?.fmt() ?: ""
    var weightField by remember {
        mutableStateOf(TextFieldValue(initialWeightText, TextRange(0, initialWeightText.length)))
    }
    var repsText by remember { mutableStateOf((initialReps ?: 1).toString()) }
    var setsText by remember { mutableStateOf((initialSets ?: 1).toString()) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val weight = parseNumber(weightField.text)
    val reps = repsText.toIntOrNull()
    val sets = setsText.toIntOrNull()
    val valid = weight != null && weight >= 0 && reps != null && reps > 0 && sets != null && sets > 0

    fun setWeight(value: Double) {
        val text = value.coerceAtLeast(0.0).fmt()
        weightField = TextFieldValue(text, TextRange(text.length))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Stepper(onMinus = { setWeight((weight ?: 0.0) - WEIGHT_STEP) }, onPlus = { setWeight((weight ?: 0.0) + WEIGHT_STEP) }) {
                    OutlinedTextField(
                        value = weightField,
                        onValueChange = { weightField = it },
                        label = { Text("Peso (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = it.focusRequester(focusRequester),
                    )
                }
                Stepper(
                    onMinus = { repsText = ((reps ?: 1) - 1).coerceAtLeast(1).toString() },
                    onPlus = { repsText = ((reps ?: 0) + 1).toString() },
                ) {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it.filter(Char::isDigit) },
                        label = { Text("Repeticiones") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = it,
                    )
                }
                Stepper(
                    onMinus = { setsText = ((sets ?: 1) - 1).coerceAtLeast(1).toString() },
                    onPlus = { setsText = ((sets ?: 0) + 1).toString() },
                ) {
                    OutlinedTextField(
                        value = setsText,
                        onValueChange = { setsText = it.filter(Char::isDigit) },
                        label = { Text("Series") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = it,
                    )
                }
                if (valid) {
                    val hints = buildList {
                        if (maxWeight != null && maxWeight > 0) add("≈ ${(weight!! / maxWeight * 100).roundToInt()} % de tu RM")
                        if (reps!! > 1) add("RM estimado ${Calc.estimated1Rm(weight!!, reps).fmt()} kg")
                        if (sets!! > 1) add("${sets * reps} reps en total")
                    }
                    if (hints.isNotEmpty()) {
                        Text(
                            hints.joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onSave(weight!!, reps!!, sets!!) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun Stepper(
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    field: @Composable (Modifier) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = onMinus, modifier = Modifier.size(48.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        field(Modifier.weight(1f))
        FilledTonalButton(onClick = onPlus, modifier = Modifier.size(48.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}
