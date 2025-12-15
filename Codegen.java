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
    public static final String RA = "$ra";
    public static final String ZERO = "$zero";
    public static final String V0 = "$v0";
    public static final String A0 = "$a0";
    public static final String A1 = "$a1";
    public static final String A2 = "$a2";
    public static final String A3 = "$a3";
    public static final String T0 = "$t0";
    public static final String T1 = "$t1";
    public static final String T2 = "$t2";
    public static final String T3 = "$t3";
    public static final String T4 = "$t4";
    public static final String T5 = "$t5";
    public static final String T6 = "$t6";
    public static final String T7 = "$t7";
    public static final String S0 = "$s0";
    public static final String S1 = "$s1";
    public static final String S2 = "$s2";
    public static final String S3 = "$s3";
    public static final String S4 = "$s4";
    public static final String S5 = "$s5";
    public static final String S6 = "$s6";
    public static final String S7 = "$s7";

    // for pretty printing generated code
    private static final int MAXLEN = 4;

    // for generating labels
    private static int currLabel = 0;

    public static void init(PrintWriter pw) {
        p = pw;
    }

    // ********************************************************************** 
    // **********************************************************************
    // GENERATE OPERATIONS
    // **********************************************************************
    // **********************************************************************

    // **********************************************************************
    // generateWithComment (string args -- perhaps empty)
    // given:  op code, and args
    // do:     write nicely formatted code (ending with new line)
    // **********************************************************************
    public static void generateWithComment(String opcode, String comment,
                                           String arg1, String arg2,
                                           String arg3) {
        int space = MAXLEN - opcode.length() + 2;

        p.print("\t" + opcode);
        if (!arg1.equals("")) {
            for (int k = 1; k <= space; k++) p.print(" ");
            p.print(arg1);
            if (!arg2.equals("")) {
                p.print(", " + arg2);
                if (!arg3.equals("")) p.print(", " + arg3);
            }
        }           
        if (!comment.equals("")) p.print("\t\t# " + comment);
        p.println();
    }

    public static void generateWithComment(String opcode, String comment,
                                           String arg1, String arg2) {
        generateWithComment(opcode, comment, arg1, arg2, "");
    }

    public static void generateWithComment(String opcode, String comment,
                                           String arg1) {
        generateWithComment(opcode, comment, arg1, "", "");
    }

    public static void generateWithComment(String opcode, String comment) {
        generateWithComment(opcode, comment, "", "", "");
    }

    // **********************************************************************
    // generate (string args -- perhaps empty)
    // given:  op code, and args
    // do:     write nicely formatted code (ending with new line)
    // **********************************************************************
    public static void generate(String opcode, String arg1, String arg2,
                                String arg3) {
        int space = MAXLEN - opcode.length() + 2;

        p.print("\t" + opcode);
        if (!arg1.equals("")) {
            for (int k = 1; k <= space; k++) p.print(" ");
            p.print(arg1);
            if (!arg2.equals("")) {
                p.print(", " + arg2);
                if (!arg3.equals("")) p.print(", " + arg3);
            }
        }
        p.println();
    }

    public static void generate(String opcode, String arg1, String arg2) {
        generate(opcode, arg1, arg2, "");
    }

    public static void generate(String opcode, String arg1) {
        generate(opcode, arg1, "", "");
    }

    public static void generate(String opcode) {
        generate(opcode, "", "", "");
    }

    // **********************************************************************
    // generate (two string args, one int)
    // given:  op code and args
    // do:     write nicely formatted code (ending with new line)
    // **********************************************************************
    public static void generate(String opcode, String arg1, String arg2, int arg3) {
        generate(opcode, arg1, arg2, String.valueOf(arg3));
    }

    // **********************************************************************
    // generate (one string arg, one int)
    // given:  op code and args
    // do:     write nicely formatted code (ending with new line)
    // **********************************************************************  
    public static void generate(String opcode, String arg1, int arg2) {
        generate(opcode, arg1, String.valueOf(arg2));
    }

    // **********************************************************************
    // generateIndexed
    // given:  op code, target register T1 (as string), indexed register T2
    //         (as string), - offset xx (int), and optional comment
    // do:     write nicely formatted code (ending with new line):
    //            op T1, xx(T2) #comment
    // **********************************************************************
    public static void generateIndexed(String opcode, String arg1,
                                       String arg2, int arg3, String comment) {
        p.print("\t" + opcode + " " + arg1 + ", " + arg3 + "(" + arg2 + ")");
        if (!comment.equals("")) p.print("\t\t# " + comment);
        p.println();
    }

    public static void generateIndexed(String opcode, String arg1,
                                       String arg2, int arg3) {
        generateIndexed(opcode, arg1, arg2, arg3, "");
    }

    // **********************************************************************
    // generateLabeled (string args -- perhaps empty)
    // given:  label, op code, comment, and arg
    // do:     write nicely formatted code (ending with new line)
    // **********************************************************************
    public static void generateLabeled(String label, String opcode,
                                       String comment, String arg1) {
        p.print(label + ": ");
        generateWithComment(opcode, comment, arg1);
    }

    public static void generateLabeled(String label, String opcode,
                                       String comment) {
        p.print(label + ": ");
        generateWithComment(opcode, comment);
    }

    // **********************************************************************
    // genPush
    // generate code to push the given value onto the stack
    // **********************************************************************
    public static void genPush(String s) {
        generateIndexed("sw", s, SP, 0, "Push");
        generate("subu", SP, SP, 4);                       
    }

    // **********************************************************************
    // genPop
    // generate code to pop into the given register
    // **********************************************************************
    public static void genPop(String s) {
        generateIndexed("lw", s, SP, 0, "Pop");
        generate("addu", SP, SP, 4);
    }

    // **********************************************************************
    // genCompare
    // given: a branch op code (whose two operands are in T0 and T1)
    // generate this code:
    //       1. branch to truelab using the given op code
    //       3. push false
    //       4. goto falselab
    //       5. truelab: push true
    //       6. falselab:
    public static void genCompare(String op) {
        String trueLabel = nextLabel();
        String endLabel = nextLabel();

        generate(op, T0, T1, trueLabel);
        generate("li", T0, FALSE);
        genPush(T0);
        generate("j", endLabel);

        genLabel(trueLabel, "true case");
        generate("li", T0, TRUE);
        genPush(T0);

        genLabel(endLabel);
    }

    // **********************************************************************
    // genLabel
    // given:    label L and comment (comment may be empty)
    // generate: L:    # comment
    // **********************************************************************
    public static void genLabel(String label, String comment) {
        p.print(label + ": ");
        if (!comment.equals("")) p.print("\t# " + comment);
        p.println();
    }

    public static void genLabel(String label) {
        genLabel(label, "");
    }

    public static void genComment(String comment) {
        p.println("\t# " + comment);
    }


    // **********************************************************************
    // Return a different label each time:
    //        ._L0 ._L1 ._L2, etc.
    // **********************************************************************
    public static String nextLabel() {
        return "._L" + (currLabel++);
    }
}
