import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public final class Parser {
    public enum TYPE {
        Int,
        Real,
        Bool,
        Char,
        String,
        Procedure,
        Label,
        Array
    }


    private static int dp = 0;
    private static final HashMap<String, TYPE> str;
    private static Token currentToken;
    private static Iterator<Token> it;
    private static final int INSTRUCTION_SIZE = 1000;
    private static Byte[] byteArray = new Byte[INSTRUCTION_SIZE];
    private static int ip = 0;

    public static Byte[] parseALL() {
        getToken();

        match("TK_PROGRAM");
        match("TK_IDENTIFIER");
        match("TK_SEMI_COLON");

        declarations();
        begin();

        return byteArray;
    }

    public static void declarations() {
        while (true) {
            if(currentToken.getTokenType() == "TK_VAR"){
                while(true) {
                    if ("TK_VAR".equals(currentToken.getTokenType())) {
                        match("TK_VAR");
                    } else {
                        break;
                    }

                    ArrayList<Token> variablesArrayList = new ArrayList<>();

                    while ("TK_IDENTIFIER".equals(currentToken.getTokenType())) {
                        currentToken.setTokenType("TK_A_VAR");
                        variablesArrayList.add(currentToken);

                        match("TK_A_VAR");

                        if ("TK_COMMA".equals(currentToken.getTokenType())) {
                            match("TK_COMMA");
                        }
                    }

                    match("TK_COLON");
                    String dataType = currentToken.getTokenType();
                    match(dataType);
                    for (Token var : variablesArrayList) {
                        Symbol symbol = new Symbol(var.getTokenValue(),
                                "TK_A_VAR",
                                str.get(dataType.toLowerCase().substring(3)),
                                dp);
                        dp += 4;
                        if (SymbolTable.lookup(var.getTokenValue()) == null) {
                            SymbolTable.insert(symbol);
                        }
                    }

                    if (dataType.equals("TK_ARRAY")){
                        arrayDeclaration(variablesArrayList);
                    }
                    match("TK_SEMI_COLON");
                }
            }
            else if(currentToken.getTokenType() == "TK_PROCEDURE"){
                if (currentToken.getTokenType().equals("TK_PROCEDURE")) {
                    match("TK_PROCEDURE");
                    currentToken.setTokenType("TK_A_PROC");
                    String procedureName = currentToken.getTokenValue();

                    match("TK_A_PROC");
                    match("TK_SEMI_COLON");

                    genOpCode(instructions.JMP);
                    int hole = ip;
                    genAddress(0);

                    Symbol symbol = new Symbol(procedureName,
                            "TK_A_PROC",
                            TYPE.Procedure,
                            ip);

                    match("TK_BEGIN");
                    statements();
                    match("TK_END");
                    match("TK_SEMI_COLON");

                    genOpCode(instructions.JMP);
                    symbol.setReturnAddress(ip);
                    genAddress(0);

                    if (SymbolTable.lookup(procedureName) == null) {
                        SymbolTable.insert(symbol);
                    }

                    int save = ip;

                    ip = hole;
                    genAddress(save);
                    ip = save;
                }
            }
            else if(currentToken.getTokenType() == "TK_LABEL"){
                while(true) {
                    if ("TK_LABEL".equals(currentToken.getTokenType())) {
                        match("TK_LABEL");
                    } else {

                        break;
                    }
                    ArrayList<Token> labelsArrayList = new ArrayList<>();
                    while ("TK_IDENTIFIER".equals(currentToken.getTokenType())) {
                        currentToken.setTokenType("TK_A_LABEL");
                        labelsArrayList.add(currentToken);

                        match("TK_A_LABEL");

                        if ("TK_COMMA".equals(currentToken.getTokenType())) {
                            match("TK_COMMA");
                        }
                    }

                    for (Token label : labelsArrayList) {


                        Symbol symbol = new Symbol(label.getTokenValue(),
                                "TK_A_LABEL",
                                TYPE.Label,
                                0);

                        if (SymbolTable.lookup(label.getTokenValue()) == null) {
                            SymbolTable.insert(symbol);
                        }
                    }
                    match("TK_SEMI_COLON");
                }
            }
            else if(currentToken.getTokenType() == "TK_BEGIN"){
                return;
            }
        }
    }

    static {
        str = new HashMap<>();
        str.put("integer", TYPE.Int);
        str.put("real", TYPE.Real);
        str.put("boolean", TYPE.Bool);
        str.put("char", TYPE.Char);
        str.put("string", TYPE.String);
        str.put("array", TYPE.Array);

    }


    static void arrayDeclaration(ArrayList<Token> variablesArrayList) {
        match("TK_OPEN_SQUARE_BRACKET");
        String v1 = currentToken.getTokenValue();
        TYPE indexType1 = getLiteralType(currentToken.getTokenType());
        match(currentToken.getTokenType());

        match("TK_RANGE");

        String v2 = currentToken.getTokenValue();
        TYPE indexType2 = getLiteralType(currentToken.getTokenType());
        match(currentToken.getTokenType());
        match("TK_CLOSE_SQUARE_BRACKET");
        match("TK_OF");

        String valueType = currentToken.getTokenType();
        match(valueType);

        if (indexType1 != indexType2){
            throw new Error(String.format("Array index LHS type (%s) is not equal to RHS type: (%s)", indexType1, indexType2));
        } else {

            assert indexType1 != null;
            switch (indexType1) {
                case Int:
                    int i1 = Integer.valueOf(v1);
                    int i2 = Integer.valueOf(v2);
                    if (i1 > i2){
                        throw new Error(String.format("Array range is invalid: %d..%d", i1, i2));
                    }

                    Symbol firstIntArray = SymbolTable.lookup(variablesArrayList.get(0).getTokenValue());
                    if (firstIntArray != null) {
                        dp = firstIntArray.getAddress();
                    }

                    for (Token var: variablesArrayList) {
                        Symbol symbol = SymbolTable.lookup(var.getTokenValue());
                        if (symbol != null){
                            int elementSize = 4;
                            int size = elementSize*(i2 - i1 + 1);
                            symbol.setAddress(dp);
                            symbol.setLow(i1);
                            symbol.setHigh(i2);
                            symbol.setTokenType("TK_AN_ARRAY");
                            symbol.setIndexType(TYPE.Int);
                            symbol.setValueType(str.get(valueType.toLowerCase().substring(3)));
                            dp += size;
                        }
                    }

                    break;
                case Char:
                    char c1 = v1.toCharArray()[0];
                    char c2 = v2.toCharArray()[0];
                    if (c1 > c2){
                        throw new Error(String.format("Array range is invalid: %c..%c", c1, c2));
                    }

                    Symbol firstCharArray = SymbolTable.lookup(variablesArrayList.get(0).getTokenValue());
                    if (firstCharArray != null) {
                        dp = firstCharArray.getAddress();
                    }

                    for (Token var: variablesArrayList) {
                        Symbol symbol = SymbolTable.lookup(var.getTokenValue());
                        if (symbol != null){
                            int size = c2 - c1 + 1;

                            symbol.setAddress(dp);
                            symbol.setLow(c1);
                            symbol.setHigh(c2);
                            symbol.setTokenType("TK_AN_ARRAY");
                            symbol.setIndexType(TYPE.Char);
                            symbol.setValueType(str.get(valueType.toLowerCase().substring(3)));

                            dp += size;
                        }
                    }

                    break;
                case Real:
                    throw new Error("Array index type: real is invalid");
            }

        }

    }

    public static void begin(){
        match("TK_BEGIN");
        statements();
        match("TK_END");
        match("TK_DOT");
        match("TK_EOF");
        genOpCode(instructions.HALT);
    }

    public static void statements(){
        while(!currentToken.getTokenType().equals("TK_END")) {
            if(currentToken.getTokenType() == "TK_CASE"){
                match("TK_CASE");
                match("TK_OPEN_PARENTHESIS");
                Token eToken = currentToken;

                TYPE t1 = E();

                if (t1 == TYPE.Real) {
                    throw new Error("Invalid type of real for case E");
                }

                match("TK_CLOSE_PARENTHESIS");
                match("TK_OF");

                ArrayList<Integer> labelsArrayList = new ArrayList<>();

                while(currentToken.getTokenType().equals("TK_INTLIT") ||
                        currentToken.getTokenType().equals("TK_CHARLIT") ||
                        currentToken.getTokenType().equals("TK_BOOLLIT")) {

                    TYPE t2 = E();
                    combine("TK_EQUAL", t1, t2);
                    match("TK_COLON");
                    genOpCode(instructions.JFALSE);
                    int hole = ip;
                    genAddress(0);
                    statements();
                    genOpCode(instructions.JMP);
                    labelsArrayList.add(ip);
                    genAddress(0);
                    int save = ip;
                    ip = hole;
                    genAddress(save);
                    ip = save;
                    if (!currentToken.getTokenValue().equals("TK_END")){
                        Symbol symbol = SymbolTable.lookup(eToken.getTokenValue());
                        if (symbol != null) {
                            genOpCode(instructions.PUSH);
                            genAddress(symbol.getAddress());
                        }
                    }
                }

                match("TK_END");
                match("TK_SEMI_COLON");

                int save = ip;

                for (Integer labelHole: labelsArrayList) {
                    ip = labelHole;
                    genAddress(save);
                }

                ip = save;
            }
            else if(currentToken.getTokenType() == "TK_GOTO"){
                match("TK_GOTO");
                Symbol symbol = SymbolTable.lookup(currentToken.getTokenValue());
                currentToken.setTokenType("TK_A_LABEL");
                match("TK_A_LABEL");
                genOpCode(instructions.JMP);
                int hole = ip;
                genAddress(0);

                if (symbol != null){
                    symbol.setAddress(hole);
                }

                match("TK_SEMI_COLON");

            }
            else if(currentToken.getTokenType() == "TK_WHILE"){
                match("TK_WHILE");
                int target = ip;
                G();
                match("TK_DO");

                genOpCode(instructions.JFALSE);
                int hole = ip;
                genAddress(0);

                match("TK_BEGIN");
                statements();
                match("TK_END");
                match("TK_SEMI_COLON");


                genOpCode(instructions.JMP);
                genAddress(target);

                int save = ip;
                ip = hole;
                genAddress(save);
                ip = save;
            }
            else if(currentToken.getTokenType() == "TK_REPEAT"){
                match("TK_REPEAT");
                int target = ip;
                statements();
                match("TK_UNTIL");
                G();
                genOpCode(instructions.JFALSE);
                genAddress(target);
            }
            else if(currentToken.getTokenType() == "TK_IF"){
                match("TK_IF");
                G();
                match("TK_THEN");
                genOpCode(instructions.JFALSE);
                int hole1 = ip;
                genAddress(0);
                statements();

                if(currentToken.getTokenType().equals("TK_ELSE")) {
                    genOpCode(instructions.JMP);
                    int hole2 = ip;
                    genAddress(0);
                    int save = ip;
                    ip = hole1;
                    genAddress(save);
                    ip = save;
                    hole1 = hole2;
                    statements();
                    match("TK_ELSE");
                    statements();
                }

                int save = ip;
                ip = hole1;
                genAddress(save);
                ip = save;
            }
            else if(currentToken.getTokenType() == "TK_FOR"){
                match("TK_FOR");
                String varName = currentToken.getTokenValue();
                currentToken.setTokenType("TK_A_VAR");
                Symbol symbol = SymbolTable.lookup(currentToken.getTokenValue());
                if (symbol != null) {
                    TYPE left = symbol.getDataType();
                    int lhsAddress = symbol.getAddress();
                    match("TK_A_VAR");
                    match("TK_ASSIGNMENT");
                    TYPE right = E();
                    if (left == right) {
                        genOpCode(instructions.POP);
                        genAddress(lhsAddress);
                    } else {
                        throw new Error(String.format("left side is (%s) right side is type: (%s)", left, right));
                    }
                }
                int target = ip;
                Symbol symbol2 = SymbolTable.lookup(varName);
                if (symbol2 != null) {
                    int address = symbol2.getAddress();
                    match("TK_TO");
                    genOpCode(instructions.PUSH);
                    genAddress(address);
                    genOpCode(instructions.PUSHI);
                    genAddress(Integer.valueOf(currentToken.getTokenValue()));
                    genOpCode(instructions.LEQ);
                    match("TK_INTLIT");
                    match("TK_DO");
                    genOpCode(instructions.JFALSE);
                    int hole = ip;
                    genAddress(0);
                    match("TK_BEGIN");
                    statements();
                    match("TK_END");
                    match("TK_SEMI_COLON");
                    genOpCode(instructions.PUSH);
                    genAddress(address);
                    genOpCode(instructions.PUSHI);
                    genAddress(1);
                    genOpCode(instructions.ADD);
                    genOpCode(instructions.POP);
                    genAddress(address);
                    genOpCode(instructions.JMP);
                    genAddress(target);

                    int save = ip;
                    ip = hole;
                    genAddress(save);
                    ip = save;
                }
            }
            else if(currentToken.getTokenType() == "TK_WRITELN"){
                write_st();
            }
            else if(currentToken.getTokenType() == "TK_IDENTIFIER"){
                Symbol symbol = SymbolTable.lookup(currentToken.getTokenValue());
                if (symbol != null) {
                    currentToken.setTokenType(symbol.getTokenType());
                }
            }
            else if(currentToken.getTokenType() == "TK_A_VAR"){
                Symbol symbol = SymbolTable.lookup(currentToken.getTokenValue());
                if (symbol != null) {
                    TYPE left = symbol.getDataType();
                    int lhsAddress = symbol.getAddress();
                    match("TK_A_VAR");
                    match("TK_ASSIGNMENT");
                    TYPE right = E();
                    if (left == right) {
                        genOpCode(instructions.POP);
                        genAddress(lhsAddress);
                    } else {
                        throw new Error(String.format("left side is (%s) right side is type: (%s)", left, right));
                    }
                }
            }
            else if(currentToken.getTokenType() == "TK_A_PROC"){
                Symbol symbol = SymbolTable.lookup(currentToken.getTokenValue());
                if (symbol != null) {
                    int address = symbol.getAddress();
                    match("TK_A_PROC");
                    match("TK_SEMI_COLON");
                    genOpCode(instructions.JMP);
                    genAddress(address);

                    int restore = ip;
                    ip = symbol.getReturnAddress();
                    genAddress(restore);
                    ip = restore;
                }
            }
            else if(currentToken.getTokenType() == "TK_A_LABEL"){
                Symbol symbol = SymbolTable.lookup(currentToken.getTokenValue());
                match("TK_A_LABEL");
                match("TK_COLON");
                if (symbol != null) {
                    int hole = symbol.getAddress();
                    int save = ip;
                    ip = hole;
                    genAddress(save);

                    ip = save;

                    statements();
                }
            }
            else if(currentToken.getTokenType() == "TK_AN_ARRAY"){
                Symbol symbol = SymbolTable.lookup(currentToken.getTokenValue());
                if (symbol != null) {
                    handleArrayAccess(symbol);
                    match("TK_ASSIGNMENT");
                    TYPE rhsType = E();
                    if (symbol.getValueType() == rhsType) {
                        genOpCode(instructions.PUT);
                    }
                }
            }
            else if(currentToken.getTokenType() == "TK_SEMI_COLON"){
                match("TK_SEMI_COLON");
            }
            else {
                return;
            }
        }

    }


    public static void write_st(){
        match("TK_WRITELN");
        match("TK_OPEN_PARENTHESIS");

        while (true) {
            Symbol symbol =  SymbolTable.lookup(currentToken.getTokenValue());
            TYPE t;

            if (symbol != null) {
                if (symbol.getDataType() == TYPE.Array) {
                    currentToken.setTokenType("TK_AN_ARRAY");
                    handleArrayAccess(symbol);

                    genOpCode(instructions.GET);

                    t = symbol.getValueType();

                } else {
                    currentToken.setTokenType("TK_A_VAR");
                    t = symbol.getDataType();
                    genOpCode(instructions.PUSH);
                    genAddress(symbol.getAddress());
                    match("TK_A_VAR");
                }
            } else {
                t = getLiteralType(currentToken.getTokenType());
                assert t != null;
                switch (t) {
                    case Real:
                        genOpCode(instructions.PUSHF);
                        genAddress(Float.valueOf(currentToken.getTokenValue()));
                        break;
                    case Int:
                        genOpCode(instructions.PUSHI);
                        genAddress(Integer.valueOf(currentToken.getTokenValue()));
                        break;
                    case Bool:
                        genOpCode(instructions.PUSHI);
                        if (currentToken.getTokenValue().equals("true")) {
                            genAddress(1);
                        } else {
                            genAddress(0);
                        }
                        break;
                    case Char:
                        genOpCode(instructions.PUSHI);
                        genAddress((int)(currentToken.getTokenValue().charAt(0)));
                        break;
                }

                match(currentToken.getTokenType());
            }

            assert t != null;
            if (t == TYPE.Int){
                genOpCode(instructions.PRINT_INT);
            }
            else if(t == TYPE.Char){
                genOpCode(instructions.PRINT_CHAR);
            }
            else if(t == TYPE.Real){
                genOpCode(instructions.PRINT_CHAR);
            }
            else if(t == TYPE.Bool){
                genOpCode(instructions.PRINT_BOOL);
            }
            else throw new Error("Cannot write unknown type");

            switch (currentToken.getTokenType()) {
                case "TK_COMMA":
                    match("TK_COMMA");
                    break;
                case "TK_CLOSE_PARENTHESIS":
                    match("TK_CLOSE_PARENTHESIS");
                    genOpCode(instructions.PRINT_NEWLINE);
                    return;
                default:
                    throw new Error(String.format("Current token type (%s) is neither TK_COMMA nor TK_CLOSE_PARENTHESIS", currentToken.getTokenType()));
            }

        }
    }

    private static void handleArrayAccess(Symbol symbol) {
        match("TK_AN_ARRAY");
        match("TK_OPEN_SQUARE_BRACKET");
        TYPE t;

        Symbol varSymbol = SymbolTable.lookup(currentToken.getTokenValue());
        if (varSymbol != null) {
            t = varSymbol.getDataType();


            if (t != symbol.getIndexType()) {
                throw new Error(String.format("Incompatible index type: (%s, %s)", t, symbol.getIndexType()));
            }
            currentToken.setTokenType("TK_A_VAR");
            genOpCode(instructions.PUSH);
            genAddress(varSymbol.getAddress());
            match("TK_A_VAR");
            match("TK_CLOSE_SQUARE_BRACKET");
            genOpCode(instructions.PUSHI);

            switch (t) {
                case Int:
                    int i1 = (int) symbol.getLow();
                    genAddress(i1);
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.SUB);
                    genOpCode(instructions.PUSHI);
                    genAddress(4);
                    genOpCode(instructions.MULT);
                    genOpCode(instructions.PUSHI);
                    genAddress(symbol.getAddress());
                    genOpCode(instructions.ADD);

                    break;
                case Char:
                    char c1 = (char) symbol.getLow();
                    genAddress(c1);
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.SUB);
                    genOpCode(instructions.PUSHI);
                    genAddress(symbol.getAddress());
                    genOpCode(instructions.ADD);

                    break;
            }
        } else {


            String index = currentToken.getTokenValue();
            t = E();

            if (t != symbol.getIndexType()) {
                throw new Error(String.format("Incompatible index type: (%s, %s)", t, symbol.getIndexType()));
            }

            match("TK_CLOSE_SQUARE_BRACKET");

            genOpCode(instructions.PUSHI);

            switch (t) {
                case Int:
                    int i1 = (int) symbol.getLow();
                    int i2 = (int) symbol.getHigh();
                    if (Integer.valueOf(index) < i1 || Integer.valueOf(index) > i2) {
                        throw new Error(String.format("Index %d is not within range %d to %d",
                                Integer.valueOf(index), i1, i2));
                    }

                    genAddress(i1);
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.SUB);

                    genOpCode(instructions.PUSHI);
                    genAddress(4);

                    genOpCode(instructions.MULT);

                    genOpCode(instructions.PUSHI);
                    genAddress(symbol.getAddress());

                    genOpCode(instructions.ADD);

                    break;
                case Char:
                    char c1 = (char) symbol.getLow();
                    char c2 = (char) symbol.getHigh();

                    if (index.toCharArray()[0] < c1 || index.toCharArray()[0] > c2) {
                        throw new Error(String.format("Index %c is not within range %c to %c",
                                index.toCharArray()[0], c1, c2));
                    }

                    genAddress(c1);
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.SUB);

                    genOpCode(instructions.PUSHI);
                    genAddress(symbol.getAddress());

                    genOpCode(instructions.ADD);

                    break;
            }

        }
    }


    public static TYPE G(){
        TYPE e1 = E();
        while ( currentToken.getTokenType().equals("TK_LESS_THAN") || currentToken.getTokenType().equals("TK_GREATER_THAN") || currentToken.getTokenType().equals("TK_LESS_THAN_EQUAL") ||
                currentToken.getTokenType().equals("TK_GREATER_THAN_EQUAL") || currentToken.getTokenType().equals("TK_EQUAL") || currentToken.getTokenType().equals("TK_NOT_EQUAL")) {
            String yolo = currentToken.getTokenType();
            match(yolo);
            TYPE e2 = T();

            e1 = combine(yolo, e1, e2);
        }
        return e1;
    }


    public static TYPE E(){
        TYPE t1 = T();
        while (currentToken.getTokenType().equals("TK_PLUS") || currentToken.getTokenType().equals("TK_MINUS")) {
            String op = currentToken.getTokenType();
            match(op);
            TYPE t2 = T();

            t1 = combine(op, t1, t2);
        }

        return t1;
    }


    public static TYPE T() {
        TYPE f1 = F();
        while ( currentToken.getTokenType().equals("TK_MULTIPLY") || currentToken.getTokenType().equals("TK_DIVIDE") || currentToken.getTokenType().equals("TK_DIV") ||
                currentToken.getTokenType().equals("TK_MOD") || currentToken.getTokenType().equals("TK_AND") || currentToken.getTokenType().equals("TK_SHL")) {
            String op = currentToken.getTokenType();
            match(op);
            TYPE f2 = F();

            f1 = combine(op, f1, f2);
        }
        return f1;
    }


    public static TYPE F() {
        switch (currentToken.getTokenType()) {
            case "TK_IDENTIFIER":
                Symbol symbol = SymbolTable.lookup(currentToken.getTokenValue());
                if (symbol != null) {
                    if (symbol.getTokenType().equals("TK_A_VAR")) {
                        currentToken.setTokenType("TK_A_VAR");
                        genOpCode(instructions.PUSH);
                        genAddress(symbol.getAddress());
                        match("TK_A_VAR");
                        return symbol.getDataType();
                    } else if (symbol.getTokenType().equals("TK_AN_ARRAY")) {
                        currentToken.setTokenType("TK_AN_ARRAY");
                        handleArrayAccess(symbol);
                        genOpCode(instructions.GET);
                        return symbol.getValueType();
                    }
                } else {
                    throw new Error(String.format("Symbol (%s) not found ", currentToken.getTokenValue()));
                }
            case "TK_INTLIT":
                genOpCode(instructions.PUSHI);
                genAddress(Integer.valueOf(currentToken.getTokenValue()));

                match("TK_INTLIT");
                return TYPE.Int;
            case "TK_FLOATLIT":
                genOpCode(instructions.PUSHF);
                genAddress(Float.valueOf(currentToken.getTokenValue()));

                match("TK_FLOATLIT");
                return TYPE.Real;
            case "TK_BOOLLIT":
                genOpCode(instructions.PUSHI);
                genAddress(Boolean.valueOf(currentToken.getTokenValue()) ? 1 : 0);

                match("TK_BOOLLIT");
                return TYPE.Bool;
            case "TK_CHARLIT":
                genOpCode(instructions.PUSHI);
                genAddress(currentToken.getTokenValue().charAt(0));

                match("TK_CHARLIT");
                return TYPE.Char;
            case "TK_STRLIT":
                for (char c: currentToken.getTokenType().toCharArray()) {
                    genOpCode(instructions.PUSHI);
                    genAddress(c);
                }

                match("TK_STRLIT");
                return TYPE.String;
            case "TK_NOT":
                match("TK_NOT");
                return F();
            case "TK_OPEN_PARENTHESIS":
                match("TK_OPEN_PARENTHESIS");
                TYPE t = E();
                match("TK_CLOSE_PARENTHESIS");
                return t;
            default:
                throw new Error("Unknown data type");
        }

    }


    public static TYPE combine(String op, TYPE t1, TYPE t2){
        switch (op) {
            case "TK_PLUS":
                if (t1 == TYPE.Int && t2 == TYPE.Int) {
                    genOpCode(instructions.ADD);
                    return TYPE.Int;
                } else if (t1 == TYPE.Int && t2 == TYPE.Real) {
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.FADD);
                    return TYPE.Real;
                } else if (t1 == TYPE.Real && t2 == TYPE.Int) {
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.FADD);
                    return TYPE.Real;
                } else if (t1 == TYPE.Real && t2 == TYPE.Real) {
                    genOpCode(instructions.FADD);
                    return TYPE.Real;
                }
            case "TK_MINUS":
                if (t1 == TYPE.Int && t2 == TYPE.Int) {
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.SUB);
                    return TYPE.Int;
                } else if (t1 == TYPE.Int && t2 == TYPE.Real) {
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.FSUB);
                    return TYPE.Real;
                } else if (t1 == TYPE.Real && t2 == TYPE.Int) {
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.FSUB);
                    return TYPE.Real;
                } else if (t1 == TYPE.Real && t2 == TYPE.Real) {
                    genOpCode(instructions.FSUB);
                    return TYPE.Real;
                }
            case "TK_MULTIPLY":
                if (t1 == TYPE.Int && t2 == TYPE.Int) {
                    genOpCode(instructions.MULT);
                    return TYPE.Int;
                } else if (t1 == TYPE.Int && t2 == TYPE.Real) {
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.FMULT);
                    return TYPE.Real;
                } else if (t1 == TYPE.Real && t2 == TYPE.Int) {
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.FMULT);
                    return TYPE.Real;
                } else if (t1 == TYPE.Real && t2 == TYPE.Real) {
                    genOpCode(instructions.FMULT);
                    return TYPE.Real;
                }
            case "TK_DIVIDE":
                if (t1 == TYPE.Int && t2 == TYPE.Int) {
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.FDIV);
                    return TYPE.Real;
                } else if (t1 == TYPE.Int && t2 == TYPE.Real) {
                    genOpCode(instructions.XCHG);
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.FDIV);
                    return TYPE.Real;
                } else if (t1 == TYPE.Real && t2 == TYPE.Int) {
                    genOpCode(instructions.CVR);
                    genOpCode(instructions.FDIV);
                    return TYPE.Real;
                } else if (t1 == TYPE.Real && t2 == TYPE.Real) {
                    genOpCode(instructions.FDIV);
                    return TYPE.Real;
                }
                break;
            case "TK_MOD":
                if (t1 == TYPE.Int && t2 == TYPE.Int) {
                    genOpCode(instructions.MOD);
                    return TYPE.Int;
                }
                break;
            case "TK_DIV":
                if (t1 == TYPE.Int && t2 == TYPE.Int) {
                    genOpCode(instructions.DIV);
                    return TYPE.Int;
                }
            case "TK_LESS_THAN":
                return combine2(instructions.LSS, t1, t2);
            case "TK_EQUAL":
                return combine2(instructions.EQL, t1, t2);
            case "TK_GREATER_THAN_EQUAL":
                return combine2(instructions.GEQ, t1, t2);
            case "TK_LESS_THAN_EQUAL":
                return combine2(instructions.LEQ, t1, t2);
            case "TK_GREATER_THAN":
                return combine2(instructions.GTR, t1, t2);
            case "TK_NOT_EQUAL":
                return combine2(instructions.NEQL, t1, t2);
        }

        return null;
    }

    public static TYPE combine2(instructions pred, TYPE t1, TYPE t2) {
        if (t1 == t2) {
            genOpCode(pred);
            return TYPE.Bool;
        } else if (t1 == TYPE.Int && t2 == TYPE.Real) {
            genOpCode(instructions.XCHG);
            genOpCode(instructions.CVR);
            genOpCode(pred);
            return TYPE.Bool;
        } else if (t1 == TYPE.Real && t2 == TYPE.Int) {
            genOpCode(instructions.CVR);
            genOpCode(pred);
            return TYPE.Bool;
        }

        return null;
    }

    public static void genOpCode(instructions b){
        byteArray[ip++] = (byte)(b.ordinal());
    }

    public static void genAddress(int a){
        byte[] intBytes = ByteBuffer.allocate(4).putInt(a).array();

        for (byte b: intBytes) {
            byteArray[ip++] = b;
        }
    }

    public static void genAddress(float a){

        byte[] intBytes = ByteBuffer.allocate(4).putFloat(a).array();

        for (byte b: intBytes) {
            byteArray[ip++] = b;
        }
    }

    public static void getToken() {
        if (it.hasNext()) {
            currentToken =  it.next();
        }
    }

    public static void match(String tokenType) {
        if (!tokenType.equals(currentToken.getTokenType())) {
            throw new Error(String.format("Token type (%s) does not match current token type (%s)", tokenType, currentToken.getTokenType()));
        } else {
            getToken();
        }
    }

    public static TYPE getLiteralType(String tokenType) {
        switch (tokenType) {
            case "TK_INTLIT":
                return TYPE.Int;
            case "TK_FLOATLIT":
                return TYPE.Real;
            case "TK_CHARLIT":
                return TYPE.Char;
            case "TK_BOOLLIT":
                return TYPE.Bool;
            default:
                return null;
        }
    }

    public enum instructions {
        PUSHI, PUSH, POP, PUSHF,FGTR, FLSS,
        JMP, JFALSE, PRINT_INT, PRINT_CHAR, PRINT_BOOL,
        OR, AND, FADD, FSUB, FMULT, FDIV,
        DUP, XCHG, REMOVE, ADD, SUB,  JTRUE, CVR, CVI, MOD,
        MULT, DIV, NEG, FNEG, EQL, NEQL, GEQ, LEQ, GTR, LSS,
        PRINT_REAL, PRINT_NEWLINE,
        HALT, GET, PUT, SHL, SHR
    }

    public static void setTokenArrayListIterator(ArrayList<Token> tokenArrayList) {
        it = tokenArrayList.iterator();
    }
}