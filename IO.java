import java.io.*;

public class IO {

    // **********************************************************************
    // openInputFile
    //
    // Open the file with the given name for reading.
    // Throw a FileNotFoundException if the file does not exist.
    // **********************************************************************
    public static StreamTokenizer openInputFile(String fileName)
	throws FileNotFoundException {
	File inFile = new File(fileName);
	if (!inFile.exists()) {
	    throw new FileNotFoundException();
	}
	return (new StreamTokenizer(new BufferedReader(
						    new FileReader(inFile))));
    }

    // **********************************************************************
    // openOutputFile
    //
    // Open the file with the given name for writing.
    // Throw a FileNotFoundException if the file does not exist.
    // **********************************************************************
    public static PrintWriter openOutputFile(String fileName) 
    throws IOException {
	File outFile = new File(fileName);
	return (new PrintWriter(new BufferedWriter(new FileWriter(outFile))));
    }

    // **********************************************************************
    // readWord
    //
    // Read and return the next word from the file associated with the given
    // StreamTokenizer.  Return null if at end-of-file.
    //
    // Throw an IOException if there is a problem reading the file.
    // **********************************************************************
    public static String readWord(StreamTokenizer input) throws IOException {
	if (input.nextToken() != StreamTokenizer.TT_EOF) {
	    if (input.ttype != StreamTokenizer.TT_WORD) {
		// bad data in input -- throw exception with error msg
		throw new IOException("Bad input on line " + input.lineno());
	    }
	    return(input.sval);
	}
	return null;
    }
}
