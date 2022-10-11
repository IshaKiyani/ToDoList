package com.todolist.ui.deletedallcompleted

import androidx.lifecycle.ViewModel
import com.todolist.data.TaskDao
import com.todolist.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel

class DeletedAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) :ViewModel(){


    fun onConfirmClick()=applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}