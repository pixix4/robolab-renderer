class TextEditor {

    /**
     *
     * @param container: HTMLElement
     */
    constructor(container) {
        let KEYWORD_PATTERN = /(\b(comment|left|center|right|name|version|spline|grouping|commen|blue|start|target|direction)|\$[\w-]*)\b/;
        let DIRECTION_PATTERN = /\b[NESWnesw]\b/;
        let NUMBER_PATTERN = /\b(-?[0-9]+)\b/;
        let STRING_PATTERN = /\b([a-zA-Z]+)\b/;
        let HASH_PATTERN = /#/;
        let COMMENT_PATTERN = /#.+/;
        let TRAILING_WHITESPACE = /\\s+$/;
        let OTHER = /.+/;

        CodeMirror.defineSimpleMode("simplemode", {
            start: [
                {
                    regex: KEYWORD_PATTERN,
                    token: "keyword"
                },
                {
                    regex: DIRECTION_PATTERN,
                    token: "direction"
                },
                {
                    regex: NUMBER_PATTERN,
                    token: "number"
                },
                {
                    regex: STRING_PATTERN,
                    token: "string"
                },
                {
                    regex: HASH_PATTERN,
                    token: "hash",
                    sol: true
                },
                {
                    regex: COMMENT_PATTERN,
                    token: "comment"
                },
                {
                    regex: TRAILING_WHITESPACE,
                    token: "whitespace"
                }
            ]
        });

        this.editor = new CodeMirror(container, {
            value: "",
            mode: "simplemode",
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
