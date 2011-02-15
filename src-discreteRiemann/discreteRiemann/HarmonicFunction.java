package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.Vertex;

import java.util.Arrays;
import java.util.List;

public class HarmonicFunction 
<
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & HasRho,
F extends Face<V, E, F>
> {

	double [] f;
	int [] eps;
	
	DirichletFunctionalNew diri;
	
	public static class OnGraph 
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
	> 
	extends HarmonicFunction {
		
		public OnGraph(DiscreteConformalStructure g) {
			f    = new double[g.getNumVertices()];
			eps  = new int[g.getNumEdges()];
			diri = new DirichletFunctionalNew.OnGraph(g, eps, f );
		}
		
		public OnGraph(DiscreteConformalStructure g, List quadCycle ) {
			this(g);
			compute( quadCycle );
		}
		
		public OnGraph(DiscreteConformalStructure g, List quadCycle, double [] initValues ) {
			this(g);
			compute( quadCycle, initValues );
		}
		
		public void computeHarmonic1Form( double [] form ) {
			CycleUtility.grad( diri.G, f, eps, form );  //TODO: should grad be in CycleUtility?
		}
		
		public double [] harmonic1Form() {
			return CycleUtility.grad( diri.G, f, eps );
		}
	}
	
	public static class OnDual 
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F> & HasRho,
	F extends Face<V, E, F>
	> 
	extends HarmonicFunction {
		
		public OnDual(DiscreteConformalStructure g) {
			f    = new double[g.getNumFaces()];
			eps  = new int[g.getNumEdges()];
			diri = new DirichletFunctionalNew.OnDual(g, eps, f );
		}
		
		public OnDual(DiscreteConformalStructure g, List quadCycle) {
			this(g);
			compute( quadCycle );
		}
		
		public OnDual(DiscreteConformalStructure g, List quadCycle, double [] initValues ) {
			this(g);
			compute( quadCycle, initValues );
		}
		
		public void computeHarmonic1Form( double [] form ) {
			CycleUtility.gradDual( diri.G, f, eps, form );  //TODO: should grad be in CycleUtility
		}
		
		public double [] harmonic1Form() {
			return CycleUtility.gradDual( diri.G, f, eps );
		}
	}
	

	public void setQuadCycle( List<? extends Edge> quadCycle ) {
		diri.setQuadCycle( quadCycle );
	}
	
	public void compute( List<? extends Edge> quadCycle ) {
		Arrays.fill(f,0.5);
		compute( quadCycle, f );
	}
	
	public void compute( List<? extends Edge> quadCycle, double [] f ) {
		if( f != this.f ) {
			//Arrays.fill(f,0.5);
			System.arraycopy(f,0,this.f,0,this.f.length);
		}
		setQuadCycle(quadCycle);
		compute();
	}
	
	public void compute() {
		ConjugateGradient.search( this.f, 1e-15, diri, 100000, false, null);
	}
}
