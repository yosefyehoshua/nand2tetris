import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CodeWriter is the translation engine of VMtranslator.
 */
public class CodeWriter {

    /******************** Constants **************************/
    private static final String INPUT_FILE_SUFFIX = ".vm";
    private static final String OUTPUT_FILE_SUFFIX = ".asm";
    private static final int INT_CONST_UPPER_BOUND = 32768;

    private static final int C_PUSH = 2;
    private static final int C_POP = 3;


    private static final String SUB_COMMAND = "sub";
    private static final String ADD_COMMAND = "add";
    private static final String AND_COMMAND = "and";
    private static final String OR_COMMAND = "or";
    private static final String NEG_COMMAND = "neg";
    private static final String NOT_COMMAND = "not";
    private static final String GT_COMMAND = "gt";
    private static final String LT_COMMAND = "lt";
    private static final String EQ_COMMAND = "eq";

    private static final String LOCAL_POINTER = "LCL";
    private static final String ARGUMENT_POINTER = "ARG";
    private static final String THIS_POINTER = "THIS";
    private static final String THAT_POINTER = "THAT";

    private static final int TEMP_ADDRESS = 5;

    private static final String LOCAL = "local";
    private static final String ARGUMENT = "argument";
    private static final String THIS = "this";
    private static final String THAT = "that";
    private static final String STATIC = "static";
    private static final String TEMP = "temp";
    private static final String CONSTANT = "constant";
    private static final String POINTER = "pointer";

    private static final String DECREASE_SP = "@SP\n" + "M=M-1\n"; //SP--
    private static final String INCREASE_SP = "@SP\n" + "M=M+1\n"; //SP++
    private static final String POP_TO_D = DECREASE_SP + "@SP\n" + "A=M\n" + "D=M\n"; // D = stack.pop()
    private static final String PUSH_FROM_D = "@SP\n" + "A=M\n" + "M=D\n" + INCREASE_SP; // D = stack.push()


    private static final String SP_LAST_CURR = "@SP\n" + "A=M-1\n"; // takes SP to last value
    private static final String COMMENT_PREFIX = "// ";
    private static final int PRINT_COMMEND = 1;
    private static final int PRINT_COMMENT = 0;
    private static final String CONDITION_COMMEND_PARTIAL =
            POP_TO_D + // D = Y
                    "@R13\n" +
                    "M=D\n" + // R13 = y
                    "@R14\n" +
                    "M=D\n" + // R14 = y
                    "@R15\n" +
                    "M=D\n" + // R15 = y
                    POP_TO_D + // D = x

                    INCREASE_SP + // x is saved in stack
                    "@R13\n" +
                    "M=M|D\n" + // R13 = y or x
                    "@R14\n" +
                    "M=M&D\n" + // R14 = y and x
                    "D=!M\n" + // D = y and x
                    "@R13\n" +
                    "MD=M&D\n" + // R13 = D, D = y XOR x

                    POP_TO_D + // D = x
                    "@R14\n" +
                    "M=D\n" + // R14 = x
                    "@R13\n" +
                    "D=M\n";
    private static final String EQ_COMMEND_IN_ASSAMBLY_PARTIAL =
            POP_TO_D +
                    "@R13\n" +
                    "M=D\n" +
                    "@R14\n" +
                    "M=D\n" +
                    POP_TO_D +
                    "@R13\n" +
                    "M=M|D\n" +
                    "@R14\n" +
                    "M=M&D\n" +
                    "D=!M\n" +
                    "@R13\n" +
                    "MD=M&D\n";
    /**************************** Arithmetic Commends ******************/
    // all binary operation (+,-,and,or)
    private static final String ADD_COMMEND_IN_ASSAMBLY = POP_TO_D + SP_LAST_CURR + "M=M+D\n";
    private static final String SUB_COMMEND_IN_ASSAMBLY = POP_TO_D + SP_LAST_CURR + "M=M-D\n";
    private static final String AND_COMMEND_IN_ASSAMBLY = POP_TO_D + SP_LAST_CURR + "M=M&D\n";
    private static final String OR_COMMEND_IN_ASSAMBLY = POP_TO_D + SP_LAST_CURR + "M=M|D\n";
    // all unary operations (not x, -x)
    private static final String NEG_COMMEND_IN_ASSAMBLY = SP_LAST_CURR + "M=-M\n";
    private static final String NOT_COMMEND_IN_ASSAMBLY = SP_LAST_CURR + "M=!M\n";
    /******************** Push&Pop Commends ********************/


