import java.io.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a "Simple" program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a sequence (for nodes that may have a variable number of children)
// or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         IdNode, ClassBodyNode
//     ClassBodyNode       DeclListNode
//     DeclListNode        sequence of DeclNode
//     FormalsListNode     sequence of FormalDeclNode
//     MethodBodyNode      DeclListNode, StmtListNode
//     StmtListNode        sequence of StmtNode
//     ExpListNode         sequence of ExpNode
//     SwitchGroupListNode sequence of SwitchGroupNode
//
//     DeclNode:
//       FieldDeclNode     TypeNode, IdNode
//       VarDeclNode       TypeNode, IdNode
//       MethodDeclNode    TypeNode, IdNode, FormalsListNode, MethodBodyNode
//       FormalDeclNode    TypeNode, IdNode
//
//     TypeNode:
//       IntNode             -- none --
//       BooleanNode         -- none --
//       StringNode          -- none --
//       VoidNode            -- none --
//
//     StmtNode:
//       PrintStmtNode       ExpNode
//       AssignStmtNode      IdNode, ExpNode
//       IfStmtNode          ExpNode, StmtListNode, StmtListNode (else list)
//       DoWhileStmtNode     StmtListNode, ExpNode
//       CallStmtNode        IdNode, ExpListNode
//       ReturnStmtNode      ExpNode (optional)
//       BlockStmtNode       DeclListNode, StmtListNode
//       SwitchStmtNode      ExpNode, SwitchGroupListNode
//
//     SwitchGroupNode:     SwitchLabelNode, StmtListNode
//
//     SwitchLabelNode:
//       SwitchLabelNode     ExpNode
//       DefaultLabelNode    -- none --
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//         ModNode
//         PowerNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************
abstract class ASTnode { 
    // every subclass must provide an decompile operation
    abstract public void decompile(PrintWriter p, int indent);

    // this method can be used by the decompile methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
    for (int k=0; k<indent; k++) p.print(" ");
    }
}

// **********************************************************************
// ProgramNode, ClassBodyNode, DeclListNode, FormalsListNode,
// MethodBodyNode, StmtListNode, ExpListNode
// **********************************************************************
class ProgramNode extends ASTnode {
        public ProgramNode(IdNode id, ClassBodyNode classBody) {
        myId = id;
        myClassBody = classBody;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("public class ");
        myId.decompile(p, 0);
        p.println(" {");
        myClassBody.decompile(p, indent+2);
        p.println("}");
    }

    // 2 kids
    private IdNode myId;
    private ClassBodyNode myClassBody;
}

class ClassBodyNode extends ASTnode {
    public ClassBodyNode(DeclListNode declList) {
        myDeclList = declList;
    }

