import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This class writes VM commands into a file. It encapsulates the VM command
 * syntax.
 */
public class VMWriter {

    /********************* Constants ************************/
    private static final String INPUT_FILE_SUFFIX = ".jack";
    private static final String OUTPUT_FILE_SUFFIX = ".vm";

    private static final String RETURN = "return";
    private static final String FUNCTION = "function ";
    private static final String CALL = "call ";
    private static final String IF_GOTO = "if-goto ";
    private static final String GOTO = "goto ";
    private static final String LABEL = "label ";
    private static final String POP = "pop ";
    private static final String PUSH = "push ";
    private static final Hashtable<String, String> arithTranslatorHashTable =
            new Hashtable<>();
    private static final Hashtable<String, String> segTranslatorHashTable =
            new Hashtable<>();
    private static final ArrayList<String> segTranslatorList = new ArrayList<>();

    static {
        arithTranslatorHashTable.put("<", "lt");
        arithTranslatorHashTable.put(">", "gt");
        arithTranslatorHashTable.put("=", "eq");
        arithTranslatorHashTable.put("+", "add");
        arithTranslatorHashTable.put("~", "not");
        arithTranslatorHashTable.put("|", "or");
        arithTranslatorHashTable.put("&", "and");
        arithTranslatorHashTable.put("-", "sub");
    }

    static {
        segTranslatorHashTable.put("var", "local");
        segTranslatorHashTable.put("field", "this");
        segTranslatorHashTable.put("arg", "argument");
    }

    static {
        segTranslatorList.add("static");
        segTranslatorList.add("local");
        segTranslatorList.add("temp");
        segTranslatorList.add("this");
        segTranslatorList.add("that");
        segTranslatorList.add("argument");
        segTranslatorList.add("constant");
        segTranslatorList.add("pointer");
    }

    /******************** Data Members *********************/
    private PrintWriter writer;


    /**
     * Creates a new file and prepares it for writing VM commands
     *
     * @param output - output file for VM code
     * @throws IOException
     */
    public VMWriter(File output) throws IOException {
        if (output.isDirectory()) {
            writer = new PrintWriter(Files.newBufferedWriter(Paths.get(output.toPath().toAbsolutePath().resolve(output.getName() + ".vm").toString())));
        } else {
            writer = new PrintWriter(Files.newBufferedWriter(Paths.get(output.getPath().replace(INPUT_FILE_SUFFIX, OUTPUT_FILE_SUFFIX))));
        }
    }

    /**
     * Closes the output file.
     */
    public void close() {
        writer.close();
    }

    /**
     * Writes a VM push command
     *
     * @param segment - string: CONST, ARG, LOCAL, STATIC, THIS,THAT, POINTER, TEMP
     * @param index   - int.
     */
    void writePush(String segment, int index) {
        if (segTranslatorList.contains(segment)) {
            writer.println(PUSH + segment + " " + index);
        } else if (segTranslatorHashTable.containsKey(segment)) {
            writer.println(PUSH + segTranslatorHashTable.get(segment) + " " + index);
        }
    }

    /**
     * Writes a VM pop command.
     *
     * @param segment - string: CONST, ARG, LOCAL, STATIC, THIS,THAT, POINTER, TEMP
     * @param index   - int.
     */
    void writePop(String segment, int index) {
        if (segTranslatorList.contains(segment)) {
            writer.println(POP + segment + " " + index);
        } else if (segTranslatorHashTable.containsKey(segment)) {
            writer.println(POP + segTranslatorHashTable.get(segment) + " " + index);
        }
    }

    /**
     * Writes a VM arithmetic command
     *
     * @param command - VM arithmetic command.
     */
    void WriteArithmetic(String command) {
        if (command.equals("neg")) {
            writer.println(command);
        } else if (arithTranslatorHashTable.containsKey(command)) {

            writer.println(arithTranslatorHashTable.get(command));
        }
    }

    /**
     * Writes a VM label command.
     *
     * @param label
     */
    void WriteLabel(String label) {
        writer.println(LABEL + label);
    }

    /**
     * Writes a VM label command
     *
     * @param label
     */
    void WriteGoto(String label) {
        writer.println(GOTO + label);
    }

    /**
     * Writes a VM If-goto command
     *
     * @param label
     */
    void WriteIf(String label) {
        writer.println(IF_GOTO + label);
    }

    /**
     * Writes a VM call command/
     *
     * @param name
     * @param nArgs
     */
    void writeCall(String name, int nArgs) {
        writer.println(CALL + name + " " + nArgs);
    }

    /**
     * Writes a VM function command.
     *
     * @param name
     * @param nLocals
     */
    void writeFunction(String name, int nLocals) {
        writer.println(FUNCTION + name + " " + nLocals);
    }

    /**
     * Writes a VM return command
     */
    void writeReturn() {
        writer.println(RETURN);
    }


}
