package halfedge.surfaceutilities;

public class SurfaceException extends Exception {

	private static final long 
		serialVersionUID = 1L;
	
	public SurfaceException(String msg){
		super(msg);
	}

	public SurfaceException() {
		super();
	}

	public SurfaceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SurfaceException(Throwable arg0) {
		super(arg0);
	}
	
}
