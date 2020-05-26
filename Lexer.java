package bs.code;

import java.util.ArrayList;

public class Lexer {
    private ArrayList<LexerToken> tokens;
    private int seek = 0;
    private String input;

    private Lexer(String input) {
        this.tokens = new ArrayList<>();
        this.input = input;
    }

    /**
     * Pushes a token to the list and advances the seek.
     *
     * @param kind   - Token kind to push
     * @param length - Length of the token
     * @author Aaron Rohan
     */
    private void push(LexerToken.Kind kind, int length) {
        tokens.add(new LexerToken(kind, seek, length));
        seek += length;
    }

    /**
     * Peek ahead from the char location in the input to see if the string will follow; if it does, returns true and
     * pushes a token to the list (if shouldPush is true) and also advance the seek.
     *
     * @param kind - Token kind to push
     * @param str  - String to peek
     * @author Aaron Rohan
     */
    private boolean lookahead(LexerToken.Kind kind, String str) {
        int i = 0;
        for (; i < str.length(); i += 1) {
            if (seek + i < input.length()) {
                if (input.charAt(seek + i) != str.charAt(i)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        push(kind, str.length());
        return true;
    }

    /**
     * Peek ahead from the char location in the input to see if the string will follow in a bare manner (useful for
     * keywords); if it does, returns true and pushes a token to the list (and also advance the seek).
     *
     * @param kind - Token kind to push
     * @param str  - String to peek
     * @author Aaron Rohan
     */
    private boolean lookaheadBare(LexerToken.Kind kind, String str) {
        int i = 0;
        for (; i < str.length(); i += 1) {
            if (seek + i < input.length()) {
                if (input.charAt(seek + i) != str.charAt(i)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        // Check to see if the string is *bare*
        if (seek + str.length() < input.length()) {
            char x = input.charAt(seek + str.length());
            if (Character.isDigit(x) || Character.isLetter(x)) {
                System.out.println("oops");
                return false;
            }
        }

        push(kind, str.length());
        return true;
    }

    /**
     * Lex and produce a token sequence from an input source.
     *
     * @param input - String to lex.
     * @author Yashwantth Gurijala
     * @author Sergio Galera
     * @author Aaron Rohan
     */
    static public ArrayList<LexerToken> lex(String input) {
        Lexer lexer = new Lexer(input);

        while (lexer.seek < input.length()) {
            char c = input.charAt(lexer.seek);

            if (c == '\"') {
                boolean isEscape = false;
                int strIdx = 1 + lexer.seek;
                for (; strIdx < input.length(); strIdx += 1) {
                    char x = input.charAt(strIdx);
                    if (isEscape) {
                        isEscape = false;
                    } else if (x == '\\') {
                        isEscape = true;
                    } else if (x == '\"') {
                        break;
                    }
                }

                if (strIdx >= (input.length() - 1)) {
                    strIdx = input.length() - 1;
                }

                lexer.push(LexerToken.Kind.String, strIdx+1 - lexer.seek);

            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                lexer.push(LexerToken.Kind.Arithmetic, 1);
            } else if (c == '<' || c == '>' || c == '!') {
                lexer.push(LexerToken.Kind.Boolean, 1);
            } else if (lexer.lookahead(LexerToken.Kind.Boolean, "=="))        {
            } else if (lexer.lookahead(LexerToken.Kind.Boolean, "!="))        {
            } else if (lexer.lookahead(LexerToken.Kind.Boolean, ">="))        {
            } else if (lexer.lookahead(LexerToken.Kind.Boolean, "<="))        {
            } else if (lexer.lookahead(LexerToken.Kind.Boolean, "&&"))        {
            } else if (lexer.lookahead(LexerToken.Kind.Boolean, "||"))        {
            } else if (lexer.lookaheadBare(LexerToken.Kind.Keyword, "while")) {
            } else if (lexer.lookaheadBare(LexerToken.Kind.Keyword, "for"))   {
            } else if (lexer.lookaheadBare(LexerToken.Kind.Keyword, "if"))    {
            } else if (lexer.lookaheadBare(LexerToken.Kind.Keyword, "else"))  {
            }

            lexer.seek += 1;
        }

        return lexer.tokens;
    }
}
