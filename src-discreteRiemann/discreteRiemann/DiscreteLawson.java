package discreteRiemann;

import halfedge.Edge;
import halfedge.Face;
import halfedge.HalfEdgeDataStructure;
import halfedge.Vertex;
import halfedge.generator.FaceByFaceGenerator;

import java.io.File;
import java.io.IOException;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.reader.ReaderJVX;
import de.jreality.reader.ReaderOBJ;
import de.jreality.reader.ReaderVRML;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.data.Attribute;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jtem.blas.ComplexMatrix;
import de.jtem.blas.IntegerMatrix;
import de.jtem.mfc.field.Complex;
import de.jtem.mfc.vector.Real3;
import de.jtem.riemann.theta.SiegelReduction;
import discreteRiemann.DiscreteConformalStructure.ConfEdge;
import discreteRiemann.DiscreteConformalStructure.ConfFace;
import discreteRiemann.DiscreteConformalStructure.ConfVertex;

public class DiscreteLawson <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & HasRho, 
F extends Face<V, E, F> 
>  extends DiscreteImmersionR3 {

	IndexedFaceSet ifs;
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
//		DiscreteLawson dl = createDiscreteLawson();
//		dl.writeOBJ();
//		display(dl);
		result();	
		//experiment();
	}

	private static void experiment() {
		DiscreteLawson dl = createDiscreteLawson();
		
		display(dl);
		

		HalfEdgeDataStructure ds = dl.g; 

		DiscreteRiemann dr = new DiscreteRiemann( new DiscreteConformalStructure(ds),null);
		
		ComplexMatrix pm = dr.getPeriodMatrix();
		System.out.println("period matrix: " + pm );
		pm.assignTimes( new Complex( 0, 2*Math.PI ) );
		System.out.println("period matrix: " + pm );
		ComplexMatrix pmRed = new SiegelReduction( pm ).getReducedPeriodMatrix();
		pmRed = pmRed.divide( new Complex( 0, 2*Math.PI ));
		System.out.println("reduced: " + pmRed);
	}
	
	public static ComplexMatrix lawson_conj() {
		ComplexMatrix pm = new ComplexMatrix(2);
		
		Complex z = new Complex( 0, 1 / Math.sqrt(3));
		
		pm = new IntegerMatrix( new int[][] { {2,-1}, {-1,2} } ).times(z);
		
		return pm;
	}
	
	public static void result() {
		Experiment [] es = new Experiment[] { lawson1162_I(), lawson5000_I(), lawson20000_I(), lawson1162_E(), lawson5000_E(), lawson20000_E() };
		
		Experiment.printTable(es, Experiment.NAME, Experiment.NOV, Experiment.L_2_ERROR, Experiment.L_INF_ERROR, Experiment.L_2_ERROR_MOD_SIGN, Experiment.L_INF_ERROR_MOD_SIGN, Experiment.IS_INTRINSIC );
		
	}
	

	public static Experiment createLawsonExperiment( int nov, boolean isIntrinsic ) {
		Experiment experiment = new Experiment("lawson" );

		experiment.setExpected(lawson_conj());

		experiment.addDescription(Experiment.NOV, nov);
		experiment.addDescription(Experiment.IS_INTRINSIC, isIntrinsic);
		return experiment;
	}
	
	
//	 result for: 	lawson1162.jvx  (intrinsic)
//			period matrix: ((-6.975140374186846E-6+0.8662444811283665i, 0.5007176932208048-1.2066475888871432E-5i),
//					(0.4994377641264024-2.254303567956559E-7i, -6.490807644774077E-6+0.8649538911638136i))
//					period matrix: ((-5.442774596251157-4.382609951460591E-5i, 7.581590401439372E-5+3.1461020530898165i),
//					(1.4164207056107168E-6+3.1380600214096352i, -5.434665580348285-4.078294722537342E-5i))
//					reduced: ((9.295483636807389E-6+1.1544085090534324i, 9.288571927587805E-6-0.5763757434778928i),
//					(-4.392729350682892E-6-0.577853304405388i, -1.1270248566420744E-5+1.1534658220203209i))
//		
	
	public static Experiment lawson1162_I() {

		Experiment experiment = createLawsonExperiment( 1162, true );
		
		ComplexMatrix pm = new ComplexMatrix(2);
		
		pm.set( 0, 0, 9.295483636807389E-6, 1.1544085090534324 );
		pm.set( 0, 1, 9.288571927587805E-6, -0.5763757434778928 );
		pm.set( 1, 0, -4.392729350682892E-6, -0.577853304405388 );
		pm.set( 1, 1, -1.1270248566420744E-5, 1.1534658220203209 );
		
		experiment.setActual(pm);
		
		return experiment;
	}
	
