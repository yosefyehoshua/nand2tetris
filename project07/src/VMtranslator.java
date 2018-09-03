import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * runs all the program, execute the translation and writes a translated file
 */
public class VMtranslator {

    public static void main(String[] args) throws IOException {
        File input = new File(Paths.get(args[0]).toAbsolutePath().toString());
        File file;

        if (!input.isDirectory() && input.isFile() && input.getName().endsWith(".vm")) {
            VMtranslator vmtranslator = new VMtranslator();
            vmtranslator.translator(input);

        } else if (input.isDirectory()) {
            String[] filesArray = input.list();

            for (String fileName : filesArray) {

                if (fileName.endsWith(".vm")) {
                    VMtranslator vmtranslator = new VMtranslator();
                    file = new File(input.toPath().toAbsolutePath().resolve(fileName).toString());
                    vmtranslator.translator(file);
                }
            }
        }
    }

    /**
     * this func. gets an input file and is used to activate the Parser and CodeWriter to create an output traslated file.
     *
     * @param inputFile - input file
     * @throws IOException
     */
    private void translator(File inputFile) throws IOException {
        Parser fileParser = new Parser(inputFile);
        CodeWriter writer = new CodeWriter(inputFile);
        String outputName = inputFile.getPath().replace(".vm", ".asm");
        writer.setFileName(inputFile.getName().replace(".vm", ""));

        Path outputPath = Paths.get(outputName);
        PrintWriter out = new PrintWriter(Files.newBufferedWriter(outputPath));

        String line;
        while (fileParser.hasMoreCommends()) {
            line = fileParser.adanvce();
            fileParser.lineAnalizer(line); // updates parser fields
            int CommendType = fileParser.getCommendType();

            switch (CommendType) {
                case Parser.C_ARITHMETIC:
                    writer.writeArithmetic(fileParser.getArg1());
                    break;
                case Parser.C_PUSH:
                case Parser.C_POP:
                    writer.writePushPop(CommendType, fileParser.getArg1(), fileParser.getArg2());
                    break;
            }

        }
        fileParser.close();
        writer.close();
    }


}
