package com.todolist.ui.tasks


import android.util.Log
import androidx.lifecycle.*
import com.todolist.data.*
import com.todolist.ui.ADD_TASK_RESULT_OK
import com.todolist.ui.EDIT_TASK_RESULT_OK
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    //remove assisted

    private val state: SavedStateHandle
) : ViewModel() {


    // init {

//        val aa = taskDao.getAllTasks()
//
//        viewModelScope.launch {
//            aa.collect { value ->
//                Log.d("taskList", "init = ${value.size}")
//                Log.d("taskList", "init = ${value.firstOrNull()?.name ?: "No Data Found from DATABASE"}")
//            }
//
//        }

    // }

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()

    val tasksEvent = tasksEventChannel.receiveAsFlow()


    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        Log.d("taskList", "taskList = $searchQuery")
        //replace query with " "           ..m
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
        //taskDao.getAllTasks()
    }

//
//     private val tasksFlow = combine(
//        searchQuery.asFlow(),
//        preferencesFlow
//    ) { query, filterPreferences ->
//        Pair(query, filterPreferences)
//    }.flatMapLatest { (query, filterPreferences) ->
//        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
//    }

    var tasks = tasksFlow.asLiveData()


    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }


    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }


    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }


    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }


    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }


    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }


    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }


    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")

        }
    }


    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }


    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
    }


    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
    }
}

