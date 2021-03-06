import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.stream.IntStream;

/**
 * This class effects the actual compilation output. Gets its input from a
 * JackTokenizer and emits its parsed structure into an output file or stream.
 * The output is generated by a series of compilexxx routines, one for
 * every syntactic element xxx of the Jack grammar. The contract between
 * these routines is that each compilexxx routine should read the syntactic
 * construct xxx from the input, advance the jackTokenizer exactly beyond xxx,
 * and output the parsing of xxx. Thus, compilexxx may only be called if
 * indeed xxx is the next syntactic element of the input.
 */
public class CompilationEngine {

    /************************ Constants *****************************/

    private static final String HYPHEN_SYMBOL = "-";
    private static final String TILDE_SYMBOL = "~";
    private static final String ADD_SYMBOL = "+";
    private static final String OPEN_BRACKET_SYMBOL = "(";
    private static final String CLOSE_BRACKET_SYMBOL = ")";
    private static final String OPEN_SQUARE_BRACKET_SYMBOL = "[";
    private static final String DOT_SYMBOL = ".";
    private static final String COMMA_SYMBOL = ",";
    private static final String ELSE_TAG = "else";
    private static final String STRING_CONST_TAG = "stringConstant";
    private static final String SEMICOLON = ";";
    private static final String IF_STR = "if";
    private static final String WHILE_STR = "while";
    private static final String LET_STR = "let";
    private static final String DO_STR = "do";
    private static final String RETURN_STR = "return";
    private static final String VAR_STR = "var";
    private static final String STATIC_STR = "static";
    private static final String FIELD_STR = "field";
    private static final String CONTRC_STR = "constructor";
    private static final String FUNC_STR = "function";
    private static final String METHOD_SRT = "method";
    private static final Hashtable<String, String> opSymbolHashTable = new Hashtable<>();
    private static final String[] opSymbolList = {"+", "-", "*", "/", "&", "<", ">", "=", "|"};
    private static final String POINTR_SEG = "pointer";
    private static final String TEMP_SEG = "temp";
    private static final String THAT_SEG = "that";
    private static final String LOOP_BEGINS_STR = "BEGIN_LOOP_";
    private static final String LOOP_ENDS_STR = "END_LOOP_";
    private static final String CONST_SEG = "constant";
    private static final String IF_CONDI_TRUE = "IF_TRUE";
    private static final String IF_CONDI_FALSE = "IF_FALSE";
    private static final String IF_END_STR = "IF_END";
    private static final String MULTI_FUNC = "Math.multiply";
    private static final String DIV_FUNC = "Math.divide";
    private static final String THIS_SEG = "this";
    private static final String ARGUMENT_STR = "argument";
    private static final String MELOCK_STR = "Memory.alloc";
    private static final String STR_CONSTRACTOR = "String.new";
    private static final String STR_APPEND = "String.appendChar";
    private static final String TURE_STR = "true";
    private static final String FALSE_STR = "false";
    private static final String NULL_STR = "null";
    private static final String NEG_SYMBOL = "neg";


    static {
        opSymbolHashTable.put("<", "&lt;");
        opSymbolHashTable.put(">", "&gt;");
        opSymbolHashTable.put("\"", "&quot;");
        opSymbolHashTable.put("&", "&amp;");
    }


    /************************ Data Member *************************/
    private VMWriter writer;
    private JackTokenizer jackTokenizer;
    private SymbolTable symbolTable;
    private String currTokenType;
    private String currentToken;
    private int whileCounter = 0;
    private int ifCounter = 0;
    private String currentClass;


    /**
     * Creates a new compilation engine with the given input and output.
     * The next routine called must be compileClass.
     *
     * @param input  - Input stream/file
     * @param output - Output stream/file
     * @throws IOException
     */
    public CompilationEngine(File input, File output) throws IOException {

        writer = new VMWriter(output);
        jackTokenizer = new JackTokenizer(input);
        symbolTable = new SymbolTable();

    }

    /**
     * close CompilationEngine.
     *
     * @throws IOException
     */
    void close() throws IOException {
        jackTokenizer.close();
        writer.close();
    }

