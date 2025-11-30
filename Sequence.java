// **********************************************************************
// Sequence class
//
// A Sequence is an ordered list of non-null objects.
// Methods are provided for inserting an object at the front of a Sequence,
// for iterating through the Sequence, and for determining the length of the
// Sequence.
//
// Public Methods
// ==============
//
// constructor
// -----------
// Sequence()    -- initialize the Sequence to be empty
//
// mutators/modifiers
// ------------------
// addToFront(Object ob)  -- add ob to the front of the Sequence
// addToEnd(Object ob)    -- add ob to the end of the Sequence
// void start()           -- make the first object be the current one
// void advance()         -- error if there is no current object
//                        -- otherwise, make the object following the
//                        -- current object be current (if the current
//                        -- object was the last object in the Sequence,
//                        -- then make there be NO current object)
//
// other operations
// ----------------
// int length()           -- return the length of the Sequence
// boolean isCurrent()    -- return true iff there is a current object
// Object getCurrent()    -- error if there is no current object
//                        -- otherwise, return the current object
// 
// **********************************************************************
public class Sequence {

    // ******************************************************************
    // Seqnode class
    //
    // This class defines the individual nodes used to implement a
    // Sequence as a linked list.
    // ******************************************************************
    static class Seqnode {
        // fields
        Object data;
        Seqnode next;
    
        // methods
        // 3 constructors
        public Seqnode() {
    	this(null, null);
        }
    
        public Seqnode(Object d) {
    	this(d, null);
        }
    
        public Seqnode(Object d, Seqnode n) {
    	data = d;
    	next = n;
        }
    }


    // fields of the Sequence class
    private Seqnode header;
    private Seqnode last;
    private int size;
    private Seqnode current;

    // methods

    // ******************
    // * constructor
    // ******************
    public Sequence() {
	header = new Seqnode();
	last = header;
	size = 0;
	current = null;
    }

    // ******************
    // * length
    // ******************
    public int length() {
	return size;
    }

    // ******************
    // * start
    // ******************
    public void start() {
	current = header.next;
    }

    // ******************
    // * getCurrent
    // ******************
    public Object getCurrent() throws NoCurrentException {
	if (current == null) {
	    throw new NoCurrentException();
	}
	return current.data;
    }

    // ******************
    // * advance
    // ******************
    public void advance() throws NoCurrentException {
	if (current == null) {
	    throw new NoCurrentException();
	}
	else current = current.next;
    }

    // ******************
    // * addToFront
    // ******************
    public void addToFront(Object ob) {
	header.next = new Seqnode(ob, header.next);
	if (last == header) {
	    last = header.next;
	}
	size++;
    }

    // ******************
    // * addToEnd
    // ******************
    public void addToEnd(Object ob) {
	last.next = new Seqnode(ob, null);
	last = last.next;
	size++;
    }

    // ******************
    // * isCurrent
    // ******************
    public boolean isCurrent() {
	return(current != null);
    }
}

