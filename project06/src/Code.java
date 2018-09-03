import java.util.Hashtable;

/**
 * this class is used as a translator form a symbolic Hack language to binary language.
 */

public class Code {

    /***************************
     * Constants
     ******************************/
    private static final int A_TYPE = 0;
    private static final int C_TYPE = 1;
    private static final int MAX_BIT_IN_MEM = 15;
    private static final String A_TYPE_BIT_INDICATOR = "0";


    /****************************
     * Instructions Dictionaries
     ***************************/

    private Hashtable<String, String> jumpTable = new Hashtable<String, String>() {{
        put("", "000");
        put("JGT", "001");
        put("JEQ", "010");
        put("JGE", "011");
        put("JLT", "100");
        put("JNE", "101");
        put("JLE", "110");
        put("JMP", "111");
    }};

    private Hashtable<String, String> destTable = new Hashtable<String, String>() {{
        put("", "000");
        put("M", "001");
        put("D", "010");
        put("MD", "011");
        put("A", "100");
        put("AM", "101");
        put("AD",
                "110");
        put("AMD",
                "111");
    }};

    private Hashtable<String, String> compTable = new Hashtable<String, String>() {{
        put("0",
                "1110101010");
        put("1", "1110111111");
        put("-1", "1110111010");
        put("D", "1110001100");
        put("A", "1110110000");
        put("!D", "1110001101");
        put("!A", "1110110001");
        put("-D", "1110001111");
        put("-A", "1110110011");
        put("D+1", "1110011111");
        put("A+1", "1110110111");
        put("D-1",
                "1110001110");
        put("A-1", "1110110010");
        put("D+A", "1110000010");
        put("D-A", "1110010011");
        put("A-D",
                "1110000111");
        put("D&A", "1110000000");
        put("D|A", "1110010101");
        put("M", "1111110000");
        put("!M",
                "1111110001");
        put("-M", "1111110011");
        put("M+1", "1111110111");
        put("M-1", "1111110010");
        put("D+M",
                "1111000010");
        put("D-M", "1111010011");
        put("M-D", "1111000111");
        put("D&M", "1111000000");
        put("D|M",
                "1111010101");
    }};

    private Hashtable<String, String> compShiftTable = new Hashtable<String, String>() {{
        put("D<<",
                "1010110000");
        put("D>>", "1010010000");
        put("A<<", "1010100000");
        put("A>>", "1010000000");
        put("M<<", "1011100000");
        put("M>>", "1011000000");
    }};

    /**
     * translate decimal int address to a binary address representation string
     *
     * @param address - int address
     * @return - binary address representation string
     */
    private static String decToBinary(int address) {
        if (address >= Math.pow(2, MAX_BIT_IN_MEM)) {
            return null;
        }
        String binary = Integer.toBinaryString(address);

        int length = MAX_BIT_IN_MEM - binary.length();
        for (int i = 0; i < length; i++) {
            binary = "0" + binary;
        }
        return binary;
    }

    /**
     * this func get an array of strings and an instruction type int, and translate its content to binary,
     * according to the instruction it carries and the instructions dictionary.
     *
     * @param groups          array of strings
     * @param instructionType instruction type int, 0 for A, 1 for C and 2 for L.
     * @return String - translated comment in binary
     */
    public String translator(String[] groups, int instructionType) {
        switch (instructionType) {
            case A_TYPE:
                String A_istruction = decToBinary(Integer.parseInt(groups[0]));
                return A_TYPE_BIT_INDICATOR + A_istruction;
            case C_TYPE:

                String dest = groups[0];
                String comp = groups[1];
                String jump = groups[2];

                dest = destTranslator(dest);
                jump = jumpTranslator(jump);
                comp = compTranslator(comp);

                if (dest != null && jump != null && comp != null) {
                    return comp + dest + jump;
                }

        }
        return null;
    }

    /**
     * get a destination string and translate it according to the hack language rules detailed in Chapter 4
     *
     * @param dest string with A,D,M char combinations, or null
     * @return the 3-bit representation of dest value in C-instruction
     */
    private String destTranslator(String dest) {
        if (destTable.containsKey(dest)) {
            return destTable.get(dest);
        }
        return null;
    }

    /**
     * get a computation string and translate it according to the hack language rules detailed in Chapter 4
     *
     * @param comp valid computation, or null
     * @return the binary representation of comp value in C-instruction
     */
    private String compTranslator(String comp) {
        if (compShiftTable.containsKey(comp)) {
            return compShiftTable.get(comp);
        } else if (compTable.containsKey(comp)) {
            return compTable.get(comp);
        }
        return null;
    }

    /**
     * get a jump string and translate it according to the hack language rules detailed in Chapter 4
     *
     * @param jump string with A,D,M jumps, or null
     * @return the 3-bit representation of jump value in C-instruction
     */
    private String jumpTranslator(String jump) {
        if (jumpTable.containsKey(jump)) {
            return jumpTable.get(jump);
        }
        return null;
    }
}

