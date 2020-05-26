package bs.code;

public class LexerToken {
    public enum Kind {
        Keyword, Arithmetic, Boolean, String
    }

    private Kind kind;
    private int offset, length;

    /**
     * Constructor for Lexer Tokens.
     */
    public LexerToken(Kind kind, int offset, int length) {
        this.kind = kind;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Gets Token Kind.
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Gets Token Offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets Token Length.
     */
    public int getLength() {
        return length;
    }
}
