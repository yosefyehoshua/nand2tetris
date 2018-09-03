/**
 * An analyzer for JACK programs
 */
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

public class JackCompiler {

    // Constants:
    /** File suffix */
    private static final String JACK_SUFFIX = ".jack";
    private static final String VM_SUFFIX = ".vm";

    // Methods:
    static void fooAdd(int i) {
        i= 100;
    }
    /**
     * The entry gate to the Jack compiler, compiling the given .jack file (or all .jack files in a given
     * folder).
     * @param args Command line arguments - containing a path to a .jack file or a folder
     * @throws IOException in case od an IO error
     */
    public static void main(String[] args) throws IOException
    {
        int i = 0;
        fooAdd(i);
        System.out.println(i);
    }
}
