package halfedge.decorations;

public interface HasQuadGraphLabeling {

	public static enum QuadGraphLabel{
		SPHERE,
		CIRCLE,
		INTERSECTION
	}
	
	public QuadGraphLabel getVertexLabel();
	
	public void setVertexLabel(QuadGraphLabel l);
	
}
