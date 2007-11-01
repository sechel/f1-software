/**
 * 
 */
package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;

import java.util.ArrayList;
import java.util.List;

import discreteRiemann.DirichletFunctional.Factory;

/**
 * Utilities that deal with harmonicity on a discrete conformal structure: the
 * computation of a cohomology basis given by harmonic forms, and the dual
 * harmonic form of a given cycle.
 * 
 * @author mercat
 * 
 */
public class HarmonicUtility {

	/**
	 * A cohomology basis of harmonic forms.
	 * 
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param root
	 *            the center of the fundamental polygon.
	 * @return a list of double[] of size corresponding to 1-forms.
	 */
	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> List<double[][]> 
	cohomologyBasis(
			final V root) {
		Factory factory = new DirichletFunctional.Factory<V, E, F>(
				(DiscreteConformalStructure<V, E, F>) root
						.getHalfEdgeDataStructure());
		factory.setRho(1);

		return cohomologyBasis(root, factory);
	}

	/**
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param root
	 *            the center vertex of the fundamental polygon
	 * @param factory
	 *            the utility that computes the harmonic solutions
	 * @return a list of double[][] {form, dualForm};
	 */
	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> List<double[][]> 
	cohomologyBasis(
			final V root, Factory factory) {

		List<List<E>> quadBasis = CycleUtility.cyclesToQuads(
				HomotopyUtility.homotopyBasis(root));
		return cohomologyBasis(quadBasis, factory);
	}

	
	/**
	 * A cohomology basis of harmonic forms on the graph that is dual to a homology basis.
	 * Beware that this procedure doesn't check whether the cycles are indeed
	 * independant or fulfill any normalization property.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param quadBasis
	 *            a homotopy basis given by a list of quads.
	 * @param factory
	 *            the factory containing the discrete conformal structure
	 * @return a list of double[][] {form, dualForm};
	 */
	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> double[][] 
	cohomologyBasisOnGraph(
			List<List<E>> quadBasis, Factory factory) {

		final int nof = quadBasis.size();
		
		double [][] result = new double[nof][];
		
		int i=0;
		for (List<E> quadCycle : quadBasis)
			result[i++] = harmonicFormOnGraph(quadCycle,factory);

		return result;

	}

	
	/**
	 * A cohomology basis of harmonic forms on the graph that is dual to a homology basis.
	 * Beware that this procedure doesn't check whether the cycles are indeed
	 * independant or fulfill any normalization property.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param quadBasis
	 *            a homotopy basis given by a list of quads.
	 * @param factory
	 *            the factory containing the discrete conformal structure
	 * @return a list of double[][] {form, dualForm};
	 */
	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> double[][] 
	cohomologyBasisOnDual(
			List<List<E>> quadBasis, Factory factory) {

		final int nof = quadBasis.size();
		
		double [][] result = new double[nof][];
		
		int i=0;
		for (List<E> quadCycle : quadBasis)
			result[i++] = harmonicFormOnDual(quadCycle,factory);

		return result;

	}
	/**
	 * A cohomology basis of harmonic forms, dual to a homology basis.
	 * Beware that this procedure doesn't check whether the cycles are indeed
	 * independant or fulfill any normalization property.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param quadBasis
	 *            a homotopy basis given by a list of quads.
	 * @param factory
	 *            the factory containing the discrete conformal structure
	 * @return a list of double[][] {form, dualForm};
	 */
	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> List<double[][]> 
	cohomologyBasis(
			List<List<E>> quadBasis, Factory factory) {

		List<double[][]> result = new ArrayList<double[][]>(quadBasis.size());

		for (List<E> quadCycle : quadBasis)
			result.add(dualHarmonicForm(quadCycle,
					factory));

		return result;

	}

