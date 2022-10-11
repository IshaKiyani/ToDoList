package com.todolist.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.snackbar.Snackbar
import com.o3interfaces.todolist.R
import com.o3interfaces.todolist.databinding.HomefragmentBinding
import com.todolist.data.SortOrder
import com.todolist.data.Task
import com.todolist.util.exhaustive
import com.todolist.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.homefragment), TasksAdapter.OnItemClickListener {


    private val viewModel: TasksViewModel by viewModels()

    private lateinit var searchView: SearchView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
        val binding = HomefragmentBinding.bind(view)
        val tasksAdapter = TasksAdapter(this)


        binding.apply {
            recyclerViewTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)

            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]

                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)


            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }


        viewModel.tasks.observe(viewLifecycleOwner) {
            Log.d("taskList","list = ${it.size}")
            tasksAdapter.submitList((it))
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {


                    is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task Deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }


                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToAddEditTaskFragment("New Tasks")
                        findNavController().navigate(action)

                    }


                    is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToAddEditTaskFragment("Edit Task")
                        findNavController().navigate(action)

                    }


                    is TasksViewModel.TasksEvent.showTaskSavesConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }


                    TasksViewModel.TasksEvent.NavigaToDeleteAllCompletedScreen -> {
                        val action =
                            HomeFragmentDirections.actionGlobalDeletedAllCompletedDialogueFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }

        }

        setHasOptionsMenu(true)
    }


    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }


    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {


        inflater.inflate(R.menu.menu_fragment_tasks, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery .isNotEmpty())
            searchItem.expandActionView()
        searchView.setQuery(pendingQuery,false)

            searchView.onQueryTextChanged {
                viewModel.searchQuery.value = it
            }


        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {


            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }


            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }


            R.id.action_hide_completed_tasks -> {

                item.isChecked = item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }


            R.id.action_delete_all_completed_tasks -> {

                viewModel.onDeleteAllCompletedClick()
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

}