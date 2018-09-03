/**
 * Compiles a stream of given tokens according to the JACK grammar
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompilationEngine {

    //constants:
    private static final Pattern OP_PATTERN = Pattern.compile("\\+|-|\\*|/|&|<|>|=|\\|");

    //Data Members:
    /** The vmWriter to the output file */
    private VMWriter vmWriter = null;

    /** The tokenizer used by the compilation engine */
    private Tokenizer tokenizer;

    /** The symbolTable used by the CompilationEngine*/
    private SymbolTable symbolTable;

    /** The current token type*/
    private String currTokenType;

    /** The current token*/
    private String currToken;

    /** The current class name*/
    private String currClass;

    /** label counters */
    private int whileLabelCounter = 0;
    private int ifLabelCounter = 0;

    // Methods:

    /**
     * Constructs a new CompilationEngine for the given file
     * @param outputPath The path of the file to write to
     */
    public CompilationEngine(Path outputPath, File input) throws IOException
    {
        this.vmWriter = new VMWriter(outputPath);
        this.tokenizer = new Tokenizer(input); // create new tokenizer
        this.symbolTable = new SymbolTable();
    }

    /**
     * Closes the compilationEngine
     * @throws IOException in case of an error
     */
    void closeEngine() throws IOException
    {
        vmWriter.close();
        tokenizer.closeTokenizer();
    }

    /**
     * Updates the currToken and currTokenType
     * @return true if successful false otherwise
     * @throws IOException in case of an IO error
     */
    private boolean getNewToken() throws IOException
    {
        if(!tokenizer.hasMoreTokens()){ // No more tokens
            return false;
        } else {
            tokenizer.advance();
            currTokenType = tokenizer.getTokenType();
            switch(currTokenType){
                case Tokenizer.IDENTIFIER_TOKEN:
                    currToken = tokenizer.getCurrentIdentifier();
                    break;
                case Tokenizer.INT_CONST_TOKEN:
                    currToken = String.valueOf(tokenizer.getCurrentIntVal());
                    break;
                case Tokenizer.KEYWORD_TOKEN:
                    currToken = tokenizer.getCurrentKeyword();
                    break;
                case Tokenizer.STRING_CONST_TOKEN:
                    currToken = tokenizer.getCurrentStrVal();
                    break;
                case Tokenizer.SYMBOL_TOKEN:
                    currToken = String.valueOf(tokenizer.getCurrentSymbol());
                    break;
                default:
                    System.out.println("Wrong Token Type");
                    System.exit(1);
            }
        }
        return true;
    }

    /**
    * Compiles a new class according to the JACK grammar
    * @throws IOException In case of an IO error
    */
    void compileClass() throws IOException
    {   // 'currToken' Empty
        getNewToken(); // Token: 'class' saved word
        getNewToken(); // Token: name of the class
        currClass = currToken; // Assign class name
        getNewToken(); // Token: '{'
        getNewToken(); // Token: '}' || class scope var kind || a subRoutine keyword
        while (currToken.equals("static") || currToken.equals("field")){
            compileClassVarDec();
            getNewToken(); // Token: '}' || a subRoutine keyword
        }
        while (currToken.equals("constructor") || currToken.equals("function") || currToken.equals("method")){
            compileSubroutine();
            getNewToken(); // Token: '}'
        }
    }

    /**
     * Compiles a new class variable declaration section according to the JACK grammar
     * @throws IOException In case of an IO error
     */
    private void compileClassVarDec() throws IOException
    {   // 'currToken': class scope var kind
        String varName, varType ,varKind;
        varKind = currToken; // varKind = 'static'/'field'
        getNewToken(); // Token: varType
        varType = currToken;
        getNewToken(); // Token: varName
        varName = currToken; //get variable name
        symbolTable.define(varName, varType, varKind);

        getNewToken(); // Token: "," || ";"
        while(currToken.equals(",")){
            // currToken: ',' - Ignored
            getNewToken(); // Token: varName
            varName = currToken;
            symbolTable.define(varName, varType, varKind);
            getNewToken(); // Token: "," || ";"
        }
        //currToken: ';' - Ignored
    }

    /**
     * Compiles a subroutine according to the JACK grammar
     * @throws IOException In case of an IO error
     */
    private void compileSubroutine() throws IOException
    {   // 'currToken': a subroutine type (constructor/method/function)
        symbolTable.startSubroutine(); // initializes a new symbolTable for the new subRoutine
        String subType = currToken;
        if(subType.equals("method")){ // subroutine is a method
            symbolTable.define("this", currClass, SymbolTable.ARGUMENT_VAR); // set "this" as argument[0]
        }
        getNewToken(); // Token: subroutine return value AKA 'void' | 'type' - ignored
        getNewToken(); // Token: subroutine name
        String currSubroutineName = currClass+"."+currToken; // update current subroutine name
        getNewToken(); // Token: '('
        compileParameterList(); // defines arguments on the symbol table
        // currToken is: ')' - Ignored
        // Subroutine body begins:
        getNewToken(); // Token: '{'
        getNewToken(); // Token: "var" || statement || '}'
        while (currToken.equals("var")){
            compileVarDec(); // defines locals on the symbol table
            getNewToken(); // Token: "var" || statement || '}'
        }
        vmWriter.writeFunction(currSubroutineName, symbolTable.varCount(SymbolTable.LOCAL_VAR));
        if(subType.equals("constructor")){ // subroutine is a constructor
            //System.out.println("curr: "+currToken);
            //System.out.println("cunt: "+symbolTable.varCount(SymbolTable.FIELD_VAR));

            vmWriter.writePush("constant", symbolTable.varCount(SymbolTable.FIELD_VAR));
            vmWriter.writeCall("Memory.alloc", 1); // call Memory.alloc(numOfFields)
            vmWriter.writePop("pointer", 0); // pointer[0] = THIS = Sys.alloc(numOfFields)
        } else if(subType.equals("method")){ // subroutine is a method
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0); // pointer[0] = THIS = argument[0]
        }
        compileStatements();
        // currToken is: '}' - Ignored
    }

    /**
     * Compiles a parameter list according to the JACK grammar
     * @throws IOException in case of an IO error
     */
    private void compileParameterList() throws IOException
    {   // 'currToken' Empty
        String varName, varType;
        getNewToken(); //Token: ')' || a variable type
        if(!currToken.equals(")")){
            varType = currToken;
            getNewToken(); // Token: a variable name
            varName = currToken;
            symbolTable.define(varName, varType, SymbolTable.ARGUMENT_VAR);
            getNewToken(); // Token: "," || ')'
            while(currToken.equals(",")){
                getNewToken(); // Token: a variable type
                varType = currToken;
                getNewToken(); // Token: a variable name
                varName = currToken;
                symbolTable.define(varName, varType, SymbolTable.ARGUMENT_VAR);
                getNewToken();
            }
        }
    }

    /**
     * Compiles a variable declaration according to the JACK grammar
     * @throws IOException in case of an IO error
     */
    private void compileVarDec() throws IOException
    {   // 'currToken': 'var'
        String varName, varType ,varKind = currToken;
        getNewToken(); // Token: the type of the variable
        varType = currToken;
        getNewToken(); // Token: the name of the variable
        varName = currToken;
        symbolTable.define(varName, varType, varKind);
        getNewToken(); // Token: "," || ";"
        while(currToken.equals(",")){
            getNewToken(); // Token: the name of the variable
            varName = currToken;
            symbolTable.define(varName, varType, varKind);
            getNewToken(); // Token: "," || ";" - Ignored if Token = ";"
        }
    }

    /**
     * Compiles a statements section according to the JACK grammar
     * @throws IOException in case of an IO error
     */
    private void compileStatements() throws IOException
    {   // 'currToken' not Empty
        boolean isStatement = true;
        while (isStatement){
            switch(currToken){
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    getNewToken();
                    break;
                case "let":
                    compileLet();
                    getNewToken();
                    break;
                case "do":
                    compileDo();
                    getNewToken();
                    break;
                case "return":
                    compileReturn();
                    getNewToken();
                    break;
                default:
                    isStatement = false;
            }
        }
    }

    /**
     * Compiles a do statement according to the JACK grammar
     * @throws IOException in case of an IO error
     */
    private void compileDo() throws IOException
    {   // 'currToken': "do" - Ignored
        String calledSubroutine;
        int nArgs = 0;
        getNewToken(); // Token: subroutine name || class Name || varName
        if(symbolTable.kindOf(currToken) != null){ // currToken: varName (var is an object)
            vmWriter.writePush(symbolTable.kindOf(currToken), symbolTable.indexOf(currToken));
            calledSubroutine = symbolTable.typeOf(currToken); // calledSubroutine = varClass
            nArgs++;
        } else { // currToken: subroutine/class name
            calledSubroutine = currToken; // save subroutine/class name
        }
        getNewToken(); //Token: "(" || "." - Ignored
        if(currToken.equals(".")){ // if true than case is : (className|varName).subroutineName(expList)
            getNewToken(); // Token: subroutine name
            calledSubroutine += "."+currToken; // called subroutine: (className|varType).subroutineName
            getNewToken(); //Token: "(" - Ignored
        } else { // case is: methodName(expList)
            vmWriter.writePush("pointer", 0); // push 'this' as the first argument
            nArgs++;
            calledSubroutine = currClass+"."+calledSubroutine;
        }
        nArgs += compileExpressionList(); // pushes all parameters and counts them
        // currToken: ')' - Ignored
        vmWriter.writeCall(calledSubroutine, nArgs);
        vmWriter.writePop("temp", 0); // Ignore return value
        getNewToken(); // Token: ';' - Ignored
    }

    /**
     * Compiles a let statement according to the JACK grammar
     * @throws IOException in case of an error
     */
    private void compileLet() throws IOException
    {   // 'currToken': "let" - Ignored
        boolean isArray = false;
        getNewToken(); // Token: varName
        String varName = currToken;
        getNewToken(); // Token: '[' || '=' - Ignored
        if(currToken.equals("[")){ // varName stands for an array
            isArray = true;
            getNewToken(); // Token: expression
            compileExpression(); // expression result (AKA position) is at the top of the stack
            vmWriter.writePush(symbolTable.kindOf(varName),symbolTable.indexOf(varName)); //push base address
            vmWriter.writeArithmetic("+"); // base address + position at the top of the stack
            //currToken: ']' - Ignored
            getNewToken(); // Token: "=" - Ignored
        }
        getNewToken(); // Token: term
        compileExpression(); // expression result is at the top of the stack
        if(isArray){
            vmWriter.writePop("temp", 0); // temp[0] = exp2 result
            vmWriter.writePop("pointer", 1); // pointer[1] = THAT = relevant address
            vmWriter.writePush("temp", 0); // exp2 result is at the top of the stack
            vmWriter.writePop("that", 0); // varName[exp1] = exp2
        } else {
            vmWriter.writePop(symbolTable.kindOf(varName), symbolTable.indexOf(varName)); // pop to varName
        }
        // currToken: ';' - ignored
    }

    /**
     * Compiles a while statement according to the JACK grammar
     * @throws IOException in case of an error
     */
    private void compileWhile() throws IOException
    {   // 'currToken': "while" - Ignored
        int labelNum = whileLabelCounter;
        whileLabelCounter++;
        getNewToken(); // Token: '(' - Ignored
        vmWriter.writeLabel("BEGIN_LOOP_"+labelNum);
        getNewToken(); // Token: condition
        compileExpression(); // pushes the evaluated condition
        vmWriter.writeArithmetic("~");
        vmWriter.writeIf("END_LOOP_"+labelNum);
        // currToken: ')' - ignored
        getNewToken(); // Token: '{' - Ignored
        getNewToken(); // Token: statement keyword || '}'
        compileStatements();
        // currToken: '}' - Ignored
        vmWriter.writeGoto("BEGIN_LOOP_"+labelNum);
        vmWriter.writeLabel("END_LOOP_"+labelNum);
    }

    /**
     * Compiles a return statement according to the JACK grammar
     * @throws IOException in case of an error
     */
    private void compileReturn() throws IOException
    {   // 'currToken': "return" - Ignored
        getNewToken(); //Token: ";" || expression
        if(!currToken.equals(";")){ // subroutine type is not void - a return value is pushed
            compileExpression();
        } else { // subroutine type is void - push '0' to the stack
            vmWriter.writePush("constant", 0);
        }
        // currToken: ';' - Ignored
        vmWriter.writeReturn();
    }

    /**
     * Compiles an if statement according to the JACK grammar
     * @throws IOException in case of an error
     */
    private void compileIf() throws IOException
    {   // 'currToken': "if" - Ignored
        int labelNum = ifLabelCounter;
        ifLabelCounter++;

        String prevtest = currToken;

        getNewToken(); // Token: '(' - Ignored
        getNewToken(); // Token: expression || ')'

        String test = currToken;
        if (test.equals("false") && prevtest.equals("if")) {
            System.out.println("ffffff");
        }

        compileExpression();
        // currToken: ')' - Ignored
        vmWriter.writeIf("IF_TRUE"+labelNum); // if True - goto IF_TRUE label
        vmWriter.writeGoto("IF_FALSE"+labelNum); // if not True - go to IF_FALSE label
        vmWriter.writeLabel("IF_TRUE"+labelNum);
        getNewToken(); // Token: '{' - Ignored
        getNewToken(); // Token: Statements || '}'
        compileStatements();
        // currToken: '}' - Ignored
        getNewToken(); // Token: 'else' || '}' (of an outer scope)
        if(currToken.equals("else")){
            vmWriter.writeGoto("IF_END"+labelNum); // if true - skip the else clause
            vmWriter.writeLabel("IF_FALSE"+labelNum); // if false - do the else clause (if exists)
            // currToken: 'else' - Ignored
            getNewToken(); // Token: '{' - Ignored
            getNewToken(); // Token: statements || '}'
            compileStatements();
            // currToken: '}' - Ignored
            getNewToken(); // Token: '}' (of an outer scope)
            vmWriter.writeLabel("IF_END"+labelNum); // The end of the else clause
        } else {
            vmWriter.writeLabel("IF_FALSE"+labelNum); // if false - skip
        }
    }

    /**
     * Compiles an expression according to the JACK grammar
     * @throws IOException in case of an error
     */
    private void compileExpression() throws IOException
    {   // 'currToken': term1
        compileTerm(); // push the result of term1 to the top of the stack
        // curr Token: 'op' || ';' || ')' || ']' || ','
        Matcher opMatcher = OP_PATTERN.matcher(currToken);
        while(opMatcher.matches()){ // currToken: 'op'
            String operator = currToken; // saves 'op'
            getNewToken(); //Token: term2
            compileTerm(); // push the result of term2 to the top of the stack
            switch (operator){
                case "*":
                    vmWriter.writeCall("Math.multiply", 2);
                    break;
                case "/":
                    vmWriter.writeCall("Math.divide", 2);
                    break;
                default:
                    vmWriter.writeArithmetic(operator); // result of term1 op term2 at the top of the stack
            }
            //currToken: 'op' || ';' || ')' || ']' || ','
            opMatcher = OP_PATTERN.matcher(currToken);
        }
    }

    /**
     * prints the set of VM commands to handle a term keyword
     */
    private void handleKeyword()
    { // currToken: 'true' | 'false' | 'null' | 'this'
        switch(currToken){
            case "true":
                vmWriter.writePush("constant", 0);
                vmWriter.writeArithmetic("~");
                break;
            case "false":case "null":
                vmWriter.writePush("constant", 0);
                break;
            case "this":
                vmWriter.writePush("pointer", 0);
                break;
            default:
                System.out.println("unrecognized keyword");
                System.exit(1);
        }
    }

    /**
     * Puts an address of a string matching 'currToken' at the top of the stack
     */
    private void handleStringConst()
    { //currToken: String constant
        vmWriter.writePush("constant", currToken.length());
        vmWriter.writeCall("String.new", 1); // the String address at the top of the stack
        //System.out.println("curr len: " +currToken.length());
        //System.out.println("curr token: " + currToken);
        for(int i=0; i<currToken.length(); i++){
            vmWriter.writePush("constant", currToken.charAt(i));
            vmWriter.writeCall("String.appendChar", 2);
        } // the String constant address is at the top of the stack
    }

    /**
     * Compiles a term according to the JACK grammar
     * @throws IOException in case of an error
     */
    private void compileTerm() throws IOException
    {   // 'currToken':term

        if(currTokenType.equals(Tokenizer.INT_CONST_TOKEN)){ // case: int Const
            vmWriter.writePush("constant", Integer.valueOf(currToken));
            getNewToken(); //load a new token before method ends
        } else if(currTokenType.equals(Tokenizer.STRING_CONST_TOKEN)){ // case: String Const
            handleStringConst();
            getNewToken(); //load a new token before method ends
        } else if(currTokenType.equals(Tokenizer.KEYWORD_TOKEN)){ // case: keyword ('true' | 'false' | 'null' | 'this')
            handleKeyword();
            getNewToken(); //load a new token before method ends
        } else if(currToken.equals("-") || currToken.equals("~")){ // case: unaryOp
            String unaryOp = currToken;
            if(unaryOp.equals("-")){
                unaryOp = "neg";
            }
            getNewToken(); //Token: term
            compileTerm(); // term result at the top of the stack
            vmWriter.writeArithmetic(unaryOp);
        } else if(currToken.equals("(") ){ // case: expression
            // currToken: '(' - Ignored
            getNewToken(); //Token: expression
            compileExpression(); // exp result at the top of the stack
            // currToken: ')' - Ignored
            getNewToken(); //load a new token before method ends
        } else { // If any of the cases above doesn't match, next token needs to be checked
            String prevToken = currToken;
            getNewToken();
            switch(currToken){
                case "[": // case: varName[expression] (prevToken: arrayName)
                    //currToken: "[" - Ignored
                    getNewToken(); // Token: expression
                    compileExpression(); // expression result (AKA array index) at the top of the stack
                    //currToken: ']' - Ignored
                    vmWriter.writePush(symbolTable.kindOf(prevToken),symbolTable.indexOf(prevToken)); //push base address
                    vmWriter.writeArithmetic("+"); // base address + position at the top of the stack
                    vmWriter.writePop("pointer", 1); // pointer[1] = THAT = &(prevToken[exp])
                    vmWriter.writePush("that", 0); // stack.push(prevToken[exp])
                    getNewToken(); //load a new token before method ends
                    break;
                case "(": // case: methodName(expressionList)
                    //currToken: '(' - Ignored
                    int nArgs = 1;
                    vmWriter.writePush("pointer", 0); // make 'this' the first argument
                    nArgs += compileExpressionList(); // pushes expression list to the top of the stack
                    vmWriter.writeCall(currClass+"."+prevToken, nArgs);
                    //currToken: ')' - Ignored
                    getNewToken(); //load a new token before method ends
                    break;
                case ".": // case: className/varName.subroutine(expList)
                    String calledSubroutine;
                    nArgs = 0;
                    if(symbolTable.kindOf(prevToken) != null){ //checks if prevToken is a varName
                        vmWriter.writePush(symbolTable.kindOf(prevToken), symbolTable.indexOf(prevToken));
                        calledSubroutine = symbolTable.typeOf(prevToken)+currToken; //currToken: '.'
                        nArgs++;
                    } else{ // prevToken is a class name
                        calledSubroutine = prevToken+currToken; //currToken: '.'
                    }
                    getNewToken(); //Token: subroutine name
                    calledSubroutine += currToken; //calledSubroutine: class.
                    getNewToken(); // Token: '(' - Ignored
                    nArgs += compileExpressionList(); // arguments are at the top of the stack
                    // currToken: ')' - Ignored
                    vmWriter.writeCall(calledSubroutine, nArgs);
                    getNewToken(); //load a new token before method ends
                    break;
                default: // case: varName

                    vmWriter.writePush(symbolTable.kindOf(prevToken), symbolTable.indexOf(prevToken));
                    //System.out.println("prev: " + prevToken);
                    //System.out.println("idx: " + symbolTable.indexOf(prevToken));

            }
        }
    }

    /**
     * Compiles an expression list according to the JACK grammar
     * @throws IOException in case of an error
     */
    private int compileExpressionList() throws IOException
    {   // 'currToken' Empty
        int nExpression = 0;
        getNewToken(); // Token: expression || ')'
        if(!currToken.equals(")") || currTokenType.equals(Tokenizer.STRING_CONST_TOKEN)){ // expression list not empty
            nExpression++;
            compileExpression(); // The result of the expression is at the top of the stack
            while(currToken.equals(",")){
                nExpression++;
                //currToken: "," - Ignored
                getNewToken(); // Token: expression
                compileExpression(); // The result of the expression is at the top of the stack
                //currToken: "," || ')'
            }
        }
        return nExpression;
    }

}
