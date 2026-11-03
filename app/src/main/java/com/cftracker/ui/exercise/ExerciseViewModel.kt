package com.cftracker.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cftracker.CfApp
import com.cftracker.data.Exercise
import com.cftracker.data.LogEntry
import com.cftracker.data.TrackerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseViewModel(
    private val repo: TrackerRepository,
    private val exerciseId: Long,
) : ViewModel() {

    val exercise: StateFlow<Exercise?> = repo.observeExercise(exerciseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Series del ejercicio, la más reciente primero. */
    val entries: StateFlow<List<LogEntry>> = repo.observeEntries(exerciseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setShowPercentages(show: Boolean) {
        viewModelScope.launch { repo.setShowPercentages(exerciseId, show) }
    }

    fun setMaxWeight(maxWeight: Double?) {
        viewModelScope.launch { repo.setMaxWeight(exerciseId, maxWeight) }
    }

    fun rename(name: String, aliases: String) {
        viewModelScope.launch { repo.rename(exerciseId, name, aliases) }
    }

    fun addEntry(weight: Double, reps: Int, sets: Int) {
        viewModelScope.launch { repo.addEntry(exerciseId, weight, reps, sets) }
    }

    fun deleteEntry(entry: LogEntry) {
        viewModelScope.launch { repo.deleteEntry(entry) }
    }

    fun restoreEntry(entry: LogEntry) {
        viewModelScope.launch { repo.restoreEntry(entry) }
    }

    fun deleteExercise(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteExercise(exerciseId)
            onDone()
        }
    }

    companion object {
        fun factory(exerciseId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExerciseViewModel((this[APPLICATION_KEY] as CfApp).repository, exerciseId) }
        }
    }
}
