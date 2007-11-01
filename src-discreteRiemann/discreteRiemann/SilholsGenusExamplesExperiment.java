package discreteRiemann;

import halfedge.HalfEdgeDataStructure;
import de.jtem.blas.ComplexMatrix;
import de.jtem.blas.IntegerMatrix;
import de.jtem.mfc.field.Complex;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

public class SilholsGenusExamplesExperiment {
	
	public static Experiment create( String name, int discr  ) {
		Experiment experiment = new Experiment( name );

		experiment.addDescription(Experiment.NOV, discr);
		//experiment.addDescription(Experiment.IS_INTRINSIC, !DiscreteImmersionR3.useRhoByImmersion);

		experiment.setExpected( ComplexMatrix.id(2).times( new Complex(0,1)) );
		
		return experiment;	
	}
	
	static ComplexMatrix periodMatrixFor3QuadExample() {
		ComplexMatrix pm = new ComplexMatrix(2);
		
		Complex z = new Complex( 0, 1.0 / 3.0);
		
		pm = new IntegerMatrix( new int[][] { {5,-4}, {-4,5} } ).times(z);
				
		return pm;
	}
	
	static ComplexMatrix periodMatrixFor4QuadExample() {
		ComplexMatrix pm = new ComplexMatrix(2);
		
		Complex a = new Complex( -2, 2*Math.sqrt(2)) ;
		Complex b = new Complex(  1, - Math.sqrt(2)) ;
		
		pm.set( 0, 0, a );
		pm.set( 0, 1, b );
		pm.set( 1, 0, b );
		pm.set( 1, 1, a );
				
		return pm.divide(3);
	}
	
	static ComplexMatrix periodMatrixFor6QuadExample() {
		ComplexMatrix pm = new ComplexMatrix(2);
		
		Complex z = new Complex( 0, 1.0 / Math.sqrt(3.0) );
		
		pm = new IntegerMatrix( new int[][] { {2,-1}, {-1,2} } ).times(z);
				
		return pm;
	}
	
	static ComplexMatrix periodMatrixFor2x4QuadExample() {
		 
ComplexMatrix pm = new ComplexMatrix(3);
		
		Complex z1 = new Complex( 0,   1.23460901581265  ) ;
		Complex z2 = new Complex( 0.5, 0.73460901581265  ) ;
		Complex z3 = new Complex(-0.5, 0.5               ) ;
		
		Complex z4 = z2.times(2);
		
		pm.set( 0, 0, z1 ); pm.set( 0, 1, z2 ); pm.set( 0, 2, z3 );
		pm.set( 1, 0, z2 ); pm.set( 1, 1, z4 ); pm.set( 1, 2, z2 );
		pm.set( 2, 0, z3 ); pm.set( 2, 1, z2 ); pm.set( 2, 2, z1 );
		
		return pm;
	}
	
	static ComplexMatrix periodMatrixFor2x4bQuadExample() {
		 
		Complex z = new Complex( 0.5, 0.5 );
		
		ComplexMatrix pm = new IntegerMatrix( new int[][] { {2, 1, 1}, {1, 2, 1}, {1, 1, 2}  } ).times(z);
				
		return pm;
	}
	public static void doSilholsExampleExperiment( String name, HalfEdgeDataStructure ds, ComplexMatrix expectedPM ) {

		
		DiscreteRiemann dr = new DiscreteRiemann( new DiscreteConformalStructure(ds),1);
		
		Experiment [] es = new Experiment[5];
		
		es[0] = create( name, dr.dcs.getNumVertices() );
		es[0].setExpected(expectedPM);
		es[0].setActual( dr.getPeriodMatrix() );
		
		
		for( int i=1; i<es.length; i++ ) {
			System.out.println("i="+i);
			dr = SubdivisionUtility.createSubdivisionOfDiscreteRiemann(dr);
			es[i] = create(name, dr.dcs.getNumVertices() );
			es[i].setExpected(expectedPM);
			es[i].setActual( dr.getPeriodMatrix() );
		
		}

		Experiment.printStandardTable(es);
	}
	
		
		public static void main(String[] args) {
			
		doSilholsExampleExperiment(
				"3 quad example",
				SilholsGenusExamples.create3QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class),
				periodMatrixFor3QuadExample() );
				
		doSilholsExampleExperiment(
				"4 quad example",
				SilholsGenusExamples.create4QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class),
				periodMatrixFor4QuadExample() );
		
		doSilholsExampleExperiment(
				"6 quad example",
				SilholsGenusExamples.create6QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class),
				periodMatrixFor6QuadExample() );
		
//		doSilholsExampleExperiment(
//				"2x4 quad example",
//				SilholsGenusExamples.create2x4QuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class),
//				periodMatrixFor2x4QuadExample() );
//	}
//	doSilholsExampleExperiment(
//			"2x4 b quad example",
//			SilholsGenusExamples.create2x4bQuadExample(ConfVertex.class,ConfEdge.class,ConfFace.class),
//			periodMatrixFor2x4bQuadExample() );
}
		
//		name  #vertices  l2-norm error  l-inf-norm error  l-inf-norm error mod sign  l-inf-norm error mod sign  is intrinsic  
//		3 quad example     25 1.129938e-08 6.904365e-09 1.129938e-08 1.129938e-08 ??? 
//		3 quad example    106 3.375121e-08 2.670217e-08 3.375121e-08 3.375121e-08 ??? 
//		3 quad example    430 4.752508e-08 2.986982e-08 4.752508e-08 4.752508e-08 ??? 
//		3 quad example   1726 1.421688e-07 1.193694e-07 1.421688e-07 1.421688e-07 ??? 
//		3 quad example   6910 1.345890e-06 9.207091e-07 1.345890e-06 1.345890e-06 ??? 
//
//		name  #vertices  l2-norm error  l-inf-norm error  l-inf-norm error mod sign  l-inf-norm error mod sign  is intrinsic  
//		4 quad example     14 1.428863e+00 9.535474e-01 3.395760e-02 3.395760e-02 ??? 
//		4 quad example     62 1.418249e+00 9.458160e-01 9.508749e-03 9.508749e-03 ??? 
//		4 quad example    254 1.415242e+00 9.435791e-01 2.435246e-03 2.435246e-03 ??? 
//		4 quad example   1022 1.414472e+00 9.430027e-01 6.122492e-04 6.122492e-04 ??? 
//		4 quad example   4094 6.666665e-01 3.333335e-01 1.531453e-04 1.531453e-04 ??? 
//		
//		name  #vertices  l2-norm error  l-inf-norm error  l-inf-norm error mod sign  l-inf-norm error mod sign  is intrinsic  
//		6 quad example     22 5.743953e-03 3.632795e-03 5.743953e-03 5.743953e-03 ??? 
//		6 quad example     94 1.633502e+00 1.155060e+00 1.137406e-03 1.137406e-03 ??? 
//		6 quad example    382 1.633079e+00 1.154761e+00 1.915410e-04 1.915410e-04 ??? 
//		6 quad example   1534 3.087766e-05 1.956912e-05 3.087766e-05 3.087766e-05 ??? 
//		6 quad example   6142 1.632995e+00 1.154702e+00 4.607676e-06 4.607676e-06 ??? 

	
}
