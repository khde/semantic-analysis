import java.io.*;
import java_cup.runtime.*;

// **********************************************************************
// Main program to test the simple parser.
//
// There should be 2 command-line arguments:
//    1. the file to be parsed
//    2. the output file into which the AST built by the parser should be
//       decompiled
// The program opens the two files, creates a scanner and a parser, and
// calls the parser.  If the parse is successful, the AST is decompiled.
// **********************************************************************


// shut up message about parser creation
@SuppressWarnings("deprecation")
public class P3 {
    public static void main(String[] args)
	throws IOException // may be thrown by the scanner
    {
	// check for command-line args
	if (args.length != 2) {
	    System.err.println("please supply name of file to be parsed and name of file for decompiled version.");
	    System.exit(-1);
	}

	// open input file
	FileReader inFile = null;
	try {
	    inFile = new FileReader(args[0]);
	} catch (FileNotFoundException ex) {
	    System.err.println("File " + args[0] + " not found.");
	    System.exit(-1);
	}

	// open output file
	PrintWriter outFile = null;
	try {
	    outFile = IO.openOutputFile(args[1]);
	} catch (IOException ex) {
	    System.err.println("File " + args[1] + " could not be opened.");
	    System.exit(-1);
	}


	parser P = new parser();
	P.setScanner(new Yylex(inFile));
	
	Symbol root=null; // the parser will return a Symbol whose value
	                  // field's type is the type associated with the
	                  // root nonterminal (i.e., with the nonterminal
	                  // "program")

	try {
	    root = P.parse(); // do the parse
	    System.out.println ("Simple program parsed correctly.");
	} catch (Exception ex){
	    System.out.println(ex);
	    System.exit(0);
	}
	((ASTnode)root.value).decompile(outFile, 0);
	outFile.close();

	return;
    }
}
