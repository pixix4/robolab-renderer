package de.robolab.client.app.viewmodel

import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.observeConst

@FormBuilderAnnotation
class FormBuilder() {

    constructor(content: Collection<FormViewModel>): this() {
        this.content.addAll(content)
    }

    private val content = mutableListOf<FormViewModel>()

    fun entry(builder: FormContentBuilder.() -> Unit): FormViewModel.Entry {
        val b = FormContentBuilder()
        builder(b)
        val result = FormViewModel.Entry(b.buildGroup())
        content += result
        return result
    }

    fun labeledEntry(label: String, builder: FormContentBuilder.() -> Unit): FormViewModel.LabeledEntry {
        val b = FormContentBuilder()
        builder(b)
        val result = FormViewModel.LabeledEntry(label, b.buildGroup())
        content += result
        return result
    }

    fun labeledGroup(label: String, builder: FormBuilder.() -> Unit): FormViewModel.LabeledGroup {
        val b = FormBuilder()
        builder(b)
        val result = b.buildLabeledGroup(label)
        content += result
        return result
    }

    fun group(builder: FormBuilder.() -> Unit): FormViewModel.Group {
        val b = FormBuilder()
        builder(b)
        val result = b.buildGroup()
        content += result
        return result
    }

    fun clear() {
        content.clear()
    }

    fun buildGroup(): FormViewModel.Group {
        return FormViewModel.Group(content.asObservable())
    }

    fun buildLabeledGroup(label: String): FormViewModel.LabeledGroup {
        return FormViewModel.LabeledGroup(label.observeConst(), content.asObservable())
    }

    fun buildSync(contentProperty: ObservableMutableList<FormViewModel>) {
        contentProperty.sync(content)
    }
}

fun buildForm(builder: FormBuilder.() -> Unit): FormViewModel.Group {
    val b = FormBuilder()
    builder(b)
    return b.buildGroup()
}

fun FormViewModel.Group.build(builder: FormBuilder.() -> Unit) {
    val b = FormBuilder(contentProperty)
    builder(b)
    b.buildSync(contentProperty)
}

fun FormViewModel.LabeledGroup.build(builder: FormBuilder.() -> Unit) {
    val b = FormBuilder(contentProperty)
    builder(b)
    b.buildSync(contentProperty)
}
