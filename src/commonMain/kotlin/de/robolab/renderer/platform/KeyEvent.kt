package de.robolab.renderer.platform

import kotlin.contracts.contract

data class KeyEvent(
        val keyCode: KeyCode,
        val text: String,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false
): Event()

enum class KeyCode(val char: Char?) {
    // Key: ','
    COMMA(','),

    // Key: '<'
    ANGLE_BRACKET_LEFT('<'),

    // Key: '.'
    PERIOD('.'),

    // Key: '>'
    ANGLE_BRACKET_RIGHT('>'),

    // Key: '/'
    SLASH('/'),

    // Key: '?'
    QUESTION_MARK('?'),

    // Key: ';'
    SEMICOLON(';'),

    // Key: ':'
    COLON(':'),

    // Key: '''
    QUOTE('\''),

    // Key: '"'
    DOUBLE_QUOTE('"'),

    // Key: '\'
    BACKSLASH('\\'),

    // Key: '|'
    PIPE('|'),

    // Key: '['
    SQUARE_BRACKET_LEFT('['),

    // Key: '{'
    CURLY_BRACKET_LEFT('{'),

    // Key: ']'
    SQUARE_BRACKET_RIGHT(']'),

    // Key: '}'
    CURLY_BRACKET_RIGHT('}'),

    // Key: '!'
    EXCLAMATION_MARK('!'),

    // Key: '@'
    AT('@'),

    // Key: '#'
    HASH('#'),

    // Key: $'
    DOLLAR('$'),

    // Key: '€'
    EURO('€'),

    // Key: '%'
    PERCENT('%'),

    // Key: '&'
    AND('&'),

    // Key: '*'
    MULTIPLY('*'),

    // Key: '('
    ROUND_BRACKET_LEFT('('),

    // Key: ')'
    ROUND_BRACKET_RIGHT(')'),

    // Key: '-'
    MINUS('-'),

    // Key: '_'
    UNDERSCORE('_'),

    // Key: '='
    EQUALS('='),

    // Key: '+'
    PLUS('+'),

    // Key: '1'
    NUM_1('1'),

    // Key: '2'
    NUM_2('2'),

    // Key: '3'
    NUM_3('3'),

    // Key: '4'
    NUM_4('4'),

    // Key: '5'
    NUM_5('5'),

    // Key: '6'
    NUM_6('6'),

    // Key: '7'
    NUM_7('7'),

    // Key: '8'
    NUM_8('8'),

    // Key: '9'
    NUM_9('9'),

    // Key: '0'
    NUM_0('0'),

    // Key: 'A'
    A('A'),

    // Key: 'B'
    B('B'),

    // Key: 'C'
    C('C'),

    // Key: 'D'
    D('D'),

    // Key: 'E'
    E('E'),

    // Key: 'F'
    F('F'),

    // Key: 'G'
    G('G'),

    // Key: 'H'
    H('H'),

    // Key: 'I'
    I('I'),

    // Key: 'J'
    J('J'),

    // Key: 'K'
    K('K'),

    // Key: 'L'
    L('L'),

    // Key: 'M'
    M('M'),

    // Key: 'N'
    N('N'),

    // Key: 'O'
    O('O'),

    // Key: 'P'
    P('P'),

    // Key: 'Q'
    Q('Q'),

    // Key: 'R'
    R('R'),

    // Key: 'S'
    S('S'),

    // Key: 'T'
    T('T'),

    // Key: 'U'
    U('U'),

    // Key: 'V'
    V('V'),

    // Key: 'W'
    W('W'),

    // Key: 'X'
    X('X'),

    // Key: 'Y'
    Y('Y'),

    // Key: 'Z'
    Z('Z'),

    // Key: ' '
    SPACE(' '),

    // Key: 'tab'
    TAB(null),

    // Key: 'shift'
    SHIFT(null),

    // Key: 'ctrl'
    CTRL(null),

    // Key: 'alt'
    ALT(null),

    // Key: 'alt gr'
    ALT_GRAPHICS(null),

    // Key: 'print'
    PRINT(null),

    // Key: 'esc'
    ESCAPE(null),

    // Key: 'home'
    HOME(null),

    // Key: 'end'
    END(null),

    // Key: 'insert'
    INSERT(null),

    // Key: 'delete'
    DELETE(null),

    // Key: 'backspace'
    BACKSPACE(null),

    // Key: 'enter'
    ENTER(null),

    // Key: 'page up'
    PAGE_UP(null),

    // Key: 'page down'
    PAGE_DOWN(null),

    // Key: 'arrow up'
    ARROW_UP(null),

    // Key: 'arrow left'
    ARROW_LEFT(null),

    // Key: 'arrow down'
    ARROW_DOWN(null),

    // Key: 'arrow right'
    ARROW_RIGHT(null),

    // Key: 'undo'
    UNDO(null),

    // Key: 'redo'
    REDO(null),

    // Key: 'cut'
    CUT(null),

    // Key: 'copy'
    COPY(null),

    // Key: 'paste'
    PASTE(null),

    // Key: 'find'
    FIND(null),

    // Key: 'F1'
    F1(null),

    // Key: 'F2'
    F2(null),

    // Key: 'F3'
    F3(null),

    // Key: 'F4'
    F4(null),

    // Key: 'F5'
    F5(null),

    // Key: 'F6'
    F6(null),

    // Key: 'F7'
    F7(null),

    // Key: 'F8'
    F8(null),

    // Key: 'F9'
    F9(null),

    // Key: 'F10'
    F10(null),

    // Key: 'F11'
    F11(null),

    // Key: 'F12'
    F12(null);
    
}
