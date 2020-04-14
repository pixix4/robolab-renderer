package de.robolab.jfx.style

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

/**
 * @author lars
 */
class CodeArea : Stylesheet() {
    companion object {
        val keyword by cssclass()
        val direction by cssclass()
        val number by cssclass()
        val comment by cssclass()
        val string by cssclass()
        val error by cssclass()
        val paragraphBox by cssclass()
        val hasCaret by csspseudoclass()

        val keywordColor = Color.web("#000080")
        val directionColor = Color.web("#660E7A")
        val numberColor = Color.web("#0000FF")
        val commentColor = Color.web("#808080")
        val stringColor = Color.web("#008000")
        val errorColor = Color.web("#F44336")
        val selectedLineColor = Color.web("#f9f4d9")
    }


    init {
        keyword {
            fill = keywordColor
            fontWeight = FontWeight.BOLD
        }
        direction {
            fill = directionColor
            fontWeight = FontWeight.BOLD
        }
        number {
            fill = numberColor
        }
        comment {
            fill = commentColor
        }
        string {
            fill = stringColor
        }
        error {
            fill = errorColor
            fontWeight = FontWeight.BOLD
            underline = true
        }
        paragraphBox {
            and(hasCaret) {
                backgroundColor += selectedLineColor
            }
        }
    }
}
