package de.robolab.renderer.platform

data class KeyEvent(
        val keyCode: KeyCode,
        val text: String,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false
)

enum class KeyCode() {
    // Key: ','
    COMMA,

    // Key: '<'
    ANGLE_BRACKET_LEFT,

    // Key: '.'
    PERIOD,

    // Key: '>'
    ANGLE_BRACKET_RIGHT,

    // Key: '/'
    SLASH,

    // Key: '?'
    QUESTION_MARK,

    // Key: ';'
    SEMICOLON,

    // Key: ':'
    COLON,

    // Key: '''
    QUOTE,

    // Key: '"'
    DOUBLE_QUOTE,

    // Key: '\'
    BACKSLASH,

    // Key: '|'
    PIPE,

    // Key: '['
    SQUARE_BRACKET_LEFT,

    // Key: '{'
    CURLY_BRACKET_LEFT,

    // Key: ']'
    SQUARE_BRACKET_RIGHT,

    // Key: '}'
    CURLY_BRACKET_RIGHT,

    // Key: '!'
    EXCLAMATION_MARK,

    // Key: '@'
    AT,

    // Key: '#'
    HASH,

    // Key: $'
    DOLLAR,

    // Key: '€'
    EURO,

    // Key: '%'
    PERCENT,

    // Key: '&'
    AND,

    // Key: '*'
    MULTIPLY,

    // Key: '('
    ROUND_BRACKET_LEFT,

    // Key: ')'
    ROUND_BRACKET_RIGHT,

    // Key: '-'
    MINUS,

    // Key: '_'
    UNDERSCORE,

    // Key: '='
    EQUALS,

    // Key: '+'
    PLUS,

    // Key: '1'
    NUM_1,

    // Key: '2'
    NUM_2,

    // Key: '3'
    NUM_3,

    // Key: '4'
    NUM_4,

    // Key: '5'
    NUM_5,

    // Key: '6'
    NUM_6,

    // Key: '7'
    NUM_7,

    // Key: '8'
    NUM_8,

    // Key: '9'
    NUM_9,

    // Key: '0'
    NUM_0,

    // Key: 'A'
    A,

    // Key: 'B'
    B,

    // Key: 'C'
    C,

    // Key: 'D'
    D,

    // Key: 'E'
    E,

    // Key: 'F'
    F,

    // Key: 'G'
    G,

    // Key: 'H'
    H,

    // Key: 'I'
    I,

    // Key: 'J'
    J,

    // Key: 'K'
    K,

    // Key: 'L'
    L,

    // Key: 'M'
    M,

    // Key: 'N'
    N,

    // Key: 'O'
    O,

    // Key: 'P'
    P,

    // Key: 'Q'
    Q,

    // Key: 'R'
    R,

    // Key: 'S'
    S,

    // Key: 'T'
    T,

    // Key: 'U'
    U,

    // Key: 'V'
    V,

    // Key: 'W'
    W,

    // Key: 'X'
    X,

    // Key: 'Y'
    Y,

    // Key: 'Z'
    Z,

    // Key: ' '
    SPACE,

    // Key: 'tab'
    TAB,

    // Key: 'shift'
    SHIFT,

    // Key: 'ctrl'
    CTRL,

    // Key: 'alt'
    ALT,

    // Key: 'alt gr'
    ALT_GRAPHICS,

    // Key: 'print'
    PRINT,

    // Key: 'esc'
    ESCAPE,

    // Key: 'home'
    HOME,

    // Key: 'end'
    END,

    // Key: 'insert'
    INSERT,

    // Key: 'delete'
    DELETE,

    // Key: 'backspace'
    BACKSPACE,

    // Key: 'enter'
    ENTER,

    // Key: 'page up'
    PAGE_UP,

    // Key: 'page down'
    PAGE_DOWN,

    // Key: 'arrow up'
    ARROW_UP,

    // Key: 'arrow left'
    ARROW_LEFT,

    // Key: 'arrow down'
    ARROW_DOWN,

    // Key: 'arrow right'
    ARROW_RIGHT,

    // Key: 'undo'
    UNDO,

    // Key: 'redo'
    REDO,

    // Key: 'cut'
    CUT,

    // Key: 'copy'
    COPY,

    // Key: 'paste'
    PASTE,

    // Key: 'find'
    FIND,

    // Key: 'F1'
    F1,

    // Key: 'F2'
    F2,

    // Key: 'F3'
    F3,

    // Key: 'F4'
    F4,

    // Key: 'F5'
    F5,

    // Key: 'F6'
    F6,

    // Key: 'F7'
    F7,

    // Key: 'F8'
    F8,

    // Key: 'F9'
    F9,

    // Key: 'F10'
    F10,

    // Key: 'F11'
    F11,

    // Key: 'F12'
    F12,
}
