package de.robolab.client.jfx.dialog

import de.robolab.client.jfx.adapter.toFx
import de.robolab.common.utils.BuildInformation
import de.westermann.kobserve.property.constObservable
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.label


class InfoDialog : GenericDialog() {

    private val runtimeJavaVersionProperty = constObservable(System.getProperty("java.version"))
    private val runtimeJavaVendorProperty = constObservable(System.getProperty("java.vm.name"))
    private val runtimeSystemNameProperty = constObservable(System.getProperty("os.name"))
    private val runtimeSystemVersionProperty = constObservable(System.getProperty("os.version"))

    override val root = buildContent("Build information") {
        form {

            val data = BuildInformation.generateDataMap(
                "JavaVersion" to runtimeJavaVersionProperty,
                "JavaVendor" to runtimeJavaVendorProperty,
                "SystemName" to runtimeSystemNameProperty,
                "SystemVersion" to runtimeSystemVersionProperty
            )

            for ((topic, content) in data) {
                fieldset(topic) {
                    for ((key, value) in content) {
                        field(key) {
                            label(value.toFx())
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun open() {
            open<InfoDialog>()
        }
    }
}
