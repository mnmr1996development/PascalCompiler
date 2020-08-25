import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public final class TokenScanner {
    private static String tokenName = "";
    private static int lineRow = 0;
    private static int lineCol = 0;


    private static ArrayList<Token> tokenArrayList = new ArrayList<>();

    enum TYPE {
        LETTER, DIGIT, SPACE, OPERATOR, QUOTE
    }
    private static final HashMap<String, String> KEYWORDS_TOKEN;
    static {
        KEYWORDS_TOKEN = new HashMap<>();
        KEYWORDS_TOKEN.put("absolute", "TK_ABSOLUTE");
        KEYWORDS_TOKEN.put("and", "TK_AND");
        KEYWORDS_TOKEN.put("array", "TK_ARRAY");
        KEYWORDS_TOKEN.put("asm", "TK_ASM");
        KEYWORDS_TOKEN.put("begin", "TK_BEGIN");
        KEYWORDS_TOKEN.put("boolean", "TK_BOOLEAN");
        KEYWORDS_TOKEN.put("case", "TK_CASE");
        KEYWORDS_TOKEN.put("char", "TK_CHAR");
        KEYWORDS_TOKEN.put("const", "TK_CONST");
        KEYWORDS_TOKEN.put( "constructor", "TK_CONSTRUCTOR");
        KEYWORDS_TOKEN.put("destructor", "TK_DESTRUCTOR");
        KEYWORDS_TOKEN.put("div", "TK_DIV");
        KEYWORDS_TOKEN.put("do", "TK_DO");
        KEYWORDS_TOKEN.put("downto", "TK_DOWNTO");
        KEYWORDS_TOKEN.put("else", "TK_ELSE");
        KEYWORDS_TOKEN.put("end", "TK_END");
        KEYWORDS_TOKEN.put("file", "TK_FILE");
        KEYWORDS_TOKEN.put("for", "TK_FOR");
        KEYWORDS_TOKEN.put("function", "TK_FUNCTION");
        KEYWORDS_TOKEN.put("goto", "TK_GOTO");
        KEYWORDS_TOKEN.put("if", "TK_IF");
        KEYWORDS_TOKEN.put("in", "TK_IN");
        KEYWORDS_TOKEN.put("integer", "TK_INTEGER");
        KEYWORDS_TOKEN.put("inherited", "TK_INHERITED");
        KEYWORDS_TOKEN.put("inline", "TK_INLINE");
        KEYWORDS_TOKEN.put("interface", "TK_INTERFACE");
        KEYWORDS_TOKEN.put("label", "TK_LABEL");
        KEYWORDS_TOKEN.put("mod", "TK_MOD");
        KEYWORDS_TOKEN.put("nil","TK_NIL");
        KEYWORDS_TOKEN.put("not", "TK_NOT");
        KEYWORDS_TOKEN.put("object", "TK_OBJECT");
        KEYWORDS_TOKEN.put("of", "TK_OF");
        KEYWORDS_TOKEN.put("operator", "TK_OPERATOR");
        KEYWORDS_TOKEN.put("or", "TK_OR");
        KEYWORDS_TOKEN.put("packed", "TK_PACKED");
        KEYWORDS_TOKEN.put("procedure", "TK_PROCEDURE");
        KEYWORDS_TOKEN.put("program", "TK_PROGRAM");
        KEYWORDS_TOKEN.put("real", "TK_REAL");
        KEYWORDS_TOKEN.put("record", "TK_RECORD");
        KEYWORDS_TOKEN.put("reintroduce", "TK_REINTRODUCE");
        KEYWORDS_TOKEN.put("repeat", "TK_REPEAT");
        KEYWORDS_TOKEN.put("shelf", "TK_SHELF");
        KEYWORDS_TOKEN.put("set", "TK_SET");
        KEYWORDS_TOKEN.put("shl", "TK_SHL");
        KEYWORDS_TOKEN.put("shr", "TK_SHR");
        KEYWORDS_TOKEN.put("string","TK_STRING");
        KEYWORDS_TOKEN.put("then", "TK_THEN");
        KEYWORDS_TOKEN.put("to", "TK_TO");
        KEYWORDS_TOKEN.put("type", "TK_TYPE");
        KEYWORDS_TOKEN.put("unit", "TK_UNIT");
        KEYWORDS_TOKEN.put("until", "TK_UNTIL");
        KEYWORDS_TOKEN.put("uses", "TK_USES");
        KEYWORDS_TOKEN.put("var", "TK_VAR");
        KEYWORDS_TOKEN.put("while", "TK_WHILE");
        KEYWORDS_TOKEN.put("with", "TK_WITH");
        KEYWORDS_TOKEN.put("xor", "TK_XOR");
        KEYWORDS_TOKEN.put("writeln", "TK_WRITELN");
    }
    private static final HashMap<String, String> OPERATORS_TOKEN;
    static {
        OPERATORS_TOKEN = new HashMap<>();
        OPERATORS_TOKEN.put("(", "TK_OPEN_PARENTHESIS");
        OPERATORS_TOKEN.put(")", "TK_CLOSE_PARENTHESIS");
        OPERATORS_TOKEN.put("[", "TK_OPEN_SQUARE_BRACKET");
        OPERATORS_TOKEN.put("]", "TK_CLOSE_SQUARE_BRACKET");
        OPERATORS_TOKEN.put(".", "TK_DOT");
        OPERATORS_TOKEN.put("..", "TK_RANGE");
        OPERATORS_TOKEN.put(":", "TK_COLON");
        OPERATORS_TOKEN.put(";", "TK_SEMI_COLON");
        OPERATORS_TOKEN.put("+", "TK_PLUS");
        OPERATORS_TOKEN.put("-", "TK_MINUS");
        OPERATORS_TOKEN.put("*", "TK_MULTIPLY");
        OPERATORS_TOKEN.put("%", "TK_MOD");
        OPERATORS_TOKEN.put("/", "TK_DIVIDE");
        OPERATORS_TOKEN.put("<", "TK_LESS_THAN");
        OPERATORS_TOKEN.put("<=", "TK_LESS_THAN_EQUAL");
        OPERATORS_TOKEN.put(">", "TK_GREATER_THAN");
        OPERATORS_TOKEN.put(">=", "TK_GREATER_THAN_EQUAL");
        OPERATORS_TOKEN.put(":=", "TK_ASSIGNMENT");
        OPERATORS_TOKEN.put(",", "TK_COMMA");
        OPERATORS_TOKEN.put("=", "TK_EQUAL");
        OPERATORS_TOKEN.put("<>", "TK_NOT_EQUAL");

    }
    private static final HashMap<String, TYPE> charTABLE;
    static {
        charTABLE = new HashMap<>();

        for (int i = 65; i <= 90; i++){
            String upperCase = String.valueOf(Character.toChars(i)[0]);
            String lowerCase = String.valueOf(Character.toChars(i+32)[0]);
            charTABLE.put(upperCase, TYPE.LETTER);
            charTABLE.put(lowerCase, TYPE.LETTER);
        }
        for (int i = 48; i < 58; i++){
            String currentChar = String.valueOf(Character.toChars(i)[0]);
            charTABLE.put(currentChar, TYPE.DIGIT);
        }
        for (String key: OPERATORS_TOKEN.keySet()) {
            charTABLE.put(key, TYPE.OPERATOR);
        }
        for (int i = 1; i < 33; i++){
            String currentChar = String.valueOf(Character.toChars(i)[0]);
            charTABLE.put(currentChar, TYPE.SPACE);
        }

        charTABLE.put(String.valueOf(Character.toChars(39)[0]), TYPE.QUOTE);
    }

    public static ArrayList<Token> scan(File file) throws FileNotFoundException {
        Scanner sc = new Scanner(file).useDelimiter("");

        while (sc.hasNext()) {
            char element = sc.next().toLowerCase().charAt(0);

            checkCharacter(element);
        }

        tokenName = "EOF";
        generateToken("TK_EOF");

        return tokenArrayList;
    }
    private static boolean isString = false;
    private static boolean readingNumber = false;
    private static boolean isFloat = false;
    private static boolean readingColon = false;
    private static boolean readingBool = false;
    private static boolean readingDot = false;
    private static boolean readingSlash = false;

    public static void checkCharacter(char element){
        switch (charTABLE.get(String.valueOf(element))){
            case LETTER:
                if (!readingNumber) {
                    tokenName += element;
                }

                break;
            case DIGIT:
                if (tokenName.isEmpty()) {
                    readingNumber = true;
                }

                tokenName += element;

                break;
            case SPACE:
                if (isString){
                    tokenName += element;
                } else if (readingColon) {

                    generateToken(OPERATORS_TOKEN.get(tokenName));

                    readingColon = false;

                } else if (readingBool) {
                    generateToken(OPERATORS_TOKEN.get(tokenName));

                    readingBool = false;

                } else if (!readingNumber) {
                    tokenName = endOfWord();

                    if (element == Character.toChars(10)[0]){
                        lineRow++;
                        lineCol = 0;
                    } else if (element == Character.toChars(9)[0]){
                        lineCol+=4;
                    } else if (element == Character.toChars(32)[0]){
                        lineCol++;
                    }
                } else {
                    handleNumber();
                }
                break;
            case OPERATOR:
                if (readingDot && element == '.') {
                    if (tokenName.equals(".")) {
                        tokenName = "";
                        generateToken("TK_RANGE");
                    } else {
                        generateToken(tokenName.substring(0, tokenName.length()-2));
                        generateToken("TK_DOT");
                        tokenName = "";
                    }
                    readingDot = false;

                } else if(isString) {
                    tokenName += element;
                } else if (readingNumber) {
                    if (isFloat && element == '.') {
                        isFloat = false;
                        tokenName = tokenName.substring(0,tokenName.length()-1);
                        handleNumber();
                        generateToken("TK_RANGE");
                        tokenName = "";
                    } else if (element == '.') {
                        isFloat = true;
                        tokenName += element;
                    } else {
                        handleNumber();


                        generateToken(OPERATORS_TOKEN.get(String.valueOf(element)));
                    }
                } else if (readingColon && element == '=') {

                    tokenName += element;

                    generateToken(OPERATORS_TOKEN.get(tokenName));


                    readingColon = false;
                } else if (readingBool) {
                    if (tokenName.equals("<") && ((element == '=') || (element == '>'))) {
                        tokenName += element;
                        generateToken(OPERATORS_TOKEN.get(tokenName));
                    } else if (tokenName.equals(">") && (element == '=')) {
                        tokenName += element;

                        generateToken(OPERATORS_TOKEN.get(tokenName));
                    }
                    readingBool = false;
                }
                else if(tokenName.equals("/") && lineCol == 0){
                    readingSlash = true;
                }
                else if(readingSlash){
                    if(tokenName.equals("/")){
                        lineRow++;
                    }
                }
                else {
                    if (element == ';') {
                        tokenName = endOfWord();

                        tokenName = ";";
                        generateToken(OPERATORS_TOKEN.get(String.valueOf(element)));
                    } else if (element == ':') {
                        tokenName = endOfWord();
                        readingColon = true;
                        tokenName += element;
                    } else if (element == '<' || element == '>') {
                        tokenName = endOfWord();
                        readingBool = true;
                        tokenName += element;
                    } else if (element == '.') {
                        tokenName += element;

                        if (tokenName.equals("end.")){
                            generateToken("TK_END");
                            generateToken("TK_DOT");
                        } else {
                            readingDot = true;
                        }
                    } else if (OPERATORS_TOKEN.containsKey(String.valueOf(element))) {
                        tokenName = endOfWord();


                        tokenName = String.valueOf(element);
                        generateToken(OPERATORS_TOKEN.get(tokenName));
                    }
                }
                break;
            case QUOTE:
                isString = !isString;
                tokenName += element;

                if (!isString) {
                    tokenName = tokenName.substring(1, tokenName.length()-1);
                    if (tokenName.length() == 1) {
                        generateToken("TK_CHARLIT");
                    } else if (tokenName.length() > 1) {
                        generateToken("TK_STRLIT");
                    }
                }
                break;
            default:
                throw new Error("Unhandled element scanned");
        }
    }
    public static void handleNumber() {
        readingNumber = false;
        if (isFloat) {
            generateToken("TK_FLOATLIT");
            isFloat = false;
        } else {
            generateToken("TK_INTLIT");
        }
    }

    public static String endOfWord(){
        if(KEYWORDS_TOKEN.containsKey(tokenName)){
            generateToken(KEYWORDS_TOKEN.get(tokenName));
        } else {
            if (tokenName.length() > 0) {

                if(tokenName.equals("true") || tokenName.equals("false")) {
                    generateToken("TK_BOOLLIT");
                } else {
                    generateToken("TK_IDENTIFIER");
                }
            }
        }

        clearStatuses();

        return tokenName;
    }

    public static void clearStatuses() {
        isString = false;
        readingNumber = false;
        isFloat = false;
        readingColon = false;
        readingBool = false;
    }

    public static void generateToken(String tokenType) {
        Token t = new Token(tokenType, tokenName, lineCol, lineRow);
        tokenArrayList.add(t);

        lineCol += tokenName.length();

        tokenName = "";
    }


}