import java.io.*;

public class Codegen {
    // file into which generated code is written
    public static PrintWriter p = null;

    // values of true and false
    public static final String TRUE = "-1";
    public static final String FALSE = "0";

    // registers
    public static final String FP = "$fp";
    public static final String SP = "$sp";
    //add other registers needed.

    // for pretty printing generated code
    private static final int MAXLEN = 4;


    // for generating labels
    private static int currLabel = 0;


    // ********************************************************************** 
    // **********************************************************************
    // GENERATE OPERATIONS
    // **********************************************************************
    // **********************************************************************

    // **********************************************************************
    // generateWithComment (string args -- perhaps empty)
    //    given:  op code, and args
    //    do:     write nicely formatted code (ending with new line)
    // **********************************************************************
    public static void generateWithComment(String opcode, String comment,
                                           String arg1, String arg2,
                                           String arg3) {
        int space = MAXLEN - opcode.length() + 2;

        p.print("\t" + opcode);
        if (arg1 != "") {
            for (int k = 1; k <= space; k++) p.print(" ");
            p.print(arg1);
            if (arg2 != "") {
                p.print(", " + arg2);
                if (arg3 != "") p.print(", " + arg3);
            }
        }           
        if (comment != "") p.print("\t\t#" + comment);
        p.println();

    }

    public static void generateWithComment(String opcode, String comment,
                                           String arg1, String arg2) {

    }

    public static void generateWithComment(String opcode, String comment,
                                           String arg1) {

    }

    public static void generateWithComment(String opcode, String comment) {

    }

    // **********************************************************************
    // generate (string args -- perhaps empty)
    //    given:  op code, and args
    //    do:     write nicely formatted code (ending with new line) 
    // **********************************************************************
    public static void generate(String opcode, String arg1, String arg2,
                                String arg3) {
        int space = MAXLEN - opcode.length() + 2;

        p.print("\t" + opcode);
        if (arg1 != "") {
            for (int k = 1; k <= space; k++) p.print(" ");
            p.print(arg1);
            if (arg2 != "") {
                p.print(", " + arg2);
                if (arg3 != "") p.print(", " + arg3);
            }
        }
        p.println();
    }

    public static void generate(String opcode, String arg1, String arg2) {

    }

    public static void generate(String opcode, String arg1) {

    }

    public static void generate(String opcode) {

    }

    // **********************************************************************
    // generate (two string args, one int)
    //    given:  op code and args
    //    do:     write nicely formatted code (ending with new line)
    // **********************************************************************
    public static void generate(String opcode, String arg1, String arg2,
                                int arg3) {

    }


    // **********************************************************************
    // generate (one string arg, one int)
    //    given:  op code and args
    //    do:     write nicely formatted code (ending with new line)
    // **********************************************************************  
    public static void generate(String opcode, String arg1, int arg2) {

    }


    // **********************************************************************
    // generateIndexed
    //    given:  op code, target register T1 (as string), indexed register T2
    //            (as string), - offset xx (int), and optional comment
    //    do:     write nicely formatted code (ending with new line):
    //                 op T1, xx(T2) #comment
    // **********************************************************************
    public static void generateIndexed(String opcode, String arg1,
                                      String arg2, int arg3, String comment)
    {

    }

    public static void generateIndexed(String opcode, String arg1,
                                       String arg2, int arg3) {

    }                   

    // **********************************************************************
    // generateLabeled (string args -- perhaps empty)
    //    given:  label, op code, comment, and arg
    //    do:     write nicely formatted code (ending with new line)
    // **********************************************************************
    public static void generateLabeled(String label, String opcode,
                                       String comment, String arg1) {

    }

    public static void generateLabeled(String label, String opcode,
                                       String comment) {

    }

    // **********************************************************************
    // genPush
    //    generate code to push the given value onto the stack
    // **********************************************************************
    public static void genPush(String s) {
        generateIndexed("sw", s, SP, 0, "PUSH");
        generate("subu", SP, SP, 4);                       

    }

    // **********************************************************************
    // genPop
    //    generate code to pop into the given register
    // **********************************************************************
    public static void genPop(String s) {

    }

    // **********************************************************************
    // genCompare
    //   given: a branch op code (whose two operands are in T0 and T1)
    //   generate this code:
    //       1. branch to truelab using the given op code
    //       3. push false
    //       4. goto falselab
    //       5. truelab: push true
    //       6. falselab:
    public static void genCompare(String op) {

    }                    

    // **********************************************************************
    // genLabel
    //   given:    label L and comment (comment may be empty)
    //   generate: L:    # comment
    // **********************************************************************
    public static void genLabel(String label, String comment) {

    }

    public static void genLabel(String label) {

    }

    // **********************************************************************
    // Return a different label each time:
    //        ._L0 ._L1 ._L2, etc.
    // **********************************************************************
    public static String nextLabel() {

    }                               
}
