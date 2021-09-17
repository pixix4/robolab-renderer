package de.robolab.client.repl.commands.window

import de.robolab.client.app.controller.ui.ContentSplitController
import de.robolab.client.app.controller.ui.GridLayout
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand

object WindowCommand : ReplSingleBindableNodeCommand<ContentSplitController>(
    "window",
    "Update general state of the user interface",
    ContentSplitController::class,
) {

    init {
        addCommand(WindowLayoutCommand)
        addCommand(WindowSplitHCommand)
        addCommand(WindowSplitVCommand)

        addCommand(WindowToggleCommand)
        addCommand(WindowZoomCommand)
        addCommand(WindowRotateCommand)
        addCommand(WindowTranslateCommand)
    }
}

object WindowLayoutCommand : ReplBindableLeafCommand<ContentSplitController>(
    "layout",
    "Set the window grid layout",
    ContentSplitController::class,
) {

    private val gridParameter = GridLayout.param("grid")

    override suspend fun execute(binding: ContentSplitController, context: IReplExecutionContext) {
        val grid = context.getParameter(gridParameter)
        binding.setGridLayout(grid.rows, grid.cols)
    }
}

object WindowSplitHCommand : ReplBindableLeafCommand<ContentSplitController>(
    "split-h",
    "Split horizontally",
    ContentSplitController::class,
) {

    override suspend fun execute(binding: ContentSplitController, context: IReplExecutionContext) {
        binding.splitEntryHorizontal()
    }
}

object WindowSplitVCommand : ReplBindableLeafCommand<ContentSplitController>(
    "split-v",
    "Split vertically",
    ContentSplitController::class,
) {

    override suspend fun execute(binding: ContentSplitController, context: IReplExecutionContext) {
        binding.splitEntryVertical()
    }
}
