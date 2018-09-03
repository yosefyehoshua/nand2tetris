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
    /*********************** Regex *************************/
    private static final String ARITHMETIC = "\\s*(?<arith>\\w+)\\s*((?://.*))*";
    private static final String PUSH = "\\s*(?<push>push)\\s*(?<seg>\\w+)\\s*(?<i>\\d+)\\s*((?://.*))*";
    private static final String POP = "\\s*(?<pop>pop)\\s*(?<seg>\\w+)\\s*(?<i>\\d+)\\s*((?://.*))*";
    private static final String COMMENT = "\\s*((?://.*))*";
    /*********************** Pattrens **********************/
    private static final Pattern ARITHMETIC_PATTERN = Pattern.compile(ARITHMETIC);
    private static final Pattern PUSH_PATTERN = Pattern.compile(PUSH);
    private static final Pattern POP_PATTERN = Pattern.compile(POP);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(COMMENT);
    /*********************** Constants **********************/
    private static final String ARITHMETIC_GROUP = "arith";
    private static final String MEM_SEGMENT_GROUP = "seg";
    private static final String INDEX_GROUP = "i";

    /*********************** Data Members ******************/
    private final BufferedReader reader;
    private String currentLine = null;
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
     * @param line string
     */
    public void lineAnalizer(String line) {

        Matcher ArithInsruction = ARITHMETIC_PATTERN.matcher(line);
        Matcher PushnInsruction = PUSH_PATTERN.matcher(line);
        Matcher PopInsruction = POP_PATTERN.matcher(line);
        Matcher commentMach = COMMENT_PATTERN.matcher(line);

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
        }
    }


}
