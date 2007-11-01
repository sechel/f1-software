package teamgeist.combinatorics;


public class TextureCoordinateMapOld {

	private double[][]
	    coords = {{0,0},
		    {138, 18},
		    {223, 63},
		    {201, 224},
		    {221, 443},
		    {137, 487},
		    {57, 443},
		    {80, 244},
		    {54, 63},
		    {291, 117},
		    {332, 34}, // 10
		    {516, 55},
		    {717, 37},
		    {759, 118},
		    {717, 197},
		    {516, 176},
		    {332, 200},
		    {392, 414},
		    {398, 485},
		    {517, 434},
		    {581, 474}, // 20
		    {642, 444},
		    {552, 345},
		    {546, 292},
		    {487, 275},
		    {449, 365}
	    };
	
	
	public TextureCoordinateMapOld() {
		for (int i = 0; i < coords.length; i++) {
			coords[i][0] /= 768.0;
			coords[i][1] /= 512.0;
		}
	}
	
	
	public double[] getPatchCoordinate(int index){
		switch(index){
			case 0: return coords[1];
			case 1: return coords[2];
			case 2: return coords[3];
			case 3: return coords[4];
			case 4: return coords[5];
			case 5: return coords[6];
			case 6: return coords[7];
			case 7: return coords[8];
			
			case 8: return coords[13];
			case 9: return coords[12];
			case 10: return coords[11];
			case 11: return coords[10];
			case 12: return coords[9];
			case 13: return coords[16];
			case 14: return coords[15];
			case 15: return coords[14];
			
			case 16: return coords[9];
			case 17: return coords[10];
			case 18: return coords[11];
			case 19: return coords[12];
			case 20: return coords[13];
			case 21: return coords[14];
			case 22: return coords[15];
			case 23: return coords[16];
			
			case 24: return coords[15];
			case 25: return coords[16];
			case 26: return coords[9];
			case 27: return coords[10];
			case 28: return coords[11];
			case 29: return coords[12];
			case 30: return coords[13];
			case 31: return coords[14];
			
			case 32: return coords[11];
			case 33: return coords[12];
			case 34: return coords[13];
			case 35: return coords[14];
			case 36: return coords[15];
			case 37: return coords[16];
			case 38: return coords[9];
			case 39: return coords[10];
			
			case 40: return coords[8];
			case 41: return coords[1];
			case 42: return coords[2];
			case 43: return coords[3];
			case 44: return coords[4];
			case 45: return coords[5];
			case 46: return coords[6];
			case 47: return coords[7];
			
			default: return coords[0];
		}
	}
	
	
	public double[] getRotorCoordinate(int index){
		switch(index){
		case 0: return coords[17];
		case 1: return coords[25];
		case 2: return coords[24];
		case 3: return coords[25];
		case 4: return coords[17];
		case 5: return coords[25];
		case 6: return coords[24];
		case 7: return coords[25];
		
		case 8: return coords[23];
		case 9: return coords[22];
		case 10: return coords[21];
		case 11: return coords[22];
		case 12: return coords[23];
		case 13: return coords[22];
		case 14: return coords[21];
		case 15: return coords[22];
		
		case 16: return coords[23];
		case 17: return coords[22];
		case 18: return coords[21];
		case 19: return coords[22];
		case 20: return coords[23];
		case 21: return coords[22];
		case 22: return coords[21];
		case 23: return coords[22];
		
		case 24: return coords[18];
		case 25: return coords[19];
		case 26: return coords[20];
		case 27: return coords[19];
		case 28: return coords[18];
		case 29: return coords[19];
		case 30: return coords[20];
		case 31: return coords[19];
		
		case 32: return coords[18];
		case 33: return coords[19];
		case 34: return coords[20];
		case 35: return coords[19];
		case 36: return coords[18];
		case 37: return coords[19];
		case 38: return coords[20];
		case 39: return coords[19];
		
		case 40: return coords[25];
		case 41: return coords[17];
		case 42: return coords[25];
		case 43: return coords[24];
		case 44: return coords[25];
		case 45: return coords[17];
		case 46: return coords[25];
		case 47: return coords[24];
		
		default: return coords[0];
	}
	}
	
	
	public boolean isInPatch(int i, int j, int k){
		return (i/8)==(j/8) && (j/8)==(k/8);
	}
	
	
	
	
	
}
