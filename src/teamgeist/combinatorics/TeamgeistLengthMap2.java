package teamgeist.combinatorics;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class TeamgeistLengthMap2 implements EdgeLengthMap{

	private Random
		rnd = new Random();
	private MutableDouble
		a = new MutableDouble(1.0),
		b = new MutableDouble(1.0), 
		c = new MutableDouble(2.0), 
		d = new MutableDouble(1.0), 
		e = new MutableDouble(1.5), 
		f = new MutableDouble(1.5), 
		g = new MutableDouble(1.5);
	
	private static final int[]
	    aIndices = {84,85, 149,148, 81,80, 83,82, 159,158, 151,150, 87,86, 157,156, 111,110, 109,108, 105,104, 107,106},
	    bIndices = {1,0, 15,14, 7,6, 9,8, 29,28, 39,38, 33,32, 35,34, 17,16, 27,26, 21,20, 23,22, 45,44, 43,42, 51,50, 53,52, 73,72, 71,70, 63,62, 65,64, 89,88, 91,90, 99,98, 97,96},
	    cIndices = {3,2, 13,12, 5,4, 11,10, 19,18, 25,24, 167,166, 165,164, 31,30, 37,36, 161,160, 163,162, 75,74, 69,68, 61,60, 67,66, 47,46, 41,40, 49,48, 55,54, 103,102, 101,100, 93,92, 95,94},
	    dIndices = {169,168, 183,182, 177,176, 189,188, 195,194, 271,270},
	    eIndices = {171,170, 217,216, 207,206, 203,202, 213,212, 215,214, 201,200, 205,204, 209,208, 211,210, 273,272, 275,274, 179,178, 181,180, 191,190, 193,192, 197,196, 199,198, 187,186, 185,184, 173,172, 175,174, 267,266, 269,268},
	    fIndices = {221,220, 219,218, 223,222, 227,226, 229,228, 225,224, 261,260, 235,234, 237,236, 231,230, 239,238, 233,232, 263,262, 257,256, 249,248, 259,258, 243,242, 247,246, 265,264, 251,250, 253,252, 255,254, 241,240, 245,244},
		gIndices = {57,56, 139,138, 137,136, 59,58, 143,142, 141,140, 153,152, 155,154, 79,78, 147,146, 145,144, 77,76, 117,116, 113,112, 115,114, 119,118, 123,122, 121,120, 131,130, 133,132, 135,134, 125,124, 127,126, 129,128};
	
	private HashMap<Integer, MutableDouble>
		lengthMap = new HashMap<Integer, MutableDouble>();
	
	
	private class MutableDouble{
		
		private Double value = 0.0;
		
		private MutableDouble(Double value) {
			this.value = value;
		}
		
		private MutableDouble() {
		}

		private void setValue(Double value) {
			this.value = value;
		}
		
		private Double getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return value.toString();
		}
		
	}
	
	
	public TeamgeistLengthMap2() {
		initLengthMap();
	}

	
	private void initLengthMap(){
		for (int i = 0; i < aIndices.length; i++)
			lengthMap.put(aIndices[i], a);
		for (int i = 0; i < bIndices.length; i++)
			lengthMap.put(bIndices[i], b);		
		for (int i = 0; i < cIndices.length; i++)
			lengthMap.put(cIndices[i], c);
		for (int i = 0; i < dIndices.length; i++)
			lengthMap.put(dIndices[i], d);
		for (int i = 0; i < eIndices.length; i++)
			lengthMap.put(eIndices[i], e);
		for (int i = 0; i < fIndices.length; i++)
			lengthMap.put(fIndices[i], f);		
		for (int i = 0; i < gIndices.length; i++)
			lengthMap.put(gIndices[i], g);
	}
	
	
	
	@Override
	public void randomize(){
		setA(rnd.nextDouble());
		setB(rnd.nextDouble());
		setC(rnd.nextDouble());
		setD(rnd.nextDouble());
		setE(rnd.nextDouble());
		setF(rnd.nextDouble());
		setG(rnd.nextDouble());
	}
	

	@Override
	public Double getLength(int index){
		Double length = lengthMap.get(index).getValue();
		if (length != null)
			return length;
		else
			return -1.0;
	}

	
	
	public Double getA() {
		return a.getValue();
	}



	public Double getB() {
		return b.getValue();
	}



	public Double getC() {
		return c.getValue();
	}



	public Double getD() {
		return d.getValue();
	}



	public Double getE() {
		return e.getValue();
	}



	public Double getF() {
		return f.getValue();
	}



	public Double getG() {
		return g.getValue();
	}



	public void setA(Double a) {
		this.a.setValue(a);
	}



	public void setB(Double b) {
		this.b.setValue(b);
	}



	public void setC(Double c) {
		this.c.setValue(c);
	}



	public void setD(Double d) {
		this.d.setValue(d);
	}



	public void setE(Double e) {
		this.e.setValue(e);
	}



	public void setF(Double f) {
		this.f.setValue(f);
	}



	public void setG(Double g) {
		this.g.setValue(g);
	}

	public static void main(String[] args) {
		TeamgeistLengthMap2 map = new TeamgeistLengthMap2();
		Set<Integer> keySet = map.lengthMap.keySet();
		LinkedList<Integer> sortedKeySet = new LinkedList<Integer>(keySet);
		Collections.sort(sortedKeySet);
		Integer counter = 0;
		for (Integer key : sortedKeySet){
			System.err.println("Key " + counter + " is " + key);
			if (!counter.equals(key)){
				System.err.println("gap in key list");
				return;
			}
			counter++;
		}
		System.err.println("edge list consistent, no error found.");
	}

	
}