//	result for: 	lawson1162.jvx  (extrinsic)
//	period matrix: ((-9.866570847954284E-4+0.8659889831887388i, 0.4995168921675075-0.0011235235662278476i),
//			(0.4998185499534661-4.273516907362286E-6i, 5.184574827922092E-4+0.8656899358993027i))
//			period matrix: ((-5.441169255350873-0.006199349298411279i, 0.007059306763592822+3.138557197554893i),
//			(2.6851298642322258E-5+3.140452569323424i, -5.439290285815736+0.0032575644382773217i))
//			reduced: ((0.001315651728853098+1.154747602239363i, 0.0019545764069325328+0.5768144553427657i),
//			(6.625219727610322E-4+0.5771642664910603i, 0.0014978560546058015+1.153992492208007i))
//			
	
//	load file: /home/schmies/tmp/lawson1162.jvx
//	useRhoByImmersion = true
//	period matrix: ((-7.344376823286772E-6+0.8666260921753864i, 0.5006837536919538-1.2499944313855506E-5i),
//	(0.4993990984883566-2.0008651809267169E-7i, -7.159523237078771E-6+0.8654549326170131i))
//	period matrix: ((-5.4451723291748495-4.614608054646573E-5i, 7.853946645337994E-5+3.145888804740807i),
//	(1.2571806706445971E-6+3.1378170780407735i, -5.437813716645316-4.498461120962416E-5i))
//	reduced: ((9.778932093636766E-6+1.1539001755855298i, 9.54090827267122E-6-0.5761611044097982i),
//	(-4.664462353536279E-6-0.5776434681545166i, -1.2050992588859665E-5+1.153881700898593i))

	public static Experiment lawson1162_E() { 

		Experiment experiment = createLawsonExperiment( 1162, false );
		
		ComplexMatrix pm = new ComplexMatrix(2);
		
		pm.set( 0, 0,  9.778932093636766E-6, +1.1539001755855298 );
		pm.set( 0, 1,  9.54090827267122E-6,  -0.5761611044097982); //these minus signs are made up
		pm.set( 1, 0, -4.664462353536279E-6, -0.5776434681545166); //these minus signs are made up
		pm.set( 1, 1, -1.2050992588859665E-5,+1.153881700898593);
		
		experiment.setActual(pm);
		
		return experiment;
	}

//	load file: /home/schmies/tmp/lawson5000_flipped.jvx
//	useRhoByImmersion = false
//	0.0% negative rhos
//	period matrix: ((-9.866570847954284E-4+0.8659889831887388i, 0.4995168921675075-0.0011235235662278476i),
//	(0.4998185499534661-4.273516907362286E-6i, 5.184574827922092E-4+0.8656899358993027i))
//	period matrix: ((-5.441169255350873-0.006199349298411279i, 0.007059306763592822+3.138557197554893i),
//	(2.6851298642322258E-5+3.140452569323424i, -5.439290285815736+0.0032575644382773217i))
//	reduced: ((0.001315651728853098+1.154747602239363i, 0.0019545764069325328+0.5768144553427657i),
//	(6.625219727610322E-4+0.5771642664910603i, 0.0014978560546058015+1.153992492208007i))

	public static Experiment lawson5000_I() {

		Experiment experiment = createLawsonExperiment( 5000, true );
		
		ComplexMatrix pm = new ComplexMatrix(2);
	
		pm.set( 0, 0,  0.001315651728853098, +1.154747602239363);
		pm.set( 0, 1,  0.0019545764069325328, +0.5768144553427657); 
		pm.set( 1, 0,  6.625219727610322E-4, +0.5771642664910603); 
		pm.set( 1, 1,  0.0014978560546058015,+1.153992492208007);
		
		experiment.setActual(pm);
		
		return experiment;
	}

//	load file: /home/schmies/tmp/lawson5000_flipped.jvx
//	useRhoByImmersion = true
//	period matrix: ((-8.787667232703528E-4+0.8659365352978834i, 0.49929507102350174-0.0013174131816824806i),
//	(0.4997788056051513-4.8815979721215335E-5i, 5.763182485445895E-4+0.8657478948336592i))
//	period matrix: ((-5.440839715533658-0.00552145416409063i, 0.008277551146632072+3.137163454202054i),
//	(3.0671984653991684E-4+3.1402028482180495i, -5.4396544525405055+0.0036211143515148376i))
//	reduced: ((0.0011719282636823802+1.1548178525628765i, 0.0021065102670583323+0.5765933178008467i),
//	(6.420784727505057E-4+0.5771534298365538i, 0.0016572544015191456+1.1539169116927153i))
//	
	public static Experiment lawson5000_E() {

		Experiment experiment = createLawsonExperiment( 5000, false );
		
		ComplexMatrix pm = new ComplexMatrix(2);
	
		pm.set( 0, 0, 0.0011719282636823802, +1.1548178525628765 );
		pm.set( 0, 1, 0.0021065102670583323, -0.5765933178008467 ); 
		pm.set( 1, 0,  6.420784727505057E-4, -0.5771534298365538); 
		pm.set( 1, 1,  0.0016572544015191456, +1.153916911692715);
		
		experiment.setActual(pm);
		
		return experiment;
	}

