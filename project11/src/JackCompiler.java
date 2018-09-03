import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * The compile program operates on a given source, where source is either a file name of the form
 * Xxx.jack or a directory name containing one or more such files. For each source Xxx.jack file, the
 * analyzer goes through the following logic:
 * 1. Create a JackTokenizer from the Xxx.jack input file;
 * 2. Create an output file called Xxx.vm ;
 * 3. Use the CompilationEngine to compile the input JackTokenizer into the output
 * file.
 */
public class JackCompiler {


    public static void main(String[] args) throws IOException {
        File input = new File(Paths.get(args[0]).toAbsolutePath().toString());
        File file;

        if (!input.isDirectory() && input.isFile() && input.getName().endsWith(".jack")) {
            CompilationEngine engine = new CompilationEngine(input, input);
            engine.compileClass();
            engine.close();

        } else if (input.isDirectory()) {
            String[] filesArray;
            filesArray = input.list();
            CompilationEngine engine = null;
            assert filesArray != null;
            for (String fileName : filesArray) {

                if (fileName.endsWith(".jack")) {
                    file = new File(input.toPath().toAbsolutePath().resolve(fileName).toString());
                    engine = new CompilationEngine(file, file);
                    engine.compileClass();
                    engine.close();
                }
            }
            assert engine != null;
            engine.close();
        }
    }


}
