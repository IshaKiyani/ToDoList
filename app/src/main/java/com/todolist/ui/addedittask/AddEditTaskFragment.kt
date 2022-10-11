package com.todolist.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.o3interfaces.todolist.R
import com.o3interfaces.todolist.databinding.AddEditTasksBinding
import com.todolist.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.add_edit_tasks) {
    private val viewModel: AddEditTaskViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = AddEditTasksBinding.bind(view)

        binding.apply {
            etTaskName.setText(viewModel.taskName)
            checkBoxImportant.isChecked = viewModel.taskImportance as Boolean
            checkBoxImportant.jumpDrawablesToCurrentState()

            txtViewDataCreated.isVisible = viewModel.task != null
            txtViewDataCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"


            etTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()

            }
            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            saveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigatedBackedWithResult -> {

                        binding.etTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_request" to event.result)
                        )
                        findNavController().popBackStack()
                    }

                }.exhaustive

            }
        }
    }
}