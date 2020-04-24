class TextEditor {

    /**
     *
     * @param container: HTMLElement
     */
    constructor(container) {
        let KEYWORD_PATTERN = /\b(name|spline|comment|blue|start|target|direction)\b/;
        let DIRECTION_PATTERN = /\b[NESWnesw]\b/;
        let NUMBER_PATTERN = /\b(-?[0-9]+)\b/;
        let STRING_PATTERN = /\b([a-zA-Z]*)\b/;
        let HASH_PATTERN = /#/;
        let COMMENT_PATTERN = /#.*/;
        let TRAILING_WHITESPACE = /\\s*$/;

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
            styleActiveLine: { nonEmpty: false }
        });
    }

    get value() {
        return this.editor.getValue()
    }

    set value(val) {
        this.editor.setValue(val)
    }

    addOnChangeListener(callback) {
        this.editor.on("changes", () => {
            callback();
        });
    }
}
