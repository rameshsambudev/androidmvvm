package com.example.notesapp.ui.addeditnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: -1L

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>()
    val event: SharedFlow<UiEvent> = _event.asSharedFlow()

    val isEditing: Boolean get() = noteId != -1L

    init {
        if (isEditing) {
            viewModelScope.launch {
                repository.getNoteById(noteId)?.let { note ->
                    _title.value = note.title
                    _content.value = note.content
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _title.value = title
    }

    fun onContentChanged(content: String) {
        _content.value = content
    }

    fun saveNote() {
        viewModelScope.launch {
            if (_title.value.isBlank() && _content.value.isBlank()) {
                _event.emit(UiEvent.ShowError("Title or content cannot be empty"))
                return@launch
            }

            val note = Note(
                id = if (isEditing) noteId else 0,
                title = _title.value.trim(),
                content = _content.value.trim()
            )

            if (isEditing) {
                repository.updateNote(note)
            } else {
                repository.insertNote(note)
            }

            _event.emit(UiEvent.NoteSaved)
        }
    }

    sealed class UiEvent {
        data object NoteSaved : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}
