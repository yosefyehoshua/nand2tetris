import java.util.Hashtable;

/**
 * this class represent the symbol table object, a data structure (using hashTable) that stores symbols and
 * their addresses, as a part of an Hack Computer Assambler, in nand2tetris course.
 * this data structure supports the function: add, get symbol address and contins.
 */
public class SymbolTable {

    /***************************** Data Members ***********************************/
    /**
     * declare smbole table data structure
     */
    private Hashtable<String, Integer> symbolTable;

    /********************************* Methods *******************************************/
    /**
     * A constructor for symbol Table
     */
    public SymbolTable() {
        symbolTable = new Hashtable<>();
    }

    /**
     * adds new symbols to table
     *
     * @param symbol  a string
     * @param address an integer
     */
    void add(String symbol, Integer address) {
        symbolTable.put(symbol, address);
    }

    /**
     * returns a boolean value whether the table already contains the input symbol.
     *
     * @param symbol - string
     * @return True/False if table contains the given symbol.
     */
    boolean contains(String symbol) {
        return symbolTable.containsKey(symbol);
    }

    /**
     * returns the address (integer) of a given symbol.
     *
     * @param symbol string.
     * @return address (integer)
     */
    Integer get(String symbol) {
        return symbolTable.get(symbol);
    }
}
