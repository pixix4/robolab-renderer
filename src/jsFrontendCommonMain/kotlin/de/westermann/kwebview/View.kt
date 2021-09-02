package de.westermann.kwebview

import de.robolab.client.utils.buildJsInterface
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.event.EventHandler
import kotlinx.browser.document
import org.w3c.dom.*
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.events.FocusEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import kotlin.js.Date

abstract class View(view: HTMLElement = createHtmlView()) {

    open val html: HTMLElement = view.also { view ->
        this::class.simpleName?.let { name ->
            view.classList.add(name.toDashCase())
        }
    }

    val classList = ClassList(view.classList)
    val dataset = DataSet(view.dataset)

    val isAttached: Boolean
        get() {
            var parent = html.parentNode
            while (parent != null) {
                if (parent == document) {
                    return true
                }
                parent = parent.parentNode
            }
            return false
        }

    var id by AttributeDelegate()

    val clientLeft: Int
        get() = html.clientLeft
    val clientTop: Int
        get() = html.clientTop
    val clientWidth: Int
        get() = html.clientWidth
    val clientHeight: Int
        get() = html.clientHeight

    val offsetLeft: Int
        get() = html.offsetLeft
    val offsetTop: Int
        get() = html.offsetTop
    val offsetWidth: Int
        get() = html.offsetWidth
    val offsetHeight: Int
        get() = html.offsetHeight


    val scrollLeft: Double
        get() = html.scrollLeft
    val scrollTop: Double
        get() = html.scrollTop
    val scrollWidth: Int
        get() = html.scrollWidth
    val scrollHeight: Int
        get() = html.scrollHeight

    fun scrollTo(left: Number? = null, top: Number? = null, behavior: ScrollBehavior? = ScrollBehavior.AUTO) {
        html.scrollTo(buildJsInterface {
            this.left = left?.toDouble()
            this.top = top?.toDouble()
            this.behavior = behavior
        })
    }

    fun scrollBy(left: Number? = null, top: Number? = null, behavior: ScrollBehavior? = ScrollBehavior.AUTO) {
        html.scrollBy(buildJsInterface {
            this.left = left?.toDouble()
            this.top = top?.toDouble()
            this.behavior = behavior
        })
    }

    fun scrollIntoView() {
        html.scrollIntoView()
    }

    fun offsetLeftTotal(maxDepth: Int = Int.MAX_VALUE): Int {
        var element: HTMLElement? = html
        var offset = 0
        var depth = 0
        while (element != null && depth <= maxDepth) {
            offset += element.offsetLeft
            element = element.offsetParent as? HTMLElement
            depth += 1
        }
        return offset
    }
    val offsetLeftTotal: Int
        get() = offsetLeftTotal()

    fun offsetTopTotal(maxDepth: Int = Int.MAX_VALUE): Int {
        var element: HTMLElement? = html
        var offset = 0
        var depth = 0
        while (element != null && depth <= maxDepth) {
            offset += element.offsetTop
            element = element.offsetParent as? HTMLElement
            depth += 1
        }
        return offset
    }
    val offsetTopTotal: Int
        get() = offsetTopTotal()

    val dimension: Rectangle
        get() = html.getBoundingClientRect().toRectangle()

    @Suppress("LeakingThis")
    val titleProperty = AttributeProperty(this, "title")
    var title by titleProperty

    val style = view.style
    fun style(block: CSSStyleDeclaration.() -> Unit) {
        block(style)
    }

    fun focus() {
        html.focus()
    }

    fun blur() {
        html.blur()
    }

    fun click() {
        html.click()
    }

    fun allowFocus() {
        html.tabIndex = 0
    }

    val onClick = EventHandler<MouseEvent>()
    val onAuxClick = EventHandler<MouseEvent>()
    val onDblClick = EventHandler<MouseEvent>()
    val onContext = EventHandler<MouseEvent>()

    val onMouseDown = EventHandler<MouseEvent>()
    val onMouseMove = EventHandler<MouseEvent>()
    val onMouseUp = EventHandler<MouseEvent>()
    val onMouseEnter = EventHandler<MouseEvent>()
    val onMouseLeave = EventHandler<MouseEvent>()

    val onTouchStart = EventHandler<TouchEvent>()
    val onTouchEnd = EventHandler<TouchEvent>()
    val onTouchMove = EventHandler<TouchEvent>()
    val onTouchCancel = EventHandler<TouchEvent>()

    val onWheel = EventHandler<WheelEvent>()

    val onKeyDown = EventHandler<KeyboardEvent>()
    val onKeyPress = EventHandler<KeyboardEvent>()
    val onKeyUp = EventHandler<KeyboardEvent>()

    val onFocus = EventHandler<FocusEvent>()
    val onBlur = EventHandler<FocusEvent>()

    val onDragOver = EventHandler<DragEvent>()
    val onDrop = EventHandler<DragEvent>()

    init {
        // console.log(Date.now(), "Create new view ${this::class.simpleName}", view)

        onClick.bind(view, "click")
        onAuxClick.bind(view, "auxclick")
        onDblClick.bind(view, "dblclick")
        onContext.bind(view, "contextmenu")

        onMouseDown.bind(view, "mousedown")
        onMouseMove.bind(view, "mousemove")
        onMouseUp.bind(view, "mouseup")
        onMouseEnter.bind(view, "mouseenter")
        onMouseLeave.bind(view, "mouseleave")

        if (Document.isTouchSupported) {
            onTouchStart.bind(view, "touchstart")
            onTouchEnd.bind(view, "touchend")
            onTouchMove.bind(view, "touchmove")
            onTouchCancel.bind(view, "touchcancel")
        }

        onWheel.bind(view, "wheel")

        onKeyDown.bind(view, "keydown")
        onKeyPress.bind(view, "keypress")
        onKeyUp.bind(view, "keyup")

        onFocus.bind(view, "focus")
        onBlur.bind(view, "blur")

        if (js("!!window.DragEvent") == true) {
            onDragOver.bind(view, "dragover")
            onDrop.bind(view, "drop")
        }
    }
}