    /**
     * Compiles a complete class.
     */
    void compileClass() throws IOException {
        try {
            for (int i = 0; i < 2; i++) {
                getNextToken();
            }
            currentClass = currentToken;
            for (int i = 0; i < 2; i++) {
                getNextToken();
            }
            compileClassHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Compiles a static declaration or a field declaration.
     */
    void compileClassVarDec() {
        try {
            compileClassVarDecHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Compiles a complete method, function, or constructor.
     */
    void compileSubroutine() {
        try {
            symbolTable.startSubroutine();
            compileSubroutineHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Compiles a possibly empty parameter list, not including the enclosing circlar brackets.
     */
    void compileParameterList() {
        try {
            getNextToken();
            compileParameterListHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles a var declaration.
     */
    void compileVarDec() {
        try {
            compileVarDecHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles a sequence of statements, not including the enclosing curvy brackets.
     */
    void compileStatements() {
        try {
            compileStatementHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles a do statement.
     */
    void compileDo() {
        try {
            compileDoHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles a let statement.
     */
    void compileLet() {
        try {
            getNextToken();
            compileLetHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles a while statement.
     */
    void compileWhile() {
        try {
            compileWhileHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles a return statement.
     */
    void compileReturn() {
        try {
            getNextToken();
            compileReturnHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles an if statement, possibly with a trailing else clause.
     */
    void compileIf() {
        try {
            compileIfHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles an expression.
     */
    void compileExpression() {
        try {
            compileTerm();
            compileExpressionHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Compiles a possibly empty comma separated list of expressions.
     */
    int compileExpressionList() {
        try {
            getNextToken();
            return compileExpressionListHelper();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Compiles a term.
     * This routine is faced with a slight difficulty when trying to decide
     * between some of the alternative parsing rules. Specifically, if the
     * current token is an identifier, the routine must distinguish between
     * a variable, an array entry, and a subroutine call.
     * A single look-ahead token, which may be one of
     * suffices to distinguish between the three possibilities.
     * Any other token is not part of this term and should not be advanced over.
     */
    void compileTerm() {
        try {
            compileTermBasicHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * get the next token and its type and updates fields:
     * currTokenType, currentToken.
     *
     * @return true or false if the update was successful.
     * @throws IOException
     */
    private boolean getNextToken() throws IOException {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            currTokenType = jackTokenizer.tokenType();
            if (currTokenType.equals(JackTokenizer.IDENTIFIER_TOKEN_TYPE)) {
                currentToken = jackTokenizer.identifier();

            } else if (currTokenType.equals(JackTokenizer.INT_CONST_TOKEN_TYPE)) {
                currentToken = String.valueOf(jackTokenizer.intVal());

            } else if (currTokenType.equals(JackTokenizer.KEYWORD_TOKEN_TYPE)) {
                currentToken = jackTokenizer.keyWord();

            } else if (currTokenType.equals(JackTokenizer.STRING_CONST_TOKEN_TYPE)) {
                currentToken = jackTokenizer.stringVal();

            } else if (currTokenType.equals(JackTokenizer.SYMBOL_TOKEN_TYPE)) {
                currentToken = String.valueOf(jackTokenizer.currentSymbol());

            } else {
                return false;

            }
            return true;
        }
        return false;
    }


    /**
     * helper function for term compilation. this func. in need calls
     * a more advance helper function.
     *
     * @throws IOException
     */
    void compileTermBasicHelper() throws IOException {


        switch (currTokenType) {
            case JackTokenizer.INT_CONST_TOKEN_TYPE:
                Integer index = Integer.valueOf(currentToken);
                writer.writePush(CONST_SEG, index);
                getNextToken();
                return;
            case JackTokenizer.STRING_CONST_TOKEN_TYPE:
                writer.writePush(CONST_SEG, currentToken.length());
                writer.writeCall(STR_CONSTRACTOR, 1);

                IntStream.range(0, currentToken.length()).forEach(i -> {
                    writer.writePush(CONST_SEG, currentToken.charAt(i));
                    writer.writeCall(STR_APPEND, 2);
                });
                getNextToken();
                return;
            case JackTokenizer.KEYWORD_TOKEN_TYPE:

                if (currentToken.equals(TURE_STR)) {
                    writer.writePush(CONST_SEG, 0);
                    writer.WriteArithmetic(TILDE_SYMBOL);

                } else if (currentToken.equals(FALSE_STR) || currentToken.equals(NULL_STR)) {
                    writer.writePush(CONST_SEG, 0);

                } else if (currentToken.equals(THIS_SEG)) {
                    writer.writePush(POINTR_SEG, 0);


                }
                getNextToken();
                return;
        }
        switch (currentToken) {
            case HYPHEN_SYMBOL:
            case TILDE_SYMBOL:
                String negop = currentToken;
                switch (negop) {
                    case HYPHEN_SYMBOL:
                        negop = NEG_SYMBOL;
                        break;
                }
                getNextToken();
                compileTerm();
                writer.WriteArithmetic(negop);
                break;
            case OPEN_BRACKET_SYMBOL:
                getNextToken();
                compileExpression();
                getNextToken();
                break;
            default:
                String prevToken = currentToken;

                getNextToken();

                compileTermAdvanceHelper(prevToken);
                break;
        }


    }


    /**
     * advance helper function for term compilation.
     *
     * @param token
     * @throws IOException
     */
    void compileTermAdvanceHelper(String token) throws IOException {

        int nArgs;
        if (currentToken.equals(OPEN_SQUARE_BRACKET_SYMBOL)) {
            getNextToken();
            compileExpression();
            writer.writePush(symbolTable.kindOf(token), symbolTable.indexOf(token));
            writer.WriteArithmetic(opSymbolList[0]);
            writer.writePop(POINTR_SEG, 1);
            writer.writePush(THAT_SEG, 0);
            getNextToken();

        } else if (currentToken.equals(OPEN_BRACKET_SYMBOL)) {
            nArgs = 1;
            writer.writePush(POINTR_SEG, 0);
            nArgs = nArgs + compileExpressionList();
            writer.writeCall(currentClass + DOT_SYMBOL + token, nArgs);
            getNextToken();

        } else if (currentToken.equals(DOT_SYMBOL)) {

            String currentSubroutine;
            nArgs = 0;
            if (symbolTable.kindOf(token) == null) {
                currentSubroutine = token + currentToken;
            } else {
                int idx = symbolTable.indexOf(token);
                writer.writePush(symbolTable.kindOf(token), idx);
                currentSubroutine = symbolTable.typeOf(token) + currentToken;
                nArgs++;
            }
            getNextToken();
            currentSubroutine += currentToken;
            getNextToken();
            nArgs = nArgs + compileExpressionList();
            writer.writeCall(currentSubroutine, nArgs);
            getNextToken();
        } else {
            Integer idx = symbolTable.indexOf(token);
            writer.writePush(symbolTable.kindOf(token), idx);
        }
    }

    /**
     * helper for compileExpressionList.
     *
     * @throws IOException
     */
    private int compileExpressionListHelper() throws IOException {
        int numberOfExpressions;
        numberOfExpressions = 0;

        if (currentToken.equals(CLOSE_BRACKET_SYMBOL) && !currTokenType.equals(STRING_CONST_TAG)) {
            return numberOfExpressions;
        }
        numberOfExpressions++;
        compileExpression();
        if (currentToken.equals(COMMA_SYMBOL)) {
            do {
                numberOfExpressions++;
                getNextToken();
                compileExpression();
            } while (currentToken.equals(COMMA_SYMBOL));
        }
        return numberOfExpressions;
    }

    /**
     * helper for compile if.
     *
     * @throws IOException
     */
    private void compileIfHelper() throws IOException {
        int numberOflabel;
        numberOflabel = ifCounter;
        ifCounter = ifCounter + 1;

        String prevv = currentToken;

        for (int i = 0; i < 2; i++) {
            getNextToken();
        }
        String curr = currentToken;

        compileExpression();
        writer.WriteIf(IF_CONDI_TRUE + numberOflabel);
        writer.WriteGoto(IF_CONDI_FALSE + numberOflabel);
        writer.WriteLabel(IF_CONDI_TRUE + numberOflabel);
        for (int i = 0; i < 2; i++) {
            getNextToken();
        }

        compileStatements();
        getNextToken();
        switch (currentToken) {
            case ELSE_TAG:
                writer.WriteGoto(IF_END_STR + numberOflabel);
                writer.WriteLabel(IF_CONDI_FALSE + numberOflabel);
                for (int i = 0; i < 2; i++) {
                    getNextToken();
                }
                compileStatements();
                getNextToken();
                writer.WriteLabel(IF_END_STR + numberOflabel);
                break;
            default:
                writer.WriteLabel(IF_CONDI_FALSE + numberOflabel);
                break;
        }
    }

    /**
     * helper for compile return.
     *
     * @throws IOException
     */
    private void compileReturnHelper() throws IOException {

        switch (currentToken) {
            case SEMICOLON:
                writer.writePush(CONST_SEG, 0);
                break;
            default:
                compileExpression();
                break;
        }
        writer.writeReturn();
    }

    /**
     * helper for copileWhile
     */
    private void compileWhileHelper() throws IOException {
        int numberOflabel;
        numberOflabel = whileCounter;
        whileCounter = whileCounter + 1;
        getNextToken();
        writer.WriteLabel(LOOP_BEGINS_STR + numberOflabel);
        getNextToken();
        compileExpression();
        writer.WriteArithmetic(TILDE_SYMBOL);
        writer.WriteIf(LOOP_ENDS_STR + numberOflabel);
        for (int i = 0; i < 2; i++) {
            getNextToken();
        }
        compileStatements();
        writer.WriteGoto(LOOP_BEGINS_STR + numberOflabel);
        writer.WriteLabel(LOOP_ENDS_STR + numberOflabel);
    }

    /**
     * helper for compileLet
     */
    private void compileLetHelper() throws IOException {
        int bool = 0;

        String varible = currentToken;
        getNextToken();
        switch (currentToken) {
            case OPEN_SQUARE_BRACKET_SYMBOL:
                bool = 1;
                getNextToken();
                compileExpression();
                writer.writePush(symbolTable.kindOf(varible), symbolTable.indexOf(varible));
                writer.WriteArithmetic(ADD_SYMBOL);
                getNextToken();
                break;
        }
        getNextToken();
        compileExpression();
        switch (bool) {
            case 1:
                writer.writePop(TEMP_SEG, 0);
                writer.writePop(POINTR_SEG, 1);
                writer.writePush(TEMP_SEG, 0);
                writer.writePop(THAT_SEG, 0);
                break;
            default:
                writer.writePop(symbolTable.kindOf(varible), symbolTable.indexOf(varible));
                break;
        }
    }

    /**
     * helper for compileDo.
     */
    private void compileDoHelper() throws IOException {
        String currentSubroutine;
        int nArgs = 0;
        getNextToken();
        if (symbolTable.kindOf(currentToken) == null) {
            currentSubroutine = currentToken;
        } else {
            writer.writePush(symbolTable.kindOf(currentToken), symbolTable.indexOf(currentToken));
            currentSubroutine = symbolTable.typeOf(currentToken);
            nArgs++;
        }
        getNextToken();
        switch (currentToken) {
            case DOT_SYMBOL:
                getNextToken();
                currentSubroutine += DOT_SYMBOL + currentToken;
                getNextToken();
                break;
            default:
                writer.writePush(POINTR_SEG, 0);
                nArgs++;
                currentSubroutine = currentClass + DOT_SYMBOL + currentSubroutine;
                break;
        }
        nArgs = nArgs + compileExpressionList();
        writer.writeCall(currentSubroutine, nArgs);

        writer.writePop(TEMP_SEG, 0);
        getNextToken();

    }


    /**
     * helper for compileStatements.
     */
    private void compileStatementHelper() throws IOException {
        boolean isStatement = true;
        while (isStatement) {
            if (currentToken.equals(IF_STR)) {
                compileIf();

            } else if (currentToken.equals(WHILE_STR)) {
                compileWhile();
                getNextToken();

            } else if (currentToken.equals(LET_STR)) {
                compileLet();
                getNextToken();

            } else if (currentToken.equals(DO_STR)) {
                compileDo();
                getNextToken();

            } else if (currentToken.equals(RETURN_STR)) {
                compileReturn();
                getNextToken();
            } else {
                isStatement = false;
            }
        }
    }

    /**
     * helper for compileVarDec.
     */
    private void compileVarDecHelper() throws IOException {
        String kind = currentToken;
        String name;
        String type;
        getNextToken();
        type = currentToken;
        getNextToken();
        name = currentToken;
        symbolTable.define(name, type, kind);
        getNextToken();
        decHelper(type, kind);
    }

    /**
     * helper for compileParameterList.
     */
    private void compileParameterListHelper() throws IOException {

        String name;
        String type;

        if (currentToken.equals(CLOSE_BRACKET_SYMBOL)) {
        } else {
            type = currentToken;
            getNextToken();
            name = currentToken;
            symbolTable.define(name, type, ARGUMENT_STR);
            getNextToken();
            if (currentToken.equals(COMMA_SYMBOL)) {
                do {
                    getNextToken();
                    type = currentToken;
                    getNextToken();
                    name = currentToken;
                    symbolTable.define(name, type, ARGUMENT_STR);
                    getNextToken();
                } while (currentToken.equals(COMMA_SYMBOL));
            }
        }
    }


    /**
     * helper for compileSubroutine;
     */
    private void compileSubroutineHelper() throws IOException {

        String tempType;
        tempType = currentToken;

        if (tempType.equals(METHOD_SRT)) {
            symbolTable.define(THIS_SEG, currentClass, ARGUMENT_STR);
        }
        for (int i = 0; i < 2; i++) {
            getNextToken();
        }
        String subroutineName;
        subroutineName = currentClass + DOT_SYMBOL + currentToken;
        getNextToken();
        compileParameterList();
        for (int i = 0; i < 2; i++) {
            getNextToken();
        }
        if (currentToken.equals(VAR_STR)) {
            do {
                compileVarDec();
                getNextToken();
            } while (currentToken.equals(VAR_STR));
        }
        writer.writeFunction(subroutineName, symbolTable.varCount(VAR_STR));
        switch (tempType) {
            case CONTRC_STR:
                writer.writePush(CONST_SEG, symbolTable.varCount(FIELD_STR));
                writer.writeCall(MELOCK_STR, 1);
                writer.writePop(POINTR_SEG, 0);
                break;
            case METHOD_SRT:
                writer.writePush(ARGUMENT_STR, 0);
                writer.writePop(POINTR_SEG, 0);

                break;
        }
        compileStatements();
    }

    /**
     * helepr for compileClassVarDec.
     */
    private void compileClassVarDecHelper() throws IOException {
        String variableName;
        String variableType;
        String variableKind;
        variableKind = currentToken;
        getNextToken();
        variableType = currentToken;
        getNextToken();
        variableName = currentToken;
        symbolTable.define(variableName, variableType, variableKind);
        getNextToken();
        decHelper(variableType, variableKind);

    }

    /**
     * dec helper
     *
     * @param type
     * @param kind
     * @throws IOException
     */
    void decHelper(String type, String kind) throws IOException {
        if (currentToken.equals(COMMA_SYMBOL)) {
            do {
                getNextToken();
                String name = currentToken;
                symbolTable.define(name, type, kind);
                getNextToken();
            } while (currentToken.equals(COMMA_SYMBOL));
        }
    }

    /**
     * helper for compileClass;
     */
    private void compileClassHelper() throws IOException {

        if (currentToken.equals(STATIC_STR) || currentToken.equals(FIELD_STR)) {
            do {
                compileClassVarDec();
                getNextToken();
            }
            while (currentToken.equals(STATIC_STR) || currentToken.equals(FIELD_STR));
        }
        if (currentToken.equals(CONTRC_STR) || currentToken.equals(FUNC_STR) || currentToken.equals(METHOD_SRT)) {
            do {
                compileSubroutine();
                getNextToken();

            }
            while (currentToken.equals(CONTRC_STR) || currentToken.equals(FUNC_STR) || currentToken.equals(METHOD_SRT));
        }
    }

    /**
     * compile Expression helper function.
     *
     * @throws IOException
     */
    private void compileExpressionHelper() throws IOException {

        while (Arrays.asList(opSymbolList).contains(currentToken)) {
            String opString = currentToken;
            getNextToken();
            compileTerm();
            if (opString.equals(opSymbolList[2])) {
                writer.writeCall(MULTI_FUNC, 2);

            } else if (opString.equals(opSymbolList[3])) {
                writer.writeCall(DIV_FUNC, 2);

            } else {
                writer.WriteArithmetic(opString);
            }
        }
    }

}
