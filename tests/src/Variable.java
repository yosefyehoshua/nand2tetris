/**
 * Represents a variable in the SymbolTable
 */
public class Variable {

    /** The name of the variable*/
    private String name;
    /** The type of the variable*/
    private String type;
    /** The kind of the variable*/
    private String kind;
    /** The index of the variable*/
    private int index;

    /**
     * Constructs a new variable
     * @param name The name of the variable
     * @param type The type of the variable
     * @param kind The kind of the variable
     * @param index The index of the variable
     */
    public Variable(String name, String type, String kind, int index){
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    /**
     * @return The name of the variable
     */
    public String getName(){return this.name;}
    /**
     * @return The type of the variable
     */
    public String getType(){return this.type;}
    /**
     * @return The index of the variable
     */
    public int getIndex() {
        return index;
    }
    /**
     * @return The kind of the variable
     */
    public String getKind() {
        return kind;
    }
}