    public void decompile(PrintWriter p, int indent) {
        myDeclList.decompile(p, indent);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(Sequence S) {
    myDecls = S;
    }

    public void decompile(PrintWriter p, int indent) {
    try {
        for (myDecls.start(); myDecls.isCurrent(); myDecls.advance()) {
        ((DeclNode)myDecls.getCurrent()).decompile(p, indent);
        }
    } catch (NoCurrentException ex) {
        System.err.println("unexpected NoCurrentException in DeclListNode.decompile");
        System.exit(-1);
    }
    }

    public Sequence getList() { return myDecls; }

    // sequence of kids (DeclNodes)
    private Sequence myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(Sequence S) {
        myFormals = S;
    }

    public void decompile(PrintWriter p, int indent) {
    p.print("(");
    try {
        myFormals.start();
        if (myFormals.isCurrent()) {
        ((FormalDeclNode)myFormals.getCurrent()).decompile(p, indent);
        myFormals.advance();
        }
        while (myFormals.isCurrent()) {
        p.print(", ");
        ((FormalDeclNode)myFormals.getCurrent()).decompile(p, indent);
        myFormals.advance();
        }
    } catch (NoCurrentException ex) {
        System.err.println("unexpected NoCurrentException in FormalsListNode.decompile");
        System.exit(-1);
    }
    p.print(")");
    }

    // sequence of kids (FormalDeclNodes)
    private Sequence myFormals;
}

class MethodBodyNode extends ASTnode {
    public MethodBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void decompile(PrintWriter p, int indent) {
        p.println(" {");
        myDeclList.decompile(p, indent+2);
        myStmtList.decompile(p, indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(Sequence S) {
        myStmts = S;
    }

    public void decompile(PrintWriter p, int indent) {
    try {
        for (myStmts.start(); myStmts.isCurrent(); myStmts.advance()) {
        ((StmtNode)myStmts.getCurrent()).decompile(p, indent);
        }
    } catch (NoCurrentException ex) {
        System.err.println("unexpected NoCurrentException in StmtListNode.decompile");
        System.exit(-1);
    }
    }

    // sequence of kids (StmtNodes)
    private Sequence myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(Sequence S) {
        myExps = S;
    }

    public void decompile(PrintWriter p, int indent) {
    try {
        myExps.start();
        if (myExps.isCurrent()) {
        ((ExpNode)myExps.getCurrent()).decompile(p, indent);
        myExps.advance();
        }
        while (myExps.isCurrent()) {
        p.print(", ");
        ((ExpNode)myExps.getCurrent()).decompile(p, indent);
        myExps.advance();
        }
    } catch (NoCurrentException ex) {
        System.err.println("unexpected NoCurrentException in ExpListNode.decompile");
        System.exit(-1);
    }
    }

    // sequence of kids (ExpNodes)
    private Sequence myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************
abstract class DeclNode extends ASTnode
{
}

class FieldDeclNode extends DeclNode {
    public FieldDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("static ");
        myType.decompile(p, indent);
        p.print(" ");
        myId.decompile(p, indent);
        p.println(";");
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.decompile(p, indent);
        p.print(" ");
        myId.decompile(p, indent);
        p.println(";");
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class MethodDeclNode extends DeclNode {
    public MethodDeclNode(TypeNode type, IdNode id, FormalsListNode formalList,
              MethodBodyNode body) {
        myReturnType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("public static ");
        myReturnType.decompile(p, indent);
        p.print(" ");
        myId.decompile(p, indent);
        myFormalsList.decompile(p, indent);
        myBody.decompile(p, indent);
    }

    // 4 kids
    private TypeNode myReturnType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private MethodBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void decompile(PrintWriter p, int indent) {
        myType.decompile(p, indent);
        p.print(" ");
        myId.decompile(p, indent);
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************
abstract class TypeNode extends ASTnode {
}

class IntNode extends TypeNode
{
    public IntNode() {
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BooleanNode extends TypeNode
{
    public BooleanNode() {
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("boolean");
    }
}

class StringNode extends TypeNode
{
    public StringNode() {
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("string");
    }
}

class VoidNode extends TypeNode
{
    public VoidNode() {
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("void");
    }
}


// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
}

class PrintStmtNode extends StmtNode {
    public PrintStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("print((");
        myExp.decompile(p, indent);
        p.println("));");
    }

    // 1 kid
    private ExpNode myExp;
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(IdNode id, ExpNode exp) {
        myId = id;
        myExp = exp;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        myId.decompile(p, indent);
        p.print(" = (");
        myExp.decompile(p, indent);
        p.println(");");
    }

    // 2 kids
    private IdNode myId;
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, StmtListNode slist1, StmtListNode slist2) {
        myExp = exp;
        myThenStmtList = slist1;
        myElseStmtList = slist2;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if ((");
        myExp.decompile(p, indent);
        p.println(")) {");
        myThenStmtList.decompile(p, indent+2);
        doIndent(p, indent);
        p.println("}");
        if (myElseStmtList != null) {
            doIndent(p, indent);
            p.println("else {");
            myElseStmtList.decompile(p, indent+2);
            doIndent(p, indent);
            p.println("}");
        }
    }

    // 3 kids
    private ExpNode myExp;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
}

class DoWhileStmtNode extends StmtNode {
    public DoWhileStmtNode(StmtListNode slist, ExpNode exp) {
        myStmtList = slist;
        myExp = exp;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.println("do {");
        myStmtList.decompile(p, indent+2);
        doIndent(p, indent);
        p.print("} while ((");
        myExp.decompile(p, indent);
        p.println("))");
    }

    // 2 kids
    private StmtListNode myStmtList;
    private ExpNode myExp;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(IdNode id, ExpListNode elist) {
        myId = id;
        myExpList = elist;
    }

    public CallStmtNode(IdNode id) {
        myId = id;
        myExpList = new ExpListNode(new Sequence());
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        myId.decompile(p, indent);
        p.print("(");
        myExpList.decompile(p, indent);
        p.println(");");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;
}


class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" (");
            myExp.decompile(p, indent);
            p.print(")");
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp;
}

class BlockStmtNode extends StmtNode {
    public BlockStmtNode(DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.println("{");
        myDeclList.decompile(p, indent+2);
        myStmtList.decompile(p, indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class SwitchStmtNode extends StmtNode {
    public SwitchStmtNode(ExpNode exp, SwitchGroupListNode groupList) {
        myExp = exp;
        myGroupList = groupList;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("switch ((");
        myExp.decompile(p, indent);
        p.println(")) {");
        myGroupList.decompile(p, indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    // 2 kids
    private ExpNode myExp;
    private SwitchGroupListNode myGroupList;
}

class SwitchGroupListNode extends ASTnode {
    public SwitchGroupListNode(Sequence S) {
        myGroups = S;
    }

    public Sequence getList() { return myGroups; }

    public void decompile(PrintWriter p, int indent) {
    try {
        for (myGroups.start(); myGroups.isCurrent(); myGroups.advance()) {
        ((SwitchGroupNode)myGroups.getCurrent()).decompile(p, indent);
        }
    } catch (NoCurrentException ex) {
        System.err.println("unexpected NoCurrentException in SwitchGroupListNode.decompile");
        System.exit(-1);
    }
    }
    
    // sequence of kids (SwitchGroupNodes)
    private Sequence myGroups;
}

class SwitchGroupNode extends ASTnode {
    public SwitchGroupNode(SwitchLabelNode label, StmtListNode stmtList) {
        myLabel = label;
        myStmtList = stmtList;
    }

    public void decompile(PrintWriter p, int indent) {
        myLabel.decompile(p, indent);
        myStmtList.decompile(p, indent+2);
    }

    // 2 kids
    private SwitchLabelNode myLabel;
    private StmtListNode myStmtList;
}

abstract class SwitchLabelNode extends ASTnode {
}

class DefaultLabelNode extends SwitchLabelNode {
    public DefaultLabelNode() {
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.println("default:");
    }
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int colNum, int intVal) {
        myIntVal = intVal;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int colNum, String strVal) {
        myStrVal = strVal;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int colNum) {
    }

    public void decompile(PrintWriter p, int indent) {
    p.print("true");
    }
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int colNum) {
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("false");
    }
}

class IdNode extends ExpNode
{
    public IdNode(int lineNum, int charNum, String strVal) {
        myStrVal = strVal;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private String myStrVal;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode id, ExpListNode elist) {
        myId = id;
        myExpList = elist;
    }

    public CallExpNode(IdNode id) {
    myId = id;
    myExpList = new ExpListNode(new Sequence());
    }

    public void decompile(PrintWriter p, int indent) {
        myId.decompile(p, indent);
        p.print("(");
        myExpList.decompile(p, indent);
        p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode
{
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode
{
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(-(");
        myExp.decompile(p, indent);
        p.print("))");
    }
}

class NotNode extends UnaryExpNode
{
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(!(");
        myExp.decompile(p, indent);
        p.print("))");
    }
}

class PlusNode extends BinaryExpNode
{
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") + (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class MinusNode extends BinaryExpNode
{
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") - (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class TimesNode extends BinaryExpNode
{
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") * (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class DivideNode extends BinaryExpNode
{
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") / (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class ModNode extends BinaryExpNode
{
    public ModNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") % (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class PowerNode extends BinaryExpNode
{
    public PowerNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") ** (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class AndNode extends BinaryExpNode
{
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") && (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class OrNode extends BinaryExpNode
{
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") || (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class EqualsNode extends BinaryExpNode
{
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") == (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class NotEqualsNode extends BinaryExpNode
{
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") != (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class LessNode extends BinaryExpNode
{
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") < (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class GreaterNode extends BinaryExpNode
{
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") > (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class LessEqNode extends BinaryExpNode
{
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") <= (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class GreaterEqNode extends BinaryExpNode
{
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") >= (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, StmtListNode slist) {
        myExp = exp;
        myStmtList = slist;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while ((");
        myExp.decompile(p, indent);
        p.println(")) {");
        myStmtList.decompile(p, indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    // 2 kids
    private ExpNode myExp;
    private StmtListNode myStmtList;
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(IdNode id, ExpNode exp) {
        myId = id;
        myExp = exp;
    }

    public void decompile(PrintWriter p, int indent) {
        myId.decompile(p, indent);
        p.print(" = ");
        myExp.decompile(p, indent);
    }

    // 2 kids
    private IdNode myId;
    private ExpNode myExp;
}

class ReadIntExpNode extends ExpNode {
    public ReadIntExpNode() {
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("read()");
    }
}

class CaseLabelNode extends SwitchLabelNode {
    public CaseLabelNode(IntLitNode intLit) {
        myIntLit = intLit;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("case ");
        myIntLit.decompile(p, indent);
        p.println(":");
    }

    // 1 kid
    private IntLitNode myIntLit;
}
