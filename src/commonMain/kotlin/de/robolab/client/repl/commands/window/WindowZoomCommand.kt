package de.robolab.client.repl.commands.window

import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.IntParameter
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand

object WindowZoomCommand : ReplSingleBindableNodeCommand<ContentController>(
    "zoom",
    "Set zoom level",
    ContentController::class,
) {

    init {
        addCommand(WindowZoomInCommand)
        addCommand(WindowZoomOutCommand)
        addCommand(WindowZoomSetCommand)
        addCommand(WindowZoomResetCommand)
    }
}

object WindowZoomInCommand : ReplBindableLeafCommand<ContentController>(
    "in",
    "Zoom in",
    ContentController::class,
) {

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        binding.zoomIn()
    }
}


object WindowZoomOutCommand : ReplBindableLeafCommand<ContentController>(
    "out",
    "Zoom out",
    ContentController::class,
) {

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        binding.zoomOut()
    }
}


object WindowZoomResetCommand : ReplBindableLeafCommand<ContentController>(
    "reset",
    "Reset zoom",
    ContentController::class,
) {

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        binding.resetZoom()
    }
}


object WindowZoomSetCommand : ReplBindableLeafCommand<ContentController>(
    "set",
    "Set zoom level",
    ContentController::class,
) {

    private val zoomParameter = IntParameter.param("zoom")

    override suspend fun execute(binding: ContentController, context: IReplExecutionContext) {
        val zoom = context.getParameter(zoomParameter)
        binding.setZoom(zoom.value)
    }
}
