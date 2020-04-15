package de.robolab.web.views

import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.Property
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*

class SettingsView : ViewCollection<View>() {

    val onClose = EventHandler<Unit>()
    
    private fun BoxView.settingsGroup(name: String, block: BoxView.() -> Unit) {
        boxView("settings-group") {
            textView(name) {
                classList += "settings-group-header"
            }

            block()
        }
    }

    private fun BoxView.settingsEntry(name: String, block: BoxView.() -> Unit) {
        boxView("settings-entry") {
            textView(name)

            block()
        }
    }

    init {
        boxView("settings-window") {

            boxView("settings-header") {
                textView("Settings")
                button {
                    iconView(MaterialIcon.CLOSE)
                    onClick {
                        println("Close")
                        onClose.emit(Unit)
                    }
                }
            }

            boxView("settings-body") {
                settingsGroup("Appearance") {
                    settingsEntry("Theme") {
                        selectView(PreferenceStorage.selectedThemeProperty) {
                            disabledProperty.bind(PreferenceStorage.useSystemThemeProperty)
                        }
                    }
                    settingsEntry("Use system theme") {
                        label(checkbox(PreferenceStorage.useSystemThemeProperty))
                    }
                }

                settingsGroup("Plotting") {
                    settingsEntry("Export scale") {
                        inputView(InputType.NUMBER, PreferenceStorage.exportScaleProperty.bindStringParsing()) {
                            min = 0.1
                            max = 100.0
                            step = 0.1
                        }
                    }
                }

                settingsGroup("Connection") {
                    settingsEntry("Server uri") {
                        inputView(PreferenceStorage.serverUriProperty)
                    }
                    settingsEntry("Username") {
                        inputView(PreferenceStorage.usernameProperty)
                    }
                    settingsEntry("Password") {
                        inputView(InputType.PASSWORD, PreferenceStorage.passwordProperty)
                    }
                    settingsEntry("Client id") {
                        inputView(PreferenceStorage.clientIdProperty)
                    }
                }
            }

            onClick { event ->
                event.stopPropagation()
            }
        }

        onClick { event ->
            event.stopPropagation()
            onClose.emit(Unit)
        }
    }

    companion object {
        fun open() {
            val settings = SettingsView()
            Body.append(settings)

            settings.onClose {
                Body.remove(settings)
            }
        }
    }
}

fun Property<Double>.bindStringParsing() = property(object : FunctionAccessor<String> {
    override fun set(value: String): Boolean {
        this@bindStringParsing.value = value.toDoubleOrNull() ?: return false
        return true
    }

    override fun get(): String {
        return this@bindStringParsing.value.toString().also(::println)
    }

}, this)
