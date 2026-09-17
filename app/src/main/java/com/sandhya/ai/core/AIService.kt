package com.sandhya.ai.core

import com.sandhya.ai.command.AssistantCommand
import com.sandhya.ai.command.CommandParser

/**
 * AI boundary: returns structured commands only.
 * Never executes model-generated code.
 *
 * A remote/local LLM can be plugged in here later. Its output should be
 * validated into AssistantCommand before TaskManager/AppManager executes it.
 */
class AIService(private val parser: CommandParser = CommandParser()) {
    fun interpret(text: String): AssistantCommand = parser.parse(text)
}
