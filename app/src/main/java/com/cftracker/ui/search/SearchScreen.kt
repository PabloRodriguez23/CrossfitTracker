@file:OptIn(ExperimentalMaterial3Api::class)

package com.cftracker.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.cftracker.ui.components.AppLogo
import com.cftracker.ui.components.InitialsAvatar
import com.cftracker.ui.theme.Brand
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
import androidx.compose.ui.platform.LocalFocusManager
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
    val focusManager = LocalFocusManager.current

    val canCreate = query.isNotBlank() && results.none { ExerciseSearch.isExactMatch(query, it.exercise) }

    fun open(id: Long) {
        focusManager.clearFocus()
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
            Row(
                Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppLogo(size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("CF Pesos", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Busca · apunta · mejora",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TextField(
                value = query,
                onValueChange = vm::updateQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                ),
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (results.isNotEmpty()) {
                    item(key = "header") {
                        Text(
                            if (query.isBlank()) "RECIENTES" else "RESULTADOS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        )
                    }
                }
                items(results, key = { it.exercise.id }) { summary ->
                    ExerciseRow(
                        summary = summary,
                        onClick = { open(summary.exercise.id) },
                        onQuickLog = { logTarget = summary },
                    )
                }
                if (canCreate) {
                    item(key = "create") {
                        OutlinedCard(
                            onClick = ::createFromQuery,
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.5.dp, Brand.gradient),
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Crear «${query.trim()}»", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Nuevo ejercicio",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
                if (results.isEmpty() && query.isBlank()) {
                    item(key = "empty") { EmptyState() }
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
            initialSets = target.lastSets,
            maxWeight = ex.maxWeight.takeIf { ex.showPercentages },
            onDismiss = { logTarget = null },
            onSave = { weight, reps, sets ->
                vm.addEntry(ex.id, weight, reps, sets)
                logTarget = null
                scope.launch { snackbar.showSnackbar("${ex.name}: ${if (sets > 1) "$sets × $reps" else "$reps reps"} · ${weight.fmt()} kg guardado") }
            },
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppLogo(size = 88.dp, modifier = Modifier.alpha(0.9f))
        Text("Tu libreta de pesos", style = MaterialTheme.typography.titleLarge)
        Text(
            "Escribe un ejercicio para buscarlo o crearlo.\nLuego pulsa + para apuntar una serie al momento.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ExerciseRow(summary: ExerciseSummary, onClick: () -> Unit, onQuickLog: () -> Unit) {
    val ex = summary.exercise
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InitialsAvatar(ex.name)
            Spacer(Modifier.width(14.dp))
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
                        val sets = summary.lastSets ?: 1
                        val work = if (sets > 1) "$sets × $reps · ${weight.fmt()} kg" else "${weight.fmt()} kg × $reps"
                        "Último: $work · ${time.relativeLabel()}"
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
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brand.gradient)
                    .clickable(onClick = onQuickLog),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Apuntar serie", tint = Color.White)
            }
        }
    }
}