//	load file: /home/schmies/tmp/lawson20000_flipped.jvx
//	useRhoByImmersion = false
//
//	period matrix: ((-0.002009893952597544+0.8660205310128263i, -0.4982802897557503+0.0034780435385853292i),
//	(-0.49962616444911756-5.163090087957475E-7i, 0.002597604400539068+0.8653729540839195i))
//	period matrix: ((-5.441367476175653-0.012628536151949992i, -0.02185319205937024-3.130787395450517i),
//	(3.2440651780298965E-6-3.139243775549187i, -5.437298630330678+0.01632122980333211i))
//	reduced: ((0.0026798743260736807+1.154700815888611i, -0.00535142826740558-0.5753553364025944i),
//	(-0.0013383391483081499-0.5769187411123367i, 0.00527102091896406+1.1528355367690701i))
	public static Experiment lawson20000_I() {

		Experiment experiment = createLawsonExperiment( 20000, true );
		
		ComplexMatrix pm = new ComplexMatrix(2);
	
		pm.set( 0, 0,  0.0026798743260736807, 1.154700815888611 );
		pm.set( 0, 1, -0.00535142826740558,-0.5753553364025944 ); 
		pm.set( 1, 0, -0.0013383391483081499, -0.5769187411123367); 
		pm.set( 1, 1,  0.0052710209189640, 1.1528355367690701);
		
		experiment.setActual(pm);
		
		return experiment;
	}
//	
//	load file: /home/schmies/tmp/lawson20000_flipped.jvx
//	useRhoByImmersion = true
//	period matrix: ((-0.002009684898122501+0.8660123548647419i, -0.498300244070641+0.003481976695529624i),
//	(-0.499606000064931+1.147543917960737E-6i, 0.002598375394477752+0.8653281117823802i))
//	period matrix: ((-5.441316103922141-0.012627222623944003i, -0.02187790481329346-3.1309127721086534i),
//	(-7.210231084674199E-6-3.1391170789867378i, -5.437016877840506+0.016326074101119574i))
//	reduced: ((0.0026796461850070916+1.1547117187626192i, -0.005355947642798394-0.5753838008250742i),
//	(-0.0013400923944905092-0.5769008999640821i, 0.0052748992510345824+1.1527933048685672i))	

	public static Experiment lawson20000_E() {

		Experiment experiment = createLawsonExperiment( 20000, false );
		
		ComplexMatrix pm = new ComplexMatrix(2);
	
		pm.set( 0, 0,  0.0026796461850070916, 1.1547117187626192);
		pm.set( 0, 1,  -0.005355947642798394,-0.5753838008250742); 
		pm.set( 1, 0, -0.0013400923944905092,-0.5769008999640821); 
		pm.set( 1, 1,  0.0052748992510345824, 1.1527933048685672);
		
		experiment.setActual(pm);
		
		return experiment;
	}
	
	DiscreteLawson( Class<V> vClass, Class<E> eClass, Class<F> fClass ) {		
	}

	
	public static DiscreteLawson createEmpty(int discr) {
		DiscreteLawson dl = new DiscreteLawson( ConfVertex.class, ConfEdge.class, ConfFace.class );
		return dl;
	}
	
	public static DiscreteLawson createDiscreteLawson() {
		//return createDiscretLawson("/home/schmies/tmp/lawson1162.jvx");
		return createDiscretLawson("/home/schmies/tmp/lawson5000_flipped.jvx");
		//return createDiscretLawson("/home/schmies/tmp/lawson20000_flipped.jvx");
		//return createDiscretLawson("/home/schmies/tmp/lawson500_flipped.jvx");
	}
	
	public static DiscreteLawson createDiscretLawson( String filename ) {
		System.out.println( "load file: " + filename );
		System.out.println( "useRhoByImmersion = " + useRhoByImmersion );
		
		ReaderJVX reader = new ReaderJVX();
		
		SceneGraphComponent lawson = null;
		try {
			
			lawson = reader.read( new File( filename ) );
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DiscreteLawson dl = new DiscreteLawson( ConfVertex.class, ConfEdge.class, ConfFace.class );
		dl.name = "lawson";
		dl.ifs = (IndexedFaceSet)lawson.getChildComponent(0).getGeometry();

		dl.createHEDS( ConfVertex.class, ConfEdge.class, ConfFace.class );
		
		dl.computeRho();
		
		return dl;
	}
	
	void createHEDS( Class<V> vClass, Class<E> eClass, Class<F> fClass) {
		
		g = HalfEdgeDataStructure.createHEDS(vClass, eClass, fClass);
		
		int [][] index = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		
		double [][] coords = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			
		xyz = new Real3[coords.length];
		
		for( int i=0; i<xyz.length; i++ ) {
			g.addNewVertex();
			
			xyz[i] = new Real3( coords[i][0], coords[i][1], coords[i][2] );
		}
		
		generator = new FaceByFaceGenerator<V,E,F>(g);
		
		for( int i=0; i<index.length; i++ ) {
			generator.addFace(
					(V)g.vertexList.get(index[i][0]),
					(V)g.vertexList.get(index[i][1]),
					(V)g.vertexList.get(index[i][2]) );
		}
	}

}
