package de.robolab.client.repl.commands.group

import de.robolab.client.app.controller.ConnectionController
import de.robolab.client.app.controller.MainController
import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.app.model.base.EmptyPlanetDocument
import de.robolab.client.app.model.group.GroupNavigationTab
import de.robolab.client.repl.base.*
import de.robolab.client.repl.commands.group.GroupAddCommand.optional

object GroupCommand : ReplSingleBindableNodeCommand<MainController>(
    "group",
    "Manage groups in the view",
    MainController::class,
) {

    init {
        addCommand(GroupAddCommand)
        addCommand(GroupMultipleCommand)
        addCommand(GroupRemoveCommand)
    }
}

object GroupAddCommand : ReplBindableLeafCommand<MainController>(
    "add",
    "Add a group to a pane",
    MainController::class,
) {

    private val groupParameter = StringParameter.param("group")
    private val indexParameter = IntParameter.optional("index")

    override suspend fun execute(binding: MainController, context: IReplExecutionContext) {
        val group = context.getParameter(groupParameter)
        val index = context.getParameter(indexParameter)

        binding.appendLiveGroupView(listOf(group.value), index?.value)
    }
}

object GroupMultipleCommand : ReplBindableLeafCommand<MainController>(
    "multiple",
    "Add a group to a pane",
    MainController::class,
) {

    private val groupsParameter = StringParameter.vararg("groups")

    override suspend fun execute(binding: MainController, context: IReplExecutionContext) {
        val groups = context.getParameter(groupsParameter)
        binding.appendLiveGroupView(groups.map { it.value })
    }
}

object GroupRemoveCommand : ReplBindableLeafCommand<MainController>(
    "remove",
    "Clear a pane",
    MainController::class,
) {

    private val indexParameter = IntParameter.optional("index")

    override suspend fun execute(binding: MainController, context: IReplExecutionContext) {
        val index = context.getParameter(indexParameter)?.value ?: 0
        binding.contentController.openDocumentAtIndex(EmptyPlanetDocument(), index, false)
    }
}
