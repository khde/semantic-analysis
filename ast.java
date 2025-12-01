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

import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a "Simple" program.
// **********************************************************************

abstract class ASTnode { 
    abstract public void decompile(PrintWriter p, int indent);

    public void analyzeNames(SymbolTable st) {
    }

    public int checkTypes() {
        return Types.ErrorType; 
    }

    public int getLine() { return 0; }
    public int getChar() { return 0; }
    // ----------------------------------

    protected static int currentMethodReturnType = Types.ErrorType;

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

    public void analyzeNames(SymbolTable st) {
        // 1. Enter Global/Class Scope
        st.enterScope();

        myClassBody.analyzeNames(st);

        SymbolTable.Sym mainSym = st.lookupLocal("main");
        if (mainSym == null) {
            Errors.fatal(0, 0, "No main method declared");
        }

        st.exitScope();
    }

    public int checkTypes() {
        myClassBody.checkTypes();
        return Types.VoidType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("public class ");
        myId.decompile(p, 0);
        p.println(" {");
        myClassBody.decompile(p, indent+2);
        p.println("}");
    }

    private IdNode myId;
    private ClassBodyNode myClassBody;
}

class ClassBodyNode extends ASTnode {
    public ClassBodyNode(DeclListNode declList) {
        myDeclList = declList;
    }

    public void analyzeNames(SymbolTable st) {
        myDeclList.analyzeNames(st);
    }

    public int checkTypes() {
        myDeclList.checkTypes();
        return Types.VoidType;
    }

    public void decompile(PrintWriter p, int indent) {
        myDeclList.decompile(p, indent);
    }

    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(Sequence S) {
        myDecls = S;
    }

    public void analyzeNames(SymbolTable st) {
        try {
            for (myDecls.start(); myDecls.isCurrent(); myDecls.advance()) {
                ((DeclNode)myDecls.getCurrent()).analyzeNames(st);
            }
        } catch (NoCurrentException ex) {}
    }

