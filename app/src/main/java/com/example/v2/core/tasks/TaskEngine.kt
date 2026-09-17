package com.example.v2.core.tasks

import com.example.v2.core.EventBus
import com.example.v2.core.AssistantEvent
import com.example.v2.core.StateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class TaskState {
    PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val state: TaskState = TaskState.PENDING,
    val progress: Float = 0f
)

class TaskEngine {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    fun submitTask(name: String): String {
        val task = Task(name = name)
        _tasks.value = _tasks.value + task
        EventBus.publish(AssistantEvent.TaskStarted(task.id))
        updateGlobalState()
        return task.id
    }

    fun updateTaskState(taskId: String, state: TaskState, progress: Float = 0f) {
        val current = _tasks.value.toMutableList()
        val idx = current.indexOfFirst { it.id == taskId }
        if (idx != -1) {
            current[idx] = current[idx].copy(state = state, progress = progress)
            _tasks.value = current
            if (state == TaskState.COMPLETED) {
                EventBus.publish(AssistantEvent.TaskCompleted(taskId))
            }
            updateGlobalState()
        }
    }
    
    private fun updateGlobalState() {
        val activeCount = _tasks.value.count { it.state == TaskState.RUNNING }
        StateManager.updateState { it.copy(activeTaskCount = activeCount) }
    }
}