    private static final String POP_THIS_IN_ASSAMBLY =
            POP_TO_D +
                    "@" + THIS_POINTER + "\n" +
                    "M=D\n";
    private static final String POP_THAT_IN_ASSAMBLY =
            POP_TO_D +
                    "@" + THAT_POINTER + "\n" +
                    "M=D\n";
    private static final String PUSH_THIS_IN_ASSAMBLY =
            "@" + THIS_POINTER + "\n" +
                    "D=M\n" +
                    PUSH_FROM_D;
    private static final String PUSH_THAT_IN_ASSAMBLY =
            "@" + THAT_POINTER + "\n" +
                    "D=M\n" +
                    PUSH_FROM_D;
    private static Integer currentCondition = 0;
    /**************** Data Members **********************/
    private PrintWriter writer;
    private String fileName;


    public CodeWriter(File inputFile) throws IOException {

        writer = new PrintWriter(Files.newBufferedWriter(
                Paths.get(inputFile.getPath().replace(
                        INPUT_FILE_SUFFIX, OUTPUT_FILE_SUFFIX))));

    }

    /**
     * closes writer - stops the CodeWriter.
     *
     * @throws IOException
     */
    void close() throws IOException {
        writer.close();
    }


    /**
     * get a string comment that correlates with arithmetic commend and translate it to Hack language
     *
     * @param command a string of the current commend that is read/
     */
    public void writeArithmetic(String command) {
        printCommendOrComment(command, PRINT_COMMENT);
        switch (command) {
            case ADD_COMMAND:
                printCommendOrComment(ADD_COMMEND_IN_ASSAMBLY, PRINT_COMMEND);

                break;
            case SUB_COMMAND:
                printCommendOrComment(SUB_COMMEND_IN_ASSAMBLY, PRINT_COMMEND);

                break;
            case AND_COMMAND:
                printCommendOrComment(AND_COMMEND_IN_ASSAMBLY, PRINT_COMMEND);

                break;
            case OR_COMMAND:
                printCommendOrComment(OR_COMMEND_IN_ASSAMBLY, PRINT_COMMEND);

                break;
            case NOT_COMMAND:
                printCommendOrComment(NOT_COMMEND_IN_ASSAMBLY, PRINT_COMMEND);

                break;
            case NEG_COMMAND:
                printCommendOrComment(NEG_COMMEND_IN_ASSAMBLY, PRINT_COMMEND);

                break;
            case GT_COMMAND:
                printCommendOrComment(getConditionalCommend(currentCondition, 1), PRINT_COMMEND);
                currentCondition++;

                break;
            case LT_COMMAND:
                printCommendOrComment(getConditionalCommend(currentCondition, 0), PRINT_COMMEND);
                currentCondition++;


                break;
            case EQ_COMMAND:
                printCommendOrComment(getEqCommand(currentCondition), PRINT_COMMEND);
                currentCondition++;
                break;
        }

    }


    /**
     * get a string comment that correlates with Push or Pop commend and translate it to Hack language
     *
     * @param commandType string indicating the type of commend
     * @param segment     - the type of one of 8 segments.
     * @param index       - memory address in the given seg. type.
     */
    public void writePushPop(int commandType, String segment, int index) {
        if (commandType == C_PUSH) {
            writePush(segment, index);
        } else if (commandType == C_POP) {
            writePop(segment, index);
        }
    }

