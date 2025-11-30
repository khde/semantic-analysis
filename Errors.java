// Errors
//
// This class is used to generate warning and fatal error messages.

class Errors {
    public static int fatalErrorCount = 0;

    static void fatal(int lineNum, int charNum, String msg) {
        System.err.println(lineNum + ":" + charNum + " **ERROR** " + msg);
        fatalErrorCount++;
    }

    static void warn(int lineNum, int charNum, String msg) {
        System.err.println(lineNum + ":" + charNum + " **WARNING** " + msg);
    }
}
