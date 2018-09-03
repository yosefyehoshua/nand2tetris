import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes all comments and white space from the input stream and breaks it
 * into Jacklanguage tokens, as specified by the Jack grammar.
 */
public class JackTokenizer {

    /****************** Token Types *******************************/
    static final String KEYWORD_TOKEN_TYPE = "keyword";
    static final String SYMBOL_TOKEN_TYPE = "symbol";
    static final String IDENTIFIER_TOKEN_TYPE = "identifier";
    static final String INT_CONST_TOKEN_TYPE = "integerConstant";
    static final String STRING_CONST_TOKEN_TYPE = "stringConstant";

    private static final String KEYWORD = "static|void|method|var|constructor|false|this|do|while|int|boolean|field|null|else|function|char|true|let|class|if|return|";
    private static final String SYMBOL = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
    private static final String INT_CONST = "\\d+";
    private static final String STRING_CONST = "\"[^\"\n]*\"";
    private static final String IDENTIFIER = "[\\w_]++";
    private static final String TOTAL =  IDENTIFIER +"|" +  KEYWORD +SYMBOL + "|" + INT_CONST + "|" + STRING_CONST ;
    private static final String TOTALL = "static|void|method|var|constructor|false|this|do|while|int|boolean|field|null|else|function|char|true|let|class|if|return|[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]|\\d+|\"[^\"\n]*\"|[\\w_]+";

