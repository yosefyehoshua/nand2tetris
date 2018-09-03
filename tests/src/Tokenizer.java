/**
 * A class for splitting .jack files into tokens
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Tokenizer  {

    //Constants:
    /** token patterns*/
    private static final Pattern WORD_BEGINNING_PATTERN = Pattern.compile("[a-zA-Z_]");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("\\{|\\}|\\(|\\)|\\[|]|\\.|,|;|\\||\\+|-|\\*|/|&|<|>|=|~");

    /** the different token types*/
    static final String KEYWORD_TOKEN = "keyword";
    static final String SYMBOL_TOKEN = "symbol";
    static final String IDENTIFIER_TOKEN = "identifier";
    static final String INT_CONST_TOKEN = "integerConstant";
    static final String STRING_CONST_TOKEN = "stringConstant";

    /** the JACK keywords*/
    private static final String keywords[] = {"class", "constructor", "function", "method", "field",
            "static","var", "int", "char", "boolean", "void", "true", "false", "null", "this",
            "let", "do", "if", "else", "while", "return"};

    //Data Members:
    /** The reader (buffered) of the parser */
    private BufferedReader reader = null;
    /** The current token in the parsing process */
    private String tokenQueue = null;
    /** Did the reader reach the end of the file? */
    private boolean reachedEOF = false;
    /** The type of the current command where */
    private String currType;
    /** The symbol of the current token (if the token is of type SYMBOL) */
    private char currentSymbol;
    /** The identifier of the current token (if the token is of type IDENTIFIER) */
    private String currentIdentifier;
    /** The integer value of the current token (if the token is of type INT_CONST) */
    private int currentIntVal;
    /** The string value of the current token (if the token is of type STRING_CONST) */
    private String currentStrVal;
    /** The keyword of the current token (if the token is of type KEYWORD) */
    private String currentKeyword;
    /** The set of keywords in the JACK language */
    static HashSet<String> keywordsSet;


    // Methods:

    /**
     * Constructs a new tokenizer of the given file
     * @param inputFile The file to parse
     */
    public Tokenizer(File inputFile) throws IOException
    {
        reader = Files.newBufferedReader(inputFile.toPath());
        keywordsSet = new HashSet<>(Arrays.asList(keywords));
    }

    /**
     * Closes the tokenizer and the reading stream
     * @throws IOException in case of an IO error
     */
    void closeTokenizer() throws IOException
    {
        reader.close();
    }

    /**
     * Adds a string constant to the token queue
     * @throws IOException In case of an IO error
     */
    private void handleStringConst() throws IOException
    {
        char tempC = (char)reader.read();
        while(tempC != '\"'){
            tokenQueue += tempC;
            tempC = (char)reader.read();
        }
        currentStrVal = tokenQueue;
        tokenQueue = ""; //Empty queue
    }

    /**
     * Adds an integer constant to the token queue
     * @throws IOException In case of an IO error
     */
    private void handleIntConst() throws IOException
    {
        reader.mark(1);
        String tempC = String.valueOf((char)reader.read());
        Matcher intConstMatcher = DIGIT_PATTERN.matcher(tempC);
        while(intConstMatcher.matches()){
            reader.mark(1);
            tokenQueue += tempC;
            tempC = String.valueOf((char)reader.read());
            intConstMatcher = DIGIT_PATTERN.matcher(tempC);
        }
        reader.reset();
        currentIntVal = Integer.valueOf(tokenQueue);
        tokenQueue = ""; //Empty queue
    }

    /**
     * Adds an identifier or a keyword to the token queue
     * @throws IOException In case of an IO error
     */
    private void handleWord() throws IOException
    {
        reader.mark(1);
        String tempC = String.valueOf((char)reader.read());
        Matcher wordMatcher = WORD_PATTERN.matcher(tempC);
        while(wordMatcher.matches()){
            reader.mark(1);
            tokenQueue += tempC;
            tempC = String.valueOf((char)reader.read());
            wordMatcher = WORD_PATTERN.matcher(tempC);
        }
        reader.reset();
        if(keywordsSet.contains(tokenQueue)){
            currType = KEYWORD_TOKEN;
            currentKeyword = tokenQueue;
        } else {
            currType = IDENTIFIER_TOKEN;
            currentIdentifier = tokenQueue;
        }
        tokenQueue = ""; //Empty queue
    }

    /**
     * Throws away comments that ends at the end of the line
     */
    private void handleEolComment() throws IOException
    {
        String tempC = String.valueOf((char)reader.read());
        while(!tempC.equals("\n")){
            tempC = String.valueOf((char)reader.read());

        }
    }

    /**
     * Throws away comments that ends with: '*' followed by '/'
     */
    private void handleComment() throws IOException
    {
        String tempC = String.valueOf((char)reader.read());
        boolean endOfComment = false;
        while(!endOfComment){
            if(tempC.equals("*")){
                tempC = String.valueOf((char)reader.read());
                if(tempC.equals("/")){
                    endOfComment = true;
                }
            }
            if(!endOfComment && !tempC.equals("*")){
                tempC = String.valueOf((char)reader.read());
            }
        }
    }

    /**
     * @return True iff there are more tokens in the file
     */
    boolean hasMoreTokens() throws IOException
    {
        if(reachedEOF){
            return false;
        }
        if(tokenQueue != null  && !tokenQueue.equals("")){ // There is a token waiting in queue
            return true;
        } // No token is waiting in queue - attempt to add a valid token
        boolean foundToken = false;
        while(!foundToken) {
            int temp = reader.read(); // read next character
            if (temp == -1) { // read returned -1 -> EOF reached
                reachedEOF = true;
                break;
            }
            String tempS = String.valueOf((char)temp);
            Matcher spaceMatcher = Pattern.compile("\\s").matcher(tempS);
            if (spaceMatcher.matches()) { // character is a space character and should be ignored
                foundToken = false;
            } else if (tempS.equals("/")) { // this maybe the beginning of a comment
                reader.mark(1); // mark the position in the stream
                tempS = String.valueOf((char)reader.read()); // read the next char
                switch (tempS){
                    case "/":
                        handleEolComment();
                        break;
                    case "*":
                        handleComment();
                        break;
                    default: // This is not the beginning  of a comment
                        tokenQueue = "/"; // the token is "/"
                        reader.reset(); // return to the position before reading tempS
                        foundToken = true;
                }
            } else { //token is valid
                tokenQueue = tempS;
                foundToken = true;
            }
        }
        return !reachedEOF;
    }

    /**
     * Reads the next token in the input file and updates the tokenizer's fields accordingly
     * This method doesn't handle unrecognized tokens!
     */
    void advance() throws IOException
    {
        if((tokenQueue == null  || tokenQueue.equals("")) && !hasMoreTokens()) { // Sanity check
            System.out.println("advance() can only be called after hasMoreTokens()");
        }
        if(reachedEOF){ // Sanity check
            return;
        }
        String tempC = tokenQueue;
        tokenQueue = ""; // empty queue
        Matcher intConstMatcher = DIGIT_PATTERN.matcher(tempC);
        Matcher wordMatcher = WORD_BEGINNING_PATTERN.matcher(tempC);
        Matcher symbolMatcher = SYMBOL_PATTERN.matcher(tempC);
        if(tempC.equals("\"")){
            currType = STRING_CONST_TOKEN;
            handleStringConst();
        } else if(intConstMatcher.matches()){
            currType = INT_CONST_TOKEN;
            tokenQueue += tempC;
            handleIntConst();
        } else if(wordMatcher.matches()){
            tokenQueue += tempC;
            handleWord();
        } else if(symbolMatcher.matches()){
            currType = SYMBOL_TOKEN;
            currentSymbol = tempC.charAt(0);
        } // This method doesn't handle unrecognized characters!
    }

    /**
     * @return The identifier of the current token
     */
    String getCurrentIdentifier() {
        return currentIdentifier;
    }

    /**
     * @return The symbol of the current token
     */
    char getCurrentSymbol() {
        return currentSymbol;
    }

    /**
     * @return The keyword of the current token
     */
    String getCurrentKeyword(){
        return currentKeyword;
    }

    /**
     * @return The integer value of the current token
     */
    int getCurrentIntVal(){
        return currentIntVal;
    }

    /**
     * @return The string value of the current token
     */
    String getCurrentStrVal(){
        return currentStrVal;
    }

    /**
     * @return The type of the current token
     */
    String getTokenType(){
        return currType;
    }
}