import java.util.Hashtable;

/**
 * A data structure designed to keep track of the different variable names while translating a Jack cod
 * into VM code.
 */
public class SymbolTable {

    /** A symbol table for the class scope*/
    Hashtable<String, Variable> classTable;

    /** A symbol table for the subroutine scope*/
    Hashtable<String, Variable> subroutineTable;

    /** names for the different kinds of variables */
    static final String STATIC_VAR = "static";
    static final String FIELD_VAR = "field";
    static final String LOCAL_VAR = "var";
    static final String ARGUMENT_VAR = "argument";

    /** indexes for the defined variables*/
    private int staticIndex = 0;
    private int fieldIndex = 0;
    private int localIndex = 0;
    private int argumentIndex = 0;

    public SymbolTable(){
        classTable = new Hashtable<>();
    }

    /**
     * Starts a new subroutine scope
     */
    public void startSubroutine(){
        subroutineTable = new Hashtable<>();
        localIndex = 0;
        argumentIndex = 0;
    }

    /**
     * Defines a new variable of the given attributes and adds it to the relevant symbolTable
     * @param name The name of the variable
     * @param type The type of the variable
     * @param kind The kind of the variable
     */
    public void define(String name, String type, String kind){
        Variable newVar;
        switch (kind){
            case STATIC_VAR:
                newVar = new Variable(name, type, kind, staticIndex);
                staticIndex++;
                classTable.put(name, newVar);
                break;
            case FIELD_VAR:
                newVar = new Variable(name, type, kind, fieldIndex);
                fieldIndex++;
                classTable.put(name, newVar);
                break;
            case LOCAL_VAR:
                newVar = new Variable(name, type, kind, localIndex);
                localIndex++;
                subroutineTable.put(name, newVar);
                break;
            case ARGUMENT_VAR:
                newVar = new Variable(name, type, kind, argumentIndex);
                argumentIndex++;
                subroutineTable.put(name, newVar);
                break;
            default:
                System.out.println("Wrong variable kind");
                System.exit(1);
        }
    }

    /**
     * @param kind The kind to enquire about
     * @return How many variables of the given kind were already defined
     */
    public int varCount(String kind){
        switch (kind){
            case STATIC_VAR:
                return staticIndex;
            case FIELD_VAR:
                return fieldIndex;
            case LOCAL_VAR:
                return localIndex;
            case ARGUMENT_VAR:
                return argumentIndex;
            default:
                System.out.println("Wrong variable kind");
                System.exit(1);
                return 0;
        }
    }

    /**
     * @param name The name of the variable to return
     * @return The variable of that name (first searches the subroutine scope then class scope), null if
     * not found
     */
    private Variable getVar(String name){
        if(subroutineTable != null){
            Variable candidate = subroutineTable.get(name);
            if(candidate != null){ // variable exists in the subroutine scope
                return candidate;
            }
        }
        return classTable.get(name);
    }

    /**
     * @param name The name of the variable
     * @return The kind of the variable with the given name, null if not found
     */
    public String kindOf(String name){
        Variable var = getVar(name);
        if (var != null){
            return var.getKind();
        }
        return null;
    }

    /**
     * @param name The name of the variable
     * @return The index of the variable with the given name, null if not found
     */
    public Integer indexOf(String name){
        Variable var = getVar(name);
        if (var != null){
            return var.getIndex();
        }
        return null;
    }

    /**
     * @param name The name of the variable
     * @return The type of the variable with the given name, null if not found
     */
    public String typeOf(String name){
        Variable var = getVar(name);
        if (var != null){
            return var.getType();
        }
        return null;
    }

    /**
     * @param name name of the variable
     * @return printable version of the variable. todo DELETE Method
     */
    public String getPrintable(String name){
        Variable var = getVar(name);
        if (var != null){
            return "name: "+var.getName()+", type: "+var.getType()+", kind: "+var.getKind()+", index: " +
                    ""+var.getIndex();
        }
        return "no such variable";
    }
}
