import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class represent a parser object. this object parses a file analyze and
 * classifies the lines it, according to the read commend in the file
 */
public class Parser {
    protected static final int C_COMMENT = 0;
    protected static final int C_ARITHMETIC = 1;
    protected static final int C_PUSH = 2;
    protected static final int C_POP = 3;
    protected static final int C_GOTO = 4;
    protected static final int C_IF_GOTO = 5;
    protected static final int C_LABEL = 6;
    protected static final int C_CALL = 7;
    protected static final int C_FUNC = 8;
    protected static final int C_RETURN = 9;

    /*********************** Regex *************************/
    // VM part I
    private static final String ARITHMETIC = "\\s*(?<arith>\\w+)\\s*((?://.*))*";
    private static final String PUSH = "\\s*(?<push>push)\\s*(?<seg>\\w+)\\s*(?<i>\\d+)\\s*((?://.*))*";
    private static final String POP = "\\s*(?<pop>pop)\\s*(?<seg>\\w+)\\s*(?<i>\\d+)\\s*((?://.*))*";
    private static final String COMMENT = "\\s*((?://.*))*";
    // VM part II
    private static final String GOTO = "\\s*(?<command>goto)\\s+(?<label>\\S+)" + COMMENT;
    private static final String IF_GOTO = "\\s*(?<command>if-goto)\\s+(?<label>\\S+)" + COMMENT;
    private static final String RETURN = "\\s*(?<command>return)" + COMMENT;
    private static final String CALL = "\\s*(?<command>call)\\s+(?<func>\\S+)\\s+" +
            "(?<nArgs>\\d+)" + COMMENT;
    private static final String LABEL = "\\s*(?<command>label)\\s+(?<label>\\S+)" + COMMENT;
    private static final String FUNC = "\\s*(?<command>function)\\s+(?<funcName>\\S+)\\s+(?<nVars>\\d+)" + COMMENT;

    /*********************** Pattrens **********************/
    // VM part I
    private static final Pattern ARITHMETIC_PATTERN = Pattern.compile(ARITHMETIC);
    private static final Pattern PUSH_PATTERN = Pattern.compile(PUSH);
    private static final Pattern POP_PATTERN = Pattern.compile(POP);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(COMMENT);
    // VM part II
    private static final Pattern IF_GOTO_PATTERN = Pattern.compile(IF_GOTO);
    private static final Pattern RETURN_PATTERN = Pattern.compile(RETURN);
    private static final Pattern LABEL_PATTERN = Pattern.compile(LABEL);
    private static final Pattern FUNC_PATTERN = Pattern.compile(FUNC);
    private static final Pattern CALL_PATTERN = Pattern.compile(CALL);
    private static final Pattern GOTO_PATTERN = Pattern.compile(GOTO);


    /*********************** Constants **********************/
    // VM part I
    private static final String ARITHMETIC_GROUP = "arith";
    private static final String MEM_SEGMENT_GROUP = "seg";
    private static final String INDEX_GROUP = "i";
    // VM part II
    private static final String NAME_GROUP = "funcName";
    private static final String LABEL_GROUP = "label";
    private static final String NVARS_GROUP = "nVars";
    private static final String FUNC_GROUP = "func";
    private static final String NARGS_GROUP = "nArgs";
    /*********************** Data Members ******************/
    private final BufferedReader reader;
    private int commendType = -1;
    private String arg1 = null;
    private int arg2 = 0;


    /**
     * Constructor of class, gets input file & initialize reader
     *
     * @param inputFile The input File to parse on.
     */
    public Parser(File inputFile) throws IOException {
        reader = Files.newBufferedReader(inputFile.toPath());
    }


    /********************** Getters ***********************/
    public int getCommendType() {
        return commendType;
    }

    public String getArg1() {
        return arg1;
    }

    public int getArg2() {
        return arg2;
    }


    /**
     * closes reader - stop the Parser.
     *
     * @throws IOException
     */
    void close() throws IOException {
        reader.close();
    }

    /**
     * read a line in the input file and return this line as a String
     *
     * @return line (String)
     * @throws IOException
     */
    String adanvce() throws IOException {
        if (hasMoreCommends()) {
            return reader.readLine();
        }
        return null;
    }

    /**
     * checks if there is more lines to read in the input file
     *
     * @return result - boolean if there is more lines (commends, etc.) to read.
     */
    public boolean hasMoreCommends() {
        boolean result = false;

        try {
            result = reader.ready();
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }


    /**
     * this func gets an line - string from a reade file, analyze it and
     * updates Parser instructions field accordingly
     *
     * @param line string
     */
    public void lineAnalizer(String line) {
        // VM part I
        Matcher ArithInsruction = ARITHMETIC_PATTERN.matcher(line);
        Matcher PushnInsruction = PUSH_PATTERN.matcher(line);
        Matcher PopInsruction = POP_PATTERN.matcher(line);
        Matcher commentMach = COMMENT_PATTERN.matcher(line);
        // VM part II
        Matcher labelInsruction = LABEL_PATTERN.matcher(line);
        Matcher funcInsruction = FUNC_PATTERN.matcher(line);
        Matcher ifGotoInsruction = IF_GOTO_PATTERN.matcher(line);
        Matcher callInsruction = CALL_PATTERN.matcher(line);
        Matcher returnInsruction = RETURN_PATTERN.matcher(line);
        Matcher gotoInsruction = GOTO_PATTERN.matcher(line);


        if (ArithInsruction.matches()) {
            commendType = C_ARITHMETIC;
            arg1 = ArithInsruction.group(ARITHMETIC_GROUP);

        } else if (PushnInsruction.matches()) {
            commendType = C_PUSH;
            arg1 = PushnInsruction.group(MEM_SEGMENT_GROUP);
            arg2 = Integer.parseInt(PushnInsruction.group(INDEX_GROUP));

        } else if (PopInsruction.matches()) {
            commendType = C_POP;
            arg1 = PopInsruction.group(MEM_SEGMENT_GROUP);
            arg2 = Integer.parseInt(PopInsruction.group(INDEX_GROUP));
        } else if (commentMach.matches()) {
            arg1 = null;
            arg2 = 0;
            commendType = C_COMMENT;
        } else if (labelInsruction.matches()) {
            arg1 = labelInsruction.group(LABEL_GROUP);
            commendType = C_LABEL;
        } else if (funcInsruction.matches()) {
            commendType = C_FUNC;
            arg1 = funcInsruction.group(NAME_GROUP);
            arg2 = Integer.parseInt(funcInsruction.group(NVARS_GROUP));
        } else if (gotoInsruction.matches()) {
            commendType = C_GOTO;
            arg1 = gotoInsruction.group(LABEL_GROUP);
        } else if (ifGotoInsruction.matches()) {
            commendType = C_IF_GOTO;
            arg1 = ifGotoInsruction.group(LABEL_GROUP);
        } else if (callInsruction.matches()) {
            commendType = C_CALL;
            arg1 = callInsruction.group(FUNC_GROUP);
            arg2 = Integer.parseInt(callInsruction.group(NARGS_GROUP));

        }
        if (returnInsruction.matches()) {
            commendType = C_RETURN;

        }
    }


}
