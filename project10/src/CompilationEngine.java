import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Objects;
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
    private static final String INPUT_FILE_SUFFIX = ".jack";
    private static final String OUTPUT_FILE_SUFFIX = ".xml";
    private static final String CLASS_TAG = "class";
    private static final String TERM_TAG = "term";
    private static final String HYPHEN_SYMBOL = "-";
    private static final String TILDE_SYMBOL = "~";
    private static final String OPEN_BRACKET_SYMBOL = "(";
    private static final String CLOSE_BRACKET_SYMBOL = ")";
    private static final String OPEN_SQUARE_BRACKET_SYMBOL = "[";
    private static final String DOT_SYMBOL = ".";
    private static final String EXPRESSION_TAG = "expression";
    private static final String EXPRESSION_LIST_TAG = "expressionList";
    private static final String COMMA_SYMBOL = ",";
    private static final String IF_TAG = "ifStatement";
    private static final String ELSE_TAG = "else";
    private static final String RETURN_TAG = "returnStatement";
    private static final String SEMICOLON = ";";
    private static final String WHILE_TAG = "whileStatement";
    private static final String LET_TAG = "letStatement";
    private static final String DO_TAG = "doStatement";
    private static final String STAT_TAG = "statements";
    private static final String IF_STR = "if";
    private static final String WHILE_STR = "while";
    private static final String LET_STR = "let";
    private static final String DO_STR = "do";
    private static final String RETURN_STR = "return";
    private static final String VAR_DEC_TAG = "varDec";
    private static final String PAR_LIST_TAG = "parameterList";
    private static final String SUB_TAG = "subroutineDec";
    private static final String SUB_BODY_TAG = "subroutineBody";
    private static final String VAR_STR = "var";
    private static final String CLASS_VAR_DEC_TAG = "classVarDec";
    private static final String STATIC_STR = "static";
    private static final Object FIELD_STR = "field";
    private static final String CONTRC_STR = "constructor";
    private static final String FUNC_STR = "function";
    private static final String METHOD_SRT = "method";
    private static final Hashtable<String, String> opSymbolHashTable = new Hashtable<>();
    private static final String[] opSymbolList = {"+", "-", "*", "/", "&", "<", ">", "=", "|"};

    static {
        opSymbolHashTable.put("<", "&lt;");
        opSymbolHashTable.put(">", "&gt;");
        opSymbolHashTable.put("\"", "&quot;");
        opSymbolHashTable.put("&", "&amp;");
    }

    /************************ Data Member *************************/
    private PrintWriter writer;
    private JackTokenizer jackTokenizer;
    private String currTokenType;
    private String currentToken;
    private int tabs = 0;
    private int OPEN_TAG_BRACKET = 0;
    private int CLOSE_TAG_BRACKET = 1;
    private int FULL_TAG_BRACKET = 2;


    /**
     * Creates a new compilation engine with the given input and output.
     * The next routine called must be compileClass.
     *
     * @param input  - Input stream/file
     * @param output - Output stream/file
     * @throws IOException
     */
    public CompilationEngine(File input, File output) throws IOException {
        writer = new PrintWriter(Files.newBufferedWriter(Paths.get(output.getPath().replace(INPUT_FILE_SUFFIX, OUTPUT_FILE_SUFFIX))));
        jackTokenizer = new JackTokenizer(input);
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
        tagBracketPrinter(CLASS_TAG, OPEN_TAG_BRACKET);
        try {
            compileClassHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(CLASS_TAG, CLOSE_TAG_BRACKET);

    }


    /**
     * Compiles a static declaration or a field declaration.
     */
    void compileClassVarDec() {
        tagBracketPrinter(CLASS_VAR_DEC_TAG, OPEN_TAG_BRACKET);
        try {
            compileClassVarDecHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(CLASS_VAR_DEC_TAG, CLOSE_TAG_BRACKET);


    }


    /**
     * Compiles a complete method, function, or constructor.
     */
    void compileSubroutine() {
        tagBracketPrinter(SUB_TAG, OPEN_TAG_BRACKET);
        try {
            compileSubroutineHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(SUB_TAG, CLOSE_TAG_BRACKET);

    }

    /**
     * Compiles a possibly empty parameter list, not including the enclosing circlar brackets.
     */
    void compileParameterList() {
        tagBracketPrinter(PAR_LIST_TAG, OPEN_TAG_BRACKET);
        try {
            compileParameterListHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(PAR_LIST_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * Compiles a var declaration.
     */
    void compileVarDec() {
        tagBracketPrinter(VAR_DEC_TAG, OPEN_TAG_BRACKET);
        try {
            compileVarDecHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(VAR_DEC_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * Compiles a sequence of statements, not including the enclosing curvy brackets.
     */
    void compileStatements() {
        tagBracketPrinter(STAT_TAG, OPEN_TAG_BRACKET);
        try {
            compileStatementHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(STAT_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * Compiles a do statement.
     */
    void compileDo() {
        tagBracketPrinter(DO_TAG, OPEN_TAG_BRACKET);
        try {
            compileDoHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(DO_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * Compiles a let statement.
     */
    void compileLet() {
        tagBracketPrinter(LET_TAG, OPEN_TAG_BRACKET);
        try {
            compileLetHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(LET_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * Compiles a while statement.
     */
    void compileWhile() {
        tagBracketPrinter(WHILE_TAG, OPEN_TAG_BRACKET);
        try {
            compileWhileHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(WHILE_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * Compiles a return statement.
     */
    void compileReturn() {
        tagBracketPrinter(RETURN_TAG, OPEN_TAG_BRACKET);
        try {
            compileReturnHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(RETURN_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * Compiles an if statement, possibly with a trailing else clause.
     */
    void compileIf() {

        tagBracketPrinter(IF_TAG, OPEN_TAG_BRACKET);
        try {
            compileIfHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(IF_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * Compiles an expression.
     */
    void compileExpression() {
        tagBracketPrinter(EXPRESSION_TAG, OPEN_TAG_BRACKET);
        try {
            compileExpressionHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(EXPRESSION_TAG, CLOSE_TAG_BRACKET);

    }

    private void compileExpressionHelper() throws IOException {

        compileTerm();
        String temp = currentToken;
        while (Arrays.asList(opSymbolList).contains(temp)) {
            printToken();
            getNextToken();
            compileTerm();
            temp = currentToken;
        }
    }

    /**
     * Compiles a possibly empty comma separated list of expressions.
     */
    void compileExpressionList() {
        tagBracketPrinter(EXPRESSION_LIST_TAG, OPEN_TAG_BRACKET);
        try {
            compileExpressionListHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(EXPRESSION_LIST_TAG, CLOSE_TAG_BRACKET);

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
        tagBracketPrinter(TERM_TAG, OPEN_TAG_BRACKET);
        try {
            compileTermBasicHelper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tagBracketPrinter(TERM_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * prints open or close tag bracket or full bracket section:
     *
     * @param tag       string tag
     * @param printType string token.
     * @param printType int 0/1 to print open/close tag bracket or full bracket section:
     */
    private void tagBracketPrinter(String tag, int printType, String... token) {
        String temp = Arrays.toString(token).substring(1, Arrays.toString(token).length() - 1);
        tabPrinter();
        if (printType == 0) {
            writer.println("<" + tag + ">");
            tabs++;
        } else if (printType == 1) {
            tabs--;
            writer.println("</" + tag + ">");
        } else if (printType == 2)

            writer.println("<" + tag + "> " + temp + " </" + tag + ">");

    }

    /**
     * replace Op Symbol to the their valid representation or
     * just returns the symbol as a string if not Op. with the help of
     * opSymbolHashTable.
     *
     * @return String valid symbol representation.
     */
    private String replaceOpSymbol(String symbol) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < symbol.length(); i++) {
            temp.append(replaceOpSymbol(symbol.charAt(i)));
        }
        return (temp.toString());
    }

    /**
     * replace Op Symbol to the their valid representation or
     * just returns the symbol as a string if not Op. with the help of
     * opSymbolHashTable.
     *
     * @param symbol char
     * @return String valid symbol representation.
     */
    private String replaceOpSymbol(char symbol) {

        if (opSymbolHashTable.containsKey(String.valueOf(symbol))) {

            return opSymbolHashTable.get(String.valueOf(symbol));
        } else {
            return String.valueOf(symbol);
        }


    }

    /**
     * just prints tabs according to 'tabs' field.
     */
    private void tabPrinter() {
        IntStream.range(0, tabs).forEach(i -> writer.print(" "));
    }


    /**
     * prints the current token with full angle bracket.
     */
    private void printToken() {
        tabPrinter();

        if (jackTokenizer.tokenType().equals(JackTokenizer.SYMBOL_TOKEN_TYPE)) {

            tagBracketPrinter(currTokenType, FULL_TAG_BRACKET, replaceOpSymbol(currentToken.charAt(0)));
        } else if (jackTokenizer.tokenType().equals(JackTokenizer.STRING_CONST_TOKEN_TYPE)) {
            currentToken = currentToken.substring(1, currentToken.length() - 1);
            tagBracketPrinter(currTokenType, FULL_TAG_BRACKET, replaceOpSymbol(currentToken));
        } else {

            tagBracketPrinter(currTokenType, FULL_TAG_BRACKET, currentToken);
        }
    }


    /**
     * given a tpoken type and a token prints the current token with full
     * angle bracket. @overload
     *
     * @param tokenType string
     * @param token     string
     */
    private void printToken(String tokenType, String token) {
        tabPrinter();
        if (tokenType.equals(JackTokenizer.SYMBOL_TOKEN_TYPE)) {
            tagBracketPrinter(tokenType, FULL_TAG_BRACKET, replaceOpSymbol(token.charAt(0)));
        } else if (tokenType.equals(JackTokenizer.STRING_CONST_TOKEN_TYPE)) {
            tagBracketPrinter(tokenType, FULL_TAG_BRACKET, replaceOpSymbol(token));
        } else {
            tagBracketPrinter(tokenType, FULL_TAG_BRACKET, token);
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
            case JackTokenizer.STRING_CONST_TOKEN_TYPE:
            case JackTokenizer.KEYWORD_TOKEN_TYPE:
                printToken();
                getNextToken();
                return;
        }
        switch (currentToken) {
            case HYPHEN_SYMBOL:
            case TILDE_SYMBOL:
                printToken();
                getNextToken();
                compileTerm();
                break;
            case OPEN_BRACKET_SYMBOL:
                printToken();
                getNextToken();
                compileExpression();
                printToken();
                getNextToken();
                break;
            default:
                String prevToken = currentToken;
                String prevTokenType = currTokenType;
                getNextToken();
                compileTermAdvanceHelper(prevTokenType, prevToken);
                break;
        }

    }

    /**
     * advance helper function for term compilation.
     *
     * @param tokenType
     * @param token
     * @throws IOException
     */
    void compileTermAdvanceHelper(String tokenType, String token) throws IOException {
        if (currentToken.equals(OPEN_SQUARE_BRACKET_SYMBOL)) {
            printToken(tokenType, token);
            printToken();
            getNextToken();
            compileExpression();
            printToken();
            getNextToken();

        } else if (currentToken.equals(OPEN_BRACKET_SYMBOL)) {
            printToken(tokenType, token);
            printToken();
            compileExpressionList();
            printToken();
            getNextToken();

        } else if (currentToken.equals(DOT_SYMBOL)) {

            printToken(tokenType, token);
            for (int i = 0; i < 2; i++) {
                printToken();
                getNextToken();
            }
            printToken();
            compileExpressionList();
            printToken();
            getNextToken();
        } else {
            printToken(tokenType, token);
        }
    }

    /**
     * helper for compileExpressionList.
     *
     * @throws IOException
     */
    private void compileExpressionListHelper() throws IOException {
        getNextToken();
        if (!currentToken.equals(CLOSE_BRACKET_SYMBOL)) {
            compileExpression();
            while (currentToken.equals(COMMA_SYMBOL)) {
                printToken();
                getNextToken();
                compileExpression();
            }
        }
    }

    /**
     * helper for compile if.
     *
     * @throws IOException
     */
    private void compileIfHelper() throws IOException {
        for (int i = 0; i < 2; i++) {
            printToken();
            getNextToken();
        }
        compileExpression();
        for (int i = 0; i < 2; i++) {
            printToken();
            getNextToken();
        }
        compileStatements();
        printToken();
        getNextToken();
        if (currentToken.equals(ELSE_TAG)) {
            for (int i = 0; i < 2; i++) {
                printToken();
                getNextToken();
            }
            compileStatements();
            printToken();
            getNextToken();
        }
    }

    /**
     * helper for compile return.
     *
     * @throws IOException
     */
    private void compileReturnHelper() throws IOException {
        printToken(); // prints 'return'
        getNextToken();
        if (!Objects.equals(currentToken, SEMICOLON)) compileExpression();
        printToken();
    }

    /**
     * helper for copileWhile
     */
    private void compileWhileHelper() throws IOException {
        for (int i = 0; i < 2; i++) {
            printToken();
            getNextToken();
        }
        compileExpression();
        for (int i = 0; i < 2; i++) {
            printToken();
            getNextToken();
        }
        compileStatements();
        printToken();
    }

    /**
     * helper for compileLet
     */
    private void compileLetHelper() throws IOException {
        for (int i = 0; i < 2; i++) {
            printToken();
            getNextToken();
        }
        switch (currentToken) {
            case OPEN_SQUARE_BRACKET_SYMBOL:
                printToken();
                getNextToken();
                compileExpression();
                printToken();
                getNextToken();
                break;
        }
        printToken();
        getNextToken();
        compileExpression();
        printToken();
    }

    /**
     * helper for compileDo.
     */
    private void compileDoHelper() throws IOException {
        for (int i = 0; i < 2; i++) {
            printToken();
            getNextToken();
        }
        if (currentToken.equals(DOT_SYMBOL)) {
            for (int i = 0; i < 2; i++) {
                printToken();
                getNextToken();
            }
        }
        printToken();
        compileExpressionList();
        printToken();
        getNextToken();
        printToken();
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
        for (int i = 0; i < 3; i++) {
            printToken();
            getNextToken();
        }
        while (currentToken.equals(COMMA_SYMBOL)) {
            for (int i = 0; i < 2; i++) {
                printToken();
                getNextToken();
            }

        }
        printToken();
    }

    /**
     * helper for compileParameterList.
     */
    private void compileParameterListHelper() throws IOException {
        getNextToken();
        if (!currentToken.equals(CLOSE_BRACKET_SYMBOL)) {
            for (int i = 0; i < 2; i++) {
                printToken();
                getNextToken();
            }
            while (currentToken.equals(COMMA_SYMBOL)) {
                for (int i = 0; i < 3; i++) {
                    printToken();
                    getNextToken();
                }
            }
        }
    }


    /**
     * helper for compileSubroutine;
     */
    private void compileSubroutineHelper() throws IOException {
        for (int i = 0; i < 3; i++) {
            printToken();
            getNextToken();
        }
        printToken();
        compileParameterList();
        printToken();

        tagBracketPrinter(SUB_BODY_TAG, OPEN_TAG_BRACKET);
        getNextToken();
        printToken();
        getNextToken();
        while (currentToken.equals(VAR_STR)) {
            compileVarDec();
            getNextToken();
        }
        compileStatements();
        printToken();
        tagBracketPrinter(SUB_BODY_TAG, CLOSE_TAG_BRACKET);
    }

    /**
     * helepr for compileClassVarDec.
     */
    private void compileClassVarDecHelper() throws IOException {
        for (int i = 0; i < 3; i++) {
            printToken();
            getNextToken();
        }
        while (currentToken.equals(COMMA_SYMBOL)) {
            for (int i = 0; i < 2; i++) {
                printToken();
                getNextToken();
            }
        }
        printToken();
    }

    /**
     * helper for compileClass;
     */
    private void compileClassHelper() throws IOException {
        getNextToken();
        for (int i = 0; i < 3; i++) {
            printToken();
            getNextToken();
        }


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


        printToken();
    }


}
