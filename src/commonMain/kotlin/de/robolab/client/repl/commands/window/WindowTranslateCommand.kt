package de.robolab.client.repl.commands.window

import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.repl.base.DoubleParameter
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand
import de.robolab.common.utils.Vector

object WindowTranslateCommand : ReplSingleBindableNodeCommand<ContentController>(
    "translate",
    "Set translation",
    ContentController::class,
) {

    init {
        addCommand(WindowTranslateUpCommand)
        addCommand(WindowTranslateLeftCommand)
        addCommand(WindowTranslateDownCommand)
        addCommand(WindowTranslateRightCommand)
        addCommand(WindowTranslateCenterCommand)
    }
}

object WindowTranslateUpCommand : ReplBindableLeafCommand<ContentController>(
    "up",
    "Translate up",
    ContentController::class,
) {

    private val byParameter = DoubleParameter.optional("by")

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        val by = context.getParameter(byParameter)
        binding.translate(Vector(0.0, 1.0), by?.value)
    }
}

object WindowTranslateLeftCommand : ReplBindableLeafCommand<ContentController>(
    "left",
    "Translate left",
    ContentController::class,
) {

    private val byParameter = DoubleParameter.optional("by")

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        val by = context.getParameter(byParameter)
        binding.translate(Vector(1.0, 0.0), by?.value)
    }
}

object WindowTranslateDownCommand : ReplBindableLeafCommand<ContentController>(
    "down",
    "Translate down",
    ContentController::class,
) {

    private val byParameter = DoubleParameter.optional("by")

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        val by = context.getParameter(byParameter)
        binding.translate(Vector(0.0, -1.0), by?.value)
    }
}

object WindowTranslateRightCommand : ReplBindableLeafCommand<ContentController>(
    "right",
    "Translate right",
    ContentController::class,
) {

    private val byParameter = DoubleParameter.optional("by")

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        val by = context.getParameter(byParameter)
        binding.translate(Vector(-1.0, 0.0), by?.value)
    }
}

object WindowTranslateCenterCommand : ReplBindableLeafCommand<ContentController>(
    "center",
    "Center planet",
    ContentController::class,
) {

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        binding.center()
    }
}
