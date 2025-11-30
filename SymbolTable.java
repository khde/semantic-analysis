import java.util.*;

public class SymbolTable {

    public class Sym {
        public String name;
        public int type;
        public List<Sym> paramTypes;

        // Constructor
        public Sym(String name, int type) {
            this.name = name;
            this.type = type;
            this.paramTypes = new ArrayList<>();
        }

        // Constructor for methods with parameters
        public Sym(String name, int type, List<Sym> params) {
            this.name = name;
            this.type = type;
            this.paramTypes = params;
        }

        public String toString() {
            return name + " (" + Types.ToString(type) + ")";
        }
    }

    // We must use a Stack of scopes, not just one table, to handle nested blocks.
    private LinkedList<Hashtable<String, Sym>> scopes;

    public SymbolTable() {
        scopes = new LinkedList<>();
        enterScope();
    }

    // Start a new block
    public void enterScope() {
        scopes.addFirst(new Hashtable<String, Sym>());
    }

    // End a block
    public void exitScope() {
        if (!scopes.isEmpty()) scopes.removeFirst();
    }

    /**
     * Looks up a name in ALL scopes (for using variables).
     */
    public Sym lookup(String name) {
        for (Hashtable<String, Sym> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    /**
     * Looks up a name in the CURRENT scope only.
     * Essential for checking "Multiply declared identifier".
     */
    public Sym lookupLocal(String name) {
        if (scopes.isEmpty()) return null;
        return scopes.getFirst().get(name);
    }

    /**
     * Inserts a symbol into the CURRENT scope.
     * Returns the symbol inserted.
     */
    public void insert(Sym sym) {
        if (scopes.isEmpty()) return;
        scopes.getFirst().put(sym.name, sym);
    }

    // Debug helper
    public void print() {
        System.out.println("--- Symbol Table ---");
        for (Hashtable<String, Sym> scope : scopes) {
            System.out.println(scope.toString());
        }
    }
}