	// /**
	// * @param <V>
	// * @param <E>
	// * @param <F>
	// * @param forms are given as a list of 2 double[], form[0][e]: one double
	// for the edge e,
	// * form[1][e]: one double for the dual edge e^*
	// * @param quadBasis a list of cycles given as quads.
	// * @return a list/cycle of list/form of 2 doubles, period[0] on the graph,
	// period[1] on the dual.
	// */
	// static public <
	// V extends Vertex<V, E, F>,
	// E extends Edge<V, E, F>,
	// F extends Face<V, E, F>
	// > List<List<double[]>> periods( final List<double[][]> forms, final
	// List<List<E>> quadBasis) {
	// ArrayList<List<double[]>> result = new
	// ArrayList<List<double[]>>(quadBasis.size());
	//	
	// for(List<E> quadCycle: quadBasis){
	// List<double[]> L = new ArrayList<double[]>(forms.size());
	// result.add(L);
	// for(double[][] form: forms){
	// L.add(period(form, quadCycle));
	// }
	// }
	// return result;
	// }

	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> 
	double[][] dualHarmonicForm(
			final List<E> cycle) {
		Factory<V, E, F> factory = new DirichletFunctional.Factory<V, E, F>(
				(DiscreteConformalStructure<V, E, F>) cycle.get(0)
						.getHalfEdgeDataStructure());
		factory.setRho(1);

		return dualHarmonicForm(CycleUtility.cycleToQuad(cycle), factory);
	}

	/**
	 * A harmonic 1-form, which gives the intersection number with a given cycle
	 * on a given HalfEdgeStructure with a given discrete Riemann Structure.
	 * Actually a couple of them, one on the graph, the other on the dual graph.
	 * It is not the dual form, it is the bi-dual form a \mapsto \alpha : \oint\gamma \alpha = (\gamma, a)
	 * 
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param quadCycle
	 *            the quadCycle across which the form should increase by 1.
	 * @param factory
	 *            the DirichletFunctional factory, containing the discrete
	 *            Riemann structure (the set of rhos).
	 * @return a list of two double[] of size corresponding to 1-forms.
	 */
	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> 
	double[][] dualHarmonicForm(
			final List<E> quadCycle,
			final DirichletFunctional.Factory<V, E, F> factory) {

		HalfEdgeDataStructure<V, E, F> dcs = quadCycle.get(0)
				.getHalfEdgeDataStructure();

		factory.setQuadCycle(quadCycle);
		factory.update(); // Computes the 1-form

		final double[] form = CycleUtility
				.grad(dcs, factory.f.f, factory.f.eps);
		final double[] dualForm = CycleUtility.gradDual(dcs, factory.fs.f,
				factory.fs.eps);

		final double[][] result = { form, dualForm };

		return result;
	}

	/**
	 * A harmonic 1-form, which gives the intersection number with a given cycle
	 * on a given HalfEdgeStructure with a given discrete Riemann Structure.
	 * Actually a couple of them, one on the graph, the other on the dual graph.
	 * It is not the dual form, it is the bi-dual form a \mapsto \alpha : \oint\gamma \alpha = (\gamma, a)
	 * 
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param quadCycle
	 *            the quadCycle across which the form should increase by 1.
	 * @param factory
	 *            the DirichletFunctional factory, containing the discrete
	 *            Riemann structure (the set of rhos).
	 * @return double[] of size corresponding to 1-forms.
	 */
	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> 
	double [] harmonicFormOnDual (
			final List<E> quadCycle,
			final DirichletFunctional.Factory<V, E, F> factory) {

		HalfEdgeDataStructure<V, E, F> dcs = quadCycle.get(0)
				.getHalfEdgeDataStructure();

		factory.setQuadCycle(quadCycle);
		factory.updateDual(); // Computes the 1-form

		//final double[] form = CycleUtility
		//		.grad(dcs, factory.f.f, factory.f.eps);
		
		final double[] dual = CycleUtility.gradDual(dcs, factory.fs.f,
				factory.fs.eps);

		return dual;
	}
	
	
	/**
	 * add documentation
	 * 
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param quadCycle
	 *            the quadCycle across which the form should increase by 1.
	 * @param factory
	 *            the DirichletFunctional factory, containing the discrete
	 *            Riemann structure (the set of rhos).
	 * @return double[] of size corresponding to 1-forms.
	 */
	static public <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasRho, F extends Face<V, E, F>> 
	double [] harmonicFormOnGraph (
			final List<E> quadCycle,
			final DirichletFunctional.Factory<V, E, F> factory) {

		HalfEdgeDataStructure<V, E, F> dcs = quadCycle.get(0)
				.getHalfEdgeDataStructure();

		factory.setQuadCycle(quadCycle);
		factory.updateGraph(); // Computes the 1-form

		final double[] form = CycleUtility
				.grad(dcs, factory.f.f, factory.f.eps);
		
		//final double[] dualForm = CycleUtility.gradDual(dcs, factory.fs.f,
		//		factory.fs.eps);

		return form;
	}
	
	
	
}
