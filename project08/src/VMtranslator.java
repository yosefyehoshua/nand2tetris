import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * runs all the program, execute the translation and writes a translated file
 */
public class VMtranslator {

    public static void main(String[] args) throws IOException {
        File input = new File(Paths.get(args[0]).toAbsolutePath().toString());
        File file;

        if (!input.isDirectory() && input.isFile() && input.getName().endsWith(".vm")) {
            CodeWriter writer = new CodeWriter(input);
            writer.writeInit();
            translator(writer, input);

        } else if (input.isDirectory()) {
            String[] filesArray;
            filesArray = input.list();
            CodeWriter writer = new CodeWriter(input);
            writer.writeInit();
            for (String fileName : filesArray) {

                if (fileName.endsWith(".vm")) {
                    file = new File(input.toPath().toAbsolutePath().resolve(fileName).toString());
                    translator(writer, file);
                }
            }
            writer.close();
        }
    }

    /**
     * this func. gets an input file and is used to activate the Parser
     * and CodeWriter to create an output traslated file.
     *
     * @param inputFile - input file
     * @throws IOException
     */
    private static void translator(CodeWriter writer, File inputFile) throws IOException {
        Parser fileParser = new Parser(inputFile);

        writer.setFileName(inputFile.getName().replace(".vm", ""));
        writer.printCommendOrComment("Current File: " + writer.fileName, CodeWriter.PRINT_COMMENT);

        String line;
        while (fileParser.hasMoreCommends()) {
            line = fileParser.adanvce();
            fileParser.lineAnalizer(line); // updates parser fields
            int CommendType = fileParser.getCommendType();

            if (CommendType == Parser.C_ARITHMETIC) {
                writer.writeArithmetic(fileParser.getArg1());

            } else if (CommendType == Parser.C_PUSH || CommendType == Parser.C_POP) {
                writer.writePushPop(CommendType, fileParser.getArg1(), fileParser.getArg2());

            } else if (CommendType == Parser.C_FUNC) {
                writer.writeFunction(fileParser.getArg1(), fileParser.getArg2());

            } else if (CommendType == Parser.C_IF_GOTO) {
                writer.writeIf(fileParser.getArg1());

            } else if (CommendType == Parser.C_LABEL) {
                writer.writeLabel(fileParser.getArg1());

            } else if (CommendType == Parser.C_RETURN) {
                writer.writeReturn();

            } else if (CommendType == Parser.C_COMMENT) {
            } else if (CommendType == Parser.C_GOTO) {
                writer.writeGoto(fileParser.getArg1());

            } else if (CommendType == Parser.C_CALL) {
                writer.writeCall(fileParser.getArg1(), fileParser.getArg2());
            }

        }
        fileParser.close();
    }


}
