import java.io.*;
import java_cup.runtime.Symbol;

@SuppressWarnings("deprecation")
public class P4 {
    public static void main(String[] args) {
        FileReader input = null;

        if (args.length == 0) {
            System.err.println("Usage: java P4 <inputfile>");
            System.exit(1);
        } else {
            try {
                input = new FileReader(args[0]);
            } catch (FileNotFoundException e) {
                System.err.println("Error: File not found: " + args[0]);
                System.exit(1);
            }
        }

        try {
            parser P = new parser(new Yylex(input));

            Symbol result = P.parse();
            ProgramNode root = (ProgramNode) result.value;

            if (Errors.fatalErrorCount == 0) {
                SymbolTable st = new SymbolTable();
                root.analyzeNames(st);
            }

            if (Errors.fatalErrorCount == 0) {
                PrintWriter out = new PrintWriter(System.out, true);
                root.decompile(out, 0); 
            }

            if (Errors.fatalErrorCount == 0) {
                root.checkTypes();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
