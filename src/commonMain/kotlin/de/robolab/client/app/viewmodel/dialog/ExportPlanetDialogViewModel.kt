package de.robolab.client.app.viewmodel.dialog

import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.viewmodel.DialogViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.property.property

class ExportPlanetDialogViewModel(private val filePlanetDocument: FilePlanetDocument) : DialogViewModel("Export planet") {

    private val fileNameProperty = property(filePlanetDocument.planetFile.planet.name.trim())

    val content = buildForm {
        labeledEntry("File name") {
            input(fileNameProperty)
        }
        labeledEntry("Export scale") {
            input(PreferenceStorage.exportScaleProperty, 0.1..100.0, 0.1)
        }
        labeledEntry("Export as") {
            button("SVG") {
                filePlanetDocument.exportAsSVG(fileNameProperty.value)
            }
            button("PNG") {
                filePlanetDocument.exportAsPNG(fileNameProperty.value)
            }
            button("Extended planet file") {
                filePlanetDocument.exportAsExtendedPlanetFile(fileNameProperty.value)
            }
        }
    }
}
