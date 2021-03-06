package de.robolab.client.app.model.group

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.Group
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.MenuBuilder
import de.robolab.client.utils.runAsync
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.formatDateTime
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class GroupAttemptNavigationList(
    private val group: Group,
    private val messageRepository: MessageRepository,
    private val tab: GroupNavigationTab,
    override val parent: INavigationBarList
) : GroupNavigationTab.RepositoryList {

    override val nameProperty = constObservable("Group ${group.name}")

    private var entryList = listOf<Entry>()
    override val childrenProperty = observableListOf<INavigationBarEntry>()

    private val liveList = listOf(LiveEntry())

    override fun onGroupAttemptListChange(group: Group) {
        if (group.groupId != this.group.groupId) return
        GlobalScope.launch {
            entryList = messageRepository
                .getGroupAttemptList(group.groupId)
                .map {
                    Entry(it)
                }

            runAsync {
                childrenProperty.sync(liveList + entryList)
            }
        }
    }

    override fun onAttemptMessageListChange(attempt: Attempt) {
        entryList.find { it.attempt.attemptId == attempt.attemptId }?.update(attempt)
    }

    init {
        GlobalScope.launch {
            entryList = messageRepository
                .getGroupAttemptList(group.groupId)
                .map {
                    Entry(it)
                }

            runAsync {
                childrenProperty.sync(liveList + entryList)
            }
        }
    }

    inner class Entry(
        attempt: Attempt
    ) : INavigationBarEntry {

        private val attemptProperty = property(attempt)
        val attempt by attemptProperty

        fun update(attempt: Attempt) {
            attemptProperty.value = attempt
        }

        override val tab = this@GroupAttemptNavigationList.tab

        override val nameProperty = attemptProperty.mapBinding { attempt ->
            formatDateTime(Instant.fromEpochMilliseconds(attempt.startMessageTime), DATE_FORMAT)
        }

        override val subtitleProperty = attemptProperty.mapBinding { attempt ->
            buildString {
                append(attempt.messageCount)
                append(" message")
                if (attempt.messageCount != 1) {
                    append("s")
                }
                if (attempt.planet != null) {
                    append(" (")
                    append(attempt.planet)
                    append(")")
                }
            }
        }

        override val enabledProperty = constObservable(true)

        override val statusIconProperty = constObservable<List<MaterialIcon>>(emptyList())

        override fun MenuBuilder.contextMenu() {
            name = nameProperty.value
            action("Open in new tab") {
                open(true)
            }
        }

        override suspend fun getRenderDataTimestamp(): Long {
            return attempt.lastMessageTime
        }

        override suspend fun <T : ICanvas> renderPreview(canvasCreator: (dimension: Dimension) -> T?): T? {
            return this@GroupAttemptNavigationList.tab.renderGroupAttemptPreview(attempt, canvasCreator)
        }
    }

    inner class LiveEntry : INavigationBarEntry {

        val entry = childrenProperty.mapBinding { entryList.firstOrNull() }

        override val nameProperty = constObservable("Live")
        override val subtitleProperty = entry.nullableFlatMapBinding { it?.subtitleProperty }.mapBinding { it ?: "" }
        override val enabledProperty = entry.nullableFlatMapBinding { it?.enabledProperty }.mapBinding { it ?: false }
        override val statusIconProperty =
            entry.nullableFlatMapBinding { it?.statusIconProperty }.mapBinding { it ?: emptyList() }

        override val tab = this@GroupAttemptNavigationList.tab

        val group: Group
            get() = this@GroupAttemptNavigationList.group

        override fun MenuBuilder.contextMenu() {
            name = nameProperty.value
            action("Open in new tab") {
                open(true)
            }
        }

        override suspend fun getRenderDataTimestamp(): Long {
            return group.lastMessageTime
        }

        override suspend fun <T : ICanvas> renderPreview(canvasCreator: (dimension: Dimension) -> T?): T? {
            return this@GroupAttemptNavigationList.tab.renderGroupLiveAttemptPreview(group, canvasCreator)
        }
    }

    companion object {
        private const val DATE_FORMAT = "HH:mm:ss"
    }
}
