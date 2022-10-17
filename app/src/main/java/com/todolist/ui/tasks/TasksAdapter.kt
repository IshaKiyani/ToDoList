package com.todolist.ui.tasks


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.o3interfaces.todolist.databinding.ItemComponentBinding
import com.todolist.data.Task
import com.o3interfaces.todolist.databinding.ItemsBinding
import kotlinx.coroutines.flow.MutableStateFlow


class TasksAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    val hideCompleted = MutableStateFlow(false)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding =
            ItemComponentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }


    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItems = getItem(position)
        holder.bind(currentItems)
    }

    inner class TasksViewHolder(private val binding: ItemComponentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem((position))
                        listener.onItemClick(task)
                    }
                }
                checkBoxCompleted.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, checkBoxCompleted.isChecked)

                    }

                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                checkBoxCompleted.isChecked = task.completed
                itemName.text = task.name
                itemName.paint.isStrikeThruText = task.completed && hideCompleted.value
                labelPriority.isVisible = task.important

            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
        fun onHideCompletedOptionChanged(isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {


        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Task, newItem: Task) =

            oldItem == newItem

    }
}