    /**
     * write all pop commends/
     *
     * @param segment - type of segment
     * @param idx     -  memory address in the given seg. type.
     */
    private void writePop(String segment, int idx) {
        printCommendOrComment("pop " + segment + " " + idx, PRINT_COMMENT);
        switch (segment) {
            case LOCAL:
                printCommendOrComment(popSeg(idx, LOCAL_POINTER), PRINT_COMMEND);
                break;
            case ARGUMENT:
                printCommendOrComment(popSeg(idx, ARGUMENT_POINTER), PRINT_COMMEND);
                break;
            case THIS:
                printCommendOrComment(popSeg(idx, THIS_POINTER), PRINT_COMMEND);
                break;
            case THAT:
                printCommendOrComment(popSeg(idx, THAT_POINTER), PRINT_COMMEND);
                break;
            case POINTER:
                if (idx == 0) {
                    printCommendOrComment(POP_THIS_IN_ASSAMBLY, PRINT_COMMEND);

                } else if (idx == 1) {
                    printCommendOrComment(POP_THAT_IN_ASSAMBLY, PRINT_COMMEND);
                }
                break;
            case TEMP:
                printCommendOrComment(popTemp(idx), PRINT_COMMEND);
                break;
            case STATIC:
                printCommendOrComment(popStatic(fileName, idx), PRINT_COMMEND);
                break;


        }
    }

    private void writePush(String segment, int idx) {
        printCommendOrComment("push " + segment + " " + idx, PRINT_COMMENT);
        switch (segment) {
            case LOCAL:
                printCommendOrComment(pushSeg(idx, LOCAL_POINTER), PRINT_COMMEND);
                break;
            case ARGUMENT:
                printCommendOrComment(pushSeg(idx, ARGUMENT_POINTER), PRINT_COMMEND);
                break;
            case THIS:
                printCommendOrComment(pushSeg(idx, THIS_POINTER), PRINT_COMMEND);
                break;
            case THAT:
                printCommendOrComment(pushSeg(idx, THAT_POINTER), PRINT_COMMEND);
                break;
            case POINTER:
                if (idx == 0) {
                    printCommendOrComment(PUSH_THIS_IN_ASSAMBLY, PRINT_COMMEND);

                } else if (idx == 1) {
                    printCommendOrComment(PUSH_THAT_IN_ASSAMBLY, PRINT_COMMEND);
                }
                break;
            case TEMP:
                printCommendOrComment(pushTemp(idx), PRINT_COMMEND);

                break;
            case STATIC:
                printCommendOrComment(pushStatic(fileName, idx), PRINT_COMMEND);
                break;

            case CONSTANT:
                printCommendOrComment(pushConst(idx), PRINT_COMMEND);
                break;

        }

    }

    /**
     * prints commend in vm lang. as a comment
     */
    public void printCommendOrComment(String command, int printType) {
        if (printType == PRINT_COMMENT) {
            writer.println(COMMENT_PREFIX + command);
        } else if (printType == PRINT_COMMEND) {
            writer.println(command);
        }
    }

    /**
     * assamble conditinal commends (larger than, lower than) string in assembly (Hack lang.)
     *
     * @param currentCondition - int signify the number of conditon in the file
     * @param zeroForlt        - int indicator on which condition we need to assamble the string.
     * @return string in assembly language.
     */
    public String getConditionalCommend(int currentCondition, int zeroForlt) {
        String i;
        String j;

        String condi2 =
                "@CONDITION_" + currentCondition + "_IS_FALSE\n" +
                        "D; JEQ\n" + // if y XOR x == 0, than x == y, than False
                        "@CHECK_" + currentCondition + "_FIRST\n" +
                        "D;JLT\n" + // if x XOR y < 0, than sign[y] != sign[x], than jump
                        "@R14\n" + // no overflow
                        "D=M\n" + // D = x
                        "@R15\n" +
                        "D=D-M\n" + // D = R15 ,R15 = x-y
                        "@CONDITION_" + currentCondition + "_IS_TRUE\n";

        String condi3 =
                "@CONDITION_" + currentCondition + "_IS_FALSE\n" +
                        "0;JMP\n" +
                        "(CHECK_" + currentCondition + "_FIRST)\n" +
                        "@R14\n" + // R14 = x
                        "D=M\n" + // D = x
                        "@CONDITION_" + currentCondition + "_IS_TRUE\n";

        String condi4 =
                "@CONDITION_" + currentCondition + "_IS_FALSE\n" +
                        "0;JMP\n" +
                        "(CONDITION_" + currentCondition + "_IS_TRUE)\n" +
                        "@0\n" +
                        "D=!A\n" + // d = true
                        "@PUSH_" + currentCondition + "_RES\n" +
                        "0;JMP\n" +
                        "(CONDITION_" + currentCondition + "_IS_FALSE)\n" +
                        "@0\n" +
                        "D=A\n" + // d = true
                        "(PUSH_" + currentCondition + "_RES)\n" +
                        PUSH_FROM_D;

        if (zeroForlt == 0) {
            i = "D;JLT\n";
            j = i;
        } else {
            i = "D;JGT\n";
            j = "D;JGE\n";
        }

        return CONDITION_COMMEND_PARTIAL + condi2 + i + condi3 + j + condi4;

    }

