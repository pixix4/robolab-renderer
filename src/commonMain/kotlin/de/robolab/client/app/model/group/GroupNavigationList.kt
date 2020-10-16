package de.robolab.client.app.model.group

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.repository.Group
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GroupNavigationList(
    private val messageRepository: MessageRepository,
    private val root: GroupNavigationRoot
) : GroupNavigationRoot.RepositoryList {

    override val parentNameProperty = constObservable<String?>(null)

    override fun openParent() {
    }

    override val childrenProperty = observableListOf<Entry>()

    override fun onGroupListChange() {
        GlobalScope.launch {
            val list = messageRepository
                .getGroupList()
                .map {
                    Entry(it)
                }

            runAsync {
                childrenProperty.sync(list)
            }
        }
    }

    override fun onGroupAttemptListChange(group: Group) {
        childrenProperty.find { it.group.groupId == group.groupId }?.update(group)
    }

    init {
        GlobalScope.launch {
            val list = messageRepository
                .getGroupList()
                .map {
                    Entry(it)
                }

            runAsync {
                childrenProperty.addAll(list)
            }
        }
    }

    inner class Entry(
        group: Group
    ) : INavigationBarEntry {

        private val groupProperty = property(group)
        val group by groupProperty

        fun update(group: Group) {
            groupProperty.value = group
        }

        override val nameProperty = groupProperty.mapBinding { group ->
            buildString {
                append(group.name)

                if (group.planet != null && group.planet.isNotEmpty()) {
                    append(" (")
                    append(group.planet)
                    append(')')
                }
            }
        }

        override val subtitleProperty = groupProperty.mapBinding { group ->
            buildString {
                append(group.attemptCount)
                append(" attempt")
                if (group.attemptCount != 1) {
                    append("s")
                }
            }
        }

        override val enabledProperty = constObservable(true)

        override val statusIconProperty = constObservable<List<MaterialIcon>>(emptyList())

        override fun open(asNewTab: Boolean) {
            if (asNewTab) {
                root.openGroupLiveAttempt(group, false)
            } else {
                root.openGroupAttemptList(group)
            }
        }
    }
}
