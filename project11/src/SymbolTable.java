import java.util.Hashtable;

/**
 * A symbol table that associates names with information needed for Jack compilation:
 * type, kind, and running index. The symbol table has 2 nested scopes
 * (class or subroutine).
 */
public class SymbolTable {
    /******************** Data Members ************************/
    Variable variable;
    private int indexOfstatic, indexOffield, indexOflocal, indexofargument;
    private Hashtable<String, Variable> classScope;
    private Hashtable<String, Variable> subroutineScope;


    /**
     * Creates a new empty symbol table.
     */
    public SymbolTable() {
        classScope = new Hashtable<>();
        subroutineScope = new Hashtable<>();
        this.indexOfstatic = 0;
        this.indexOffield = 0;
        this.indexOflocal = 0;
        this.indexofargument = 0;
    }

    /**
     * Starts a new subroutine scope i.e. erases all names in the previous
     * subroutines scope.
     */
    void startSubroutine() {
        subroutineScope.clear();
        indexOflocal = 0;
        indexofargument = 0;
    }

    /**
     * Defines a new identifier of a given name, type, and kind and assigns it
     * a running index. STATIC and FIELD identifiers have a class scope, while
     * ARG and VAR identifiers have a subroutine scope.
     *
     * @param name -> var
     * @param type -> var
     * @param kind -> var
     */
    void define(String name, String type, String kind) {
        if (kind.equals("static")) {
            variable = new Variable(name, type, kind, indexOfstatic);
            classScope.put(name, variable);
            indexOfstatic++;

        } else if (kind.equals("field")) {
            variable = new Variable(name, type, kind, indexOffield);
            classScope.put(name, variable);
            indexOffield++;

        } else if (kind.equals("var")) {
            variable = new Variable(name, type, kind, indexOflocal);
            subroutineScope.put(name, variable);
            indexOflocal++;


        } else if (kind.equals("argument")) {
            variable = new Variable(name, type, kind, indexofargument);
            subroutineScope.put(name, variable);
            indexofargument++;

        } else {
            System.out.println("invalid kind string");
        }
    }

    /**
     * Returns the number of variables of the given kind already defined in
     * the current scope.
     *
     * @param kind -> var
     * @return int
     */
    int varCount(String kind) {
        if (kind.equals("static")) {
            return indexOfstatic;
        } else if (kind.equals("field")) {
            return indexOffield;
        } else if (kind.equals("var")) {
            return indexOflocal;
        } else if (kind.equals("argument")) {
            return indexofargument;
        } else {
            System.out.println("invalid variable");
            return 0;
        }
    }

    /**
     * Returns the kind of the named identifier in the current scope.
     * Returns NONE if the identifier is unknown in the current scope.
     *
     * @param name
     * @return String - kind of the named identifier in the current scope
     */
    String kindOf(String name) {
        Variable localVariable = getVariable(name);
        return localVariable != null ? localVariable.getKind() : null;
    }

    /**
     * Returns the type of the named identifier in the current scope.
     *
     * @param name
     * @return String - type of the named identifier in the current scope
     */
    String typeOf(String name) {
        Variable localVariable = getVariable(name);
        return localVariable != null ? localVariable.getType() : null;
    }

    /**
     * Returns the index assigned to named identifier.
     *
     * @param name
     * @return
     */
    Integer indexOf(String name) {
        Variable localVariable = getVariable(name);
        return localVariable != null ? localVariable.getIndex() : null;
    }


    Variable getVariable(String name) {
        if (subroutineScope.isEmpty()) {
            return classScope.get(name);
        }
        Variable newVaraible;
        newVaraible = subroutineScope.get(name);
        if (newVaraible == null) {
            return classScope.get(name);
        }
        return newVaraible;

    }

    /**
     * nested class in symbol table, of an variable object in the symbol table.
     */
    public class Variable {

        private String name, type, kind;
        private int index;

        Variable(String name, String type, String kind, int index) {
            this.name = name;
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }

        public int getIndex() {
            return index;
        }

        public String getKind() {
            return kind;
        }
    }

}
