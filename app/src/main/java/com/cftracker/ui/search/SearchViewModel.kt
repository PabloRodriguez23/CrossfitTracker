package com.cftracker.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cftracker.CfApp
import com.cftracker.data.ExerciseSummary
import com.cftracker.data.TrackerRepository
import com.cftracker.util.ExerciseSearch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(private val repo: TrackerRepository) : ViewModel() {

    var query by mutableStateOf("")
        private set

    val results: StateFlow<List<ExerciseSummary>> =
        combine(repo.observeSummaries(), snapshotFlow { query }) { all, q -> ExerciseSearch.filter(all, q) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQuery(value: String) {
        query = value
    }

    fun create(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch { onCreated(repo.createExercise(name)) }
    }

    fun addEntry(exerciseId: Long, weight: Double, reps: Int) {
        viewModelScope.launch { repo.addEntry(exerciseId, weight, reps) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { SearchViewModel((this[APPLICATION_KEY] as CfApp).repository) }
        }
    }
}