    private static final Pattern TOKENS_PATTERN = Pattern.compile(TOTAL);
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(KEYWORD);
    private static final Pattern SYMBOL_PATTERN = Pattern.compile(SYMBOL);
    private static final Pattern STRING_CONST_PATTERN = Pattern.compile(STRING_CONST);
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER);
    private static final Pattern INT_CONST_PATTERN = Pattern.compile(INT_CONST);
    private static final String ERROR_MESSAGE = "Function called for the wrong type! expected: ";
    private static final String ERROR_MESSAGE_GOT = "got: ";


    /********************** Data Members **************************/
    private final BufferedReader reader;
    public String currType;
    public String currentToken;
    private String currentLine;
    private int currentTokenNumber = 0;
    private Hashtable<Integer, String> tokensHashTable;


    /**
     * Opens the input file/stream and gets ready to tokenize it.
     *
     * @param inputFile - input file
     * @throws IOException
     */
    public JackTokenizer(File inputFile) throws IOException {
        reader = Files.newBufferedReader(inputFile.toPath());
        tokensHashTable = new Hashtable<>();
        fillHashTable();
    }


    /**
     * fills the token hashTable with tokens from the file.
     * each key is the number of token in the file,
     * the function also updates the total number of tokens in the file
     *
     * @throws IOException - by reader
     */
    private void fillHashTable() throws IOException {
        Integer currTokenNumber = 0;
        while (hasMoreLines()) { // parse over file
            currentLine = trimCommentsAndSpaces(reader.readLine());
            Matcher tokenMatcher = TOKENS_PATTERN.matcher(currentLine);

            // parse a line and adds token to hashTable
            while (tokenMatcher.find()) if (!tokenMatcher.group().equals("")) {
                tokensHashTable.put(currTokenNumber, tokenMatcher.group());
                currTokenNumber++;

            }
        }

    }


    /**
     * Closes the JackTokenizer reading stream
     *
     * @throws IOException
     */
    void close() throws IOException {
        reader.close();
    }


    /**
     * returns a boolean for if we have more tokens in the input
     *
     * @return true/false.
     * @throws IOException
     */
    boolean hasMoreTokens() throws IOException {
        if (currentTokenNumber < tokensHashTable.size()) {
            return true;
        } else return false;
    }

    /**
     * Gets the next token from the input and makes it the current token.
     * This method should only be called if hasMoreTokens is true.
     * Initially there is no current token.
     */
    void advance() throws IOException {
        if (hasMoreTokens()) {

            String tempToken = tokensHashTable.get(currentTokenNumber);
            Matcher keywordMatcher = KEYWORD_PATTERN.matcher(tempToken);
            Matcher symbolMatcher = SYMBOL_PATTERN.matcher(tempToken);
            Matcher stringConstMatcher = STRING_CONST_PATTERN.matcher(tempToken);
            Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(tempToken);
            Matcher intCostMatcher = INT_CONST_PATTERN.matcher(tempToken);

            if (keywordMatcher.matches()) {

                currType = KEYWORD_TOKEN_TYPE;
                currentToken = tempToken;
            } else if (stringConstMatcher.matches()) {
                currType = STRING_CONST_TOKEN_TYPE;
                currentToken = tempToken;
            } else if (intCostMatcher.matches()) {
                currType = INT_CONST_TOKEN_TYPE;
                currentToken = tempToken;
            } else if (identifierMatcher.matches()) {
                currType = IDENTIFIER_TOKEN_TYPE;
                currentToken = tempToken;
            } else if (symbolMatcher.matches()) {
                currType = SYMBOL_TOKEN_TYPE;
                currentToken = tempToken;
            }

            currentTokenNumber++; // advance token cursor


        }
    }

    /**
     * delete comments and leading and trailing white spaces from a given line,
     * and updates the inCommentScope accordingly.
     *
     * @param line - string of the line currently read
     * @return line without comments and leading and trailing white spaces
     */
    private String trimCommentsAndSpaces(String line) {
        line = line.trim(); // trim the outlines spaces
        int start = line.indexOf("\"");
        int end = line.lastIndexOf("\"");
        int lineCommentIndex = line.indexOf("//");


        if (lineCommentIndex != -1 && lineCommentIndex < start || lineCommentIndex > end) {
            line = line.substring(0, lineCommentIndex); // delete line comment
        } else {
            int lineBlockIndex = line.indexOf("/*");
            if (lineBlockIndex != -1 && lineBlockIndex < start || lineBlockIndex > end) {
                line = line.substring(0, lineBlockIndex); // delete method/func comment
            } else {
                int endLineBlockIndex = line.indexOf("*/");
                if (endLineBlockIndex != -1 && endLineBlockIndex < start || endLineBlockIndex > end) {
                    line = "";
                } else {
                    int middleBlockIndex = line.indexOf("*");
                    if (middleBlockIndex == 0) {
                        line = "";
                    }
                }

            }


        }
        return line;

    }

    /**
     * Returns the type of the current token.
     *
     * @return String - type of the current token.
     */
    String tokenType() {
        return currType;
    }


    /**
     * Returns the keyword which is the current token.
     * Should be called only when tokenType is KEYWORD.
     *
     * @return String -  keyword which is the current token.
     */
    String keyWord() {
        if (currType.equals(KEYWORD_TOKEN_TYPE)) return currentToken;
        else
            throw new IllegalArgumentException(ERROR_MESSAGE + KEYWORD_TOKEN_TYPE + ERROR_MESSAGE_GOT + currType);
    }

    /**
     * Returns the character which is the current token.
     * Should be called only when tokenType is SYMBOL.
     *
     * @return char - which is the current token.
     */
    char currentSymbol() {

        if (currType.equals(SYMBOL_TOKEN_TYPE)) return currentToken.charAt(0);
        else
            throw new IllegalArgumentException(ERROR_MESSAGE + STRING_CONST_TOKEN_TYPE + ERROR_MESSAGE_GOT + currType);

    }

    /**
     * Returns the identifier which is the current token.
     * Should be called only when tokenType is IDENTIFIER
     *
     * @return String - identifier which is the current token.
     */
    String identifier() {
        if (currType.equals(IDENTIFIER_TOKEN_TYPE)) return currentToken;
        else
            throw new IllegalArgumentException(ERROR_MESSAGE + IDENTIFIER_TOKEN_TYPE + ERROR_MESSAGE_GOT + currType);
    }


    /**
     * Returns the integer value of the current token.
     * Should be called only when tokenType is INT_CONST
     *
     * @return int - value of the current token.
     */
    int intVal() {
        if (currType.equals(INT_CONST_TOKEN_TYPE))
            return Integer.valueOf(currentToken);
        else
            throw new IllegalArgumentException(ERROR_MESSAGE + INT_CONST_TOKEN_TYPE + ERROR_MESSAGE_GOT + currType);
    }

    /**
     * Returns the string value of the current token, without the
     * double quotes. Should be called only when tokenType is STRING_CONST.
     *
     * @return String - alue of the current token, without the double quotes
     */
    String stringVal() {
        if (currType.equals(STRING_CONST_TOKEN_TYPE)) return currentToken;
        else
            throw new IllegalArgumentException(ERROR_MESSAGE + STRING_CONST_TOKEN_TYPE + ERROR_MESSAGE_GOT + currType);
    }

    /**
     * checks if there is more lines to read in the input file
     *
     * @return result - boolean if there is more lines commends, etc. to read.
     */
    private boolean hasMoreLines() {
        boolean result = false;
        try {
            result = reader.ready();
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }


}
