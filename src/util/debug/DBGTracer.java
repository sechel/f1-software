package util.debug;


/**
 * A debug message tracer
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class DBGTracer {
	
    public static final int
    	DEBUG_LEVEL_LOW = 0,
    	DEBUG_LEVEL_MID = 5,
    	DEBUG_LEVEL_HIG = 10;
    
	private static boolean 
		active = true;
	private static int 
		indent = 0,
		level = DEBUG_LEVEL_MID;
	
	public static void msg(String msg){
		if (!active) return;
		String ind = "";
		for (int i = 0; i < indent; i++)
			ind += "\t";
		System.err.println(ind + "dbg >> " + msg);
	}
	
	public static void print(String msg){
		if (!active) return;
		System.err.print(msg);
	}
	
	public static void println(String msg){
		if (!active) return;
		System.err.println(msg);
	}
	
	public static void msg(String msg, int dbg_level){
	    if (dbg_level < level) return;
	    msg(msg);
	}
	
	public static void stackTrace(Throwable t, int dbg_level){
		if (dbg_level < level) return;
		t.printStackTrace();
	}
	
	public static void stackTrace(Throwable t){
		if (!active) return;
		t.printStackTrace();
	}
	
	
	public static boolean isActive() {
		return active;
	}

	public static void setActive(boolean set) {
		active = set;
	}

	public static int getIndent() {
		return indent;
	}

	public static void setIndent(int indent) {
		DBGTracer.indent = indent;
	}
	
	public static void incIndent(){
		indent++;
	}
	public static void decIndent(){
		indent--;
	}
	
    /**
     * @return Returns the level.
     */
    public static int getLevel() {
        return level;
    }
    
    /**
     * @param level The level to set.
     */
    public static void setLevel(int level) {
        DBGTracer.level = level;
    }
}
