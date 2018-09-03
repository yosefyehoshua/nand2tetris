import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * this class obj get an input file "XXX.asm", with symbolic hack language instructions, and output a binary
 * translation of
 * this file "XXX.hack"
 */
public class Assembler {


    /****************************
     * Constants
     *********************************/
    private static final String[] PRE_DEFINED_SYMBOLS = {"SP", "LCL", "ARG", "THIS", "THAT", "R0", "R1", "R2",
            "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15", "SCREEN",
            "KEYBOARD"};
    // each address corresponds to the same values in PRE_DEFINED_SYMBOLS array
    private static final Integer[] PRE_DEFINED_SYMBOLS_ADDRESS = {0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12,
            13, 14, 15, 16384, 24576};

    private static final int INIT_ROM = 0;

    private static final int A_TYPE = 0;
    private static final int C_TYPE = 1;
    private static final int L_TYPE = 2;
    /***************************
     * Data Member
     *******************************/
    private SymbolTable symbolTable;
    private Code code = new Code();

    /**
     * Assembler obj constructor
     */
    public Assembler() {
        symbolTable = new SymbolTable(); // initializing symbol table with pre defined symbols
        for (int i = 0; i < PRE_DEFINED_SYMBOLS.length; i++) {
            symbolTable.add(PRE_DEFINED_SYMBOLS[i], PRE_DEFINED_SYMBOLS_ADDRESS[i]);
        }
    }

    /**
     * this function runs all the prosses, all classes and functions, its the entry gate to the assembler.
     * it gets input files XXX.asm and with the help of Parser, Code and SymbolTable classes, translate the
     * written in hack language to binary code and outputs this in a file XXX.hack, in the same directory
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File input = new File(Paths.get(args[0]).toAbsolutePath().toString());
        File file;

        if (!input.isDirectory() && input.isFile() && input.getName().endsWith(".asm")) {
            Assembler assembler = new Assembler();
            assembler.firstParse(input);
            assembler.secondParse(input);

        } else if (input.isDirectory()) {
            String[] filesArray = input.list();

            for (String fileName : filesArray) {

                if (fileName.endsWith(".asm")) {
                    Assembler assembler = new Assembler();
                    file = new File(input.toPath().toAbsolutePath().resolve(fileName).toString());
                    assembler.firstParse(file);
                    assembler.secondParse(file);
                }
            }
        }
    }

    /**
     * this func parses the file for the first time ignoring all commends except label symbols - (XXX), and
     * add them to a symbol table.
     *
     * @param inputFile - XXX.asm file
     * @throws IOException
     */
    private void firstParse(File inputFile) throws IOException {

        Parser fileParser = new Parser(inputFile);
        String line = fileParser.readLine();
        int romAddress = INIT_ROM;
        while (line != null) {

            fileParser.lineAnalizer(line);
            String symbol = fileParser.getLineSymbol();
            int type = fileParser.getInstructionType();

            if (type == A_TYPE) {
                romAddress++;
            } else if (type == C_TYPE) {
                romAddress++;
            } else if (type == L_TYPE) {
                if (!symbolTable.contains(symbol)) {
                    symbolTable.add(symbol, romAddress);
                }
            } else {
                line = fileParser.readLine();
                continue;
            }
            line = fileParser.readLine();
        }
        fileParser.close();

    }

    /**
     * this func parses the input file after firstParse(), add symbol to symbol table, sends read line for
     * translation and write the translated file.
     *
     * @param inputFile - XXX.asm file
     * @throws IOException
     */
    private void secondParse(File inputFile) throws IOException {
        Parser fileParser = new Parser(inputFile);
        String line = fileParser.readLine();

        String outputName = inputFile.getPath().replace(".asm", ".hack");
        Path outputPath = Paths.get(outputName);
        PrintWriter out = new PrintWriter(Files.newBufferedWriter(outputPath));

        String lineOutput;
        Integer n = 16;
        while (line != null) {
            fileParser.lineAnalizer(line); // updates parser fields
            String symbol = fileParser.getLineSymbol();
            int type = fileParser.getInstructionType();


            if (type == A_TYPE) {
                if (fileParser.isAaNumber(symbol)) { // checks if A instruction in @INTEGER
                    lineOutput = code.translator(new String[]{symbol}, A_TYPE);

                } else { // else - is a symbol
                    if (!symbolTable.contains(symbol)) {
                        symbolTable.add(symbol, n);
                        lineOutput = code.translator(new String[]{n.toString()}, A_TYPE);
                        n++;
                    } else {
                        lineOutput = code.translator(new String[]{symbolTable.get(symbol).toString()}, A_TYPE);
                    }

                }
            } else if (type == C_TYPE) {

                String[] cArray = {fileParser.getDest(), fileParser.getComp(), fileParser.getJump()};
                lineOutput = code.translator(cArray, C_TYPE);


            } else {
                line = fileParser.readLine();
                continue;
            }
            out.println(lineOutput);
            line = fileParser.readLine();
        }
        fileParser.close();
        out.close();
    }
}



