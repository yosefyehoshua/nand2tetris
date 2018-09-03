import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class represent a parser object. this object perses a file analyze and classifies the lines it
 * reads to A, C or L instraction and update its fields accordingly
 */
public class Parser {
    /***************************
     * Regexes
     ********************************/
    private static final String COMMENT = "\\s*((?://.*))*";
    private static final String ADDRESS = "\\s*@\\s*(?<address>\\S+)";
    private static final String INT_ADDRESS = "(?<intaddress>\\d+)";
    private static final String DEST = "(?<dest>[AMD]*)";
    private static final String COMP = "(?<comp>[AMD]?[-!]?\\s*[AMD01]\\s*(?:[-&+|]|<<|>>)?\\s*[AMD1]?)";
    private static final String JUMP = "(?<jump>J[MELGN][PQTE])";
    private static final String LABEL = "(?<label>\\S+)";
    private static final String A_INSTRUCTION = ADDRESS + COMMENT;
    private static final String C_INSTRUCTION = "\\s*(?:" + DEST + "\\s*=)?\\s*" + COMP + "\\s*" + "(?:;\\s*"
            + JUMP + ")?" + COMMENT;
    private static final String L_INSTRUCTION = "\\(" + LABEL + "\\)" + COMMENT;

    /***************************
     * Patterns
     ********************************/
    private static final Pattern A_INSTRUCTION_PATTERN = Pattern.compile(A_INSTRUCTION);
    private static final Pattern C_INSTRUCTION_PATTERN = Pattern.compile(C_INSTRUCTION);
    private static final Pattern L_INSTRUCTION_PATTERN = Pattern.compile(L_INSTRUCTION);
    private static final Pattern IF_ALL_INT_PATTERN = Pattern.compile(INT_ADDRESS);

    /***************************
     * Constants
     ******************************/
    private static final String ADDRESS_GROUP = "address";
    private static final String DEST_GROUP = "dest";
    private static final String COMP_GROUP = "comp";
    private static final String JUMP_GROUP = "jump";
    private static final String LABEL_GROUP = "label";
    private static final int A_TYPE = 0;
    private static final int C_TYPE = 1;
    private static final int L_TYPE = 2;
    private static final String EMPTY_STR = "";
    public Code code;
    /***************************
     * Data Members
     ***************************/
    private BufferedReader reader;
    private String lineSymbol = null;
    private int instructionType = -1;
    private String dest = EMPTY_STR, comp = EMPTY_STR, jump = EMPTY_STR;


    /**
     * A constructor of Parser object
     *
     * @param inputFile - File object of the input text file
     * @throws IOException - for in valid input file
     */
    public Parser(File inputFile) throws IOException {
        reader = Files.newBufferedReader(inputFile.toPath());
        code = new Code();
    }

    /********************************
     * Getters
     **************************/
    public String getLineSymbol() {
        return lineSymbol;
    }

    public int getInstructionType() {
        return instructionType;
    }

    public String getDest() {
        return dest;
    }

    public String getComp() {
        return comp;
    }

    /********************************* Methods **************************/

    public String getJump() {
        return jump;
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
    String readLine() throws IOException {
        return reader.readLine();
    }

    /**
     * this func checks if a symbol (string) is an integer value (such as "1","2" etc.)
     *
     * @param aSymbol string - A instruction symbol
     * @return True/False whether the sring has an integer value
     */
    boolean isAaNumber(String aSymbol) {
        Matcher intAinsruction = IF_ALL_INT_PATTERN.matcher(aSymbol);
        return intAinsruction.matches();
    }

    /**
     * this func gets an line - string from a reade file, analyze it and updates Parser instructions field
     * accordingly
     *
     * @param line string
     */
    public void lineAnalizer(String line) {

        Matcher Ainsruction = A_INSTRUCTION_PATTERN.matcher(line);
        Matcher Cinsruction = C_INSTRUCTION_PATTERN.matcher(line);
        Matcher Linsruction = L_INSTRUCTION_PATTERN.matcher(line);

        if (Ainsruction.matches()) {
            lineSymbol = Ainsruction.group(ADDRESS_GROUP).replaceAll("\\s+", "");
            instructionType = A_TYPE;

        } else if (Cinsruction.matches()) {
            lineSymbol = null;
            instructionType = C_TYPE;

            dest = EMPTY_STR;
            comp = EMPTY_STR;
            jump = EMPTY_STR; // resetting c fields

            if (Cinsruction.group(DEST_GROUP) != null) {
                dest = Cinsruction.group(DEST_GROUP).replaceAll("\\s+", "");
            }
            if (Cinsruction.group(COMP_GROUP) != null) {
                comp = Cinsruction.group(COMP_GROUP).replaceAll("\\s+", "");
            }
            if (Cinsruction.group(JUMP_GROUP) != null) {
                jump = Cinsruction.group(JUMP_GROUP).replaceAll("\\s+", "");
            }
        } else if (Linsruction.matches()) {
            lineSymbol = Linsruction.group(LABEL_GROUP);
            instructionType = L_TYPE;
        }
    }
}