    public int checkTypes() {
        try {
            for (myDecls.start(); myDecls.isCurrent(); myDecls.advance()) {
                ((DeclNode)myDecls.getCurrent()).checkTypes();
            }
        } catch (NoCurrentException ex) {}
        return Types.VoidType;
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
    private Sequence myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(Sequence S) {
        myFormals = S;
    }

    public void analyzeNames(SymbolTable st) {
        try {
            for (myFormals.start(); myFormals.isCurrent(); myFormals.advance()) {
                ((FormalDeclNode)myFormals.getCurrent()).analyzeNames(st);
            }
        } catch (NoCurrentException ex) {}
    }

    public List<SymbolTable.Sym> getSignatureParams() {
        List<SymbolTable.Sym> params = new ArrayList<>();
        try {
            for (myFormals.start(); myFormals.isCurrent(); myFormals.advance()) {
                FormalDeclNode node = (FormalDeclNode) myFormals.getCurrent();
                params.add(new SymbolTable().new Sym(node.name(), node.getType()));
            }
        } catch (NoCurrentException ex) {}
        return params;
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

    public Sequence getList() { return myFormals; }
    private Sequence myFormals;
}

class MethodBodyNode extends ASTnode {
    public MethodBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void analyzeNames(SymbolTable st) {
        myDeclList.analyzeNames(st);
        myStmtList.analyzeNames(st);
    }

    public int checkTypes() {
        myDeclList.checkTypes();
        myStmtList.checkTypes();
        return Types.VoidType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.println(" {");
        myDeclList.decompile(p, indent+2);
        myStmtList.decompile(p, indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(Sequence S) {
        myStmts = S;
    }

    public void analyzeNames(SymbolTable st) {
        try {
            for (myStmts.start(); myStmts.isCurrent(); myStmts.advance()) {
                ((StmtNode)myStmts.getCurrent()).analyzeNames(st);
            }
        } catch (NoCurrentException ex) {}
    }

    public int checkTypes() {
        try {
            for (myStmts.start(); myStmts.isCurrent(); myStmts.advance()) {
                ((StmtNode)myStmts.getCurrent()).checkTypes();
            }
        } catch (NoCurrentException ex) {}
        return Types.VoidType;
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

    private Sequence myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(Sequence S) {
        myExps = S;
    }

    public void analyzeNames(SymbolTable st) {
        try {
            for (myExps.start(); myExps.isCurrent(); myExps.advance()) {
                ((ExpNode)myExps.getCurrent()).analyzeNames(st);
            }
        } catch (NoCurrentException ex) {}
    }

    public List<Integer> getTypes() {
        List<Integer> types = new ArrayList<>();
        try {
            for (myExps.start(); myExps.isCurrent(); myExps.advance()) {
                types.add(((ExpNode)myExps.getCurrent()).checkTypes());
            }
        } catch (NoCurrentException ex) {}
        return types;
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

    private Sequence myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************
abstract class DeclNode extends ASTnode {
}

class FieldDeclNode extends DeclNode {
    public FieldDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void analyzeNames(SymbolTable st) {
        if (st.lookupLocal(myId.strVal()) != null) {
            Errors.fatal(myId.getLine(), myId.getChar(), "Identifier declared multiple times");
        } else {
            SymbolTable.Sym sym = st.new Sym(myId.strVal(), myType.getType());
            st.insert(sym);
            myId.link(sym);
        }
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("static ");
        myType.decompile(p, indent);
        p.print(" ");
        myId.decompile(p, indent);
        p.println(";");
    }

    private TypeNode myType;
    private IdNode myId;
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void analyzeNames(SymbolTable st) {
        if (st.lookupLocal(myId.strVal()) != null) {
            Errors.fatal(myId.getLine(), myId.getChar(), "Identifier declared multiple times");
        } else {
            SymbolTable.Sym sym = st.new Sym(myId.strVal(), myType.getType());
            st.insert(sym);
            myId.link(sym);
        }
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.decompile(p, indent);
        p.print(" ");
        myId.decompile(p, indent);
        p.println(";");
    }

    private TypeNode myType;
    private IdNode myId;
}

class MethodDeclNode extends DeclNode {
    public MethodDeclNode(TypeNode type, IdNode id, FormalsListNode formalList, MethodBodyNode body) {
        myReturnType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void analyzeNames(SymbolTable st) {
        if (st.lookupLocal(myId.strVal()) != null) {
            Errors.fatal(myId.getLine(), myId.getChar(), "Identifier declared multiple times");
        }

        List<SymbolTable.Sym> params = myFormalsList.getSignatureParams();
        SymbolTable.Sym methodSym = st.new Sym(myId.strVal(), myReturnType.getType(), params);
        st.insert(methodSym);
        myId.link(methodSym);

        st.enterScope();

        myFormalsList.analyzeNames(st);
        myBody.analyzeNames(st);

        st.exitScope();
    }

    public int checkTypes() {
        currentMethodReturnType = myReturnType.getType();
        myBody.checkTypes();
        return Types.VoidType;
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

    public String name() { return myId.strVal(); }
    public int getType() { return myType.getType(); }

    public void analyzeNames(SymbolTable st) {
        if (st.lookupLocal(myId.strVal()) != null) {
            Errors.fatal(myId.getLine(), myId.getChar(), "Identifier declared multiple times");
        } else {
            SymbolTable.Sym sym = st.new Sym(myId.strVal(), myType.getType());
            st.insert(sym);
            myId.link(sym);
        }
    }

    public void decompile(PrintWriter p, int indent) {
        myType.decompile(p, indent);
        p.print(" ");
        myId.decompile(p, indent);
    }

    private TypeNode myType;
    private IdNode myId;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************
abstract class TypeNode extends ASTnode {
    abstract public int getType();
}

class IntNode extends TypeNode {
    public IntNode() {}
    public int getType() { return Types.IntType; }
    public void decompile(PrintWriter p, int indent) { p.print("int"); }
}

class BooleanNode extends TypeNode {
    public BooleanNode() {}
    public int getType() { return Types.BoolType; }
    public void decompile(PrintWriter p, int indent) { p.print("boolean"); }
}

class StringNode extends TypeNode {
    public StringNode() {}
    public int getType() { return Types.StringType; }
    public void decompile(PrintWriter p, int indent) { p.print("string"); }
}

class VoidNode extends TypeNode {
    public VoidNode() {}
    public int getType() { return Types.VoidType; }
    public void decompile(PrintWriter p, int indent) { p.print("void"); }
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

    public void analyzeNames(SymbolTable st) { myExp.analyzeNames(st); }
    public int checkTypes() { myExp.checkTypes(); return Types.VoidType; }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("print((");
        myExp.decompile(p, indent);
        p.println("));");
    }

    private ExpNode myExp;
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(IdNode id, ExpNode exp) {
        myId = id;
        myExp = exp;
    }

    public void analyzeNames(SymbolTable st) {
        myId.analyzeNames(st);
        myExp.analyzeNames(st);
    }

    public int checkTypes() {
        int tId = myId.checkTypes();
        int tExp = myExp.checkTypes();

        if (tId != Types.ErrorType && tExp != Types.ErrorType) {
            if (tId != tExp) {
                Errors.fatal(myId.getLine(), myId.getChar(), "Assignment type mismatch");
            }
        }
        return Types.VoidType;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        myId.decompile(p, indent);
        p.print(" = (");
        myExp.decompile(p, indent);
        p.println(");");
    }

    private IdNode myId;
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, StmtListNode slist1, StmtListNode slist2) {
        myExp = exp;
        myThenStmtList = slist1;
        myElseStmtList = slist2; 
    }

    public void analyzeNames(SymbolTable st) {
        myExp.analyzeNames(st);
        st.enterScope();
        myThenStmtList.analyzeNames(st);
        st.exitScope();
        if (myElseStmtList != null) {
            st.enterScope();
            myElseStmtList.analyzeNames(st);
            st.exitScope();
        }
    }

    public int checkTypes() {
        int condType = myExp.checkTypes();
        if (condType != Types.BoolType && condType != Types.ErrorType) {
             Errors.fatal(myExp.getLine(), myExp.getChar(), "Non-boolean condition in if-statement");
        }
        myThenStmtList.checkTypes();
        if (myElseStmtList != null) myElseStmtList.checkTypes();
        return Types.VoidType;
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

    private ExpNode myExp;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
}

class DoWhileStmtNode extends StmtNode {
    public DoWhileStmtNode(StmtListNode slist, ExpNode exp) {
        myStmtList = slist;
        myExp = exp;
    }

    public void analyzeNames(SymbolTable st) {
        st.enterScope();
        myStmtList.analyzeNames(st);
        st.exitScope();
        myExp.analyzeNames(st);
    }

    public int checkTypes() {
        myStmtList.checkTypes();
        int condType = myExp.checkTypes();
        if (condType != Types.BoolType && condType != Types.ErrorType) {
             Errors.fatal(myExp.getLine(), myExp.getChar(), "Non-boolean condition in if-statement");
        }
        return Types.VoidType;
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

    public void analyzeNames(SymbolTable st) {
        myId.analyzeNames(st);
        myExpList.analyzeNames(st);
    }

    public int checkTypes() {
        SymbolTable.Sym sym = myId.getSym();
        if (sym == null) return Types.ErrorType;

        List<Integer> argTypes = myExpList.getTypes();
        List<SymbolTable.Sym> paramSyms = sym.paramTypes;

        if (argTypes.size() != paramSyms.size()) {
            Errors.fatal(myId.getLine(), myId.getChar(), "Incorrect number of arguments");
            return Types.ErrorType;
        }

        for (int i = 0; i < argTypes.size(); i++) {
            int argT = argTypes.get(i);
            int paramT = paramSyms.get(i).type;
            if (argT != Types.ErrorType && argT != paramT) {
                Errors.fatal(myId.getLine(), myId.getChar(), "Assignment type mismatch");
            }
        }
        return Types.VoidType;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        myId.decompile(p, indent);
        p.print("(");
        myExpList.decompile(p, indent);
        p.println(");");
    }

    private IdNode myId;
    private ExpListNode myExpList;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) { 
        myExp = exp;
    }

    public void analyzeNames(SymbolTable st) {
        if (myExp != null) myExp.analyzeNames(st);
    }

    public int checkTypes() {
        int returnValType = (myExp != null) ? myExp.checkTypes() : Types.VoidType;
        if (returnValType != Types.ErrorType && currentMethodReturnType != Types.ErrorType) {
            if (returnValType != currentMethodReturnType) {
                int line = (myExp != null) ? myExp.getLine() : 0;
                int col = (myExp != null) ? myExp.getChar() : 0;
                Errors.fatal(line, col, "Incorrect return type");
            }
        }
        return Types.VoidType;
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

    private ExpNode myExp;
}

class BlockStmtNode extends StmtNode {
    public BlockStmtNode(DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void analyzeNames(SymbolTable st) {
        st.enterScope();
        myDeclList.analyzeNames(st);
        myStmtList.analyzeNames(st);
        st.exitScope();
    }

    public int checkTypes() {
        myDeclList.checkTypes();
        myStmtList.checkTypes();
        return Types.VoidType;
    }

    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.println("{");
        myDeclList.decompile(p, indent+2);
        myStmtList.decompile(p, indent+2);
        doIndent(p, indent);
        p.println("}");
    }

    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class SwitchStmtNode extends StmtNode {
    public SwitchStmtNode(ExpNode exp, SwitchGroupListNode groupList) {
        myExp = exp;
        myGroupList = groupList;
    }

    public void analyzeNames(SymbolTable st) {
        myExp.analyzeNames(st);
        myGroupList.analyzeNames(st);
    }

    public int checkTypes() {
        int t = myExp.checkTypes();
        if (t != Types.ErrorType && t != Types.IntType) {
            Errors.fatal(myExp.getLine(), myExp.getChar(), "Expression of switch statement is not integer");
        }
        myGroupList.checkTypes();
        return Types.VoidType;
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

    private ExpNode myExp;
    private SwitchGroupListNode myGroupList;
}

class SwitchGroupListNode extends ASTnode {
    public SwitchGroupListNode(Sequence S) {
        myGroups = S;
    }
    public Sequence getList() { return myGroups; }

    public void analyzeNames(SymbolTable st) {
        try {
            for (myGroups.start(); myGroups.isCurrent(); myGroups.advance()) {
                ((SwitchGroupNode)myGroups.getCurrent()).analyzeNames(st);
            }
        } catch (NoCurrentException ex) {}
    }

    public int checkTypes() {
        try {
            for (myGroups.start(); myGroups.isCurrent(); myGroups.advance()) {
                ((SwitchGroupNode)myGroups.getCurrent()).checkTypes();
            }
        } catch (NoCurrentException ex) {}
        return Types.VoidType;
    }

    public void decompile(PrintWriter p, int indent) {
        try {
            for (myGroups.start(); myGroups.isCurrent(); myGroups.advance()) {
                ((SwitchGroupNode)myGroups.getCurrent()).decompile(p, indent);
            }
        } catch (NoCurrentException ex) {
            System.exit(-1);
        }
    }
    private Sequence myGroups;
}

class SwitchGroupNode extends ASTnode {
    public SwitchGroupNode(SwitchLabelNode label, StmtListNode stmtList) {
        myLabel = label;
        myStmtList = stmtList;
    }
    public void analyzeNames(SymbolTable st) {
        myStmtList.analyzeNames(st);
    }
    public int checkTypes() {
        myStmtList.checkTypes();
        return Types.VoidType;
    }
    public void decompile(PrintWriter p, int indent) {
        myLabel.decompile(p, indent);
        myStmtList.decompile(p, indent+2);
    }
    private SwitchLabelNode myLabel;
    private StmtListNode myStmtList;
}

abstract class SwitchLabelNode extends ASTnode {
}

class DefaultLabelNode extends SwitchLabelNode {
    public DefaultLabelNode() {}
    public void decompile(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.println("default:");
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
    private IntLitNode myIntLit;
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int colNum, int intVal) {
        myLineNum = lineNum;
        myColNum = colNum;
        myIntVal = intVal;
    }
    private int myLineNum;
    private int myColNum;
    private int myIntVal;

    public int getLine() { return myLineNum; }
    public int getChar() { return myColNum; }

    public int checkTypes() { return Types.IntType; }
    public void decompile(PrintWriter p, int indent) { p.print(myIntVal); }
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int colNum, String strVal) {
        myLineNum = lineNum;
        myColNum = colNum;
        myStrVal = strVal;
    }
    private int myLineNum;
    private int myColNum;
    private String myStrVal;

    public int getLine() { return myLineNum; }
    public int getChar() { return myColNum; }

    public int checkTypes() { return Types.StringType; }
    public void decompile(PrintWriter p, int indent) { p.print(myStrVal); }
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int colNum) {
        myLineNum = lineNum;
        myColNum = colNum;
    }
    private int myLineNum;
    private int myColNum;

    public int getLine() { return myLineNum; }
    public int getChar() { return myColNum; }

    public int checkTypes() { return Types.BoolType; }
    public void decompile(PrintWriter p, int indent) { p.print("true"); }
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int colNum) {
        myLineNum = lineNum;
        myColNum = colNum;
    }
    private int myLineNum;
    private int myColNum;

    public int getLine() { return myLineNum; }
    public int getChar() { return myColNum; }

    public int checkTypes() { return Types.BoolType; }
    public void decompile(PrintWriter p, int indent) { p.print("false"); }
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    private SymbolTable.Sym mySym;
    private int myLineNum; 
    private int myCharNum; 
    private String myStrVal;

    public void link(SymbolTable.Sym s) {
        mySym = s;
    }

    public SymbolTable.Sym getSym() {
        return mySym;
    }

    public String strVal() { return myStrVal; }

    public void analyzeNames(SymbolTable st) {
        SymbolTable.Sym s = st.lookup(myStrVal);
        if (s == null) {
            Errors.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(s);
        }
    }

    public int checkTypes() {
        if (mySym != null) return mySym.type;
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("(" + Types.ToString(mySym.type) + ")");
        }
    }

    public int getLine() { return myLineNum; }
    public int getChar() { return myCharNum; }
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

    public void analyzeNames(SymbolTable st) {
        myId.analyzeNames(st);
        myExpList.analyzeNames(st);
    }

    public int checkTypes() {
        SymbolTable.Sym sym = myId.getSym();
        if (sym == null) return Types.ErrorType; 

        // Get list of argument types from the expression list
        List<Integer> argTypes = myExpList.getTypes();
        List<SymbolTable.Sym> paramSyms = sym.paramTypes;

        // Check argument count
        if (argTypes.size() != paramSyms.size()) {
            Errors.fatal(myId.getLine(), myId.getChar(), "Incorrect number of arguments");
            return Types.ErrorType;
        }

        // Check argument types
        for (int i = 0; i < argTypes.size(); i++) {
            int argT = argTypes.get(i);
            int paramT = paramSyms.get(i).type;
            if (argT != Types.ErrorType && argT != paramT) {
                Errors.fatal(myId.getLine(), myId.getChar(), "Assignment type mismatch");
            }
        }
        // Return the actual return type of the method
        return sym.type; 
    } 

    public void decompile(PrintWriter p, int indent) {
        myId.decompile(p, indent);
        p.print("(");
        myExpList.decompile(p, indent);
        p.print(")");
    }

    private IdNode myId;
    private ExpListNode myExpList;
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }
    // Delegate location to the child expression
    public int getLine() { return myExp.getLine(); }
    public int getChar() { return myExp.getChar(); }

    public void analyzeNames(SymbolTable st) { myExp.analyzeNames(st); }
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
    // Delegate location to the first child expression
    public int getLine() { return myExp1.getLine(); }
    public int getChar() { return myExp1.getChar(); }

    public void analyzeNames(SymbolTable st) {
        myExp1.analyzeNames(st);
        myExp2.analyzeNames(st);
    }
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) { super(exp); }

    public int checkTypes() {
        int t = myExp.checkTypes();
        if (t != Types.ErrorType && t != Types.IntType) {
            Errors.fatal(myExp.getLine(), myExp.getChar(), "Non-numeric operand with arithmetic operator");
            return Types.ErrorType;
        }
        return Types.IntType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(-(");
        myExp.decompile(p, indent);
        p.print("))");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) { super(exp); }

    public int checkTypes() {
        int t = myExp.checkTypes();
        if (t != Types.ErrorType && t != Types.BoolType) {
            Errors.fatal(myExp.getLine(), myExp.getChar(), "Non-boolean operand with logical operator");
            return Types.ErrorType;
        }
        return Types.BoolType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(!(");
        myExp.decompile(p, indent);
        p.print("))");
    }
}

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.IntType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-numeric operand with arithmetic operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") + (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.IntType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-numeric operand with arithmetic operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") - (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.IntType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-numeric operand with arithmetic operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") * (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.IntType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-numeric operand with arithmetic operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") / (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.BoolType && t2 == Types.BoolType) return Types.BoolType;
            Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-boolean operand with logical operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") && (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.BoolType && t2 == Types.BoolType) return Types.BoolType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-boolean operand with logical operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") || (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;

        if (t1 == t2) return Types.BoolType;

        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Type mismatch in equal-statement");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") == (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == t2) return Types.BoolType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Type mismatch in equal-statement");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") != (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.BoolType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Relational operator applied to non-numeric operand");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") < (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.BoolType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Relational operator applied to non-numeric operand");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") > (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.BoolType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-numeric operand with relational operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") <= (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.BoolType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-numeric operand with relational operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") >= (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class ModNode extends BinaryExpNode {
    public ModNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.IntType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-numeric operand with arithmetic operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") % (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class PowerNode extends BinaryExpNode {
    public PowerNode(ExpNode exp1, ExpNode exp2) { super(exp1, exp2); }

    public int checkTypes() {
        int t1 = myExp1.checkTypes();
        int t2 = myExp2.checkTypes();
        if (t1 == Types.ErrorType || t2 == Types.ErrorType) return Types.ErrorType;
        if (t1 == Types.IntType && t2 == Types.IntType) return Types.IntType;
        Errors.fatal(myExp1.getLine(), myExp1.getChar(), "Non-numeric operand with arithmetic operator");
        return Types.ErrorType;
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("((");
        myExp1.decompile(p, indent);
        p.print(") ** (");
        myExp2.decompile(p, indent);
        p.print("))");
    }
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, StmtListNode slist) {
        myExp = exp;
        myStmtList = slist;
    }

    public void analyzeNames(SymbolTable st) {
        myExp.analyzeNames(st);
        st.enterScope();
        myStmtList.analyzeNames(st);
        st.exitScope();
    }

    public int checkTypes() {
        int condType = myExp.checkTypes();
        if (condType != Types.BoolType && condType != Types.ErrorType) {
            Errors.fatal(myExp.getLine(), myExp.getChar(), "Non-boolean condition in while-statement");
        }
        myStmtList.checkTypes();
        return Types.VoidType;
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

    private ExpNode myExp;
    private StmtListNode myStmtList;
}

class ReadIntExpNode extends ExpNode {
    public ReadIntExpNode() {}
    public int checkTypes() { return Types.IntType; }
    public void decompile(PrintWriter p, int indent) { p.print("read()"); }
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(IdNode id, ExpNode exp) {
        myId = id;
        myExp = exp;
    }

    public void analyzeNames(SymbolTable st) {
        myId.analyzeNames(st);
        myExp.analyzeNames(st);
    }

    public int checkTypes() {
        int tId = myId.checkTypes();
        int tExp = myExp.checkTypes();

        if (tId != Types.ErrorType && tExp != Types.ErrorType) {
            if (tId != tExp) {
                Errors.fatal(myId.getLine(), myExp.getChar(), "Type mismatch in assignment expression");
                return Types.ErrorType;
            }
        }
        return tId; 
    }

    public void decompile(PrintWriter p, int indent) {
        p.print("(");
        myId.decompile(p, indent);
        p.print(" = ");
        myExp.decompile(p, indent);
        p.print(")");
    }

    private IdNode myId;
    private ExpNode myExp;
}
