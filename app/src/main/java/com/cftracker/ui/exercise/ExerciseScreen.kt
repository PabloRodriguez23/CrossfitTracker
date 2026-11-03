@file:OptIn(ExperimentalMaterial3Api::class)

package com.cftracker.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.cftracker.R
import com.cftracker.ui.theme.Brand
import kotlin.math.roundToInt
import java.time.LocalDate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cftracker.data.Exercise
import com.cftracker.data.LogEntry
import com.cftracker.data.aliasList
import com.cftracker.ui.components.LogSetDialog
import com.cftracker.ui.components.PerformanceChart
import com.cftracker.util.Calc
import com.cftracker.util.DaySummary
import com.cftracker.util.dayLabel
import com.cftracker.util.fmt
import com.cftracker.util.groupByDay
import com.cftracker.util.label
import com.cftracker.util.parseNumber
import com.cftracker.util.relativeLabel
import com.cftracker.util.shortLabel
import com.cftracker.util.timeLabel
import kotlinx.coroutines.launch

@Composable
fun ExerciseScreen(exerciseId: Long, onBack: () -> Unit) {
    val vm: ExerciseViewModel = viewModel(factory = ExerciseViewModel.factory(exerciseId))
    val exercise by vm.exercise.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val days = remember(entries) { groupByDay(entries) }
    val suggestedRm = remember(entries) { entries.maxOfOrNull { Calc.estimated1Rm(it.weight, it.reps) } }

    var showLog by rememberSaveable { mutableStateOf(false) }
    var showEdit by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val ex = exercise

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ex?.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    IconButton(onClick = { showEdit = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar nombres") }
                    IconButton(onClick = { showDelete = true }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar ejercicio") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showLog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Apuntar serie") },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (ex != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { LastSetCard(ex, entries.firstOrNull(), today = days.firstOrNull()?.takeIf { it.date == LocalDate.now() }) }
                item {
                    PercentagesCard(
                        exercise = ex,
                        suggestedRm = suggestedRm,
                        onToggle = vm::setShowPercentages,
                        onMaxChange = vm::setMaxWeight,
                    )
                }
                if (days.isNotEmpty()) {
                    item { PerformanceCard(days) }
                    item { Text("Historial", style = MaterialTheme.typography.titleMedium) }
                    items(days, key = { it.date.toEpochDay() }) { day ->
                        DayCard(day, onDelete = { entry ->
                            vm.deleteEntry(entry)
                            scope.launch {
                                val result = snackbar.showSnackbar(
                                    "Serie eliminada",
                                    actionLabel = "Deshacer",
                                    duration = SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed) vm.restoreEntry(entry)
                            }
                        })
                    }
                }
            }
        }
    }

    if (ex != null) {
        if (showLog) {
            val last = entries.firstOrNull()
            LogSetDialog(
                title = ex.name,
                initialWeight = last?.weight,
                initialReps = last?.reps,
                initialSets = last?.sets,
                maxWeight = ex.maxWeight.takeIf { ex.showPercentages },
                onDismiss = { showLog = false },
                onSave = { weight, reps, sets ->
                    vm.addEntry(weight, reps, sets)
                    showLog = false
                },
            )
        }
        if (showEdit) {
            EditNamesDialog(
                exercise = ex,
                onDismiss = { showEdit = false },
                onSave = { name, aliases ->
                    vm.rename(name, aliases)
                    showEdit = false
                },
            )
        }
        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text("¿Eliminar «${ex.name}»?") },
                text = { Text("Se borrarán también todas sus series.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDelete = false
                        vm.deleteExercise(onDone = onBack)
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } },
            )
        }
    }
}

