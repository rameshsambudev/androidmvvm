package com.example.notesapp.ui.addeditnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notesapp.databinding.FragmentAddEditNoteBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditNoteFragment : Fragment() {

    private var _binding: FragmentAddEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditNoteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = if (viewModel.isEditing) "Edit Note" else "Add Note"
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.editTitle.addTextChangedListener { viewModel.onTitleChanged(it?.toString() ?: "") }
        binding.editContent.addTextChangedListener { viewModel.onContentChanged(it?.toString() ?: "") }

        binding.fabSave.setOnClickListener { viewModel.saveNote() }

        observeState()
        observeEvents()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.title.collect { title ->
                        if (binding.editTitle.text.toString() != title) {
                            binding.editTitle.setText(title)
                        }
                    }
                }
                launch {
                    viewModel.content.collect { content ->
                        if (binding.editContent.text.toString() != content) {
                            binding.editContent.setText(content)
                        }
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is AddEditNoteViewModel.UiEvent.NoteSaved -> {
                            findNavController().popBackStack()
                        }
                        is AddEditNoteViewModel.UiEvent.ShowError -> {
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