    /**
     * assamble eq. to string commend in assembly.
     *
     * @param currCondition -  int signify the number of conditon in the file
     * @return string in assembly language.
     */
    public String getEqCommand(int currCondition) {

        String eq2 = "@CONDITION_" + currCondition + "_IS_TRUE\n" +
                "D;JEQ\n" +
                "@0\n" +
                "D=A\n" +
                "@PUSH_" + currCondition + "_RES\n" +
                "0;JMP\n" +
                "(CONDITION_" + currCondition + "_IS_TRUE)\n" +
                "@0\n" +
                "D=!A\n" +
                "(PUSH_" + currCondition + "_RES)\n" +
                PUSH_FROM_D;

        return EQ_COMMEND_IN_ASSAMBLY_PARTIAL + eq2;

    }

    /**
     * assamble pop to segment to string commend in assembly.
     *
     * @param index   - index in a specific segment
     * @param segType - the type of segment (we have 8)
     * @return string in assembly language.
     */
    public String popSeg(int index, String segType) {

        return "@" + index + "\n" +
                "D=A\n" +
                "@" + segType + "\n" +
                "A=M\n" +
                "D=A+D\n" +
                "@R13\n" +
                "M=D\n" +
                POP_TO_D +
                "@R13\n" +
                "A=M\n" +
                "M=D\n";
    }

    /**
     * pops to temp segmant
     *
     * @param index -  index in a specific segment
     * @return string in assembly language.
     */
    public String popTemp(int index) {
        return POP_TO_D +
                "@" + (TEMP_ADDRESS + index) + "\n" +
                "M=D\n";
    }

    /**
     * pops to static segmant
     *
     * @param fileName - name of current input file
     * @param index    index in that specific segment
     * @return string in assembly language.
     */
    public String popStatic(String fileName, int index) {
        return POP_TO_D +
                "@" + fileName + "." + index + "\n" +
                "M=D\n";
    }

    /**
     * push from segment.
     *
     * @param index   index in that specific segment
     * @param segType the type of segment (we have 8)
     * @return string in assembly language.
     */
    public String pushSeg(int index, String segType) {
        return "@" + index + "\n" +
                "D=A\n" +
                "@" + segType + "\n" +
                "A=M\n" +
                "A=A+D\n" +
                "D=M\n" +
                PUSH_FROM_D;
    }

    /**
     * push from Static segment.
     *
     * @param fileName name of current input file
     * @param index    index in that specific segment
     * @return string in assembly language.
     */
    public String pushStatic(String fileName, int index) {
        return "@" + fileName + "." + index + "\n" +
                "D=M\n" +
                PUSH_FROM_D;
    }

    /**
     * push from Constant segment.
     *
     * @param constant - int value
     * @return string in assembly language, or null if string isnt valid according to INT_CONST_UPPER_BOUND
     */
    public String pushConst(int constant) {
        if (constant < 0 || constant >= INT_CONST_UPPER_BOUND) {
            return null;
        } else {
            return "@" + constant + "\n" +
                    "D=A\n" +
                    PUSH_FROM_D;
        }

    }

    /**
     * push from Temp seg.
     *
     * @param index index in that specific segment
     * @return string in assembly language
     */
    private String pushTemp(int index) {
        return "@" + (TEMP_ADDRESS + index) + "\n" +
                "D=M\n" +
                PUSH_FROM_D;
    }

    /**
     * sets current file name
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
