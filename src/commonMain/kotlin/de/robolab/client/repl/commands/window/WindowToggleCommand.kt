package de.robolab.client.repl.commands.window

import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.repl.base.BooleanParameter
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand
import de.westermann.kobserve.toggle

object WindowToggleCommand : ReplSingleBindableNodeCommand<UiController>(
    "toggle",
    "Toggle visibility of user interface elements",
    UiController::class,
) {

    init {
        addCommand(WindowToggleFullscreenCommand)
        addCommand(WindowToggleInfoBarCommand)
        addCommand(WindowToggleNavigationBarCommand)
        addCommand(WindowToggleTerminalCommand)
    }
}

object WindowToggleFullscreenCommand : ReplBindableLeafCommand<UiController>(
    "fullscreen",
    "Toggle the fullscreen mode",
    UiController::class,
) {

    private val forceParameter = BooleanParameter.optional("force")

    override suspend fun execute(binding: UiController, context: IReplExecutionContext) {
        val force = context.getParameter(forceParameter)

        if (force == null) {
            binding.fullscreenProperty.toggle()
        } else {
            binding.fullscreenProperty.value = force.value
        }
    }
}

object WindowToggleInfoBarCommand : ReplBindableLeafCommand<UiController>(
    "info-bar",
    "Toggle the right info bar",
    UiController::class,
) {

    private val forceParameter = BooleanParameter.optional("force")

    override suspend fun execute(binding: UiController, context: IReplExecutionContext) {
        val force = context.getParameter(forceParameter)

        if (force == null) {
            binding.infoBarEnabledProperty.toggle()
        } else {
            binding.infoBarEnabledProperty.value = force.value
        }
    }
}

object WindowToggleNavigationBarCommand : ReplBindableLeafCommand<UiController>(
    "navigation-bar",
    "Toggle the left navigation bar",
    UiController::class,
) {

    private val forceParameter = BooleanParameter.optional("force")

    override suspend fun execute(binding: UiController, context: IReplExecutionContext) {
        val force = context.getParameter(forceParameter)

        if (force == null) {
            binding.navigationBarEnabledProperty.toggle()
        } else {
            binding.navigationBarEnabledProperty.value = force.value
        }
    }
}

object WindowToggleTerminalCommand : ReplBindableLeafCommand<UiController>(
    "terminal",
    "Toggle the terminal",
    UiController::class,
) {

    private val forceParameter = BooleanParameter.optional("force")

    override suspend fun execute(binding: UiController, context: IReplExecutionContext) {
        val force = context.getParameter(forceParameter)

        if (force == null) {
            binding.terminalEnabledProperty.toggle()
        } else {
            binding.terminalEnabledProperty.value = force.value
        }
    }
}
