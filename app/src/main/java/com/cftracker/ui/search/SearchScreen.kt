@file:OptIn(ExperimentalMaterial3Api::class)

package com.cftracker.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cftracker.data.ExerciseSummary
import com.cftracker.data.aliasList
import com.cftracker.ui.components.LogSetDialog
import com.cftracker.util.ExerciseSearch
import com.cftracker.util.fmt
import com.cftracker.util.relativeLabel
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(onOpenExercise: (Long) -> Unit) {
    val vm: SearchViewModel = viewModel(factory = SearchViewModel.Factory)
    val results by vm.results.collectAsStateWithLifecycle()
    val query = vm.query
    var logTarget by remember { mutableStateOf<ExerciseSummary?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val canCreate = query.isNotBlank() && results.none { ExerciseSearch.isExactMatch(query, it.exercise) }

    fun open(id: Long) {
        vm.updateQuery("")
        onOpenExercise(id)
    }

    fun createFromQuery() {
        vm.create(query.trim()) { open(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::updateQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Buscar ejercicio…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { vm.updateQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Borrar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(onSearch = {
                    when {
                        query.isNotBlank() && results.isNotEmpty() -> open(results.first().exercise.id)
                        canCreate -> createFromQuery()
                    }
                }),
            )

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(results, key = { it.exercise.id }) { summary ->
                    ExerciseRow(
                        summary = summary,
                        onClick = { open(summary.exercise.id) },
                        onQuickLog = { logTarget = summary },
                    )
                }
                if (canCreate) {
                    item(key = "create") {
                        OutlinedCard(onClick = ::createFromQuery, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(12.dp))
                                Text("Crear «${query.trim()}»", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                if (results.isEmpty() && query.isBlank()) {
                    item(key = "empty") {
                        Text(
                            "Escribe el nombre de un ejercicio para buscarlo o crearlo.",
                            modifier = Modifier.padding(top = 24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    logTarget?.let { target ->
        val ex = target.exercise
        LogSetDialog(
            title = ex.name,
            initialWeight = target.lastWeight,
            initialReps = target.lastReps,
            maxWeight = ex.maxWeight.takeIf { ex.showPercentages },
            onDismiss = { logTarget = null },
            onSave = { weight, reps ->
                vm.addEntry(ex.id, weight, reps)
                logTarget = null
                scope.launch { snackbar.showSnackbar("${ex.name}: ${weight.fmt()} kg × $reps guardado") }
            },
        )
    }
}

@Composable
private fun ExerciseRow(summary: ExerciseSummary, onClick: () -> Unit, onQuickLog: () -> Unit) {
    val ex = summary.exercise
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(ex.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                val aliases = ex.aliasList()
                if (aliases.isNotEmpty()) {
                    Text(
                        aliases.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val weight = summary.lastWeight
                val reps = summary.lastReps
                val time = summary.lastTimestamp
                Text(
                    if (weight != null && reps != null && time != null) {
                        "Último: ${weight.fmt()} kg × $reps · ${time.relativeLabel()}"
                    } else {
                        "Sin registros"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                val max = ex.maxWeight
                if (ex.showPercentages && max != null) {
                    Text("RM ${max.fmt()} kg", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            FilledTonalIconButton(onClick = onQuickLog) {
                Icon(Icons.Default.Add, contentDescription = "Apuntar serie")
            }
        }
    }
}