@Composable
private fun LastSetCard(exercise: Exercise, last: LogEntry?, today: DaySummary?) {
    val soft = Color.White.copy(alpha = 0.8f)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Brand.gradient)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_kettlebell),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.12f),
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 28.dp),
        )
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val aliases = exercise.aliasList()
            if (aliases.isNotEmpty()) {
                Text("También: " + aliases.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = soft)
            }
            if (last == null) {
                Text("Aún no hay series", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text("Pulsa «Apuntar serie» para empezar.", style = MaterialTheme.typography.bodyMedium, color = soft)
            } else {
                Text("ÚLTIMO REGISTRO", style = MaterialTheme.typography.labelMedium, color = soft)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        last.weight.fmt(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp),
                        color = Color.White,
                    )
                    Text(
                        if (last.sets > 1) " kg · ${last.sets} × ${last.reps}" else " kg × ${last.reps}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                if (today == null) {
                    Text(last.timestamp.relativeLabel().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, color = soft)
                } else {
                    val seriesWord = if (today.totalSets == 1) "serie" else "series"
                    Text(
                        "Hoy: ${today.totalSets} $seriesWord · ${today.totalReps} reps · ${today.volume.fmt()} kg",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
                val max = exercise.maxWeight
                if (exercise.showPercentages && max != null && max > 0) {
                    Text(
                        "${(last.weight / max * 100).roundToInt()} % de tu RM (${max.fmt()} kg)",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun PercentagesCard(
    exercise: Exercise,
    suggestedRm: Double?,
    onToggle: (Boolean) -> Unit,
    onMaxChange: (Double?) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = exercise.showPercentages, role = Role.Checkbox, onValueChange = onToggle),
            ) {
                Checkbox(checked = exercise.showPercentages, onCheckedChange = null)
                Spacer(Modifier.width(12.dp))
                Text("Mostrar RM y porcentajes", style = MaterialTheme.typography.titleMedium)
            }
            if (exercise.showPercentages) {
                PercentagesBody(exercise, suggestedRm, onMaxChange)
            }
        }
    }
}

/** Contenido visible solo con la casilla marcada; se coloca dentro de la columna de la tarjeta. */
@Composable
private fun PercentagesBody(exercise: Exercise, suggestedRm: Double?, onMaxChange: (Double?) -> Unit) {
    // Texto local para no pisar lo que se está escribiendo ("100," aún no es número).
    var maxText by rememberSaveable(exercise.id) { mutableStateOf(exercise.maxWeight?.fmt() ?: "") }
    LaunchedEffect(exercise.maxWeight) {
        if (parseNumber(maxText)?.takeIf { it > 0 } != exercise.maxWeight) maxText = exercise.maxWeight?.fmt() ?: ""
    }
    OutlinedTextField(
        value = maxText,
        onValueChange = {
            maxText = it
            onMaxChange(parseNumber(it)?.takeIf { v -> v > 0 })
        },
        label = { Text("Peso máximo (RM) en kg") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
    val max = exercise.maxWeight
    if (suggestedRm != null && (max == null || suggestedRm > max)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "RM estimado por tus series: ${Calc.roundToStep(suggestedRm).fmt()} kg",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { onMaxChange(Calc.roundToStep(suggestedRm)) }) { Text("Usar") }
        }
    }
    if (max != null) {
        PercentTable(base = max)
        Text(
            "Redondeado a 0,5 kg",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    HorizontalDivider()
    PercentCalculator(onSaveAsMax = onMaxChange)
}

/** "Si X kg es el Y %": calcula el 100 % y el resto de porcentajes. */
@Composable
private fun PercentCalculator(onSaveAsMax: (Double) -> Unit) {
    var weightText by rememberSaveable { mutableStateOf("") }
    var percentText by rememberSaveable { mutableStateOf("") }
    Text("Calcular desde un porcentaje", style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Peso (kg)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
        Text("es el")
        OutlinedTextField(
            value = percentText,
            onValueChange = { percentText = it },
            label = { Text("%") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
    }
    val weight = parseNumber(weightText)
    val percent = parseNumber(percentText)
    if (weight != null && weight > 0 && percent != null && percent > 0) {
        val base = Calc.fromPercent(weight, percent)
        val rounded = Calc.roundToStep(base)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "100 % ≈ ${base.fmt()} kg",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { onSaveAsMax(rounded) }) { Text("Guardar como RM") }
        }
        PercentTable(base = base)
    }
}

@Composable
private fun PercentTable(base: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Calc.PERCENTAGES.reversed().chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { p ->
                    PercentCell(percent = p, weight = Calc.roundToStep(base * p / 100.0), modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PercentCell(percent: Int, weight: Double, modifier: Modifier = Modifier) {
    val highlight = percent == 100
    val textColor = if (highlight) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (highlight) Modifier.background(Brand.gradient)
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "$percent %",
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) textColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("${weight.fmt()} kg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun PerformanceCard(days: List<DaySummary>) {
    val latest = days.first()
    val previous = days.getOrNull(1)
    val chartDays = days.take(30).reversed()

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Rendimiento", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${latest.performance.fmt()} kg",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (previous != null && previous.performance > 0) {
                    val diff = (latest.performance - previous.performance) / previous.performance * 100
                    Spacer(Modifier.width(8.dp))
                    Text(
                        (if (diff >= 0) "▲ +" else "▼ ") + diff.fmt() + " %",
                        color = if (diff >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            Text(
                "Media de RM estimado · ${latest.date.dayLabel()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (chartDays.size >= 2) {
                PerformanceChart(
                    values = chartDays.map { it.performance },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(chartDays.first().date.shortLabel(), style = MaterialTheme.typography.labelSmall)
                    Text(chartDays.last().date.shortLabel(), style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Stat("Mejor RM est.", "${days.maxOf { it.best1Rm }.fmt()} kg", Modifier.weight(1f))
                Stat("Días", days.size.toString(), Modifier.weight(1f))
                Stat("Series", days.sumOf { it.entries.size }.toString(), Modifier.weight(1f))
            }
            Text(
                "RM estimado de una serie = peso × (1 + reps / 30)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DayCard(day: DaySummary, onDelete: (LogEntry) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp)) {
            Row(Modifier.fillMaxWidth().padding(end = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(day.date.dayLabel(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "≈ ${day.performance.fmt()} kg",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            day.entries.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.label(), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        entry.timestamp.timeLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = { onDelete(entry) }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar serie", modifier = Modifier.size(18.dp))
                    }
                }
            }
            Row(
                Modifier
                    .padding(top = 6.dp, end = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Stat("Series", day.totalSets.toString())
                Stat("Reps", day.totalReps.toString())
                Stat("Volumen", "${day.volume.fmt()} kg")
            }
            Text(
                "Media ${day.avgWeight.fmt()} kg × ${day.avgReps.fmt()} reps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun EditNamesDialog(exercise: Exercise, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(exercise.name) }
    var aliases by rememberSaveable { mutableStateOf(exercise.aliases) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nombres del ejercicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = aliases,
                    onValueChange = { aliases = it },
                    label = { Text("Otros nombres") },
                    supportingText = { Text("Separados por comas. Ej: BS, sentadilla trasera") },
                )
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(name, aliases) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
