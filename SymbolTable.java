import java.util.Hashtable;

public class SymbolTable {
    
    public class Sym {
	public Sym (String id) { myName = id; }
	public String name () { return myName; }

	// private fields
	private String myName;
    };

    Hashtable table;

    SymbolTable () { table = new Hashtable(); }

    public Sym lookup (String name) { 
	return (Sym) table.get(name); 
    }
    public Sym insert (String name) {
	if (table.containsKey(name)) 
	    return (Sym) table.get(name);
	Sym sym = new Sym(name);
	table.put(name, sym);
	return sym;
    }
}
