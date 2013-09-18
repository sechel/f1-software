package minimalsurface.frontend.content;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.proxy.scene.SceneGraphComponent;

public class Ring extends IndexedFaceSet {

	
	public Ring() {
		this(0.1, 40, 20);
	}
	
	public Ring(double radius, int bDetail, int sDetail) {
		super("Ring");
		makeGeometry(radius, bDetail, sDetail);
	}

	
	private void makeGeometry(final double radius, int bDetail, int sDetail) {
	    ParametricSurfaceFactory.Immersion immersion =
	        new ParametricSurfaceFactory.Immersion() {

                public int getDimensionOfAmbientSpace() {
                    return 3;
                }

                public void evaluate(double x, double y, double[] targetArray, int arrayLocation) {
                    double sRMulSinY=radius*Math.sin(y);
                    targetArray[arrayLocation  ] = Math.cos(-x)*(1.0 + sRMulSinY);
                    targetArray[arrayLocation+1] = Math.sin(-x)*(1.0 + sRMulSinY);   
                    targetArray[arrayLocation+2] = radius*Math.cos(y);
                }

				public boolean isImmutable() {
					return true;
				}
	        
	    };
	    
	    ParametricSurfaceFactory factory = new ParametricSurfaceFactory( immersion);
	    
	    factory.setULineCount(bDetail+1);
	    factory.setVLineCount(sDetail+1);
	    
	    factory.setClosedInUDirection(true);
	    factory.setClosedInVDirection(true);
	    
	    factory.setUMax(2*Math.PI);
	    factory.setVMax(2*Math.PI);
	    
	    factory.setGenerateFaceNormals(true);
	    factory.setGenerateVertexNormals(true);
	    factory.setGenerateEdgesFromFaces(true);
	    factory.setEdgeFromQuadMesh(true);
	    factory.update();
	    IndexedFaceSet g = factory.getIndexedFaceSet();
		setVertexCountAndAttributes(g.getVertexAttributes());
		setEdgeCountAndAttributes(g.getEdgeAttributes());
		setFaceCountAndAttributes(g.getFaceAttributes());
	}
	

	
	public static void main(String[] args) {
		Ring r = new Ring(0.3, 40, 20);
		Appearance app = new Appearance();
		app.setAttribute(VERTEX_DRAW, false);
		app.setAttribute(EDGE_DRAW, false);
		SceneGraphComponent c = new SceneGraphComponent();
		c.setGeometry(r);
		c.setAppearance(app);
		JRViewer.display(c);
	}
	
	
}
