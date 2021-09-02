class TextEditor {

    /**
     *
     * @param container: HTMLElement
     */
    constructor(container) {
        this.editor = new CodeMirror(container, {
            value: "",
            mode: "application/ld+json",
            styleActiveLine: {nonEmpty: false}
        });
    }

    get value() {
        return this.editor.getValue()
    }

    set value(val) {
        let cursor = this.editor.getCursor();
        let scroll = this.editor.getScrollInfo();
        this.editor.setValue(val);
        this.editor.setCursor(cursor);
        this.editor.scrollTo(scroll.left, scroll.top);
    }

    addOnChangeListener(callback) {
        this.editor.on("changes", () => {
            callback();
        });
    }

    addOnCursorListener(callback) {
        this.editor.on("cursorActivity", () => {
            let cursor = this.editor.getCursor();
            callback(cursor.line, cursor.ch);
        })
    }

    setCursor(line, ch) {
        this.editor.setCursor({line: line, ch: ch});
    }

    refresh() {
        this.editor.refresh()
    }
}
