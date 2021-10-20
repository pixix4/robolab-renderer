package de.robolab.client.repl.commands.window

import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.IntParameter
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand

object WindowRotateCommand : ReplSingleBindableNodeCommand<ContentController>(
    "rotate",
    "Set rotation",
    ContentController::class,
) {

    init {
        addCommand(WindowRotateClockwiseCommand)
        addCommand(WindowRotateCounterClockwiseCommand)
        addCommand(WindowRotateResetCommand)
        addCommand(WindowRotateSetCommand)
    }
}

object WindowRotateClockwiseCommand : ReplBindableLeafCommand<ContentController>(
    "clockwise",
    "Rotate clockwise",
    ContentController::class,
) {

    private val degreeParameter = IntParameter.optional("degree")

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        val degree = context.getParameter(degreeParameter)

        binding.rotateClockwise(degree?.value)
    }
}


object WindowRotateCounterClockwiseCommand : ReplBindableLeafCommand<ContentController>(
    "counter-clockwise",
    "Rotate counter clockwise",
    ContentController::class,
) {

    private val degreeParameter = IntParameter.optional("degree")

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        val degree = context.getParameter(degreeParameter)

        binding.rotateCounterClockwise(degree?.value)
    }
}


object WindowRotateResetCommand : ReplBindableLeafCommand<ContentController>(
    "reset",
    "Reset rotation to 0",
    ContentController::class,
) {

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        binding.resetRotation()
    }
}


object WindowRotateSetCommand : ReplBindableLeafCommand<ContentController>(
    "set",
    "Set rotation",
    ContentController::class,
) {

    private val degreeParameter = IntParameter.param("degree")

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        val degree = context.getParameter(degreeParameter)
        binding.setRotation(degree.value)
    }
}
