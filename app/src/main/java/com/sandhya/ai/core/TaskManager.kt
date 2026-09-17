package com.sandhya.ai.core

enum class TaskState { WAITING, LISTENING, PROCESSING, EXECUTING, COMPLETED, ERROR }

class TaskManager {
    var state: TaskState = TaskState.WAITING
        private set

    fun set(newState: TaskState) { state = newState }
}
